package me.luis.repotab.suffix;

import me.luis.repotab.db.DatabaseManager;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;

public class CustomSuffixManager {
    private final DatabaseManager db;
    // Cache simples para performance
    private final Map<UUID, List<String>> cache = new HashMap<>();

    public CustomSuffixManager(DatabaseManager db) {
        this.db = db;
        createTable();
    }

    private void createTable() {
        try (Connection conn = db.getConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS player_suffixes (" +
                    "uuid VARCHAR(36) NOT NULL," +
                    "suffix VARCHAR(32) NOT NULL," +
                    "PRIMARY KEY(uuid, suffix))");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addSuffix(Player player, String suffix) {
        UUID uuid = player.getUniqueId();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT IGNORE INTO player_suffixes (uuid, suffix) VALUES (?, ?)")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, suffix);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        cache.remove(uuid); // Força reload
    }

    public void removeSuffix(Player player, String suffix) {
        UUID uuid = player.getUniqueId();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM player_suffixes WHERE uuid = ? AND suffix = ?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, suffix);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        cache.remove(uuid); // Força reload
    }

    public List<String> getSuffixes(Player player) {
        UUID uuid = player.getUniqueId();
        if (cache.containsKey(uuid)) return cache.get(uuid);

        List<String> suffixes = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT suffix FROM player_suffixes WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                suffixes.add(rs.getString("suffix"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        cache.put(uuid, suffixes);
        return suffixes;
    }

    public String getFormattedSuffixes(Player player) {
        List<String> list = getSuffixes(player);
        if (list.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (String suf : list) {
            sb.append(" [").append(suf).append("]");
        }
        return sb.toString();
    }
}
