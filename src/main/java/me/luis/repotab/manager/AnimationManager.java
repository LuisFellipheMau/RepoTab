package me.luis.repotab.manager;

import me.luis.repotab.RepoTab;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimationManager {

    private final Map<String, List<String>> animations = new HashMap<>();

    public void loadAnimations(FileConfiguration config) {
        animations.clear();
        ConfigurationSection section = config.getConfigurationSection("animations");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            animations.put(key, config.getStringList("animations." + key));
            RepoTab.getInstance().getLogger().info("Animação carregada: " + key);
        }
    }

    public String getAnimatedText(String animationName) {
        List<String> frames = animations.get(animationName);
        if (frames == null || frames.isEmpty()) return "";

        long index = (System.currentTimeMillis() / 500) % frames.size(); // Atualiza a cada 0.5s
        return frames.get((int) index);
    }
}