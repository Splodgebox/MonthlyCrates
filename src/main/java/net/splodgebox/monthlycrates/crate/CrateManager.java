package net.splodgebox.monthlycrates.crate;

import lombok.Getter;
import net.splodgebox.monthlycrates.MonthlyCrates;
import net.splodgebox.monthlycrates.utils.Chat;
import net.splodgebox.monthlycrates.utils.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CrateManager {

    @Getter
    private String name;

    @Getter
    private Player player;

    @Getter
    private MonthlyCrates plugin;

    /**
     * Player is used to replace the placeholder
     * with the players name
     * @param name
     * @param player
     */
    public CrateManager(String name, Player player){
        this.name = name;
        this.player = player;
        this.plugin = MonthlyCrates.getInstance();
    }


    /**
     * Return the crate as an itemstack
     * @return
     */
    public ItemStack getItemStack(){
        String path = "Crates." + getName() + ".";
        String name = plugin.crates.getConfiguration().getString(path + "name");
        Material material = Material.getMaterial(plugin.crates.getConfiguration().getString(path + "material"));
        List<String> lore = plugin.crates.getConfiguration().getStringList(path + "lore").stream().map(string ->
                Chat.color(string).replace("%player%", getPlayer().getName())).collect(Collectors.toList());
        return new ItemStackBuilder(material)
                .setName(name)
                .setLore(lore)
                .nbt()
                .set("MonthlyCrate", name)
                .set("DontStackPlz", String.valueOf(UUID.randomUUID()))
                .build();
    }

}
