package net.splodgebox.monthlycrates.controllers;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import lombok.Getter;
import net.splodgebox.monthlycrates.MonthlyCrates;
import net.splodgebox.monthlycrates.data.Crate;
import net.splodgebox.monthlycrates.data.Reward;
import net.splodgebox.monthlycrates.utils.ItemStackBuilder;
import net.splodgebox.monthlycrates.utils.Pair;
import net.splodgebox.monthlycrates.utils.RandomCollection;
import net.splodgebox.monthlycrates.utils.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class CrateAnimationController {

    private static final int TOTAL_REWARDS = 9;
    private static final int FINAL_SLOT = 49;
    private static final List<Integer> REWARD_SLOTS = List.of(12, 13, 14, 21, 22, 23, 30, 31, 32);

    private static final int[][] COLUMNS = {
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

    private static final int[] ALL_COLUMN_SLOTS =
            java.util.Arrays.stream(COLUMNS).flatMapToInt(java.util.Arrays::stream).toArray();

    private final MonthlyCrates plugin;
    private final Player player;
    private final Crate crate;
    private final Gui inventory;

    private final Map<Integer, int[]> animationSlots;
    private final RandomCollection<Reward> rewardCollection = new RandomCollection<>();
    private final List<Reward> rewards = new ArrayList<>();

    private final Set<Integer> spinning = new HashSet<>();

    @Getter private int completedRewards;
    @Getter private boolean completed;
    @Getter private boolean redeemed;

    private boolean finalRewardScheduled;

    public CrateAnimationController(MonthlyCrates plugin, Player player, Crate crate) {
        this.plugin = plugin;
        this.player = player;
        this.crate = crate;

        this.inventory = new Gui(crate.getTitle(), 6);

        for (Pair<Double, Reward> reward : crate.getRewards()) {
            rewards.add(reward.getValue());
            rewardCollection.add(reward.getKey(), reward.getValue());
        }

        animationSlots = new HashMap<>();
        animationSlots.put(12, new int[]{3, 39, 48, 9, 10, 11, 15, 16, 17});
        animationSlots.put(13, new int[]{4, 40, 9, 10, 11, 15, 16, 17});
        animationSlots.put(14, new int[]{5, 41, 50, 9, 10, 11, 15, 16, 17});

        animationSlots.put(21, new int[]{3, 39, 48, 18, 19, 20, 24, 25, 26});
        animationSlots.put(22, new int[]{4, 40, 18, 19, 20, 24, 25, 26});
        animationSlots.put(23, new int[]{5, 41, 50, 18, 19, 20, 24, 25, 26});

        animationSlots.put(30, new int[]{3, 39, 48, 27, 28, 29, 33, 34, 35});
        animationSlots.put(31, new int[]{4, 40, 27, 28, 29, 33, 34, 35});
        animationSlots.put(32, new int[]{5, 41, 50, 27, 28, 29, 33, 34, 35});

        plugin.getCrateController().registerActive(player.getUniqueId(), inventory);
    }

    public void start() {
        IntStream.range(0, inventory.getInventory().getSize())
                .forEach(i -> setStatic(i, crate.getFillerPane()));

        REWARD_SLOTS.forEach(slot ->
                inventory.setItem(slot, crate.getHiddenPane(), (p, event) -> shuffleRewards(event.getSlot())));

        setStatic(FINAL_SLOT, crate.getLockedPane());

        inventory.open(player);

        inventory.setCloseAction((p, event) -> Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (p.isOnline() && plugin.getCrateController().isActive(p.getUniqueId())) {
                inventory.open(p);
            }
        }, 5L));
    }

    private void shuffleRewards(int slot) {
        if (!spinning.add(slot)) {
            return;
        }
        setStatic(slot, crate.getHiddenPane());

        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                Reward reward = rewardCollection.next();
                if (reward == null) {
                    cancel();
                    return;
                }

                if (tick < 18) {
                    setStatic(slot, new ItemStackBuilder(reward.create())
                            .setAmount(reward.getAmount())
                            .build());
                    if (tick % 2 == 1) {
                        XSound.UI_BUTTON_CLICK.play(player, 1.0f, 1.0f);
                    }
                    addPanes(slot);
                    tick++;
                    return;
                }

                cancel();
                resolve(slot, reward);
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    private void resolve(int slot, Reward reward) {
        ItemStack display = reward.create();
        setStatic(slot, new ItemStackBuilder(display.clone()).setAmount(reward.getAmount()).build());

        for (String command : reward.getCommands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    command.replace("%player%", player.getName()));
        }

        if (reward.isGiveItem()) {
            player.getInventory().addItem(display.clone());
        }

        if (!crate.isDuplicateReward()) {
            rewards.removeIf(r -> r == reward);
            rewardCollection.clear();
            rewards.forEach(r -> rewardCollection.add(r.getChance(), r));
        }

        XSound.ENTITY_PLAYER_LEVELUP.play(player, 1.0f, 2.0f);

        ItemStack grey = new ItemStackBuilder(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem())
                .setName(" ")
                .build();
        for (int decorative : animationSlots.get(slot)) {
            setStatic(decorative, grey.clone());
        }

        completedRewards++;
        if (completedRewards >= TOTAL_REWARDS && !finalRewardScheduled) {
            finalRewardScheduled = true;
            completed = true;
            finalReward();
        }
    }

    private void finalReward() {
        RandomCollection<Reward> bonus = new RandomCollection<>();
        crate.getBonusRewards().forEach(b -> bonus.add(b.getKey(), b.getValue()));

        setPanes(() -> inventory.setItem(FINAL_SLOT, crate.getFinalPane(), (p, event) -> {
            if (!completed || redeemed) {
                return;
            }
            redeemed = true;

            Reward reward = bonus.next();
            if (reward == null) {
                plugin.getCrateController().unregisterActive(player.getUniqueId());
                return;
            }

            ItemStack display = reward.create();
            setStatic(FINAL_SLOT, display.clone());

            for (String command : reward.getCommands()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        command.replace("%player%", player.getName()));
            }

            if (reward.isGiveItem()) {
                player.getInventory().addItem(display.clone());
            }

            XSound.ENTITY_PLAYER_LEVELUP.play(player, 1.0f, 2.0f);
            plugin.getCrateController().unregisterActive(player.getUniqueId());
        }));
    }

    private void addPanes(int slot) {
        List<XMaterial> colors = crate.getColors();
        if (colors.isEmpty()) {
            return;
        }
        for (int decorative : animationSlots.get(slot)) {
            XMaterial color = colors.get(ThreadLocalRandom.current().nextInt(colors.size()));
            setStatic(decorative, new ItemStackBuilder(color.parseItem()).setName(" ").build());
        }
    }

    private void setPanes(Runnable onComplete) {
        List<XMaterial> colors = crate.getColors();
        if (colors.isEmpty()) {
            onComplete.run();
            return;
        }

        new BukkitRunnable() {
            int timer = crate.getShuffleTime();
            int index = 0;

            @Override
            public void run() {
                if (timer <= 0) {
                    for (int slot : ALL_COLUMN_SLOTS) {
                        setStatic(slot, crate.getFillerPane());
                    }
                    XSound.ENTITY_PLAYER_LEVELUP.play(player, 1.0f, 1.0f);
                    cancel();
                    onComplete.run();
                    return;
                }

                if (index >= colors.size()) {
                    index = 0;
                }

                ItemStack pane = new ItemStackBuilder(colors.get(index).parseItem()).setName(" ").build();
                for (int slot : ALL_COLUMN_SLOTS) {
                    setStatic(slot, pane.clone());
                }

                XSound.BLOCK_ANVIL_LAND.play(player, 1.0f, 1.0f);
                index++;
                timer--;
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void setStatic(int slot, ItemStack item) {
        inventory.setItem(slot, item, (p, event) -> {});
    }
}