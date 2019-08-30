package net.splodgebox.monthlycrates.cmds;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import net.splodgebox.monthlycrates.MonthlyCrates;
import net.splodgebox.monthlycrates.crate.CrateManager;
import net.splodgebox.monthlycrates.utils.gui.Gui;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicInteger;

@CommandAlias("cc|cosmiccrate")
public class CosmicCrateCommand extends BaseCommand {
    @Default
    public void openGUI(CommandSender commandSender) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            if (MonthlyCrates.getInstance().getConfig().getBoolean("Display.enabled")) {
                Gui inventory = new Gui("All current monthly crates", 4);
                for (String string : MonthlyCrates.getInstance().crates.getConfiguration().getConfigurationSection("Crates").getKeys(false)) {
                    inventory.addItem(new CrateManager(string, MonthlyCrates.getInstance()).getItemStack(
                            MonthlyCrates.getInstance().crates.getConfiguration().getString("Preview.placeholder")), (clicker, event) -> {
                    });
                }
            }
        }
    }
}
