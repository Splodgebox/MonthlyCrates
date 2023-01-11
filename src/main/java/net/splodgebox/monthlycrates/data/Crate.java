package net.splodgebox.monthlycrates.data;

import com.cryptomorin.xseries.XMaterial;
import com.google.common.collect.ImmutableMap;
import de.tr7zw.nbtapi.NBTItem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.splodgebox.monthlycrates.utils.ItemStackBuilder;
import net.splodgebox.monthlycrates.utils.ItemUtils;
import net.splodgebox.monthlycrates.utils.Pair;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class Crate {

    private final String id;
    private final String title;
    private final String name;
    private final List<String> lore;
    private final XMaterial material;
    private final List<String> nbt;
    private final int customModelData;

    private final List<XMaterial> colors;
    private final int shuffleTime;
    private final boolean duplicateReward;
    private final ItemStack fillerPane;
    private final ItemStack hiddenPane;
    private final ItemStack lockedPane;
    private final ItemStack finalPane;

    private final List<Pair<Double, Reward>> bonusRewards;
    private final List<Pair<Double, Reward>> rewards;

    public ItemStack create(String player) {
        ItemStack itemStack =  new ItemStackBuilder(material.parseItem())
                .setName(name)
                .setLore(lore)
                .nbt()
                .set("MonthlyCrates", id)
                .set("NoStack", UUID.randomUUID().toString())
                .build(ImmutableMap.of("%player%", player));

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
