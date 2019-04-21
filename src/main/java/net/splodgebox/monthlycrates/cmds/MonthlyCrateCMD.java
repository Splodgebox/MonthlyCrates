package net.splodgebox.monthlycrates.cmds;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import net.splodgebox.monthlycrates.MonthlyCrates;
import net.splodgebox.monthlycrates.crate.AnimationManager;
import net.splodgebox.monthlycrates.crate.CrateManager;
import org.bukkit.command.CommandSender;

import java.util.stream.IntStream;

@CommandAlias("mc|monthlycrate|monthlycrates")
public class MonthlyCrateCMD extends BaseCommand {

    @Subcommand("give")
    @CommandPermission("monthlycrate.give")
    @CommandCompletion("@players @crates")
    public void giveCrate(CommandSender commandSender, OnlinePlayer player, String crate, @Default("1") Integer amount){
        if (!MonthlyCrates.getInstance().crates.getConfiguration()
                .getConfigurationSection("Crates").getKeys(false).contains(crate)){
            // ADD MESSAGE
            return;
        }
        CrateManager crateManager = new CrateManager(crate, player.getPlayer());
        IntStream.range(0, amount).forEach(i -> player.getPlayer().getInventory().addItem(crateManager.getItemStack()));
        new AnimationManager(player.getPlayer(), "example", MonthlyCrates.getInstance()).init();
    }


}
