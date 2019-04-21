package net.splodgebox.monthlycrates;

import co.aikar.commands.PaperCommandManager;
import lombok.Getter;
import net.splodgebox.monthlycrates.cmds.MonthlyCrateCMD;
import net.splodgebox.monthlycrates.crate.AnimationManager;
import net.splodgebox.monthlycrates.crate.CrateManager;
import net.splodgebox.monthlycrates.utils.FileManager;
import net.splodgebox.monthlycrates.utils.gui.GuiListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class MonthlyCrates extends JavaPlugin {

    @Getter
    private static MonthlyCrates instance;
    public FileManager crates;

    @Override
    public void onEnable() {
        instance = this;
        crates = new FileManager(this, "crates", getDataFolder().getAbsolutePath());
        getServer().getPluginManager().registerEvents(new GuiListener(), this);

        PaperCommandManager commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new MonthlyCrateCMD());
        commandManager.getCommandCompletions().registerCompletion("crates", c -> crates.getConfiguration().getConfigurationSection("Crates").getKeys(false));
    }

    @Override
    public void onDisable() {
        instance = null;

    }
}
