package warps.mongo.inventory;

import javafx.util.Pair;
import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryCustom;
import org.bukkit.entity.Player;
import warps.mongo.MongoWarps;
import warps.mongo.message.Message;
import warps.mongo.util.ItemBuilder;
import warps.mongo.warp.Warp;

import java.util.List;

public class WarpInventory extends CraftInventoryCustom {
    private final InventoryManager manager = MongoWarps.get().getInventoryManager();

    @Getter private final Player player;
    private final int maxWarpsSize;
    private final List<String> availableWarps;

    private int page;

    public WarpInventory(Player player, int size, List<String> availableWarps) {
        super(player, size);
        this.player = player;
        this.maxWarpsSize = size - 9; // Se resta la fila de navegación
        this.availableWarps = availableWarps;

        loadItems();
    }

    private void loadItems() {
        // Cargo los warps de la página actual
        int index = 0;
        for (int i = page * maxWarpsSize, max = Math.min((page + 1) * maxWarpsSize, availableWarps.size()); i < max; i++) {
            setWarpItem(availableWarps.get(i), index++);
        }

        // Cargo los items de navegación
        int maxPage = getMaxPage();
        if (page > 0) {
            ItemBuilder builder = new ItemBuilder(manager.getBackwardItem().clone());
            builder.replaceNameAndLore(new Pair<>("{current}", Integer.toString(page-1)), new Pair<>("{max}", Integer.toString(maxPage)));
            setItem(getSize() - 9, builder.build());
        }
        if (page < maxPage) {
            ItemBuilder builder = new ItemBuilder(manager.getForwardItem().clone());
            builder.replaceNameAndLore(new Pair<>("{current}", Integer.toString(page+1)), new Pair<>("{max}", Integer.toString(maxPage)));
            setItem(getSize() - 1, builder.build());
        }
    }

    private void reloadItems() {
        clear();
        loadItems();
    }

    protected void reloadMenuTitle() {
        manager.setInventoryTitle(player, Message.WARP_MENU_TITLE.toString().replace("{amount}", Integer.toString(availableWarps.size())));
    }

    private int getMaxPage() {
        return (availableWarps.size() - 1) / maxWarpsSize;
    }

    /**
     * Coloca un warp en el inventario
     * @param warpName nombre del warp
     * @param slot slot
     */
    private void setWarpItem(String warpName, int slot) {
        ItemBuilder builder = new ItemBuilder(manager.getWarpItem().clone());
        builder.replaceNameAndLore(new Pair<>("{warp}", warpName));
        setItem(slot, builder.build());
    }

    /**
     * Agrega un warp al inventario, si el usuario tiene permiso
     * @param warp warp
     */
    public void addWarp(Warp warp) {
        if (!warp.hasPermission(player)) return;

        availableWarps.add(warp.getName());

        reloadItems();
        reloadMenuTitle();
    }

    /**
     * Elimina un warp del inventario, si existe en el inventario
     * @param warp warp
     */
    public void removeWarp(Warp warp) {
        int index = availableWarps.indexOf(warp.getName());
        // Si no está en la lista no hago nada
        if (index == -1) return;

        availableWarps.remove(index);

        if (availableWarps.isEmpty()) {
            player.closeInventory();
            Message.WARP_MENU_NO_WARPS.send(player);
            return;
        }

        // Si no hay warps en la página actual, retrocedo
        int maxPage = getMaxPage();
        if (page > maxPage) page = maxPage;

        reloadItems();
        reloadMenuTitle();
    }

    /**
     * Llamado cada vez que se hace clic en el inventario
     * @param slot raw slot
     * @return <tt>true</tt> si se debe cancelar el evento
     */
    public boolean onClick(int slot) {
        int size = getSize();
        if (slot < 0 || slot > size) return false;

        player.playSound(player.getLocation(), Sound.CLICK, .6f, 1);

        if (slot < maxWarpsSize) onWarpClick(slot);
        else if (slot == size - 1) onForwardClick();
        else if (slot == size - 9) onBackwardClick();
        return true;
    }

    private void onWarpClick(int slot) {
        int index = page * maxWarpsSize + slot;
        if (index >= availableWarps.size()) return;

        String warpName = availableWarps.get(index);
        Warp warp = MongoWarps.get().getWarpManager().getWarpByName(warpName);
        if (warp == null) {
            // El warp ya no existe, lo elimino de la lista
            reloadItems();
            return;
        }

        warp.teleport(player);
        Message.WARP_TELEPORT.send(player, "{warp}", warp.getName());
    }

    private void onForwardClick() {
        if (page >= availableWarps.size() / maxWarpsSize) return;

        page++;
        reloadItems();
    }

    private void onBackwardClick() {
        if (page <= 0) return;

        page--;
        reloadItems();
    }
}
