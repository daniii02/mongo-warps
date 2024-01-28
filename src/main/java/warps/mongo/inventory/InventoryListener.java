package warps.mongo.inventory;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public class InventoryListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;

        Inventory clickedInventory = event.getClickedInventory();
        if (!(clickedInventory instanceof WarpInventory)) return;

        WarpInventory inventory = (WarpInventory) event.getInventory();
        event.setCancelled(inventory.onClick(event.getRawSlot()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        if (!(topInventory instanceof WarpInventory)) return;

        // Compruebo si los drags contienen slots de arriba
        int topInventorySize = topInventory.getSize();
        for (Integer i : event.getRawSlots()) {
            if (i >= topInventorySize) continue;

            // Si contiene slots de arriba, cancelo el evento
            event.setCancelled(true);
            return;
        }
    }
}
