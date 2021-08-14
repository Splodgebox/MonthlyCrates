package net.splodgebox.monthlycrates.listeners;

import lombok.RequiredArgsConstructor;
import net.splodgebox.monthlycrates.MonthlyCrates;
import net.splodgebox.monthlycrates.managers.AnimationManager;
import net.splodgebox.monthlycrates.utils.CompatibleHand;
import net.splodgebox.monthlycrates.utils.ItemUtils;
import net.splodgebox.monthlycrates.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class PlayerListeners implements Listener {

    private final MonthlyCrates plugin;

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        if (plugin.getPlayerList().containsKey(player.getUniqueId())) {
            Bukkit.getScheduler().runTaskLater(plugin, () ->
                    plugin.getPlayerList().get(player.getUniqueId()).open(player), 5L);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItem();

        if (!ItemUtils.isValid(itemStack)) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!ItemUtils.hasNbt(itemStack, "MonthlyCrate")) return;
        event.setCancelled(true);

        String crate = ItemUtils.getNbtString(itemStack, "MonthlyCrate");

        if (!plugin.crates.getConfiguration().getBoolean("Crates." + crate + ".animation.duplicate-rewards") &&
                plugin.crates.getConfiguration().getConfigurationSection("Crates." + crate + ".rewards").getKeys(false).size() < 9) {
            Message.NOT_ENOUGH_REWARDS.msg(player);
            return;
        }

        ItemUtils.takeActiveItem(player, CompatibleHand.getHand(event), 1);

        new AnimationManager(plugin, crate, player);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack itemStack = event.getItemInHand();
        if (ItemUtils.isValid(itemStack) && ItemUtils.hasNbt(itemStack, "MonthlyCrate"))
            event.setCancelled(true);
    }

}
