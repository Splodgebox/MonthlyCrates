package net.splodgebox.monthlycrates.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.splodgebox.monthlycrates.utils.ItemStackBuilder;
import net.splodgebox.monthlycrates.utils.XMaterial;
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

    public ItemStack create() {
        return new ItemStackBuilder(material.parseItem())
                .setName(name)
                .setLore(lore)
                .addEnchants(enchants)
                .build();
    }

}
