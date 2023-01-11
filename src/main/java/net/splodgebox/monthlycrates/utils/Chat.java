package net.splodgebox.monthlycrates.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class Chat {

    public static String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static void msg(Player player, String... messages) {
        Arrays.stream(messages).forEach((s) -> {
            player.sendMessage(color(s));
        });
    }

    public static void msg(CommandSender sender, String... messages) {
        Arrays.stream(messages).forEach((s) -> {
            sender.sendMessage(color(s));
        });
    }

    public static void msgAll(String... messages) {
        Bukkit.getOnlinePlayers().forEach((o) -> {
            Arrays.stream(messages).forEach((s) -> {
                o.sendMessage(color(s));
            });
        });
    }

    public static void log(String message) {
        Bukkit.getConsoleSender().sendMessage(color(message));
    }
}