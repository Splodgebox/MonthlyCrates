package net.splodgebox.monthlycrates.controllers;

import com.google.common.collect.Maps;
import lombok.Getter;
import net.splodgebox.monthlycrates.MonthlyCrates;
import net.splodgebox.monthlycrates.data.Crate;
import net.splodgebox.monthlycrates.data.Reward;
import net.splodgebox.monthlycrates.utils.FileManager;
import net.splodgebox.monthlycrates.utils.RandomCollection;
import net.splodgebox.monthlycrates.utils.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CrateController {

    @Getter
    private final HashMap<String, Crate> crates;
    private final FileManager cratesFile;

    public CrateController(MonthlyCrates plugin) {
        crates = Maps.newHashMap();
        cratesFile = new FileManager(plugin, "crates", plugin.getDataFolder().getAbsolutePath());

        Bukkit.getScheduler().runTaskLater(plugin, this::loadCrates, 20L);
    }

    public void loadCrates() {
        YamlConfiguration config = cratesFile.getConfiguration();

        for (String crate : config.getConfigurationSection("Crates").getKeys(false)) {
            String path = "Crates." + crate + ".";

            List<XMaterial> colors = config.getStringList(path + "animation.colors").stream()
                    .map(s -> XMaterial.matchXMaterial(s).orElse(XMaterial.AIR))
                    .collect(Collectors.toList());

            crates.put(crate, new Crate(
                    crate,
                    config.getString(path + "name"),
                    config.getStringList(path + "description"),
                    XMaterial.matchXMaterial(Objects.requireNonNull(config.getString(path + "material")))
                            .orElse(XMaterial.CHEST),
                    colors,
                    config.getInt(path + "animation.shuffle-time"),
                    config.getBoolean(path + "animation.give-item"),
                    fetchRewards(crate, "bonus-rewards"),
                    fetchRewards(crate, "rewards")
            ));
        }
    }

    public RandomCollection<Reward> fetchRewards(String crate, String identifier) {
        YamlConfiguration config = cratesFile.getConfiguration();
        RandomCollection<Reward> rewards = new RandomCollection<>();

        for (String key : config.getConfigurationSection("Crates." + crate + "." + identifier).getKeys(false)) {
            String path = "Crates." + crate + "." + identifier + "." + key + ".";

            HashMap<Enchantment, Integer> enchants = config.getStringList(path + "enchants").stream()
                    .map(s -> s.split(":"))
                    .collect(Collectors.toMap(index -> Enchantment.getByName(index[0]), index -> Integer.parseInt(index[1]), (a, b) -> b, Maps::newHashMap));

            Reward reward = new Reward(
                    config.getDouble(path + "chance"),
                    XMaterial.matchXMaterial(Objects.requireNonNull(config.getString(path + "material")))
                            .orElse(XMaterial.AIR),
                    config.getString(path + "name"),
                    config.getStringList(path + "lore"),
                    enchants,
                    config.getStringList(path + "command"),
                    config.getBoolean(path + "give-item")
            );
            rewards.add(reward.getChance(), reward);
        }

        return rewards;
    }

}
