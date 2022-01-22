package net.splodgebox.monthlycrates.utils;

import com.google.common.collect.Lists;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemStackBuilder {

    private ItemStack itemStack;

    public ItemStackBuilder(Material material) {
        this.itemStack = new ItemStack(material);
    }

    public ItemStackBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemStackBuilder setMaterial(Material material) {
        this.itemStack.setType(material);
        return this;
    }

    public ItemStackBuilder setAmount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    public ItemStackBuilder setName(String name) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(Chat.color(name));
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemStackBuilder setLore(List<String> lore) {
        ItemMeta meta = itemStack.getItemMeta();
        List<String> temp = Lists.newArrayList();

        lore.stream().forEach(s -> temp.add(Chat.color(s)));

        meta.setLore(temp);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemStackBuilder addLore(String name) {
        ItemMeta meta = itemStack.getItemMeta();
        List<String> lore = meta.getLore();

        if (lore == null)
            lore = Lists.newArrayList();

        lore.add(Chat.color(name));
        meta.setLore(lore);

        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemStackBuilder addItemFlags(ItemFlag... flags) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.addItemFlags(flags);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemStackBuilder setData(int data) {
        itemStack.setDurability((short) data);
        return this;
    }

    public ItemStackBuilder addEnchant(Enchantment enchantment, int level) {
        itemStack.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public ItemStackBuilder addEnchants(Map<Enchantment, Integer> enchants) {
        enchants.forEach((enchantment, integer) -> itemStack.addUnsafeEnchantment(enchantment, integer));
        return this;
    }

    public ItemStackBuilder withGlow() {
        ItemMeta meta = itemStack.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        itemStack.setItemMeta(meta);
        itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        return this;
    }

    public Nbt nbt() {
        return new Nbt(this);
    }

    public ItemStack build() {
        return itemStack;
    }

    public ItemStack build(Map<String, String> placeholders) {
        return replaceData(itemStack, placeholders);
    }

    public class Nbt {

        protected final ItemStackBuilder builder;
        protected NBTItem nbtItem;

        public Nbt(ItemStackBuilder builder) {
            this.builder = builder;
            this.nbtItem = new NBTItem(builder.itemStack);
        }

        public Nbt set(String key, String value) {
            nbtItem.setString(key, value);
            return this;
        }

        public Nbt set(String key, Integer intVal) {
            nbtItem.setInteger(key, intVal);
            return this;
        }

        public Nbt set(String key, Double intVal) {
            nbtItem.setDouble(key, intVal);
            return this;
        }

        public Nbt set(String key, Boolean bool) {
            nbtItem.setBoolean(key, bool);
            return this;
        }

        public ItemStack build() {
            return nbtItem.getItem();
        }

        public ItemStack build(Map<String, String> placeholders) {
            return replaceData(nbtItem.getItem(), placeholders);
        }

        public ItemStackBuilder builder() {
            return builder;
        }

    }

    private ItemStack replaceData(ItemStack itemStack, Map<String, String> replaceMap) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        boolean hasName = itemMeta.hasDisplayName();
        boolean hasLore = itemMeta.hasLore();
        List<String> newLore = new ArrayList<>();
        String name = itemStack.getType().name();
        if (hasName) name = itemMeta.getDisplayName();
        if (replaceMap != null && !replaceMap.isEmpty()) {
            if (hasName) {
                for (String s : replaceMap.keySet())
                    if (name.contains(s)) name = name.replace(s, replaceMap.get(s));
                itemMeta.setDisplayName(Chat.color(name));
            }
            if (hasLore) {
                for (String s : itemMeta.getLore()) {
                    for (String z : replaceMap.keySet()) if (s.contains(z)) s = s.replace(z, replaceMap.get(z));
                    newLore.add(Chat.color(s));
                }
                itemMeta.setLore(newLore);
            }
        }

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

}
