package warps.mongo.event;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import warps.mongo.warp.Warp;

@Getter
@Setter
@RequiredArgsConstructor
public abstract class WarpEvent extends Event implements Cancellable {
    @Getter private static final HandlerList handlerList = new HandlerList();

    private final CommandSender sender;
    private final @NonNull Warp warp;

    private boolean cancelled = false;

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
