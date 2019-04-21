package net.splodgebox.monthlycrates.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

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
        Bukkit.getOnlinePlayers().stream().forEach((o) -> {
            Arrays.stream(messages).forEach((s) -> {
                o.sendMessage(color(s));
            });
        });
    }

    public static void log(String message) {
        Bukkit.getConsoleSender().sendMessage(color(message));
    }

    public static String uppercaseFirst(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1, str.length()).toLowerCase();
    }

    public static int getRandomNumber(int min, int max) {
        return (new Random()).nextInt(max - min + 1);
    }

    public static String formatMs(long ms) {
        long seconds = ms / 1000L % 60L;
        long minutes = ms / 60000L % 60L;
        long hours = ms / 3600000L % 24L;
        return (hours > 0L ? hours + "h " : "") + (minutes > 0L ? minutes + "m " : "") + seconds + "s";
    }

    public static String timeLeft(long timeoutSeconds) {
        long days = timeoutSeconds / 86400L;
        long hours = timeoutSeconds / 3600L % 24L;
        long minutes = timeoutSeconds / 60L % 60L;
        long seconds = timeoutSeconds % 60L;
        return (days > 0L ? " " + days + "d" + (days != 1L ? "" : "") : "") + (hours > 0L ? " " + hours + "h" + (hours != 1L ? "" : "") : "") + (minutes > 0L ? " " + minutes + "m" + (minutes != 1L ? "" : "") : "") + (seconds > 0L ? " " + seconds + "s" + (seconds != 1L ? "" : "") : "");
    }

    public static String formatDoubleValue(double value) {
        NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        return nf.format(value);
    }

    public static boolean isServerOnline(String address, int port) {
        try {
            Socket s = new Socket();
            s.connect(new InetSocketAddress(address, port), 10);
            s.close();
            return true;
        } catch (Exception var3) {
            return false;
        }
    }

    public static int getPlayers(String address, int port) {
        int players = 0;
        if (!isServerOnline(address, port)) {
            return players;
        } else {
            try {
                Socket s = new Socket();
                s.connect(new InetSocketAddress(address, port), 10);
                DataOutputStream out = new DataOutputStream(s.getOutputStream());
                DataInputStream in = new DataInputStream(s.getInputStream());
                out.write(254);
                StringBuilder str = new StringBuilder();

                int b;
                while ((b = in.read()) != -1) {
                    if (b != 0 && b > 16 && b != 255 && b != 23 && b != 24) {
                        str.append((char) b);
                    }
                }

                String[] data = str.toString().split("§");
                players = Integer.valueOf(data[1]);
                s.close();
            } catch (Exception var9) {
                var9.printStackTrace();
            }

            return players;
        }
    }

    public static int getMaxPlayers(String address, int port) {
        int players = 0;
        if (!isServerOnline(address, port)) {
            return players;
        } else {
            try {
                Socket s = new Socket();
                s.connect(new InetSocketAddress(address, port), 10);
                DataOutputStream out = new DataOutputStream(s.getOutputStream());
                DataInputStream in = new DataInputStream(s.getInputStream());
                out.write(254);
                StringBuilder str = new StringBuilder();

                int b;
                while ((b = in.read()) != -1) {
                    if (b != 0 && b > 16 && b != 255 && b != 23 && b != 24) {
                        str.append((char) b);
                    }
                }

                String[] data = str.toString().split("§");
                players = Integer.valueOf(data[2]);
                s.close();
            } catch (Exception var9) {
                var9.printStackTrace();
            }

            return players;
        }
    }
}