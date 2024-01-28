package warps.mongo.event;

import org.bukkit.command.CommandSender;
import warps.mongo.warp.Warp;

import javax.annotation.Nullable;

/**
 * Llamado cuando se crea un warp
 */
public class WarpCreateEvent extends WarpEvent {
    public WarpCreateEvent(@Nullable CommandSender sender, Warp warp) {
        super(sender, warp);
    }

    @Nullable
    @Override
    public CommandSender getSender() {
        return super.getSender();
    }
}
