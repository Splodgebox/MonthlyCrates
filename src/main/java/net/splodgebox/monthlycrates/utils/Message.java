package net.splodgebox.monthlycrates.utils;

import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.Map;

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
    NOT_ENOUGH_REWARDS("&C&L(!) &CThis crate does not contain enough rewards! [Min: 9] OR You can enable duplicate rewards"),
    INVENTORY_FULL("&c&l(!) &cYour inventory was full so your item was dropped on the floor!");

    public static SimpleDateFormat sdf;
    private static FileConfiguration LANG;
    private String path;
    private String msg;

    Message(String path, String start) {
        this.path = path;
        this.msg = start;
    }

    Message(String string) {
        this.path = this.name().replace("__", ".");
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

    public void msg(CommandSender p, String... placeholders) {
        Map<String, String> holders = Maps.newHashMap();

        int i = 0;
        String value = "";
        for (String placeholder : placeholders) {
            if (i == 1) {
                holders.put(value, placeholder);
                i = 0;
                continue;
            }

            value = placeholder;
            i = 1;
        }

        msg(p, holders);
    }

    public void msg(CommandSender p, Map<String, String> placeholders) {
        String s = toString();

        if (s.contains("\n")) {
            String[] split = s.split("\n");

            for (String inner : split) {
                sendMessage(p, inner, placeholders);
            }

        } else {
            sendMessage(p, s, placeholders);
        }
    }

    public void broadcast(Object... args) {
        String s = toString();

        if (s.contains("\n")) {
            String[] split = s.split("\n");

            for (String inner : split) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    sendMessage(player, inner, null);
                }
            }
        } else {
            for (Player player : Bukkit.getOnlinePlayers()) {
                sendMessage(player, s, null);
            }
        }
    }

    private void sendMessage(CommandSender target, String string, Map<String, String> placeholders) {
        if (placeholders != null) {
            for (String s : placeholders.keySet()) {
                if (string.contains(s)) {
                    string = string.replace(s, placeholders.get(s));
                }
            }
        }

        target.sendMessage(Chat.color(string));
    }

    private String getItemStackName(ItemStack itemStack) {
        String name = itemStack.getType().toString().replace("_", " ");

        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()) {
            return itemStack.getItemMeta().getDisplayName();
        }

        return name;
    }

}