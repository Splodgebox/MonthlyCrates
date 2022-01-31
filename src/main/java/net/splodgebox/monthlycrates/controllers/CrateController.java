package net.splodgebox.monthlycrates.controllers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import javafx.util.Pair;
import lombok.Getter;
import net.splodgebox.monthlycrates.MonthlyCrates;
import net.splodgebox.monthlycrates.data.Crate;
import net.splodgebox.monthlycrates.data.Reward;
import net.splodgebox.monthlycrates.utils.FileManager;
import net.splodgebox.monthlycrates.utils.ItemStackBuilder;
import net.splodgebox.monthlycrates.utils.RandomCollection;
import net.splodgebox.monthlycrates.utils.XMaterial;
import net.splodgebox.monthlycrates.utils.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class CrateController {

    @Getter
    private final HashMap<String, Crate> crates;
    private final FileManager cratesFile;

    @Getter
    private HashMap<UUID, Gui> activeCrates;

    public CrateController(MonthlyCrates plugin) {
        crates = Maps.newHashMap();
        activeCrates = Maps.newHashMap();
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
                    config.getString(path + "title"),
                    config.getString(path + "name"),
                    config.getStringList(path + "lore"),
                    XMaterial.matchXMaterial(Objects.requireNonNull(config.getString(path + "material")))
                            .orElse(XMaterial.CHEST),
                    colors,
                    config.getInt(path + "animation.shuffle-time"),
                    config.getBoolean(path + "animation.duplicate-rewards"),
                    getPane(crate, "filler"),
                    getPane(crate, "hidden"),
                    getPane(crate, "locked"),
                    getPane(crate, "final"),
                    fetchRewards(crate, "bonus-rewards"),
                    fetchRewards(crate, "rewards")
            ));
        }
    }

    public List<Pair<Double, Reward>> fetchRewards(String crate, String identifier) {
        YamlConfiguration config = cratesFile.getConfiguration();
        List<Pair<Double, Reward>> rewards = Lists.newArrayList();

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
            rewards.add(new Pair<>(reward.getChance(), reward));
        }

        return rewards;
    }

    public ItemStack getPane(String crate, String identifier) {
        YamlConfiguration config = cratesFile.getConfiguration();
        String path = "Crates." + crate + ".animation.panes." + identifier + ".";

        return new ItemStackBuilder(XMaterial.matchXMaterial(config.getString(path + "material"))
                .orElse(XMaterial.BARRIER).parseItem())
                .setName(config.getString(path + "name"))
                .setLore(config.getStringList(path + "lore"))
                .build();
    }

}
