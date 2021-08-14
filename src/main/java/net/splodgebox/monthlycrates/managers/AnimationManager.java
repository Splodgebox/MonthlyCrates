package net.splodgebox.monthlycrates.managers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import net.splodgebox.monthlycrates.MonthlyCrates;
import net.splodgebox.monthlycrates.data.Reward;
import net.splodgebox.monthlycrates.utils.ItemStackBuilder;
import net.splodgebox.monthlycrates.utils.RandomCollection;
import net.splodgebox.monthlycrates.utils.XMaterial;
import net.splodgebox.monthlycrates.utils.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class AnimationManager {

    private final MonthlyCrates plugin;
    private final String crate;

    private Player player;
    private Gui inventory;

    private static final int[][] columns = {
            {0, 9, 18, 27, 36, 45},
            {1, 10, 19, 28, 37, 46},
            {2, 11, 20, 29, 38, 47},
            {3, 39, 48},
            {4, 40},
            {5, 41, 50},
            {6, 15, 24, 33, 42, 51},
            {7, 16, 25, 34, 43, 52},
            {8, 17, 26, 35, 44, 53},
    };

    private List<String> colorList;
    private RandomCollection<Reward> rewardCollection;
    private List<Reward> rewards;
    private Map<Integer, Integer[]> animationSlots;

    @Getter @Setter private int completedRewards;
    @Getter @Setter private boolean completed;

    public AnimationManager(MonthlyCrates plugin, String crate, Player player) {
        this.plugin = plugin;
        this.crate = crate;
        this.player = player;

        String displayName = plugin.crates.getConfiguration().getString("Crates." + crate + ".title");
        if (displayName == null) displayName = crate;

        inventory = new Gui(displayName, 6);

        colorList = plugin.crates.getConfiguration().getStringList("Crates." + crate + ".animation.colors");

        rewardCollection = new RandomCollection<>();

        rewards = Lists.newArrayList();
        rewards.addAll(MonthlyCrates.getRewardMap().get(crate));
        rewards.forEach(rewardManager -> rewardCollection.add(rewardManager.getChance(), rewardManager));

        animationSlots = Maps.newHashMap();
        animationSlots.put(12, new Integer[]{3, 39, 48, 9, 10, 11, 15, 16, 17});
        animationSlots.put(13, new Integer[]{4, 40, 9, 10, 11, 15, 16, 17});
        animationSlots.put(14, new Integer[]{5, 41, 50, 9, 10, 11, 15, 16, 17});

        animationSlots.put(21, new Integer[]{3, 39, 48, 18, 19, 20, 24, 25, 26});
        animationSlots.put(22, new Integer[]{4, 40, 18, 19, 20, 24, 25, 26});
        animationSlots.put(23, new Integer[]{5, 41, 50, 18, 19, 20, 24, 25, 26});

        animationSlots.put(30, new Integer[]{3, 39, 48, 27, 28, 29, 33, 34, 35});
        animationSlots.put(31, new Integer[]{4, 40, 27, 28, 29, 33, 34, 35});
        animationSlots.put(32, new Integer[]{5, 41, 50, 27, 28, 29, 33, 34, 35});

        plugin.getPlayerList().put(player.getUniqueId(), inventory);
        start();
    }

    public void start() {
        IntStream.range(0, inventory.getInventory().getSize()).forEach(i -> inventory.setItem(i,
                new ItemStackBuilder(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem()).setName(" ").build(), (player, inventoryClickEvent) -> {}));

        List<Integer> slots = Lists.newArrayList(12, 13, 14, 21, 22, 23, 30, 31, 32);
        slots.forEach(integer ->
                inventory.setItem(integer, new ItemStackBuilder(XMaterial.ENDER_CHEST.parseMaterial())
                                .setName("&7&l???")
                                .addLore("&7Click to redeem an item")
                                .addLore("&7from this monthly crate")
                                .build(), (player, inventoryClickEvent) -> {
                            shuffleRewards(inventoryClickEvent.getSlot());
                        })
        );

        inventory.setItem(49,
                new ItemStackBuilder(XMaterial.RED_STAINED_GLASS_PANE.parseItem())
                        .setName("&c&lLOCKED")
                        .addLore("&7You must unlock all other rewards")
                        .addLore("&7to obtain the bonus item")
                        .build(), (player1, inventoryClickEvent) -> {
                }
        );

        inventory.open(player);
    }

    private void shuffleRewards(int slot) {
        AtomicInteger integer = new AtomicInteger(0);

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    Reward rewardManager = rewardCollection.next();
                    inventory.setItem(slot, rewardManager.getItemStack(), (player, inventoryClickEvent) -> {});

                    if (integer.get() == 9) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                rewardManager.getCommand().replace("%player%", player.getName()));
                        if (rewardManager.isGiveItem()) {
                            player.getInventory().addItem(rewardManager.getItemStack());
                        }
                        setCompletedRewards(getCompletedRewards() + 1);
                        if (getCompletedRewards() == 9) {
                            setCompleted(true);
                            finalReward();
                        }
                        if (!plugin.crates.getConfiguration().getBoolean("Crates." + crate + ".animation.duplicate-rewards")) {
                            rewards.remove(rewardManager);
                            rewardCollection.clear();
                            rewards.forEach(rewardManagerr -> rewardCollection.add(rewardManagerr.getChance(), rewardManagerr));
                        }
                        cancel();
                        Arrays.stream(animationSlots.get(slot)).forEach(integers -> inventory.setItem(integers,
                                new ItemStackBuilder(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem()).setName(" ").build(),
                                (player, inventoryClickEvent) -> {
                                }));
                        return;
                    }
                    addPanes(slot);
                    integer.getAndAdd(1);
                } catch (NullPointerException ex) {
                    inventory.setItem(slot, new ItemStack(Material.AIR), (player, inventoryClickEvent) -> {});
                    Arrays.stream(animationSlots.get(slot)).forEach(integers -> inventory.setItem(integers,
                            new ItemStackBuilder(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem()).setName(" ").build(), (player, inventoryClickEvent) -> {}));
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    public void addPanes(int slot) {
        int amount = 0;
        for (Integer integers : animationSlots.get(slot)) {
            if (amount == colorList.size()) amount = 0;
            String color = colorList.get(amount);
            inventory.setItem(
                    integers,
                    new ItemStackBuilder(XMaterial.valueOf(color).parseItem())
                            .setName(" ")
                            .build(), (player1, inventoryClickEvent) -> {
                    }
            );
            amount++;
        }
    }

    @Getter
    @Setter
    private boolean redeemed;

    public void finalReward() {
        setPanes();
        RandomCollection<Reward> randomCollection = new RandomCollection<>();
        Objects.requireNonNull(plugin.crates.getConfiguration().getConfigurationSection("Crates." + crate + ".bonus-rewards")).getKeys(false).forEach(string -> {
            String path = "Crates." + crate + ".bonus-rewards." + string + ".";
            randomCollection.add(
                    plugin.crates.getConfiguration().getDouble(path + "chance"),
                    new Reward(
                            plugin.crates.getConfiguration().getDouble(path + "chance"),
                            Material.valueOf(plugin.crates.getConfiguration().getString(path + "material")),
                            plugin.crates.getConfiguration().getString(path + "name"),
                            plugin.crates.getConfiguration().getStringList(path + "lore"),
                            plugin.crates.getConfiguration().getInt(path + "amount"),
                            plugin.crates.getConfiguration().getString(path + "command"),
                            plugin.crates.getConfiguration().getStringList(path + "enchants"),
                            plugin.crates.getConfiguration().getBoolean(path + "give_item")
                    )
            );
        });

        Bukkit.getScheduler().runTaskLater(plugin, () -> inventory.setItem(49, new ItemStackBuilder(Material.ENDER_CHEST)
                        .setName("&7&l???")
                        .addLore("&7Click to redeem your")
                        .addLore("&7bonus reward")
                        .build(), (player1, inventoryClickEvent) -> {
                    if (isCompleted() && !isRedeemed()) {
                        Reward rewardManager = randomCollection.next();
                        inventory.setItem(49, rewardManager.getItemStack(), (player2, inventoryClickEven2) -> { });
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                rewardManager.getCommand().replace("%player%", player.getName()));
                        if (rewardManager.isGiveItem()) player.getInventory().addItem(rewardManager.getItemStack());
                        setRedeemed(true);
                        plugin.getPlayerList().remove(player.getUniqueId());

                        Bukkit.getScheduler().runTaskLater(plugin, () -> player.closeInventory(), 100L);
                    }
                }
        ), 110L);
    }

    public void setPanes() {
        new BukkitRunnable() {
            int timer = plugin.crates.getConfiguration().getInt("Crates." + crate + ".animation.shuffle-time");
            int amount = 0;

            @Override
            public void run() {
                if (timer == 0) {
                    Arrays.stream(columns).flatMapToInt(Arrays::stream).forEach(integers -> inventory.setItem(
                            integers, new ItemStackBuilder(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem())
                                    .setName(" ").build(), (player1, inventoryClickEvent) -> {}));
                    cancel();
                    return;
                }

                if (amount == colorList.size()) amount = 0;
                String color = colorList.get(amount);
                Arrays.stream(columns).flatMapToInt(Arrays::stream).forEach(integers -> inventory.setItem(
                        integers, new ItemStackBuilder(XMaterial.valueOf(color).parseItem())
                                .setName(" ").build(), (player1, inventoryClickEvent) -> {}));
                amount++;
                timer--;
            }
        }.runTaskTimer(plugin, 20L, 20l);
    }

}

