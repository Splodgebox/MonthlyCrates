package net.splodgebox.monthlycrates.crate;

import lombok.Getter;
import net.splodgebox.monthlycrates.utils.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

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
    private boolean giveItem;


    public RewardManager(double chance, Material material, String name, int amount, String command, boolean giveItem){
        this.chance = chance;
        this.material = material;
        this.name = name;
        this.amount = amount;
        this.command = command;
        this.giveItem = giveItem;
    }

    public ItemStack getItemStack(){
        return new ItemStackBuilder(material)
                .setName(name)
                .build();
    }

}
