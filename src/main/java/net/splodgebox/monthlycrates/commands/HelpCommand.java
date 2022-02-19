package net.splodgebox.monthlycrates.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import net.splodgebox.monthlycrates.utils.Chat;
import org.bukkit.command.CommandSender;

@CommandAlias("%alias")
public class HelpCommand extends BaseCommand {

    @Default
    public void sendHelp(CommandSender sender) {
        Chat.msg(sender,
                "&e&l----- &6&lMonthly&e&lCrates &6&lCommands &e&l-----",
                "",
                "&e/monthlycrate give &6<player> <crate> &e[amount]",
                "&e/monthlycrate addreward &6<crate> <give-item> <chance> <command>",
                "&e/monthlycrate list",
                "&e/monthlycrate reload",
                "",
                "&6<> &7- &fRequire Argument",
                "&e[] &7- &fOptional Argument",
                "",
                "&6&lAliases: &e<monthlycrates|mc|mcrates|mcrate>");
    }

}
