package net.splodgebox.monthlycrates.controllers;

import com.cryptomorin.xseries.XMaterial;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import net.splodgebox.monthlycrates.MonthlyCrates;
import net.splodgebox.monthlycrates.data.Crate;
import net.splodgebox.monthlycrates.data.Reward;
import net.splodgebox.monthlycrates.utils.FileManager;
import net.splodgebox.monthlycrates.utils.ItemStackBuilder;
import net.splodgebox.monthlycrates.utils.Pair;
import net.splodgebox.monthlycrates.utils.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.*;
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
                    config.getStringList(path + "nbt"),
                    config.getInt(path + "customModelData", 0),
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
                    config.getInt(path + "amount", 1),
                    config.getString(path + "name"),
                    config.getStringList(path + "lore"),
                    enchants,
                    config.getStringList(path + "command"),
                    config.getBoolean(path + "give-item"),
                    config.getStringList(path + "nbt"),
                    config.getInt(path + "customModelData", 0)
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


    public void addReward(ItemStack itemStack, String crate, boolean giveItem, double chance, String command) {
        String path = "Crates." + crate + ".rewards." + getLength(cratesFile.getConfiguration().getConfigurationSection("Crates." + crate + ".rewards")) + ".";
        String name = itemStack.getItemMeta().getDisplayName();
        if (!itemStack.getItemMeta().hasDisplayName()) name = "";
        if (name.contains(String.valueOf(ChatColor.COLOR_CHAR)))
            name = name.replace(String.valueOf(ChatColor.COLOR_CHAR), "&");

        List<String> lore = itemStack.getItemMeta().getLore();
        if (lore == null) lore = Lists.newArrayList();

        if (!lore.isEmpty()) {
            lore.stream().filter(s -> s.contains(String.valueOf(ChatColor.COLOR_CHAR))).forEach(s ->
                    s.replace(String.valueOf(ChatColor.COLOR_CHAR), "&"));
        }

        List<String> enchantmentList = Lists.newArrayList();
        itemStack.getEnchantments().forEach((enchantment, integer) ->
                enchantmentList.add(enchantment.getName() + ":" + integer));

        String material = itemStack.getType().toString();
        cratesFile.getConfiguration().set(path + "chance", chance);
        cratesFile.getConfiguration().set(path + "material", material);
        cratesFile.getConfiguration().set(path + "name", name);
        cratesFile.getConfiguration().set(path + "lore", lore);
        cratesFile.getConfiguration().set(path + "amount", itemStack.getAmount());
        cratesFile.getConfiguration().set(path + "enchants", enchantmentList);
        cratesFile.getConfiguration().set(path + "command", Lists.newArrayList(command));
        cratesFile.getConfiguration().set(path + "give_item", giveItem);
        cratesFile.save();
    }

    public int getLength(ConfigurationSection section) {
        int length = section.getKeys(false).size();
        return length + 1;
    }

    public void reload() {
        cratesFile.reload();
        crates.clear();
        loadCrates();
    }


}
