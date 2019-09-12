package net.splodgebox.monthlycrates;

import co.aikar.commands.PaperCommandManager;
import com.google.common.collect.Maps;
import lombok.Getter;
import net.splodgebox.monthlycrates.cmds.CosmicCrateCommand;
import net.splodgebox.monthlycrates.cmds.MonthlyCrateCMD;
import net.splodgebox.monthlycrates.crate.CrateManager;
import net.splodgebox.monthlycrates.crate.RewardManager;
import net.splodgebox.monthlycrates.crate.events.PlayerEvents;
import net.splodgebox.monthlycrates.utils.FileManager;
import net.splodgebox.monthlycrates.utils.Message;
import net.splodgebox.monthlycrates.utils.Metrics;
import net.splodgebox.monthlycrates.utils.gui.GuiListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;

public final class MonthlyCrates extends JavaPlugin {

    @Getter
    private static MonthlyCrates instance;
    public FileManager crates;
    public FileManager lang;

    @Getter
    private static Map<String, List<RewardManager>> rewardMap = Maps.newHashMap();

    @Override
    public void onEnable() {
        instance = this;
        crates = new FileManager(this, "crates", getDataFolder().getAbsolutePath());
        lang = new FileManager(this, "lang", getDataFolder().getAbsolutePath());
        loadMessages();
        saveConfig();
        getServer().getPluginManager().registerEvents(new GuiListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerEvents(), this);

        PaperCommandManager commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new MonthlyCrateCMD());
        commandManager.registerCommand(new CosmicCrateCommand());
        commandManager.getCommandCompletions().registerCompletion("crates", c -> crates.getConfiguration().getConfigurationSection("Crates").getKeys(false));
        Metrics metrics = new Metrics(this);

        new BukkitRunnable() {
            @Override
            public void run() {
                loadCrates();
            }
        }.runTaskLater(this, 20L);
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

    public void loadCrates() {
        crates.getConfiguration().getConfigurationSection("Crates").getKeys(false).forEach(s -> {
            new CrateManager(s, this).loadRewards();
        });
    }
}