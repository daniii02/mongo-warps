package warps.mongo.event;

import lombok.NonNull;
import org.bukkit.command.CommandSender;
import warps.mongo.warp.Warp;

/**
 * Llamado cuando un jugador se teletransporta a un warp
 */
public class WarpTeleportEvent extends WarpEvent {
    public WarpTeleportEvent(@NonNull CommandSender sender, Warp warp) {
        super(sender, warp);
    }
}
