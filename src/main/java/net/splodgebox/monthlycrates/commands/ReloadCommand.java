package net.splodgebox.monthlycrates.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import lombok.RequiredArgsConstructor;
import net.splodgebox.monthlycrates.MonthlyCrates;
import net.splodgebox.monthlycrates.utils.Message;
import org.bukkit.command.CommandSender;

@CommandAlias("monthlycrates|mc|mcrates|mcrate|cc|crates|crate")
@RequiredArgsConstructor
public class ReloadCommand extends BaseCommand {

    private final MonthlyCrates plugin;

    @Subcommand("reload")
    @CommandPermission("monthlycrates.reload")
    public void reloadCrates(CommandSender sender) {
        plugin.getCrateController().reload();
        plugin.getLangFile().reload();
        Message.setFile(plugin.getLangFile().getConfiguration());
        Message.CONFIGURATION_RELOAD.msg(sender);
    }

}
