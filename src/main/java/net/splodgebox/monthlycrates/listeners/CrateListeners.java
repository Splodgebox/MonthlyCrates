package net.splodgebox.monthlycrates.listeners;

import lombok.RequiredArgsConstructor;
import net.splodgebox.monthlycrates.MonthlyCrates;
import net.splodgebox.monthlycrates.controllers.CrateAnimationController;
import net.splodgebox.monthlycrates.data.Crate;
import net.splodgebox.monthlycrates.utils.ItemUtils;
import net.splodgebox.monthlycrates.utils.Message;
import net.splodgebox.monthlycrates.utils.enums.CompatibleHand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class CrateListeners implements Listener {

    private final MonthlyCrates plugin;

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItem();

        if (!ItemUtils.isValid(itemStack)) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (!ItemUtils.hasNBT(itemStack, "MonthlyCrates")) return;

        String crateName = ItemUtils.getNBTString(itemStack, "MonthlyCrates");
        if (!plugin.getCrateController().getCrates().containsKey(crateName)) {
            Message.INVALID_CRATE.msg(player);
            return;
        }

        Crate crate = plugin.getCrateController().getCrates().get(crateName);
        if (!crate.isDuplicateReward() &&
                crate.getRewards().size() < 9) {
            Message.NOT_ENOUGH_REWARDS.msg(player);
            return;
        }

        ItemUtils.takeActiveItem(player, CompatibleHand.getHand(event), 1);

        CrateAnimationController crateAnimationController = new CrateAnimationController(plugin, player, crate);
        crateAnimationController.start();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack itemStack = event.getItemInHand();
        if (ItemUtils.isValid(itemStack) && ItemUtils.hasNBT(itemStack, "MonthlyCrates")) {
            event.setCancelled(true);
        }
    }

}
