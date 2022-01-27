package net.splodgebox.monthlycrates.controllers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.splodgebox.monthlycrates.MonthlyCrates;
import net.splodgebox.monthlycrates.data.Crate;
import net.splodgebox.monthlycrates.utils.ItemStackBuilder;
import net.splodgebox.monthlycrates.utils.XMaterial;
import net.splodgebox.monthlycrates.utils.gui.Gui;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
    @Getter @Setter private int completedRewards;
    @Getter @Setter private boolean completed;

    private Gui inventory;

    public CrateAnimationController(MonthlyCrates plugin, Player player, Crate crate) {
        this.plugin = plugin;
        this.player = player;
        this.crate = crate;

        inventory = new Gui(crate.getTitle(), 6);

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
                crate.getFiller(), (player, inventoryClickEvent) -> {}));

        List<Integer> slots = Lists.newArrayList(12, 13, 14, 21, 22, 23, 30, 31, 32);
        slots.forEach(integer -> inventory.setItem(integer, crate.getHidden(), (player, inventoryClickEvent) -> {
                    // shuffleRewards(inventoryClickEvent.getSlot());
        }));

        inventory.setItem(49, crate.getLocked(), (player1, inventoryClickEvent) -> {});

        inventory.open(player);
    }

    private void shuffleRewards(int slot) {

    }

    public void addPanes(int slot) {
        int amount = 0;
        for (Integer integers : animationSlots.get(slot)) {
            if (amount == crate.getColors().size()) amount = 0;
            XMaterial color = crate.getColors().get(amount);
            inventory.setItem(
                    integers,
                    new ItemStackBuilder(color.parseItem())
                            .setName(" ")
                            .build(), (player1, inventoryClickEvent) -> {
                    }
            );
            amount++;
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
                            integers, crate.getFiller(), (player1, inventoryClickEvent) -> {}));
                    cancel();
                    return;
                }

                if (amount == crate.getColors().size()) amount = 0;
                XMaterial color = crate.getColors().get(amount);
                Arrays.stream(columns).flatMapToInt(Arrays::stream).forEach(integers -> inventory.setItem(integers,
                        new ItemStackBuilder(color.parseItem()).setName(" ").build(), (player1, inventoryClickEvent) -> {}));
                amount++;
                timer--;
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

}
