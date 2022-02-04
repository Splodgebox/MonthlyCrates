package net.splodgebox.monthlycrates.controllers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import net.splodgebox.monthlycrates.MonthlyCrates;
import net.splodgebox.monthlycrates.data.Crate;
import net.splodgebox.monthlycrates.data.Reward;
import net.splodgebox.monthlycrates.utils.*;
import net.splodgebox.monthlycrates.utils.gui.Gui;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class CrateAnimationController {

    private final MonthlyCrates plugin;
    private final Player player;
    private final Crate crate;

    private final int[][] columns = {
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

    private final Map<Integer, Integer[]> animationSlots;

    private RandomCollection<Reward> rewardCollection;
    private List<Reward> rewards;

    @Getter @Setter private int completedRewards;
    @Getter @Setter private boolean completed;
    @Getter @Setter private boolean redeemed;

    private final Gui inventory;

    public CrateAnimationController(MonthlyCrates plugin, Player player, Crate crate) {
        this.plugin = plugin;
        this.player = player;
        this.crate = crate;

        inventory = new Gui(crate.getTitle(), 6);

        rewardCollection = new RandomCollection<>();

        rewards = Lists.newArrayList();

        for (Pair<Double, Reward> reward : crate.getRewards()) {
            rewards.add(reward.getValue());
            rewardCollection.add(reward.getKey(), reward.getValue());
        }

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

        plugin.getCrateController().getActiveCrates().put(player.getUniqueId(), inventory);
    }

    public void start() {
        IntStream.range(0, inventory.getInventory().getSize()).forEach(i -> inventory.setItem(i,
                crate.getFillerPane(), (player, inventoryClickEvent) -> {}));

        List<Integer> slots = Lists.newArrayList(12, 13, 14, 21, 22, 23, 30, 31, 32);
        slots.forEach(integer -> inventory.setItem(integer, crate.getHiddenPane(), (player, inventoryClickEvent) -> {
            shuffleRewards(inventoryClickEvent.getSlot());
        }));

        inventory.setItem(49, crate.getLockedPane(), (player1, inventoryClickEvent) -> {});

        inventory.open(player);

        inventory.setCloseAction((player, event) -> {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (plugin.getCrateController().getActiveCrates().containsKey(player.getUniqueId())) {
                    inventory.open(player);
                }
            }, 5L);
        });
    }

    private void shuffleRewards(int slot) {
        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                Reward reward = rewardCollection.next();
                inventory.setItem(slot, reward.create(), (player, inventoryClickEvent) -> {});

                if (i < 18) {
                    if ((i % 2) == 1) {
                        XSound.UI_BUTTON_CLICK.play(player, 5, 1);
                    }

                    addPanes(slot);
                    i++;
                    return;
                }

                for (String command : reward.getCommands()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            command.replace("%player%", player.getName()));
                }

                if (reward.isGiveItem()) {
                    player.getInventory().addItem(reward.create());
                }

                setCompletedRewards(getCompletedRewards() + 1);

                if (getCompletedRewards() == 9) {
                    setCompleted(true);
                    finalReward();
                }

                if (!crate.isDuplicateReward()) {
                    rewards.remove(reward);
                    rewardCollection.clear();
                    rewards.forEach(rewardManager -> rewardCollection.add(rewardManager.getChance(), rewardManager));
                }

                XSound.ENTITY_PLAYER_LEVELUP.play(player, 5, 8);

                cancel();
                Arrays.stream(animationSlots.get(slot)).forEach(integers -> inventory.setItem(integers,
                        new ItemStackBuilder(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem()).setName(" ").build(),
                        (player, inventoryClickEvent) -> {}));
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    public void finalReward() {
        setPanes();
        RandomCollection<Reward> randomCollection = new RandomCollection<>();
        crate.getBonusRewards().forEach(bonusReward -> randomCollection.add(bonusReward.getKey(), bonusReward.getValue()));

        Bukkit.getScheduler().runTaskLater(plugin, () -> inventory.setItem(49, crate.getFinalPane(), (player1, inventoryClickEvent) -> {
            if (isCompleted() && !isRedeemed()) {
                Reward rewardManager = randomCollection.next();
                inventory.setItem(49, rewardManager.create(), (player2, inventoryClickEven2) -> { });

                for (String command : rewardManager.getCommands()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            command.replace("%player%", player.getName()));
                }

                if (rewardManager.isGiveItem())
                    player.getInventory().addItem(rewardManager.create());
                setRedeemed(true);

                XSound.ENTITY_PLAYER_LEVELUP.play(player, 5, 8);
                plugin.getCrateController().getActiveCrates().remove(player.getUniqueId());
            }
        }), 110L);
    }

    public void addPanes(int slot) {
        for (Integer integers : animationSlots.get(slot)) {
            int amount = ThreadLocalRandom.current().nextInt(crate.getColors().size());
            XMaterial color = crate.getColors().get(amount);
            inventory.setItem(
                    integers,
                    new ItemStackBuilder(color.parseItem())
                            .setName(" ")
                            .build(), (player1, inventoryClickEvent) -> {
                    }
            );
        }
    }

    public void setPanes() {
        new BukkitRunnable() {
            int timer = crate.getShuffleTime();
            int amount = 0;

            @Override
            public void run() {
                if (timer == 0) {
                    Arrays.stream(columns).flatMapToInt(Arrays::stream).forEach(integers -> inventory.setItem(
                            integers, crate.getFillerPane(), (player1, inventoryClickEvent) -> {}));
                    XSound.ENTITY_PLAYER_LEVELUP.play(player, 5, 1);
                    cancel();
                    return;
                }

                if (amount == crate.getColors().size()) amount = 0;
                XMaterial color = crate.getColors().get(amount);
                Arrays.stream(columns).flatMapToInt(Arrays::stream).forEach(integers -> inventory.setItem(integers,
                        new ItemStackBuilder(color.parseItem()).setName(" ").build(), (player1, inventoryClickEvent) -> {}));
                XSound.BLOCK_ANVIL_LAND.play(player, 5, 1);
                amount++;
                timer--;
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

}
