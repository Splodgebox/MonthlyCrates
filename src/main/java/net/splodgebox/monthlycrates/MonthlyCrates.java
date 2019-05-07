package net.splodgebox.monthlycrates;

import co.aikar.commands.PaperCommandManager;
import lombok.Getter;
import net.splodgebox.monthlycrates.cmds.MonthlyCrateCMD;
import net.splodgebox.monthlycrates.crate.events.PlayerEvents;
import net.splodgebox.monthlycrates.utils.FileManager;
import net.splodgebox.monthlycrates.utils.Message;
import net.splodgebox.monthlycrates.utils.gui.GuiListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class MonthlyCrates extends JavaPlugin {

    @Getter
    private static MonthlyCrates instance;
    public FileManager crates;
    public FileManager lang;

    @Override
    public void onEnable() {
        instance = this;
        crates = new FileManager(this, "crates", getDataFolder().getAbsolutePath());
        lang = new FileManager(this, "lang", getDataFolder().getAbsolutePath());
        loadMessages();
        getServer().getPluginManager().registerEvents(new GuiListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerEvents(), this);

        PaperCommandManager commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new MonthlyCrateCMD());
        commandManager.getCommandCompletions().registerCompletion("crates", c -> crates.getConfiguration().getConfigurationSection("Crates").getKeys(false));
    }

    @Override
    public void onDisable() {
        instance = null;

    }

    private void loadMessages() {
        for (Message message : Message.values()) {
            if (!this.lang.getConfiguration().contains(message.getPath())) {
                this.lang.getConfiguration().set(message.getPath(), message.getDefault());
            }
        }

        lang.save();
        Message.setFile(this.lang.getConfiguration());
    }
}
