package warps.mongo;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.plugin.java.JavaPlugin;
import warps.mongo.command.DelWarpCommand;
import warps.mongo.command.SetWarpCommand;
import warps.mongo.command.WarpCommand;
import warps.mongo.inventory.InventoryListener;
import warps.mongo.inventory.InventoryManager;
import warps.mongo.manager.ConfigManager;
import warps.mongo.manager.MongoManager;
import warps.mongo.manager.TaskManager;
import warps.mongo.message.MessageManager;
import warps.mongo.util.LibraryLoader;
import warps.mongo.warp.WarpManager;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@Getter
public class MongoWarps extends JavaPlugin {
    private static MongoWarps instance;

    private ConfigManager configManager;
    private MessageManager messageManager;
    private TaskManager taskManager;

    private MongoManager mongoManager;
    private WarpManager warpManager;
    private InventoryManager inventoryManager;

    private final List<Command> commands = new ArrayList<>();

    private boolean fullyEnabled = false;

    @Override
    public void onLoad() {
        instance = this;

        loadLibraries();
    }

    @Override
    public void onEnable() {
        loadManagers();
        loadCommands();
        loadListeners();

        fullyEnabled = true;

        info("Plugin activado correctamente.");
    }

    @Override
    public void onDisable() {
        if (fullyEnabled) {
            disableManagers();
            disableCommands();

            fullyEnabled = false;
        }

        instance = null;
    }

    /**
     * Carga las bibliotecas desde <tt>.libraries</tt>
     */
    private void loadLibraries() {
        try (BufferedReader reader = new BufferedReader(getTextResource(".libraries"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                LibraryLoader.loadLibraryFromMaven(line);
            }
        }
        catch (Throwable exception) {
            error("Error al cargar las bibliotecas");
            throw new RuntimeException(exception);
        }
    }

    private void loadManagers() {
        try {
            configManager = new ConfigManager();
            (messageManager = new MessageManager()).reloadMessages();
            taskManager = new TaskManager();

            mongoManager = new MongoManager();
            (warpManager = new WarpManager()).load();
            inventoryManager = new InventoryManager();
        } catch (Exception exception) {
            error("Error al cargar los managers");
            throw new RuntimeException(exception);
        }
    }

    private void loadCommands() {
        commands.add(new WarpCommand());
        commands.add(new SetWarpCommand());
        commands.add(new DelWarpCommand());

        SimpleCommandMap commandMap = ((CraftServer) Bukkit.getServer()).getCommandMap();
        commands.forEach(command -> commandMap.register("warp", command));
    }

    private void loadListeners() {
        Bukkit.getPluginManager().registerEvents(new InventoryListener(), this);
    }

    private void disableManagers() {
        warpManager.clear();
        mongoManager.stop();
        taskManager.stop();
    }

    private void disableCommands() {
        commands.clear();
    }

    public static MongoWarps get() {
        return instance;
    }

    public void info(String message) {
        Bukkit.getConsoleSender().sendMessage("§a§l[MongoWarps] §7" + message);
    }

    public void warning(String message) {
        getLogger().warning(message);
    }

    public void warning(Throwable exception) {
        warning(exception.getMessage());
    }

    public void error(String message) {
        getLogger().severe(message);
    }

    public void error(String message, Throwable exception) {
        getLogger().log(Level.SEVERE, message, exception);
    }

    public void error(Throwable exception) {
        error(exception.getMessage());
    }
}