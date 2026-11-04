package com.customnametag.hook;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlaceholderHook {

    private boolean available = false;

    public PlaceholderHook() {
        this.available = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    public boolean isAvailable() {
        return available;
    }

    public String setPlaceholders(Player player, String text) {
        if (!available || text == null || player == null) {
            return text;
        }
        try {
            return PlaceholderAPI.setPlaceholders(player, text);
        } catch (Throwable t) {
            return text;
        }
    }
}
