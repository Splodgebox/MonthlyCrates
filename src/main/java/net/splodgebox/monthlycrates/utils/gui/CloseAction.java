package net.splodgebox.monthlycrates.utils.gui;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

public interface CloseAction {

    void close(Player player, InventoryCloseEvent event);

}
