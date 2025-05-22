package me.luis.repotab.manager;

import me.luis.repotab.RepoTab;
import me.luis.repotab.suffix.CustomSuffixManager;
import me.luis.repotab.util.GradientUtil;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TabManager {

    private final AnimationManager animationManager;
    private final Map<Player, List<String[]>> cachedTabs = new HashMap<>();
    private final CustomSuffixManager customSuffixManager;
    private BukkitTask task;

    public TabManager(AnimationManager animationManager, CustomSuffixManager customSuffixManager) {
        this.animationManager = animationManager;
        this.customSuffixManager = customSuffixManager;
    }

    public void start() {
        stop(); // Garante que não há tasks duplicadas
        int interval = RepoTab.getInstance().getUpdateInterval();
        task = Bukkit.getScheduler().runTaskTimer(RepoTab.getInstance(), this::updateAllTabs, 0L, interval);
    }

    private void updateAllTabs() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Header/Footer (mantém seu código atual)
            List<String> headerLines = new ArrayList<>();
            List<String> footerLines = new ArrayList<>();
            for (String line : RepoTab.getInstance().getConfig().getStringList("header")) {
                headerLines.add(processDynamicLine(player, line));
            }
            for (String line : RepoTab.getInstance().getConfig().getStringList("footer")) {
                footerLines.add(processDynamicLine(player, line));
            }
            player.setPlayerListHeaderFooter(
                    String.join("\n", headerLines),
                    String.join("\n", footerLines)
            );

            // ----------- NOVO: Atualizar nome na TabList -----------
            String prefix = "";
            String suffix = "";
            try {
                LuckPerms lp = LuckPermsProvider.get();
                User user = lp.getUserManager().getUser(player.getUniqueId());
                if (user != null) {
                    CachedMetaData meta = user.getCachedData().getMetaData(QueryOptions.defaultContextualOptions());
                    prefix = meta.getPrefix() != null ? meta.getPrefix() : "";
                    suffix = meta.getSuffix() != null ? meta.getSuffix() : "";
                }
            } catch (Exception ignored) {}

            // Sufixos personalizados
            String customSuffix = customSuffixManager.getFormattedSuffixes(player);

            // Montar o nome final
            String tabName = ChatColor.translateAlternateColorCodes('&',
                    prefix + player.getName() + suffix + (customSuffix.isEmpty() ? "" : " " + customSuffix)
            );

            // Limite de tamanho (por segurança)
            if (tabName.length() > 32) tabName = tabName.substring(0, 32);

            player.setPlayerListName(tabName);
            // -------------------------------------------------------
        }
    }
    private String processDynamicLine(Player player, String line) {

        // Processamento completo a cada tick
        return ChatColor.translateAlternateColorCodes('&',
                GradientUtil.apply(
                        parseAnimations(
                                replaceDynamicPlaceholders(
                                        player,
                                        parsePlaceholders(player, line)
                                )
                        )
                )
        );
    }

    private List<String[]> processTab(Player player) {
        List<String[]> processed = new ArrayList<>();
        List<String> headerConfig = RepoTab.getInstance().getConfig().getStringList("header");
        List<String> footerConfig = RepoTab.getInstance().getConfig().getStringList("footer");

        for (int i = 0; i < Math.max(headerConfig.size(), footerConfig.size()); i++) {
            String headerLine = i < headerConfig.size() ? processLine(player, headerConfig.get(i)) : "";
            String footerLine = i < footerConfig.size() ? processLine(player, footerConfig.get(i)) : "";
            processed.add(new String[]{headerLine, footerLine});
        }

        return processed;
    }

    private String processLine(Player player, String line) {
        // Nova ordem corrigida
        line = ChatColor.translateAlternateColorCodes('&', line); // 1. Converter & para §
        line = parsePlaceholders(player, line);                   // 2. Placeholders
        line = parseAnimations(line);                             // 3. Animações
        line = GradientUtil.apply(line);                          // 4. Gradientes
        return line;
    }

    private String parsePlaceholders(Player player, String text) {
        // PlaceholderAPI
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            text = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
        }

        // Placeholders internos
        return text.replace("%player_ping%", String.valueOf(player.getPing()))
                .replace("%server_time%", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")))
                .replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("%max_players%", String.valueOf(Bukkit.getMaxPlayers()));
    }

    private String replaceDynamicPlaceholders(Player player, String text) {
        return text.replace("%player_ping%", String.valueOf(player.getPing()))
                .replace("%server_time%", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    private String parseAnimations(String text) {
        Matcher matcher = Pattern.compile("<animation:(\\w+)>").matcher(text);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String animationName = matcher.group(1);
            String replacement = animationManager.getAnimatedText(animationName);
            matcher.appendReplacement(buffer, replacement != null ? replacement : "");
        }
        return matcher.appendTail(buffer).toString();
    }

    public void stop() {
        if (task != null) task.cancel();
        cachedTabs.clear();
    }

    public void reload() {
        stop(); // Para a task atual
        start(); // Inicia uma nova task com o intervalo atualizado
    }
}