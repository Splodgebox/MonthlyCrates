package net.splodgebox.monthlycrates.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.splodgebox.monthlycrates.utils.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@RequiredArgsConstructor
public class Reward {

    @Getter
    private final double chance;

    @Getter
    private final Material material;

    @Getter
    private final String name;

    @Getter
    private final List<String> lore;

    @Getter
    private final int amount;

    @Getter
    private final String command;

    @Getter
    private final List<String> enchants;

    @Getter
    private final boolean giveItem;


    public ItemStack getItemStack(){
        ItemStack itemStack = new ItemStackBuilder(material)
                .setAmount(amount)
                .setName(name)
                .setLore(lore)
                .build();
        if (enchants == null || enchants.isEmpty())
            return itemStack;
        for (String enchantments : enchants){
            String[] index = enchantments.split(":");
            itemStack.addUnsafeEnchantment(Enchantment.getByName(index[0]), Integer.parseInt(index[1]));
        }
        return itemStack;
    }

}
