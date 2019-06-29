package net.splodgebox.monthlycrates.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;

public enum Message {
    COMMAND_MESSAGE(
            "&e&l----- &6&lMonthly&e&lCrates &6&lCommands &e&l-----" + "\n" +
                    "" + "\n" +
                    "&e/monthlycrate give &6<player> <crate> &e[amount]" + "\n" +
                    "&e/monthlycrate addreward &6<crate> <give-item> <chance> <command>" + "\n" +
                    "&e/monthlycrate reload" + "\n" +
                    "" + "\n" +
                    "&6<> &7- &fRequire Argument" + "\n" +
                    "&e[] &7- &fOptional Argument" + "\n" +
                    "" + "\n" +
                    "&6&lAliases: &e<mc, cc, monthlycrate, monthlycrates>" + "\n"),
    CONFIGURATION_RELOAD("&6&l(!) &eConfiguration Files have been reloaded!"),
    INVALID_CRATE("&4&l(!) &cThat is an invalid crate type!"),
    GIVEN_CRATE("&6&l(!) &eYou have given &6%player% &ex%amount% of &6%crate%&e crates!"),
    MUST_BE_PLAYER("&c&l(!) &cYou must be a player to execute this command!"),
    CANNOT_ADD_AIR("&c&l(!) &cYou cannot add air to a crate!"),
    ADDED_REWARD("&a&l(!) &aReward added!"),
    NOT_ENOUGH_REWARDS("&C&L(!) &CThis crate does not contain enough rewards! [Min: 9]");

    private String path;
    private String msg;
    private static FileConfiguration LANG;
    public static SimpleDateFormat sdf;

    Message(String path, String start) {
        this.path = path;
        this.msg = start;
    }

    Message(String string) {
        this.path = this.name().replace("_", ".");
        this.msg = string;
    }

    public static void setFile(FileConfiguration configuration) {
        LANG = configuration;
    }

    @Override
    public String toString() {
        return ChatColor.translateAlternateColorCodes('&', LANG.getString(this.path, msg));
    }

    public String getDefault() {
        return this.msg;
    }

    public String getPath() {
        return this.path;
    }

    public void msg(CommandSender p, Object... args) {
        String s = toString();

        if (s.contains("\n")) {
            String[] split = s.split("\n");

            for (String inner : split) {
                sendMessage(p, inner, args);
            }

        } else {
            sendMessage(p, s, args);
        }
    }

    public void broadcast(Object... args) {
        String s = toString();

        if (s.contains("\n")) {
            String[] split = s.split("\n");

            for (String inner : split) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    sendMessage(player, inner, args);
                }
            }
        } else {
            for (Player player : Bukkit.getOnlinePlayers()) {
                sendMessage(player, s, args);
            }
        }
    }

    private String getFinalized(String string, Object... order) {
        int current = 0;

        for (Object object : order) {
            String placeholder = "{" + current + "}";

            if (string.contains(placeholder)) {
                if (object instanceof CommandSender) {
                    string = string.replace(placeholder, ((CommandSender) object).getName());
                } else if (object instanceof OfflinePlayer) {
                    string = string.replace(placeholder, ((OfflinePlayer) object).getName());
                } else if (object instanceof Location) {
                    Location location = (Location) object;
                    String repl = location.getWorld().getName() + ", " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();

                    string = string.replace(placeholder, repl);
                } else if (object instanceof Double) {
                    string = string.replace(placeholder, "" + object);
                } else if (object instanceof Integer) {
                    string = string.replace(placeholder, "" + object);
                }
            }

            current++;
        }

        return string;
    }

    private void sendMessage(CommandSender target, String string, Object... order) {
        string = getFinalized(string, order);

        target.sendMessage(string);
    }

}
