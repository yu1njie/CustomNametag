package com.customnametag.hook;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class LuckPermsHook {

    private boolean available = false;
    private LuckPerms luckPerms = null;

    public LuckPermsHook() {
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
            try {
                this.luckPerms = LuckPermsProvider.get();
                this.available = true;
            } catch (IllegalStateException | NoClassDefFoundError e) {
                this.available = false;
            }
        }
    }

    public boolean isAvailable() {
        return available && luckPerms != null;
    }

    public String getPrimaryGroup(Player player) {
        if (!isAvailable() || player == null) {
            return "default";
        }
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            return "default";
        }
        String group = user.getPrimaryGroup();
        return group != null ? group : "default";
    }

    public String getPrefix(Player player) {
        if (!isAvailable() || player == null) {
            return "";
        }
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            return "";
        }
        String prefix = user.getCachedData().getMetaData().getPrefix();
        return prefix != null ? prefix : "";
    }
}
