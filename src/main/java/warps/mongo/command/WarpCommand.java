package warps.mongo.command;

import javafx.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import warps.mongo.MongoWarps;
import warps.mongo.event.WarpTeleportEvent;
import warps.mongo.inventory.InventoryManager;
import warps.mongo.manager.TaskManager;
import warps.mongo.message.Message;
import warps.mongo.util.Permissions;
import warps.mongo.warp.Warp;
import warps.mongo.warp.WarpManager;

import java.util.Collections;
import java.util.List;

public class WarpCommand extends ICommand {
    private final WarpManager warpManager = MongoWarps.get().getWarpManager();

    public WarpCommand() {
        super("warp", "Comando principal plugin Warps.",  "/warp <add|remove|menu|reload|help>", Collections.singletonList("warps"));
        super.setProhibitConsole();
    }

    @Override
    void onExecute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                if (Permissions.WARP_CREATE.checkAndSend(player)) return;
                if (args.length < 2) break;

                SetWarpCommand.createWarpByName(player, args[1]);
                return;
            case "remove":
                if (Permissions.WARP_REMOVE.checkAndSend(player)) return;
                if (args.length < 2) break;

                DelWarpCommand.deleteWarpByName(player, args[1]);
                return;
            case "menu":
                if (Permissions.WARP_MENU.checkAndSend(player)) return;

                if (!MongoWarps.get().getInventoryManager().openWarpInventory(player)) {
                    Message.WARP_MENU_NO_WARPS.send(player);
                }
                return;
            case "reload":
                if (Permissions.WARP_RELOAD.checkAndSend(player)) return;
                if (args.length < 2) break;

                TaskManager taskManager = MongoWarps.get().getTaskManager();
                InventoryManager inventoryManager = MongoWarps.get().getInventoryManager();
                switch (args[1].toLowerCase()) {
                    case "config":
                        // Recarga las configuraciones
                        taskManager.runAsync(() -> {
                            MongoWarps.get().reloadConfig();
                            warpManager.reloadConfiguration();
                            inventoryManager.reloadConfiguration();
                            inventoryManager.reopenInventories();

                            Message.RELOADED_CONFIG.send(player);
                        }).exceptionally(throwable -> {
                            MongoWarps.get().error("Error al recargar las configuraciones:", throwable);
                            Message.COMMAND_INTERNAL_ERROR.send(player);
                            return null;
                        });
                        return;
                    case "database":
                        // Recarga la configuración, reconecta la base de datos y recarga los warps
                        taskManager.runAsync(() -> MongoWarps.get().reloadConfig())
                            .thenCompose(v -> MongoWarps.get().getMongoManager().reconnect())
                            .thenRun(warpManager::loadWarpsCollection)
                            .thenCompose(v -> warpManager.reload())
                            .thenRun(() -> Message.RELOADED_DATABASE.send(player))
                        .exceptionally(throwable -> {
                            MongoWarps.get().error("Error al recargar la base de datos:", throwable);
                            Message.COMMAND_INTERNAL_ERROR.send(player);
                            return null;
                        });
                        return;
                    case "messages":
                        // Recarga los mensajes
                        taskManager.runAsync(() -> {
                            MongoWarps.get().getMessageManager().reloadMessages();
                            inventoryManager.reloadInventoryTitles();
                            Message.RELOADED_MESSAGES.send(player);
                        }).exceptionally(throwable -> {
                            MongoWarps.get().error("Error al recargar los mensajes:", throwable);
                            Message.COMMAND_INTERNAL_ERROR.send(player);
                            return null;
                        });
                        return;
                    case "warps":
                        // Recarga los warps
                        warpManager.reload().thenAccept(amount -> {
                            Message.RELOADED_WARPS.send(player, "{amount}", Integer.toString(amount));
                        }).exceptionally(throwable -> {
                            MongoWarps.get().error("Error al recargar los warps:", throwable);
                            Message.COMMAND_INTERNAL_ERROR.send(player);
                            return null;
                        });
                        return;
                }
            case "help":
                break;
            default:
                Warp warp = warpManager.getWarpByName(args[0]);
                if (warp == null) {
                    Message.WARP_NOT_FOUND.send(player, "{warp}", args[0]);
                    return;
                }

                if (!warp.hasPermission(player)) {
                    Message.WARP_PERMISSION.send(player, "{warp}", warp.getName());
                    return;
                }

                WarpTeleportEvent warpTeleportEvent = new WarpTeleportEvent(player, warp);
                Bukkit.getPluginManager().callEvent(warpTeleportEvent);
                if (warpTeleportEvent.isCancelled()) return;

                warp.teleport(player);
                Message.WARP_TELEPORT.send(player, "{warp}", warp.getName());
                return;
        }

        sendHelp(player);
    }

    private void sendHelp(Player player) {
        Message.COMMAND_USAGE_HEADER.send(player, "{name}", "Warps");
        if (Permissions.WARP_CREATE.check(player)) {
            Message.COMMAND_USAGE_ENTRY.send(player, new Pair<>("{usage}", "/warp add <name>"), new Pair<>("{description}", "Añade un warp en tu posición"));
        }
        if (Permissions.WARP_REMOVE.check(player)) {
            Message.COMMAND_USAGE_ENTRY.send(player, new Pair<>("{usage}", "/warp remove <name>"), new Pair<>("{description}", "Elimina el warp introducido"));
        }
        if (Permissions.WARP_MENU.check(player)) {
            Message.COMMAND_USAGE_ENTRY.send(player, new Pair<>("{usage}", "/warp menu"), new Pair<>("{description}", "Abre el menú de warps"));
        }
        if (Permissions.WARP_RELOAD.check(player)) {
            Message.COMMAND_USAGE_ENTRY.send(player, new Pair<>("{usage}", "/warp reload config"), new Pair<>("{description}", "Recarga las configuraciones"));
            Message.COMMAND_USAGE_ENTRY.send(player, new Pair<>("{usage}", "/warp reload database"), new Pair<>("{description}", "Recarga la base de datos"));
            Message.COMMAND_USAGE_ENTRY.send(player, new Pair<>("{usage}", "/warp reload messages"), new Pair<>("{description}", "Recarga los mensajes"));
            Message.COMMAND_USAGE_ENTRY.send(player, new Pair<>("{usage}", "/warp reload warps"), new Pair<>("{description}", "Recarga los warps"));
        }
        Message.COMMAND_USAGE_ENTRY.send(player, new Pair<>("{usage}", "/warp <name>"), new Pair<>("{description}", "Teletransporta al warp"));
    }

    @Override
    void onTabComplete(CommandSender sender, String[] args, boolean emptyArg, List<String> completions) {
        switch (args.length) {
            case 1:
                completions.add("help");
                if (Permissions.WARP_CREATE.check(sender)) completions.add("create");
                if (Permissions.WARP_REMOVE.check(sender)) completions.add("remove");
                if (Permissions.WARP_MENU.check(sender)) completions.add("menu");
                if (Permissions.WARP_RELOAD.check(sender)) completions.add("reload");

                warpManager.addAvailableWarpNames(sender, completions);
                break;
            case 2:
                switch (args[0].toLowerCase()) {
                    case "create":
                        if (emptyArg && Permissions.WARP_CREATE.check(sender)) completions.add("<name>");
                        break;
                    case "remove":
                        if (!Permissions.WARP_REMOVE.check(sender)) break;

                        warpManager.addAvailableWarpNames(sender, completions);
                        if (emptyArg && completions.isEmpty()) completions.add("<name>");
                        break;
                    case "reload":
                        if (!Permissions.WARP_RELOAD.check(sender)) break;

                        completions.add("config");
                        completions.add("database");
                        completions.add("messages");
                        completions.add("warps");
                        break;
                }
                break;
        }
    }
}
