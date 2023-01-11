package net.splodgebox.monthlycrates.utils.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public class GuiListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Inventory inventory = e.getInventory();
        int slot = e.getRawSlot();

        if (Gui.getGuis().stream().noneMatch(gui -> gui.getInventory().equals(inventory))) return;
        Gui gui = Gui.getGuis().stream().filter(g -> g.getInventory().equals(inventory)).findAny().get();

        e.setCancelled(true);

        ClickAction clickAction = gui.getClickAction(slot);
        if (clickAction == null) return;

        clickAction.click(player, e);

    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Player player = (Player)e.getPlayer();
        Inventory inventory = e.getInventory();

        if (Gui.getGuis().stream().noneMatch(gui -> gui.getInventory().equals(inventory))) return;
        Gui gui = Gui.getGuis().stream().filter(g -> g.getInventory().equals(inventory)).findAny().get();

        CloseAction closeAction = gui.getCloseAction();
        if (closeAction == null) return;

        closeAction.close(player, e);
    }

}
