package net.splodgebox.monthlycrates.commands.crates;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import lombok.RequiredArgsConstructor;
import net.splodgebox.monthlycrates.MonthlyCrates;
import net.splodgebox.monthlycrates.data.Crate;
import net.splodgebox.monthlycrates.utils.Chat;
import net.splodgebox.monthlycrates.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.stream.IntStream;

@CommandAlias("%alias")
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

    @Subcommand("giveall")
    @CommandPermission("monthlycrate.giveall")
    public void giveEveryoneCrate(CommandSender commandSender, String crate, boolean dropItem, String message) {
        if (!plugin.getCrateController().getCrates().containsKey(crate)) {
            Message.INVALID_CRATE.msg(commandSender);
            return;
        }

        Crate customCrate = plugin.getCrateController().getCrates().get(crate);

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (dropItem) {
                if (player.getInventory().firstEmpty() == -1)
                    player.getLocation().getWorld().dropItem(player.getLocation(),
                            customCrate.create(player.getName()));
                else player.getInventory().addItem(customCrate.create(player.getName()));
            } else {
                player.getInventory().addItem(customCrate.create(player.getName()));
            }
        }

        Bukkit.broadcastMessage(Chat.color(message));
    }


}
