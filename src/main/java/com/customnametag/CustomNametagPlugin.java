package com.customnametag;

import com.customnametag.command.NametagCommand;
import com.customnametag.hook.LuckPermsHook;
import com.customnametag.hook.PlaceholderHook;
import com.customnametag.listener.PlayerListener;
import com.customnametag.manager.NametagManager;
import com.customnametag.manager.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class CustomNametagPlugin extends JavaPlugin {

    private static CustomNametagPlugin instance;

    private LuckPermsHook luckPermsHook;
    private PlaceholderHook placeholderHook;
    private PlayerDataManager playerDataManager;
    private NametagManager nametagManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.luckPermsHook = new LuckPermsHook();
        if (luckPermsHook.isAvailable()) {
            getLogger().info("LuckPerms integration enabled successfully.");
        } else {
            getLogger().warning("LuckPerms not found or not ready, using default nametag format.");
        }

        this.placeholderHook = new PlaceholderHook();
        if (placeholderHook.isAvailable()) {
            getLogger().info("PlaceholderAPI integration enabled.");
        }

        this.playerDataManager = new PlayerDataManager(this);
        this.nametagManager = new NametagManager(this);

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        Bukkit.getCommandMap().register("customnametag", new NametagCommand(this));

        for (Player player : Bukkit.getOnlinePlayers()) {
            nametagManager.setupPlayer(player);
        }

        getLogger().info("CustomNametag v" + getPluginMeta().getVersion() + " by u5dt enabled successfully.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling CustomNametag, cleaning up nametags...");

        if (nametagManager != null) {
            nametagManager.cleanup();
        }

        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }

        getLogger().info("CustomNametag disabled successfully.");
    }

    public static CustomNametagPlugin getInstance() {
        return instance;
    }

    public LuckPermsHook getLuckPermsHook() {
        return luckPermsHook;
    }

    public PlaceholderHook getPlaceholderHook() {
        return placeholderHook;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public NametagManager getNametagManager() {
        return nametagManager;
    }
}
