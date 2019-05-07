package net.splodgebox.monthlycrates.crate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.splodgebox.monthlycrates.MonthlyCrates;
import net.splodgebox.monthlycrates.utils.Chat;
import net.splodgebox.monthlycrates.utils.ItemStackBuilder;
import net.splodgebox.monthlycrates.utils.RandomCollection;
import net.splodgebox.monthlycrates.utils.XMaterial;
import net.splodgebox.monthlycrates.utils.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class AnimationManager {

    @Getter
    private final String crate;

    @Getter
    private final MonthlyCrates plugin;

    @Getter
    private final Player player;

    @Getter
    @Setter
    private int completedRewards;

    @Getter
    @Setter
    private boolean completed;

    @Getter
    private static HashMap<UUID, Gui> playerList = Maps.newHashMap();

    public Gui inventory = new Gui("", 6);

    private Map<Integer, Integer[]> animationSlots = Maps.newHashMap();

    private RandomCollection<RewardManager> rewards = new RandomCollection<>();

    private List<RewardManager> rewardManagers = Lists.newArrayList();


    private static int[][] columns = {
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

    public void init() {
        plugin.crates.getConfiguration().getConfigurationSection("Crates." + crate + ".rewards").getKeys(false).forEach(string -> rewardManagers.add(new RewardManager(
                plugin.crates.getConfiguration().getDouble("Crates." + crate + ".rewards." + string + ".chance"),
                Material.valueOf(plugin.crates.getConfiguration().getString("Crates." + crate + ".rewards." + string + ".material")),
                plugin.crates.getConfiguration().getString("Crates." + crate + ".rewards." + string + ".name"),
                plugin.crates.getConfiguration().getInt("Crates." + crate + ".rewards." + string + ".amount"),
                plugin.crates.getConfiguration().getString("Crates." + crate + ".rewards." + string + ".command"),
                plugin.crates.getConfiguration().getBoolean("Crates." + crate + ".rewards." + string + ".give_item")
        )));
        rewardManagers.forEach(rewardManager -> rewards.add(rewardManager.getChance(), rewardManager));

        animationSlots.put(12, new Integer[]{3, 39, 48, 9, 10, 11, 15, 16, 17});
        animationSlots.put(13, new Integer[]{4, 40, 9, 10, 11, 15, 16, 17});
        animationSlots.put(14, new Integer[]{5, 41, 50, 9, 10, 11, 15, 16, 17});

        animationSlots.put(21, new Integer[]{3, 39, 48, 18, 19, 20, 24, 25, 26});
        animationSlots.put(22, new Integer[]{4, 40, 18, 19, 20, 24, 25, 26});
        animationSlots.put(23, new Integer[]{5, 41, 50, 18, 19, 20, 24, 25, 26});

        animationSlots.put(30, new Integer[]{3, 39, 48, 27, 28, 29, 33, 34, 35});
        animationSlots.put(31, new Integer[]{4, 40, 27, 28, 29, 33, 34, 35});
        animationSlots.put(32, new Integer[]{5, 41, 50, 27, 28, 29, 33, 34, 35});
        playerList.put(player.getUniqueId(), inventory);
        openGUI();
    }

    public void openGUI() {
        IntStream.range(0, inventory.getInventory().getSize()).forEach(i -> inventory.setItem(i,
                new ItemStackBuilder(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem()).setName(" ").build(),
                (player, inventoryClickEvent) -> {
                }));
        List<Integer> slots = Lists.newArrayList(12, 13, 14, 21, 22, 23, 30, 31, 32);
        slots.forEach(integer ->
                inventory.setItem(integer, new ItemStackBuilder(XMaterial.ENDER_CHEST.parseMaterial())
                                .setName("&7&l???")
                                .addLore("&7Click to redeem an item")
                                .addLore("&7from this monthly crate")
                                .build(),
                        (player, inventoryClickEvent) -> {
                            shuffleRewards(inventoryClickEvent.getSlot());
                        }
                ));
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
                    RewardManager rewardManager = rewards.getRandomValue();
                    inventory.setItem(slot, rewardManager.getItemStack(), (player, inventoryClickEvent) -> {
                    });
                    if (integer.get() == 9) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                rewardManager.getCommand().replace("%player%", player.getName()));
                        setCompletedRewards(getCompletedRewards() + 1);
                        if (getCompletedRewards() == 9) {
                            setCompleted(true);
                            finalReward();
                        }
                        rewardManagers.remove(rewardManager);
                        rewards.clear();
                        rewardManagers.forEach(rewardManagerr -> rewards.add(rewardManagerr.getChance(), rewardManagerr));
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
                    inventory.setItem(slot, new ItemStack(Material.AIR), (player, inventoryClickEvent) -> {
                    });
                    Arrays.stream(animationSlots.get(slot)).forEach(integers -> inventory.setItem(integers,
                            new ItemStackBuilder(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem()).setName(" ").build(),
                            (player, inventoryClickEvent) -> {
                            }));
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    public void addPanes(int slot) {
        for (Integer integers : animationSlots.get(slot)) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int amount = random.nextInt(1, 5);
            switch (amount) {
                case 1:
                    inventory.setItem(
                            integers,
                            new ItemStackBuilder(XMaterial.CYAN_STAINED_GLASS_PANE.parseItem())
                                    .setName(" ")
                                    .build(),
                            (player1, inventoryClickEvent) -> {
                            }
                    );
                    break;
                case 2:
                    inventory.setItem(
                            integers,
                            new ItemStackBuilder(XMaterial.WHITE_STAINED_GLASS_PANE.parseItem())
                                    .setName(" ")
                                    .build(), (player1, inventoryClickEvent) -> {
                            }
                    );
                    break;
                case 3:
                    inventory.setItem(
                            integers,
                            new ItemStackBuilder(XMaterial.BLUE_STAINED_GLASS_PANE.parseItem())
                                    .setName(" ")
                                    .build(), (player1, inventoryClickEvent) -> {
                            }
                    );
                    break;
                case 4:
                    inventory.setItem(
                            integers,
                            new ItemStackBuilder(XMaterial.PINK_STAINED_GLASS_PANE.parseItem())
                                    .setName(" ")
                                    .build(), (player1, inventoryClickEvent) -> {
                            }
                    );
                    break;
            }
        }
    }

    @Getter
    @Setter
    private boolean redeemed;

    public void finalReward() {
        setPanes(10, "CYAN_STAINED_GLASS_PANE");
        setPanes(20, "WHITE_STAINED_GLASS_PANE");
        setPanes(30, "BLUE_STAINED_GLASS_PANE");
        setPanes(40, "PINK_STAINED_GLASS_PANE");
        setPanes(50, "CYAN_STAINED_GLASS_PANE");
        setPanes(60, "WHITE_STAINED_GLASS_PANE");
        setPanes(70, "BLUE_STAINED_GLASS_PANE");
        setPanes(80, "PINK_STAINED_GLASS_PANE");
        setPanes(100, "GRAY_STAINED_GLASS_PANE");
        RandomCollection<RewardManager> randomCollection = new RandomCollection<>();
        for (String string : Objects.requireNonNull(plugin.crates.getConfiguration().getConfigurationSection("Crates." + crate + ".bonus-rewards")).getKeys(false)) {
            randomCollection.add(
                    plugin.crates.getConfiguration().getDouble("Crates." + crate + ".bonus-rewards." + string + ".chance"),
                    new RewardManager(
                            plugin.crates.getConfiguration().getDouble("Crates." + crate + ".bonus-rewards." + string + ".chance"),
                            Material.valueOf(plugin.crates.getConfiguration().getString("Crates." + crate + ".bonus-rewards." + string + ".material")),
                            plugin.crates.getConfiguration().getString("Crates." + crate + ".bonus-rewards." + string + ".name"),
                            plugin.crates.getConfiguration().getInt("Crates." + crate + ".bonus-rewards." + string + ".amount"),
                            plugin.crates.getConfiguration().getString("Crates." + crate + ".bonus-rewards." + string + ".command"),
                            plugin.crates.getConfiguration().getBoolean("Crates." + crate + ".bonus-rewards." + string + ".give_item")
                    ));
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                inventory.setItem(49, new ItemStackBuilder(Material.ENDER_CHEST)
                                .setName("&7&l???")
                                .addLore("&7Click to redeem your")
                                .addLore("&7bonus reward")
                                .build(), (player1, inventoryClickEvent) -> {
                            if (isCompleted() && !isRedeemed()) {
                                RewardManager rewardManager = randomCollection.getRandomValue();
                                inventory.setItem(49, rewardManager.getItemStack(), (player2, inventoryClickEven2) -> {
                                });
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                        rewardManager.getCommand()
                                                .replace("%player%", player.getName()));
                                if (rewardManager.isGiveItem()) {
                                    player.getInventory().addItem(randomCollection.getRandomValue().getItemStack());
                                }
                                setRedeemed(true);
                                playerList.remove(player.getUniqueId());
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        player.closeInventory();
                                    }
                                }.runTaskLater(getPlugin(), 100L);
                            }

                        }
                );
            }
        }.runTaskLater(getPlugin(), 110L);
    }

    public void setPanes(long delay, String material) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (int[] integer : columns) {
                    for (Integer integers : integer) {
                        inventory.setItem(
                                integers,
                                new ItemStackBuilder(XMaterial.valueOf(material).parseItem())
                                        .setName(" ")
                                        .build(),
                                (player1, inventoryClickEvent) -> {
                                }
                        );
                    }
                }
            }
        }.runTaskLater(MonthlyCrates.getInstance(), delay);
    }

}
