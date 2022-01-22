package net.splodgebox.monthlycrates.listeners;

import net.splodgebox.monthlycrates.utils.ItemUtils;
import net.splodgebox.monthlycrates.utils.enums.CompatibleHand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class CrateListeners implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItem();

        if (!ItemUtils.isValid(itemStack)) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (!ItemUtils.hasNBT(itemStack, "MonthlyCrates")) return;

        ItemUtils.takeActiveItem(player, CompatibleHand.getHand(event), 1);

        // TODO: Run animation
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack itemStack = event.getItemInHand();
        if (ItemUtils.isValid(itemStack) && ItemUtils.hasNBT(itemStack, "MonthlyCrates")) {
            event.setCancelled(true);
        }
    }

}
