package net.splodgebox.monthlycrates.utils;

import com.google.common.collect.Lists;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ItemUtils {

    public static boolean isValid(ItemStack item) {
        return item != null && item.getType() != Material.AIR;
    }

    public static NBTItem getNbtItem(ItemStack item) {
        return new NBTItem(item);
    }

    public static String getNbtString(ItemStack item, String key) {
        return getNbtItem(item).getString(key);
    }

    public static boolean hasNbt(ItemStack item, String key) {
        return getNbtItem(item).hasKey(key);
    }

    public static Integer getNbtInt(ItemStack item, String key) {
        return getNbtItem(item).getInteger(key);
    }

    public static Double getNbtDouble(ItemStack item, String key) {
        return getNbtItem(item).getDouble(key);
    }

    public static Boolean getNbtBoolean(ItemStack item, String key) {
        return getNbtItem(item).getBoolean(key);
    }

    public static void setLore(ItemStack itemStack, String... lore) {
        List<String> newLore = Lists.newArrayList();
        ItemMeta meta = itemStack.getItemMeta();

        Arrays.stream(lore).forEach(str -> newLore.add(Chat.color(str)));
        meta.setLore(newLore);

        itemStack.setItemMeta(meta);
    }

    public static void setLore(ItemStack itemStack, int line, String lore) {
        ItemMeta meta = itemStack.getItemMeta();
        List<String> loreList = meta.getLore();

        loreList.set(line, Chat.color(lore));
        meta.setLore(loreList);

        itemStack.setItemMeta(meta);
    }

    public static void setLore(ItemStack itemStack, List<String> lore) {
        setLore(itemStack, lore.toArray(new String[0]));
    }

    public static void setName(ItemStack itemStack, String name) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(Chat.color(name));

        itemStack.setItemMeta(meta);
    }

    public static void addLore(ItemStack itemStack, List<String> lore) {
        ItemMeta meta = itemStack.getItemMeta();
        List<String> newLore = meta.hasLore() ?
                meta.getLore() : Lists.newArrayList();

        lore.forEach(str -> newLore.add(Chat.color(str)));
        meta.setLore(newLore);

        itemStack.setItemMeta(meta);
    }

    public static void addLore(ItemStack itemStack, String lore) {
        addLore(itemStack, Collections.singletonList(lore));
    }

    public static void removeLore(ItemStack itemStack, int loreLine) {
        ItemMeta meta = itemStack.getItemMeta();
        if (!meta.hasLore())
            return;

        List<String> lore = meta.getLore();
        lore.remove(loreLine);

        meta.setLore(lore);
        itemStack.setItemMeta(meta);
    }

    public static void removeLore(ItemStack itemStack, String loreLine) {
        ItemMeta meta = itemStack.getItemMeta();
        if (!meta.hasLore())
            return;

        List<String> lore = meta.getLore();
        lore.remove(loreLine);

        meta.setLore(lore);
        itemStack.setItemMeta(meta);
    }

    public static String toBase64(ItemStack[] items) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(items.length);

            for (int i = 0; i < items.length; i++)
                dataOutput.writeObject(items[i]);

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    public static String toBase64(ItemStack item) {
        return toBase64(new ItemStack[]{ item });
    }

    public static ItemStack fromBase64(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] items = new ItemStack[dataInput.readInt()];

            for (int i = 0; i < items.length; i++)
                items[i] = (ItemStack) dataInput.readObject();

            dataInput.close();
            return items[0];
        } catch (Exception e) {
            throw new IllegalStateException("Unable to convert base64 to item stacks.", e);
        }
    }

    public static void takeActiveItem(Player player, CompatibleHand hand, int amount) {
        if (hand == CompatibleHand.MAIN_HAND) {
            ItemStack item = player.getInventory().getItemInHand();

            int result = item.getAmount() - amount;
            item.setAmount(result);

            player.setItemInHand(result > 0 ? item : null);
        } else {
            ItemStack item = player.getInventory().getItemInOffHand();

            int result = item.getAmount() - amount;
            item.setAmount(result);

            player.getEquipment().setItemInOffHand(result > 0 ? item : null);
        }
    }
}
