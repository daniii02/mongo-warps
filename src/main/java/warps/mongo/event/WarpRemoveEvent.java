package warps.mongo.event;

import org.bukkit.command.CommandSender;
import warps.mongo.warp.Warp;

import javax.annotation.Nullable;

/**
 * Llamado cuando se elimina un warp
 */
public class WarpRemoveEvent extends WarpEvent {
    public WarpRemoveEvent(@Nullable CommandSender sender, Warp warp) {
        super(sender, warp);
    }

    @Nullable
    @Override
    public CommandSender getSender() {
        return super.getSender();
    }
}
