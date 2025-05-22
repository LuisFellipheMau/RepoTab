package me.luis.repotab.hook;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlaceholderHook {
    public static String setInternalPlaceholders(Player player, String text) {
        return text.replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("%max_players%", String.valueOf(Bukkit.getMaxPlayers()))
                .replace("%ping%", String.valueOf(player.getPing()));
    }
}