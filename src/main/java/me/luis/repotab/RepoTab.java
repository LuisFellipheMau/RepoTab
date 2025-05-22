package me.luis.repotab;

import me.luis.repotab.db.DatabaseManager;
import me.luis.repotab.manager.AnimationManager;
import me.luis.repotab.manager.TabManager;
import me.luis.repotab.suffix.CustomSuffixManager;
import me.luis.repotab.command.TabSuffixCommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class RepoTab extends JavaPlugin {

    public int getUpdateInterval() {
        return getConfig().getInt("settings.update-ticks", 20);
    }

    private static RepoTab instance;
    private TabManager tabManager;
    private AnimationManager animationManager;
    private CustomSuffixManager customSuffixManager;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Inicializa o banco de dados usando as configs do config.yml
        String host = getConfig().getString("database.host", "localhost");
        int port = getConfig().getInt("database.port", 3306);
        String database = getConfig().getString("database.name", "repotab");
        String user = getConfig().getString("database.user", "root");
        String password = getConfig().getString("database.password", "senha");
        databaseManager = new DatabaseManager(host, port, database, user, password);

        // Inicializa gerenciadores
        customSuffixManager = new CustomSuffixManager(databaseManager);
        animationManager = new AnimationManager();
        animationManager.loadAnimations(getConfig());
        tabManager = new TabManager(animationManager, customSuffixManager);
        tabManager.start();

        // Comando principal de reload
        getCommand("repotab").setExecutor((sender, command, label, args) -> {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("repotab.admin")) {
                    reloadConfig();
                    animationManager.loadAnimations(getConfig());
                    tabManager.reload();
                    sender.sendMessage("§aConfiguração recarregada com sucesso!");
                } else {
                    sender.sendMessage("§cVocê não tem permissão!");
                }
                return true;
            }
            return false;
        });

        // Comando de sufixo personalizado
        getCommand("tabsuffix").setExecutor(new TabSuffixCommand(customSuffixManager));

        getLogger().info("Plugin ativado!");
    }

    @Override
    public void onDisable() {
        if (tabManager != null) {
            tabManager.stop();
        }
        getLogger().info("Plugin desativado");
    }

    public static RepoTab getInstance() {
        return instance;
    }

    public CustomSuffixManager getCustomSuffixManager() {
        return customSuffixManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}
