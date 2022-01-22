package net.splodgebox.monthlycrates.commands.crates;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import lombok.RequiredArgsConstructor;
import net.splodgebox.monthlycrates.MonthlyCrates;
import net.splodgebox.monthlycrates.data.Crate;
import net.splodgebox.monthlycrates.utils.Message;
import org.bukkit.command.CommandSender;

import java.util.stream.IntStream;

@CommandAlias("monthlycrates|mc|mcrates|mcrate|cc|crates|crate")
@RequiredArgsConstructor
public class GiveCrateCommand extends BaseCommand {

    private final MonthlyCrates plugin;

    @Subcommand("give")
    @CommandPermission("monthlycrates.give")
    @CommandCompletion("@players @crates")
    public void giveCrate(CommandSender sender, OnlinePlayer onlinePlayer, String crate, @Default("1") int amount) {
        if (!plugin.getCrateController().getCrates().containsKey(crate)) {
            Message.INVALID_CRATE.msg(sender);
            return;
        }

        Crate customCrate = plugin.getCrateController().getCrates().get(crate);
        IntStream.range(0, amount).forEach(i -> onlinePlayer.getPlayer().getInventory()
                .addItem(customCrate.create(onlinePlayer.getPlayer().getName())));

        Message.GIVEN_CRATE.msg(sender,
                "%player%", onlinePlayer.getPlayer().getName(),
                "%amount%", String.valueOf(amount),
                "%crate%", crate
        );
    }

}
