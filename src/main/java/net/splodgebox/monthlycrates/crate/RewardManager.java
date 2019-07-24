package net.splodgebox.monthlycrates.crate;

import lombok.Getter;
import net.splodgebox.monthlycrates.utils.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class RewardManager {

    @Getter
    private double chance;

    @Getter
    private Material material;

    @Getter
    private String name;

    @Getter
    private int amount;

    @Getter
    private String command;

    @Getter
    private List<String> enchants;

    @Getter
    private boolean giveItem;

    @Getter
    private List<String> lore;

    public RewardManager(double chance, Material material, String name, List<String> lore, int amount, String command, List<String> enchants, boolean giveItem){
        this.chance = chance;
        this.material = material;
        this.name = name;
        this.lore = lore;
        this.amount = amount;
        this.command = command;
        this.enchants = enchants;
        this.giveItem = giveItem;
    }

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
