package net.splodgebox.monthlycrates.utils;

import de.tr7zw.nbtapi.NBTItem;
import net.splodgebox.monthlycrates.utils.enums.CompatibleHand;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemUtils {

    public static boolean isValid(ItemStack itemStack) {
        return itemStack != null && itemStack.getType() != Material.AIR;
    }

    public static boolean hasNBT(ItemStack itemStack, String tag) {
        if (!isValid(itemStack)) return false;

        NBTItem nbtItem = new NBTItem(itemStack);
        return nbtItem.hasKey(tag);
    }

    public static String getNBTString(ItemStack itemStack, String tag) {
        NBTItem nbtItem = new NBTItem(itemStack);
        return nbtItem.getString(tag);
    }

    public static ItemStack setCustomModelData(ItemStack itemStack, int model) {
        ItemStack clone = itemStack;
        ItemMeta itemMeta = clone.getItemMeta();
        itemMeta.setCustomModelData(model);
        clone.setItemMeta(itemMeta);

        return clone;
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
