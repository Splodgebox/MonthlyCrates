package net.splodgebox.monthlycrates.crate;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.splodgebox.monthlycrates.MonthlyCrates;
import net.splodgebox.monthlycrates.utils.Chat;
import net.splodgebox.monthlycrates.utils.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.Month;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CrateManager {

    @Getter
    private final String crate;

    @Getter
    private final MonthlyCrates plugin;

    /**
     * Return the crate as an itemstack
     * @return
     */
    public ItemStack getItemStack(String player){
        String path = "Crates." + getCrate() + ".";
        String name = plugin.crates.getConfiguration().getString(path + "name");
        Material material = Material.getMaterial(plugin.crates.getConfiguration().getString(path + "material"));
        List<String> lore = plugin.crates.getConfiguration().getStringList(path + "lore").stream().map(string ->
                Chat.color(string).replace("%player%", player)).collect(Collectors.toList());
        return new ItemStackBuilder(material)
                .setName(name)
                .setLore(lore)
                .nbt()
                .set("MonthlyCrate", getCrate())
                .set("DontStackPlz", String.valueOf(UUID.randomUUID()))
                .build();
    }

    public void loadRewards(){
        MonthlyCrates.getRewardMap().remove(crate);
        List<RewardManager> list = Lists.newArrayList();

        plugin.crates.getConfiguration().getConfigurationSection("Crates." + crate + ".rewards").getKeys(false).forEach(string -> {
            int amount =   plugin.crates.getConfiguration().getInt("Crates." + crate + ".rewards." + string + ".amount");
            if (amount == 0) amount = 1;
            list.add(
                    new RewardManager(
                            plugin.crates.getConfiguration().getDouble("Crates." + crate + ".rewards." + string + ".chance"),
                            Material.valueOf(plugin.crates.getConfiguration().getString("Crates." + crate + ".rewards." + string + ".material")),
                            plugin.crates.getConfiguration().getString("Crates." + crate + ".rewards." + string + ".name"),
                            plugin.crates.getConfiguration().getStringList("Crates." + crate + ".rewards." + string + ".lore"),
                            amount,
                            plugin.crates.getConfiguration().getString("Crates." + crate + ".rewards." + string + ".command"),
                            plugin.crates.getConfiguration().getStringList("Crates." + crate + ".rewards." + string + ".enchants"),
                            plugin.crates.getConfiguration().getBoolean("Crates." + crate + ".rewards." + string + ".give_item")));
        });
        MonthlyCrates.getRewardMap().put(crate, list);
    }


}
