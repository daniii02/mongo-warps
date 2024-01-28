package warps.mongo.inventory;

import lombok.Getter;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.Container;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutOpenWindow;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftContainer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import warps.mongo.MongoWarps;
import warps.mongo.manager.ConfigManager;
import warps.mongo.util.ItemBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@Getter
public class InventoryManager {
    // Tamaño del inventario de warps, no se incluye la fila de navegación.
    private int menuSize;
    // Items para el menu de warps.
    private ItemStack warpItem, forwardItem, backwardItem;

    public InventoryManager() {
        reloadConfiguration();
    }

    /**
     * Recarga las configuraciones relacionadas con el menu de warps
     */
    public void reloadConfiguration() {
        FileConfiguration config = MongoWarps.get().getConfig();

        // Se suman 9 por la fila de navegación
        menuSize = config.getInt("warp-menu-size")+9;
        if (!Arrays.asList(18, 27, 36, 45, 54).contains(menuSize)) {
            MongoWarps.get().warning("Tamaño de menú de warps inválido [warp-menu-size]: " + menuSize);
            MongoWarps.get().warning("Se utilizará el tamaño por defecto.");
            menuSize = 27;
        }

        ConfigManager configManager = MongoWarps.get().getConfigManager();
        warpItem = configManager.deserializeItem(config, "warp-menu-items.warp");
        if (warpItem == null) warpItem = new ItemBuilder(Material.ENDER_PEARL).setName("&a&lWarp {warp}").setLore("&7Click para teletransportarte al warp &f{warp}&7.").build();

        forwardItem = configManager.deserializeItem(config, "warp-menu-items.forward");
        if (forwardItem == null) forwardItem = new ItemBuilder(Material.ARROW).setName("&aPágina siguiente ({current}/{max})").build();

        backwardItem = configManager.deserializeItem(config, "warp-menu-items.backward");
        if (backwardItem == null) backwardItem = new ItemBuilder(Material.ARROW).setName("&aPágina anterior ({current}/{max})").build();
    }

    /**
     * Reabre los inventarios
     */
    public void reopenInventories() {
        computeWarpInventories(warpInventory -> openWarpInventory(warpInventory.getPlayer()));
    }

    /**
     * Recarga los títulos de los inventarios
     */
    public void reloadInventoryTitles() {
        computeWarpInventories(WarpInventory::reloadMenuTitle);
    }

    /**
     * Ejecuta una acción en todos los inventarios de warps abiertos
     * @param consumer acción
     */
    public void computeWarpInventories(Consumer<WarpInventory> consumer) {
        MongoWarps.get().getTaskManager().sync(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Inventory topInventory = player.getOpenInventory().getTopInventory();
                if (!(topInventory instanceof WarpInventory)) continue;

                consumer.accept((WarpInventory) topInventory);
            }
        });
    }

    /**
     * Crea un inventario para un jugador y lo abre
     * Uso interno
     * @param player jugador
     * @return <tt>true</tt> si se pudo abrir el inventario, <tt>false</tt> si no hay warps disponibles
     */
    public boolean openWarpInventory(Player player) {
        List<String> availableWarps = MongoWarps.get().getWarpManager().getPlayerAvailableWarps(player);
        if (availableWarps.isEmpty()) return false;

        WarpInventory inventory = new WarpInventory(player, menuSize, availableWarps);
        int id = ((CraftPlayer) player).getHandle().nextContainerCounter();
        player.openInventory(new CraftContainer(inventory, player, id).getBukkitView());
        inventory.reloadMenuTitle();
        return true;
    }

    /**
     * Actualiza el título de un inventario
     * @param player jugador
     * @param title título
     */
    public void setInventoryTitle(Player player, String title) {
        EntityPlayer handle = ((CraftPlayer) player).getHandle();
        Container activeContainer = handle.activeContainer;
        InventoryView bukkitView = activeContainer.getBukkitView();
        String type = CraftContainer.getNotchInventoryType(bukkitView.getType());
        int size = bukkitView.getTopInventory().getSize();
        handle.playerConnection.sendPacket(new PacketPlayOutOpenWindow(activeContainer.windowId, type, new ChatComponentText(title), size));
        handle.updateInventory(activeContainer);
    }
}
