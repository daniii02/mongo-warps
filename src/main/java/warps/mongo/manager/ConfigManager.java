package warps.mongo.manager;

import lombok.Getter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import warps.mongo.MongoWarps;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

@Getter
public final class ConfigManager {
    public ConfigManager() {
        MongoWarps.get().saveDefaultConfig();
    }

    /**
     * Carga una configuración a partir del nombre introducido.
     * @param name nombre del archivo de configuración
     * @return configuración cargada
     * @throws RuntimeException si ocurre un error al cargar la configuración
     */
    public YamlConfiguration loadConfiguration(String name) throws RuntimeException {
        File file = new File(MongoWarps.get().getDataFolder(), name);
        if (!file.exists()) {
            MongoWarps.get().saveResource(name, false);
        }

        // Creo y cargo la configuración
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException("Error al cargar la configuración " + name + ".", e);
        }
        return config;
    }

    /**
     * Deserializa un item de la configuración.
     * @param configuration configuración
     * @param path ruta del item
     * @return item deserializado o <tt>null</tt> si ocurre un error
     */
    public @Nullable ItemStack deserializeItem(FileConfiguration configuration, String path) {
        ItemStack itemStack = configuration.getItemStack(path);
        if (itemStack == null) {
            MongoWarps.get().warning("Item inválido ["+path+"]. Se utilizará el item por defecto.");
        }
        return itemStack;
    }
}
