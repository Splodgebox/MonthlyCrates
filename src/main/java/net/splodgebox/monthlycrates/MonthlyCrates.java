package net.splodgebox.monthlycrates;

import co.aikar.commands.PaperCommandManager;
import lombok.Getter;
import net.splodgebox.monthlycrates.commands.HelpCommand;
import net.splodgebox.monthlycrates.commands.ReloadCommand;
import net.splodgebox.monthlycrates.commands.crates.AddRewardCrateCommand;
import net.splodgebox.monthlycrates.commands.crates.GiveCrateCommand;
import net.splodgebox.monthlycrates.commands.crates.ListCrateCommand;
import net.splodgebox.monthlycrates.controllers.CrateController;
import net.splodgebox.monthlycrates.listeners.CrateListeners;
import net.splodgebox.monthlycrates.utils.FileManager;
import net.splodgebox.monthlycrates.utils.Message;
import net.splodgebox.monthlycrates.utils.Metrics;
import net.splodgebox.monthlycrates.utils.gui.GuiListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class MonthlyCrates extends JavaPlugin {

    @Getter
    private static MonthlyCrates instance;

    @Getter private FileManager langFile;
    @Getter private CrateController crateController;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        crateController = new CrateController(this);
        loadMessages();

        registerListeners();
        registerCommands();

        new Metrics(this);
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    public void registerCommands() {
        PaperCommandManager commandManager = new PaperCommandManager(this);

        commandManager.registerCommand(new HelpCommand());
        commandManager.registerCommand(new ReloadCommand(this));
        commandManager.registerCommand(new GiveCrateCommand(this));
        commandManager.registerCommand(new AddRewardCrateCommand());
        commandManager.registerCommand(new ListCrateCommand(this));

        commandManager.getCommandCompletions().registerStaticCompletion("crates",
                crateController.getCrates().keySet());
    }

    public void registerListeners() {
        getServer().getPluginManager().registerEvents(new CrateListeners(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(), this);
    }

    private void loadMessages() {
        langFile = new FileManager(this, "lang", getDataFolder().getAbsolutePath());
        for (Message message : Message.values()) {
            if (!this.langFile.getConfiguration().contains(message.getPath())) {
                this.langFile.getConfiguration().set(message.getPath(), message.getDefault());
            }
        }

        langFile.save();
        Message.setFile(this.langFile.getConfiguration());
    }
}
