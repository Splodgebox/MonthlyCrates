package net.splodgebox.monthlycrates.crate.events;

import de.tr7zw.changeme.nbtapi.NBTItem;
import net.splodgebox.monthlycrates.MonthlyCrates;
import net.splodgebox.monthlycrates.crate.AnimationManager;
import net.splodgebox.monthlycrates.utils.Message;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerEvents implements Listener {

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (AnimationManager.getPlayerList().containsKey(player.getUniqueId())) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    AnimationManager.getPlayerList().get(player.getUniqueId()).open(player);
                }
            }.runTaskLater(MonthlyCrates.getInstance(), 10L);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItem();
        if (itemStack == null || itemStack.getType() == Material.AIR) return;
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            NBTItem nbtItem = new NBTItem(itemStack);
            if (nbtItem.hasKey("MonthlyCrate")) {
                String crate = nbtItem.getString("MonthlyCrate");
                if (!MonthlyCrates.getInstance().crates.getConfiguration().getBoolean("Crates." + crate + ".animation.duplicate-rewards")) {
                    if (MonthlyCrates.getInstance().crates.getConfiguration().getConfigurationSection("Crates." + crate + ".rewards").getKeys(false).size() < 9) {
                        Message.NOT_ENOUGH_REWARDS.msg(player);
                        return;
                    }
                }
                if (nbtItem.getItem().getAmount() > 1) return;
                player.setItemInHand(new ItemStack(Material.AIR));
                new AnimationManager(crate, MonthlyCrates.getInstance(), player).init();
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack itemStack = event.getItemInHand();
        if (itemStack == null || itemStack.getType() == Material.AIR) return;
        NBTItem nbtItem = new NBTItem(itemStack);
        if (nbtItem.hasKey("MonthlyCrate")) {
            event.setCancelled(true);
        }
    }
}
