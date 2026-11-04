package com.customnametag.manager;

import com.customnametag.CustomNametagPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class PlayerDataManager {

    private final CustomNametagPlugin plugin;
    private final File dataFile;
    private FileConfiguration dataConfig;

    private final Map<UUID, PlayerSettings> settingsMap = new ConcurrentHashMap<>();

    public static class PlayerSettings {
        private boolean visibility;
        private String backgroundColor;
        private boolean textShadow;

        public PlayerSettings(boolean visibility, String backgroundColor, boolean textShadow) {
            this.visibility = visibility;
            this.backgroundColor = backgroundColor;
            this.textShadow = textShadow;
        }

        public boolean isVisibility() {
            return visibility;
        }

        public void setVisibility(boolean visibility) {
            this.visibility = visibility;
        }

        public String getBackgroundColor() {
            return backgroundColor;
        }

        public void setBackgroundColor(String backgroundColor) {
            this.backgroundColor = backgroundColor;
        }

        public boolean isTextShadow() {
            return textShadow;
        }

        public void setTextShadow(boolean textShadow) {
            this.textShadow = textShadow;
        }
    }

    public PlayerDataManager(CustomNametagPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        load();
    }

    public void load() {
        if (!dataFile.exists()) {
            try {
                if (!plugin.getDataFolder().exists()) {
                    plugin.getDataFolder().mkdirs();
                }
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create playerdata.yml!", e);
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public PlayerSettings getSettings(UUID uuid) {
        return settingsMap.computeIfAbsent(uuid, this::loadSettingsFor);
    }

    private PlayerSettings loadSettingsFor(UUID uuid) {
        boolean defaultVis = plugin.getConfig().getBoolean("defaults.visibility", true);
        String defaultBg = plugin.getConfig().getString("defaults.background", "transparent");
        boolean defaultShadow = plugin.getConfig().getBoolean("defaults.text-shadow", false);

        String path = "players." + uuid.toString();
        if (dataConfig.contains(path)) {
            boolean vis = dataConfig.getBoolean(path + ".visibility", defaultVis);
            String bg = dataConfig.getString(path + ".background", defaultBg);
            boolean shadow = dataConfig.getBoolean(path + ".text-shadow", defaultShadow);
            return new PlayerSettings(vis, bg, shadow);
        }

        return new PlayerSettings(defaultVis, defaultBg, defaultShadow);
    }

    public void saveSettings(UUID uuid) {
        PlayerSettings settings = settingsMap.get(uuid);
        if (settings == null) return;

        String path = "players." + uuid.toString();
        dataConfig.set(path + ".visibility", settings.isVisibility());
        dataConfig.set(path + ".background", settings.getBackgroundColor());
        dataConfig.set(path + ".text-shadow", settings.isTextShadow());

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save player data for: " + uuid, e);
        }
    }

    public void saveAll() {
        for (UUID uuid : settingsMap.keySet()) {
            saveSettings(uuid);
        }
    }

    public void unloadPlayer(UUID uuid) {
        saveSettings(uuid);
        settingsMap.remove(uuid);
    }
}
