package net.splodgebox.monthlycrates.commands.crates;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import lombok.RequiredArgsConstructor;
import net.splodgebox.monthlycrates.MonthlyCrates;
import net.splodgebox.monthlycrates.utils.gui.Gui;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("%alias")
@RequiredArgsConstructor
public class ListCrateCommand extends BaseCommand {

    private final MonthlyCrates plugin;

    @Subcommand("list")
    @CommandPermission("monthlycrates.list")
    public void listCrates(CommandSender sender) {
        if (!(sender instanceof Player)) return;

        Player player = (Player) sender;
        int size = (plugin.getCrateController().getCrates().size() / 9) + 1;
        if (!MonthlyCrates.getInstance().getConfig().getBoolean("Display.enabled")) return;

        Gui inventory = new Gui("All current monthly crates", size);
        plugin.getCrateController().getCrates().values().forEach(value ->
                inventory.addItem(value.create(player.getName()), (clicker, event) -> {}));

        inventory.open(player);
    }

}
