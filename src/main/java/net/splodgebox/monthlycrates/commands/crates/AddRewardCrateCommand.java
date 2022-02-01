package net.splodgebox.monthlycrates.commands.crates;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import net.splodgebox.monthlycrates.MonthlyCrates;
import net.splodgebox.monthlycrates.utils.ItemUtils;
import net.splodgebox.monthlycrates.utils.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("monthlycrates|mc|mcrates|mcrate|cc|crates|crate")
public class AddRewardCrateCommand extends BaseCommand {

    @Subcommand("addreward")
    @CommandPermission("monthlycrate.addreward")
    public void addReward(CommandSender commandSender, String crate, boolean giveItem, double chance, String command) {
        if (commandSender instanceof ConsoleCommandSender) {
            Message.MUST_BE_PLAYER.msg(commandSender);
            return;
        }
        Player player = (Player) commandSender;
        ItemStack itemStack = player.getInventory().getItemInHand();
        if (!ItemUtils.isValid(itemStack)) {
            Message.CANNOT_ADD_AIR.msg(player);
            return;
        }
        MonthlyCrates.getInstance().getCrateController().addReward(itemStack, crate, giveItem, chance, command);
        Message.ADDED_REWARD.msg(player);
    }

}
