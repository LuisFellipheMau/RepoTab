package me.luis.repotab.command;

import me.luis.repotab.suffix.CustomSuffixManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TabSuffixCommand implements CommandExecutor {

    private final CustomSuffixManager suffixManager;

    public TabSuffixCommand(CustomSuffixManager suffixManager) {
        this.suffixManager = suffixManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Apenas jogadores podem usar este comando.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("repotab.admin")) {
            player.sendMessage(ChatColor.RED + "Você não tem permissão para usar este comando.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.YELLOW + "Uso: /tabsuffix <add|remove> <sufixo>");
            return true;
        }

        String action = args[0].toLowerCase();
        String suffix = args[1];

        switch (action) {
            case "add":
                suffixManager.addSuffix(player, suffix);
                player.sendMessage(ChatColor.GREEN + "Sufixo '" + suffix + "' adicionado!");
                break;
            case "remove":
                suffixManager.removeSuffix(player, suffix);
                player.sendMessage(ChatColor.RED + "Sufixo '" + suffix + "' removido!");
                break;
            default:
                player.sendMessage(ChatColor.YELLOW + "Uso: /tabsuffix <add|remove> <sufixo>");
        }
        return true;
    }
}
