package net.splodgebox.monthlycrates.controllers;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import net.splodgebox.monthlycrates.MonthlyCrates;
import net.splodgebox.monthlycrates.data.Crate;
import net.splodgebox.monthlycrates.data.Reward;
import net.splodgebox.monthlycrates.utils.FileManager;
import net.splodgebox.monthlycrates.utils.ItemStackBuilder;
import net.splodgebox.monthlycrates.utils.Pair;
import net.splodgebox.monthlycrates.utils.gui.Gui;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CrateController {

    private static final String SECTION = "value";
    private static final char ALT_COLOR_CHAR = '&';

    private final MonthlyCrates plugin;
    private final Logger logger;
    private final FileManager cratesFile;

    private final Map<String, Crate> crates = new LinkedHashMap<>();
    private final Map<UUID, Gui> activeCrates = new HashMap<>();

    public CrateController(MonthlyCrates plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.cratesFile = new FileManager(plugin, "crates", plugin.getDataFolder().getAbsolutePath());

        loadCrates();
    }

    /** Read-only view. Mutate via {@link #putCrate}/{@link #reload}. */
    public Map<String, Crate> getCrates() {
        return Collections.unmodifiableMap(crates);
    }

    public Optional<Crate> getCrate(String id) {
        return Optional.ofNullable(crates.get(id));
    }

    public void registerActive(UUID uuid, Gui gui) {
        activeCrates.put(uuid, gui);
    }

    public void unregisterActive(UUID uuid) {
        activeCrates.remove(uuid);
    }

    public boolean isActive(UUID uuid) {
        return activeCrates.containsKey(uuid);
    }

    public Optional<Gui> getActive(UUID uuid) {
        return Optional.ofNullable(activeCrates.get(uuid));
    }

    public void loadCrates() {
        YamlConfiguration config = cratesFile.getConfiguration();

        ConfigurationSection root = config.getConfigurationSection("Crates");
        if (root == null) {
            logger.warning("crates.yml has no 'Crates' section — no crates loaded.");
            return;
        }

        for (String id : root.getKeys(false)) {
            try {
                crates.put(id, readCrate(config, id));
            } catch (Exception e) {
                logger.log(Level.WARNING, "Skipping crate '" + id + "': " + e.getMessage(), e);
            }
        }

        logger.info("Loaded " + crates.size() + " crate(s).");
    }

    private Crate readCrate(YamlConfiguration config, String id) {
        String path = "Crates." + id + ".";

        List<XMaterial> colors = new ArrayList<>();
        for (String raw : config.getStringList(path + "animation.colors")) {
            XMaterial.matchXMaterial(raw).ifPresentOrElse(
                    colors::add,
                    () -> logger.warning("Crate '" + id + "': unknown animation colour '" + raw + "', skipped."));
        }

        return new Crate(
                id,
                config.getString(path + "title", id),
                config.getString(path + "name", id),
                config.getStringList(path + "lore"),
                requireMaterial(config, path + "material", XMaterial.CHEST, "crate '" + id + "'"),
                config.getStringList(path + "nbt"),
                config.getInt(path + "customModelData", 0),
                config.getString(path + "data", ""),
                colors,
                config.getInt(path + "animation.shuffle-time", 5),
                config.getBoolean(path + "animation.duplicate-rewards"),
                readPane(config, id, "filler"),
                readPane(config, id, "hidden"),
                readPane(config, id, "locked"),
                readPane(config, id, "final"),
                readRewards(config, id, "bonus-rewards"),
                readRewards(config, id, "rewards")
        );
    }

    private List<Pair<Double, Reward>> readRewards(YamlConfiguration config, String crate, String identifier) {
        List<Pair<Double, Reward>> rewards = new ArrayList<>();

        ConfigurationSection section = config.getConfigurationSection("Crates." + crate + "." + identifier);
        if (section == null) {
            logger.warning("Crate '" + crate + "' has no '" + identifier + "' section.");
            return rewards;
        }

        for (String key : section.getKeys(false)) {
            String path = "Crates." + crate + "." + identifier + "." + key + ".";
            String where = "crate '" + crate + "' " + identifier + " '" + key + "'";

            Optional<XMaterial> material = XMaterial.matchXMaterial(config.getString(path + "material", ""));
            if (material.isEmpty() || material.get() == XMaterial.AIR) {
                logger.warning("Skipping " + where + ": missing or invalid material.");
                continue;
            }

            double chance = config.getDouble(path + "chance");
            if (chance <= 0) {
                logger.warning("Skipping " + where + ": chance must be > 0.");
                continue;
            }

            Reward reward = new Reward(
                    chance,
                    material.get(),
                    config.getInt(path + "amount", 1),
                    config.getString(path + "name"),
                    config.getStringList(path + "lore"),
                    readEnchants(config, path, where),
                    config.getStringList(path + "command"),
                    config.getBoolean(path + "give-item"),
                    config.getStringList(path + "nbt"),
                    config.getInt(path + "customModelData", 0),
                    config.getString(path + "data", "")
            );
            rewards.add(new Pair<>(reward.getChance(), reward));
        }

        return rewards;
    }

    private HashMap<Enchantment, Integer> readEnchants(YamlConfiguration config, String path, String where) {
        HashMap<Enchantment, Integer> enchants = new HashMap<>();

        for (String raw : config.getStringList(path + "enchants")) {
            String[] parts = raw.split(":");
            if (parts.length != 2) {
                logger.warning(where + ": malformed enchant '" + raw + "', expected NAME:LEVEL.");
                continue;
            }

            Optional<XEnchantment> match = XEnchantment.of(parts[0].trim());
            if (match.isEmpty() || match.get().get() == null) {
                logger.warning(where + ": unknown enchant '" + parts[0] + "'.");
                continue;
            }

            try {
                enchants.put(match.get().get(), Integer.parseInt(parts[1].trim()));
            } catch (NumberFormatException e) {
                logger.warning(where + ": bad enchant level '" + parts[1] + "'.");
            }
        }

        return enchants;
    }

    private ItemStack readPane(YamlConfiguration config, String crate, String identifier) {
        String path = "Crates." + crate + ".animation.panes." + identifier + ".";
        String where = "crate '" + crate + "' pane '" + identifier + "'";

        return new ItemStackBuilder(requireMaterial(config, path + "material", XMaterial.BARRIER, where).parseItem())
                .setName(config.getString(path + "name", " "))
                .setLore(config.getStringList(path + "lore"))
                .build();
    }

    private XMaterial requireMaterial(YamlConfiguration config, String path, XMaterial fallback, String where) {
        String raw = config.getString(path);
        if (raw == null) {
            logger.warning(where + ": missing material at '" + path + "', using " + fallback + ".");
            return fallback;
        }
        return XMaterial.matchXMaterial(raw).orElseGet(() -> {
            logger.warning(where + ": unknown material '" + raw + "', using " + fallback + ".");
            return fallback;
        });
    }

    public void addReward(ItemStack itemStack, String crate, boolean giveItem, double chance, String command) {
        YamlConfiguration config = cratesFile.getConfiguration();

        ConfigurationSection section = config.getConfigurationSection("Crates." + crate + ".rewards");
        String path = "Crates." + crate + ".rewards." + nextKey(section) + ".";

        ItemMeta meta = itemStack.getItemMeta();

        String name = "";
        List<String> lore = new ArrayList<>();

        if (meta != null) {
            if (meta.hasDisplayName()) {
                name = toAltColor(meta.getDisplayName());
            }
            if (meta.getLore() != null) {
                meta.getLore().forEach(line -> lore.add(toAltColor(line)));
            }
        }

        List<String> enchantmentList = new ArrayList<>();
        itemStack.getEnchantments().forEach((enchantment, level) ->
                enchantmentList.add(XEnchantment.of(enchantment).name() + ":" + level));

        config.set(path + "chance", chance);
        config.set(path + "material", XMaterial.matchXMaterial(itemStack).name());
        config.set(path + "name", name);
        config.set(path + "lore", lore);
        config.set(path + "amount", itemStack.getAmount());
        config.set(path + "enchants", enchantmentList);
        config.set(path + "command", new ArrayList<>(List.of(command)));
        config.set(path + "give-item", giveItem);

        cratesFile.save();
    }

    /** max(numeric key) + 1. size()+1 left gaps and reused indices once keys weren't dense. */
    private int nextKey(ConfigurationSection section) {
        if (section == null) {
            return 0;
        }
        int max = -1;
        for (String key : section.getKeys(false)) {
            try {
                max = Math.max(max, Integer.parseInt(key));
            } catch (NumberFormatException ignored) {
                // non-numeric keys don't participate in numbering
            }
        }
        return max + 1;
    }

    private String toAltColor(String input) {
        return input.replace(ChatColor.COLOR_CHAR, ALT_COLOR_CHAR);
    }

    public void putCrate(String id, Crate crate) {
        crates.put(id, crate);
    }

    public void reload() {
        new ArrayList<>(activeCrates.keySet()).forEach(uuid -> {
            Optional.ofNullable(plugin.getServer().getPlayer(uuid))
                    .ifPresent(HumanEntity::closeInventory);
        });
        activeCrates.clear();

        cratesFile.reload();
        crates.clear();
        loadCrates();
    }
}