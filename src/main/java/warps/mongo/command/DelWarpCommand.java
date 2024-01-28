package warps.mongo.command;

import com.mongodb.MongoWriteException;
import javafx.util.Pair;
import org.bukkit.command.CommandSender;
import warps.mongo.MongoWarps;
import warps.mongo.message.Message;
import warps.mongo.util.Permissions;
import warps.mongo.warp.Warp;
import warps.mongo.warp.WarpManager;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DelWarpCommand extends ICommand {
    public DelWarpCommand() {
        super("delwarp", "Elimina el warp introducido.", "/delwarp <name>", Collections.singletonList("deletewarp"));
        super.setPermission(Permissions.WARP_REMOVE.getName());
    }

    @Override
    void onExecute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            Message.COMMAND_PARAM_NOT_FOUND.send(sender, new Pair<>("{name}", "name"), new Pair<>("{command}", "/delwarp <nombre>"));
            return;
        }

        deleteWarpByName(sender, args[0]);
    }

    /**
     * Elimina un warp con el nombre especificado.
     * Uso interno.
     * @param sender entidad que borra el warp
     * @param warpName nombre del warp
     */
    protected static void deleteWarpByName(CommandSender sender, String warpName) {
        WarpManager manager = MongoWarps.get().getWarpManager();

        Warp warp = manager.getWarpByName(warpName);
        if (warp == null) {
            Message.WARP_NOT_FOUND.send(sender, "{warp}", warpName);
            return;
        }

        if (!warp.hasPermission(sender)) {
            Message.WARP_PERMISSION.send(sender, "{warp}", warp.getName());
            return;
        }

        CompletableFuture<Void> future = manager.deleteWarp(sender, warp);
        // Si la eliminación ha sido cancelada por un evento.
        if (future == null) return;

        future.thenRun(() -> {
            Message.WARP_DELETED.send(sender, "{warp}", warp.getName());
        }).exceptionally(throwable -> {
            if (throwable instanceof MongoWriteException) {
                Message.WARP_NOT_FOUND.send(sender, "{warp}", warp.getName());
            }
            else {
                MongoWarps.get().error("Error al eliminar el warp " + warp.getName() + ":", throwable);
                Message.COMMAND_INTERNAL_ERROR.send(sender);
            }
            return null;
        });
    }

    @Override
    void onTabComplete(CommandSender sender, String[] args, boolean emptyArg, List<String> completions) {
        if (args.length != 1) return;

        MongoWarps.get().getWarpManager().addAvailableWarpNames(sender, completions);
        if (emptyArg && completions.isEmpty()) completions.add("<name>");
    }
}
