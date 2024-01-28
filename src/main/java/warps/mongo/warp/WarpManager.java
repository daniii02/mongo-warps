package warps.mongo.warp;

import com.mongodb.client.MongoCollection;
import lombok.Getter;
import lombok.NonNull;
import org.apache.logging.log4j.util.Strings;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import warps.mongo.MongoWarps;
import warps.mongo.event.WarpCreateEvent;
import warps.mongo.event.WarpRemoveEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class WarpManager {
    private final Map<String, Warp> warpsByName = new HashMap<>();

    private MongoCollection<Document> warpsCollection;

    // Formato de permiso para utilizar en cada warp
    @Getter private @Nullable String warpPermissionFormat;
    // Forzar el nombre de los warps a mayúsculas
    @Getter private boolean warpNameUppercase;
    // Expresión regular para validar el nombre de los warps
    @Getter private Pattern warpNameExpression;

    public WarpManager() {
        reloadConfiguration();
        loadWarpsCollection();
    }

    /**
     * Recarga las configuraciones relacionadas con los warps
     */
    public void reloadConfiguration() {
        FileConfiguration config = MongoWarps.get().getConfig();

        warpPermissionFormat = config.getString("warp-permission-format");
        // Cuando está vació o no existe, todos los warps son públicos
        if (Strings.isBlank(warpPermissionFormat)) warpPermissionFormat = null;

        // Recarga los permisos de los warps
        warpsByName.values().forEach(warp -> warp.loadPermission(warpPermissionFormat));

        String warpNameRegex = config.getString("warp-name-expression");
        try {
            warpNameExpression = Pattern.compile(warpNameRegex);
        }
        catch (NullPointerException | PatternSyntaxException exception) {
            MongoWarps.get().warning("Expresión regular del nombre de warps inválida [warp-name-expression]: " + warpNameRegex);
            MongoWarps.get().warning("Se utilizará la expresión regular por defecto.");
            warpNameExpression = Pattern.compile("^[A-Za-z0-9]{5,20}$");
        }

        warpNameUppercase = config.getBoolean("warp-name-uppercase");
    }

    /**
     * Carga la colección de warps de la base de datos
     * Uso interno
     */
    public void loadWarpsCollection() {
        warpsCollection = MongoWarps.get().getMongoManager().getCollection("warps");
    }

    /**
     * Carga los warps de la base de datos
     * Uso interno
     */
    public void load() {
        warpsCollection.find().forEach(document -> {
            String name = document.getString("_id");
            Location location = Location.deserialize(document.get("world", Document.class));

            Warp warp = new Warp(name, location);
            warpsByName.put(name, warp);
        });

        int amount = warpsByName.size();
        if (amount == 0) return;
        if (amount == 1) MongoWarps.get().info("Cargado un warp.");
        else MongoWarps.get().info("Cargados " + amount + " warps.");
    }

    /**
     * Borra todos los warps de la memoria
     * Uso interno
     */
    public void clear() {
        warpsByName.clear();
    }

    /**
     * Elimina los warps cacheados y los vuelve a cargar de la base de datos
     * Devuelve la cantidad de warps cargados
     * @return {@link CompletableFuture} de la recarga
     */
    public CompletableFuture<Integer> reload() {
        return MongoWarps.get().getTaskManager().supplyAsync(() -> {
            clear();
            load();
            return warpsByName.size();
        });
    }

    /**
     * Crea un warp en la base de datos
     * Si se inserta correctamente, se guarda en memoria.
     * Devuelve un Future con el resultado de la creación.
     * @param sender consola o jugador que crea el warp
     * @param warp objeto warp a insertar
     * @return {@link CompletableFuture} de la creación. Si es <tt>null</tt> la creación ha sido cancelada por un evento.
     * @throws com.mongodb.MongoWriteException si ya existe un warp con el mismo nombre
     */
    public @Nullable CompletableFuture<Void> createWarp(@Nullable CommandSender sender, @NonNull Warp warp) {
        WarpCreateEvent warpCreateEvent = new WarpCreateEvent(sender, warp);
        Bukkit.getPluginManager().callEvent(warpCreateEvent);
        if (warpCreateEvent.isCancelled()) return null;

        return MongoWarps.get().getTaskManager().runAsync(() -> {
            Document document = new Document();
            document.put("_id", warp.getName());
            document.put("world", warp.getLocation().serialize());
            warpsCollection.insertOne(document);

            warpsByName.put(warp.getName(), warp);

            // Agrego el warp a todos los inventarios abiertos
            MongoWarps.get().getInventoryManager().computeWarpInventories(warpInventory -> warpInventory.addWarp(warp));
        });
    }

    /**
     * Elimina un warp de la base de datos y de la caché.
     * Devuelve un Future con el resultado de la eliminación.
     * @param sender consola o jugador que elimina el warp
     * @param warp objeto warp a eliminar
     * @return {@link CompletableFuture} de la eliminación. Si es <tt>null</tt> la eliminación ha sido cancelada por un evento.
     * @throws com.mongodb.MongoWriteException si el warp no existe en la base de datos
     */
    public @Nullable CompletableFuture<Void> deleteWarp(@Nullable CommandSender sender, @Nonnull Warp warp) {
        WarpRemoveEvent warpRemoveEvent = new WarpRemoveEvent(sender, warp);
        Bukkit.getPluginManager().callEvent(warpRemoveEvent);
        if (warpRemoveEvent.isCancelled()) return null;

        return MongoWarps.get().getTaskManager().runAsync(() -> {
            Document document = new Document("_id", warp.getName());
            warpsCollection.deleteOne(document);
            warpsByName.remove(warp.getName());

            // Elimino el warp de todos los inventarios abiertos
            MongoWarps.get().getInventoryManager().computeWarpInventories(warpInventory -> warpInventory.removeWarp(warp));
        });
    }

    /**
     * Comprueba si existe un warp con el nombre especificado
     * @param name nombre del warp
     * @return <tt>true</tt> si el warp existe
     */
    public boolean warpNameExists(String name) {
        return warpsByName.containsKey(name);
    }

    /**
     * Obtiene un warp por su nombre
     * @param name nombre del warp
     * @return el warp introducido si existe, o <tt>null</tt> si no existe
     */
    public @Nullable Warp getWarpByName(String name) {
        return warpsByName.get(name);
    }

    /**
     * Obtiene todos los nombres de los warps
     * @return nombres de los warps
     */
    public Set<String> getWarpNames() {
        return warpsByName.keySet();
    }

    public List<String> getPlayerAvailableWarps(Player player) {
        List<String> warpNames = new ArrayList<>();
        warpsByName.values().forEach(warp -> {
            if (warp.hasPermission(player)) warpNames.add(warp.getName());
        });
        return warpNames;
    }

    /**
     * Añade los nombres de los warps disponibles a la lista
     * @param sender jugador o consola
     * @param names lista de nombres
     */
    public void addAvailableWarpNames(CommandSender sender, List<String> names) {
        if (sender instanceof Player) {
            names.addAll(getPlayerAvailableWarps(((Player) sender)));
        } else {
            names.addAll(getWarpNames());
        }
    }
}
