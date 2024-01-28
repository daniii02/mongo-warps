package warps.mongo.message;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import warps.mongo.MongoWarps;

import java.util.Arrays;

public final class MessageManager {
    private YamlConfiguration messagesConfig;

    /**
     * Recarga los mensajes de la configuración.
     */
    public void reloadMessages() {
        messagesConfig = MongoWarps.get().getConfigManager().loadConfiguration("messages.yml");

        // Recarga todos los mensajes
        Arrays.stream(Message.values()).forEach(Message::load);
    }

    /**
     * Obtiene un mensaje de la configuración y traduce los colores.
     * Si no existe el mensaje, devuelve la ruta.
     * @param path ruta del mensaje
     * @return mensaje traducido
     */
    public String getMessage(String path) {
        return ChatColor.translateAlternateColorCodes('&', messagesConfig.getString(path, path));
    }
}
