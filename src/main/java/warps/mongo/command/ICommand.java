package warps.mongo.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import warps.mongo.message.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Interfaz de comandos sencilla, para mantener más limpio el código.
 * Comprueba automáticamente si el comando está prohibido en la consola y si el jugador tiene permiso
 */
abstract class ICommand extends Command {
    private boolean prohibitConsole;

    protected ICommand(String name, String description, String usageMessage) {
        this(name, description, usageMessage, Collections.emptyList());
    }

    protected ICommand(String name, String description, String usageMessage, List<String> aliases) {
        super(name, description, usageMessage, aliases);
        super.setPermissionMessage(Message.COMMAND_PERMISSION_REQUIRED.toString());
    }

    /**
     * Establece si el comando está prohibido en la consola y será comprobado automáticamente en la ejecución
     */
    protected void setProhibitConsole() {
        prohibitConsole = true;
    }

    @Override
    public final boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (prohibitConsole && !(sender instanceof Player)) {
            Message.COMMAND_PLAYER_ONLY.send(sender);
            return true;
        }

        if (!super.testPermissionSilent(sender)) {
            Message.COMMAND_PERMISSION_REQUIRED.send(sender);
            return true;
        }

        onExecute(sender, args);
        return true;
    }

    abstract void onExecute(CommandSender sender, String[] args);

    @Override
    public final List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        List<String> completions = new ArrayList<>();
        String lastWord = args[args.length - 1];

        onTabComplete(sender, args, lastWord.isEmpty(), completions);

        if (lastWord.isEmpty() || completions.isEmpty()) return completions;

        return StringUtil.copyPartialMatches(lastWord, completions, new ArrayList<>(completions.size()));
    }

    void onTabComplete(CommandSender sender, String[] args, boolean emptyArg, List<String> completions) {}
}
