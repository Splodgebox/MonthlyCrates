package net.splodgebox.monthlycrates.data;

import com.cryptomorin.xseries.XMaterial;
import de.tr7zw.nbtapi.NBTItem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.splodgebox.monthlycrates.utils.ItemStackBuilder;
import net.splodgebox.monthlycrates.utils.ItemUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class Reward {

    private final double chance;
    private final XMaterial material;
    private final int amount;
    private final String name;
    private final List<String> lore;
    private final HashMap<Enchantment, Integer> enchants;
    private final List<String> commands;
    private final boolean giveItem;
    private final List<String> nbt;
    private final int customModelData;

    public ItemStack create() {
        ItemStack itemStack =  new ItemStackBuilder(material.parseItem())
                .setName(name)
                .setLore(lore)
                .addEnchants(enchants)
                .build();

        if (!nbt.isEmpty()) {
            NBTItem nbtItem = new NBTItem(itemStack);
            nbt.stream().map(tag -> tag.split(":")).forEach(index -> nbtItem.setString(index[0], index[1]));
            itemStack = nbtItem.getItem();
        }

        if (customModelData > 0) {
            itemStack = ItemUtils.setCustomModelData(itemStack, customModelData);
        }

        return itemStack;
    }

}
