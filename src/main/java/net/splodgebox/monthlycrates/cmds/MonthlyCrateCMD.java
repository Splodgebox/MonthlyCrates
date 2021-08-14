package net.splodgebox.monthlycrates.cmds;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import net.splodgebox.monthlycrates.MonthlyCrates;
import net.splodgebox.monthlycrates.managers.CrateManager;
import net.splodgebox.monthlycrates.utils.Chat;
import net.splodgebox.monthlycrates.utils.ConfigurationUtils;
import net.splodgebox.monthlycrates.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.stream.IntStream;

@CommandAlias("mc|monthlycrate|monthlycrates")
public class MonthlyCrateCMD extends BaseCommand {

    @HelpCommand
    public void init(CommandSender commandSender) {
        Message.COMMAND_MESSAGE.msg(commandSender);
    }

    @Subcommand("give")
    @CommandPermission("monthlycrate.give")
    @CommandCompletion("@players @crates")
    public void giveCrate(CommandSender commandSender, OnlinePlayer player, String crate, @Default("1") Integer amount) {
        if (!MonthlyCrates.getInstance().crates.getConfiguration()
                .getConfigurationSection("Crates").getKeys(false).contains(crate)) {
            Message.INVALID_CRATE.msg(commandSender);
            return;
        }
        CrateManager crateManager = new CrateManager(crate, MonthlyCrates.getInstance());
        IntStream.range(0, amount).forEach(i -> player.getPlayer().getInventory().addItem(crateManager.getItemStack(player.getPlayer().getName())));
        String message = Message.GIVEN_CRATE.toString()
                .replace("%player%", player.getPlayer().getName())
                .replace("%amount%", String.valueOf(amount))
                .replace("%crate%", crate);
        Chat.msg(commandSender, message);
    }

    @Subcommand("giveall")
    @CommandPermission("monthlycrate.giveall")
    public void giveEveryoneCrate(CommandSender commandSender, String crate, boolean dropItem, String message) {
        if (!MonthlyCrates.getInstance().crates.getConfiguration()
                .getConfigurationSection("Crates").getKeys(false).contains(crate)) {
            Message.INVALID_CRATE.msg(commandSender);
            return;
        }
        CrateManager crateManager = new CrateManager(crate, MonthlyCrates.getInstance());
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (dropItem) {
                if (player.getInventory().firstEmpty() == -1)
                    player.getLocation().getWorld().dropItem(player.getLocation(),
                            crateManager.getItemStack(player.getName()));
                else player.getInventory().addItem(crateManager.getItemStack(player.getName()));
            } else {
                player.getInventory().addItem(crateManager.getItemStack(player.getName()));
            }
        }
        Bukkit.broadcastMessage(Chat.color(message));
    }

    @Subcommand("addreward")
    @CommandPermission("monthlycrate.addreward")
    public void addReward(CommandSender commandSender, String crate, boolean giveItem, double chance, String command) {
        if (commandSender instanceof ConsoleCommandSender) {
            Message.MUST_BE_PLAYER.msg(commandSender);
            return;
        }
        Player player = (Player) commandSender;
        ItemStack itemStack = player.getInventory().getItemInHand();
        if (itemStack.getType() == Material.AIR) {
            Message.CANNOT_ADD_AIR.msg(player);
            return;
        }
        new ConfigurationUtils(MonthlyCrates.getInstance()).addReward(itemStack, crate, giveItem, chance, command);
        Message.ADDED_REWARD.msg(player);
    }


    @Subcommand("reload")
    @CommandPermission("monthlycrate.reload")
    public void reloadConfiguration(CommandSender commandSender) {
        MonthlyCrates.getInstance().crates.reload();
        MonthlyCrates.getInstance().lang.reload();
        MonthlyCrates.getInstance().loadCrates();
        Message.CONFIGURATION_RELOAD.msg(commandSender);
    }


}
