package warps.mongo.util;

import javafx.util.Pair;
import lombok.AllArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Clase para crear ItemStacks de forma más sencilla.
 */
@AllArgsConstructor
public final class ItemBuilder {
    private final ItemStack itemStack;
    private final ItemMeta itemMeta;

    public ItemBuilder(ItemStack itemStack) {
        this(itemStack, itemStack.getItemMeta());
    }

    public ItemBuilder(Material material) {
        this(new ItemStack(material));
    }

    public Material getType() {
        return itemStack.getType();
    }

    public int getMaxStackSize() {
        return itemStack.getMaxStackSize();
    }

    public int getAmount() {
        return itemStack.getAmount();
    }

    public ItemBuilder setAmount(final int amount) {
        this.itemStack.setAmount(amount);
        return this;
    }

    public boolean hasDisplayName() {
        return itemMeta.hasDisplayName();
    }

    public String getName() {
        return itemMeta.hasDisplayName() ? itemMeta.getDisplayName() : "";
    }

    public ItemBuilder setName(final String name) {
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        return this;
    }

    /**
     * Reemplaza el nombre del item.
     * @param replaced texto que será reemplazado
     * @param replacement texto de reemplazo
     * @return instancia actual
     */
    public ItemBuilder replaceName(CharSequence replaced, CharSequence replacement) {
        return replaceName(new Pair<>(replaced, replacement));
    }

    /**
     * Reemplaza el nombre del item. Cada pareja representa un reemplazo.
     * @param replacements lista de reemplazos
     * @return instancia actual
     */
    @SafeVarargs
    public final ItemBuilder replaceName(Pair<CharSequence, CharSequence>... replacements) {
        String name = getName();
        for (Pair<CharSequence, CharSequence> replacement : replacements) {
            name = name.replace(replacement.getKey(), replacement.getValue());
        }
        setName(name);
        return this;
    }

    public ItemBuilder setLore(final String... lore) {
        return setLore(Arrays.asList(lore));
    }

    public ItemBuilder setLore(final List<String> lore) {
        itemMeta.setLore(lore.stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList()));
        return this;
    }

    public ItemBuilder addLore(final String... lore) {
        return addLore(Arrays.asList(lore));
    }

    public ItemBuilder addLore(final List<String> lore) {
        List<String> curLore = getLore();
        curLore.addAll(lore.stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList()));
        itemMeta.setLore(curLore);
        return this;
    }

    /**
     * Reemplaza cada línea del lore.
     * @param replaced texto que será reemplazado
     * @param replacement texto de reemplazo
     * @return instancia actual
     */
    public ItemBuilder replaceLore(CharSequence replaced, CharSequence replacement) {
        return replaceLore(new Pair<>(replaced, replacement));
    }

    /**
     * Reemplaza cada línea del lore.
     * Cada pareja representa un reemplazo.
     * @param replacements lista de reemplazos
     * @return instancia actual
     */
    @SafeVarargs
    public final ItemBuilder replaceLore(Pair<CharSequence, CharSequence>... replacements) {
        setLore(getLore().stream().map(s -> {
            for (Pair<CharSequence, CharSequence> replacement : replacements) {
                s = s.replace(replacement.getKey(), replacement.getValue());
            }
            return s;
        }).collect(Collectors.toList()));
        return this;
    }

    @SafeVarargs
    public final ItemBuilder replaceNameAndLore(Pair<CharSequence, CharSequence>... replacements) {
        replaceName(replacements);
        replaceLore(replacements);
        return this;
    }

    public List<String> getLore() {
        List<String> lore = itemMeta.getLore();
        return lore == null ? new ArrayList<>() : lore;
    }

    public ItemBuilder addEnchantment(final Enchantment enchantment, final int level) {
        itemMeta.addEnchant(enchantment, level, false);
        return this;
    }

    public ItemBuilder addFlags(ItemFlag... flags) {
        itemMeta.addItemFlags(flags);
        return this;
    }

    public ItemStack build() {
        this.itemStack.setItemMeta(this.itemMeta);
        return this.itemStack;
    }
}
