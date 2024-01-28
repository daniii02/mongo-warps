package warps.mongo.command;

import com.mongodb.MongoWriteException;
import javafx.util.Pair;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import warps.mongo.MongoWarps;
import warps.mongo.message.Message;
import warps.mongo.util.Permissions;
import warps.mongo.warp.Warp;
import warps.mongo.warp.WarpManager;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class SetWarpCommand extends ICommand {
    public SetWarpCommand() {
        super("setwarp", "Crea un warp en la posición del jugador.", "/setwarp <name>");
        super.setProhibitConsole();
        super.setPermission(Permissions.WARP_CREATE.getName());
    }

    @Override
    void onExecute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length == 0) {
            Message.COMMAND_PARAM_NOT_FOUND.send(player, new Pair<>("{name}", "name"), new Pair<>("{command}", "/setwarp <name>"));
            return;
        }

        createWarpByName(player, args[0]);
    }

    /**
     * Crea un warp con el nombre especificado.
     * Uso interno.
     * @param player jugador
     * @param warpName nombre del warp
     */
    protected static void createWarpByName(Player player, String warpName) {
        if (Arrays.asList("create", "remove", "menu", "reload", "help").contains(warpName.toLowerCase())) {
            Message.WARP_RESERVED_NAME.send(player, "{warp}", warpName);
            return;
        }

        WarpManager manager = MongoWarps.get().getWarpManager();
        if (manager.isWarpNameUppercase()) {
            warpName = warpName.toUpperCase();
        }

        if (!manager.getWarpNameExpression().matcher(warpName).matches()) {
            Message.WARP_INVALID_NAME.send(player, "{warp}", warpName);
            return;
        }

        if (manager.warpNameExists(warpName)) {
            Message.WARP_ALREADY_EXISTS.send(player, "{warp}", warpName);
            return;
        }

        Warp warp = new Warp(warpName, player.getLocation());
        CompletableFuture<Void> future = manager.createWarp(player, warp);
        // Si la creación ha sido cancelada por un evento.
        if (future == null) return;

        future.thenRun(() -> {
            Message.WARP_CREATED.send(player, "{warp}", warp.getName());
        }).exceptionally(throwable -> {
            if (throwable instanceof MongoWriteException) {
                Message.WARP_ALREADY_EXISTS.send(player, "{warp}", warp.getName());
            }
            else {
                MongoWarps.get().error("Error al crear el warp " + warp.getName() + ":", throwable);
                Message.COMMAND_INTERNAL_ERROR.send(player);
            }
            return null;
        });
    }
}
