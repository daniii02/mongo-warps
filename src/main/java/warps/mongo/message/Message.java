package warps.mongo.message;

import javafx.util.Pair;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import warps.mongo.MongoWarps;

@RequiredArgsConstructor
public enum Message {
    COMMAND_PERMISSION_REQUIRED("command-permission-required"),
    COMMAND_PLAYER_ONLY("command-player-only"),
    COMMAND_USAGE_HEADER("command-usage-header"),
    COMMAND_USAGE_ENTRY("command-usage-entry"),
    COMMAND_PARAM_NOT_FOUND("command-param-not-found"),
    COMMAND_INTERNAL_ERROR("command-internal-error"),

    WARP_INVALID_NAME("warp-invalid-name"),
    WARP_RESERVED_NAME("warp-reserved-name"),
    WARP_ALREADY_EXISTS("warp-already-exists"),
    WARP_PERMISSION("warp-permission"),
    WARP_NOT_FOUND("warp-not-found"),
    WARP_CREATED("warp-created"),
    WARP_DELETED("warp-deleted"),
    WARP_TELEPORT("warp-teleport"),

    WARP_MENU_TITLE("warp-menu-title"),
    WARP_MENU_NO_WARPS("warp-menu-no-warps"),

    RELOADED_CONFIG("reloaded-config"),
    RELOADED_DATABASE("reloaded-database"),
    RELOADED_MESSAGES("reloaded-messages"),
    RELOADED_WARPS("reloaded-warps");

    private final String path;
    private String messageString;

    /**
     * Carga el mensaje desde un archivo de configuración.
     * Método interno.
     */
    void load() {
        messageString = MongoWarps.get().getMessageManager().getMessage(path);
    }

    /**
     * Envía el mensaje a un jugador.
     * @param target jugador o consola
     */
    public void send(CommandSender target) {
        target.sendMessage(messageString);
    }

    /**
     * Envía el mensaje a una entidad, reemplazando un texto por otro.
     * @param target jugador o consola
     * @param replaced texto que será reemplazado
     * @param replacement texto de reemplazo
     */
    public void send(CommandSender target, CharSequence replaced, CharSequence replacement) {
        send(target, new Pair<>(replaced, replacement));
    }

    /**
     * Envía el mensaje a una entidad, reemplazando varios textos por otros.
     * Cada entrada del mapa representa un reemplazo.
     * @param target jugador o consola
     * @param replacements mapa de reemplazos
     */
    @SafeVarargs
    public final void send(CommandSender target, Pair<CharSequence, CharSequence>... replacements) {
        // Reemplaza los textos del map
        String replacedMessage = messageString;
        for (Pair<CharSequence, CharSequence> entry : replacements) {
            replacedMessage = replacedMessage.replace(entry.getKey(), entry.getValue());
        }

        target.sendMessage(replacedMessage);
    }

    @Override
    public String toString() {
        return messageString;
    }
}
