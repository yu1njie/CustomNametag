package com.customnametag.manager;

import com.customnametag.CustomNametagPlugin;
import com.customnametag.util.ColorUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import org.bukkit.util.Transformation;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NametagManager {

    private final CustomNametagPlugin plugin;
    private final Map<UUID, TextDisplay> displays = new ConcurrentHashMap<>();
    private static final String TEAM_NAME = "cnt_hidden";
    private Team hiddenTeam;
    private BukkitTask updateTask;

    public NametagManager(CustomNametagPlugin plugin) {
        this.plugin = plugin;
        setupScoreboardTeam();
        startUpdateTask();
    }

    private void setupScoreboardTeam() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        hiddenTeam = scoreboard.getTeam(TEAM_NAME);
        if (hiddenTeam == null) {
            hiddenTeam = scoreboard.registerNewTeam(TEAM_NAME);
        }
        hiddenTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
    }

    public void startUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
        }
        long interval = plugin.getConfig().getLong("update-interval-ticks", 20L);
        if (interval < 5L) interval = 5L;

        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player == null || !player.isOnline()) continue;
                updateNametag(player);
            }
        }, interval, interval);
    }

    public void setupPlayer(Player player) {
        if (player == null || !player.isOnline()) return;

        if (hiddenTeam != null && !hiddenTeam.hasEntry(player.getName())) {
            hiddenTeam.addEntry(player.getName());
        }

        removeDisplay(player.getUniqueId());

        player.getPassengers().forEach(passenger -> {
            if (passenger instanceof TextDisplay && passenger.getScoreboardTags().contains("custom_nametag")) {
                passenger.remove();
            }
        });

        PlayerDataManager.PlayerSettings settings = plugin.getPlayerDataManager().getSettings(player.getUniqueId());

        float yOffset = (float) plugin.getConfig().getDouble("nametag.y-offset", 0.35);
        String billboardStr = plugin.getConfig().getString("nametag.billboard", "CENTER");
        String alignStr = plugin.getConfig().getString("nametag.alignment", "CENTER");
        boolean seeThrough = plugin.getConfig().getBoolean("nametag.see-through", false);

        TextDisplay display = player.getWorld().spawn(player.getLocation(), TextDisplay.class, entity -> {
            entity.setPersistent(false);
            entity.addScoreboardTag("custom_nametag");

            try {
                entity.setBillboard(Display.Billboard.valueOf(billboardStr.toUpperCase()));
            } catch (Exception e) {
                entity.setBillboard(Display.Billboard.CENTER);
            }

            try {
                entity.setAlignment(TextDisplay.TextAlignment.valueOf(alignStr.toUpperCase()));
            } catch (Exception e) {
                entity.setAlignment(TextDisplay.TextAlignment.CENTER);
            }

            entity.setSeeThrough(seeThrough);

            entity.setTransformation(new Transformation(
                    new Vector3f(0f, yOffset, 0f),
                    new AxisAngle4f(),
                    new Vector3f(1f, 1f, 1f),
                    new AxisAngle4f()
            ));

            applyStyle(entity, settings);
        });

        player.addPassenger(display);
        displays.put(player.getUniqueId(), display);

        player.hideEntity(plugin, display);

        updateText(player, display);

        updateVisibilityForViewers(player, display);
        updateViewerVisibilityForPlayer(player);
    }

    public void updateNametag(Player player) {
        if (player == null || !player.isOnline()) return;
        TextDisplay display = displays.get(player.getUniqueId());
        if (display == null || !display.isValid() || display.isDead()) {
            setupPlayer(player);
            return;
        }

        if (!player.getPassengers().contains(display)) {
            player.addPassenger(display);
        }

        updateText(player, display);
    }

    public void applyStyle(TextDisplay display, PlayerDataManager.PlayerSettings settings) {
        if (display == null || settings == null) return;

        Color color = ColorUtil.parseColor(settings.getBackgroundColor());
        display.setBackgroundColor(color);
        display.setShadowed(settings.isTextShadow());
    }

    public void updateStyle(Player player) {
        TextDisplay display = displays.get(player.getUniqueId());
        if (display == null) return;
        PlayerDataManager.PlayerSettings settings = plugin.getPlayerDataManager().getSettings(player.getUniqueId());
        applyStyle(display, settings);
    }

    private void updateText(Player player, TextDisplay display) {
        if (player == null || display == null) return;

        String defaultGroupName = plugin.getConfig().getString("nametag.default-group-name", "default");
        String primaryGroup = plugin.getLuckPermsHook().getPrimaryGroup(player);
        boolean isDefault = primaryGroup.equalsIgnoreCase(defaultGroupName);

        String line1Raw;
        if (isDefault) {
            line1Raw = plugin.getConfig().getString("nametag.default-rank-format", "&7{player}");
        } else {
            line1Raw = plugin.getConfig().getString("nametag.custom-rank-format", "%luckperms_prefix% &7▪ &7{player}");
        }

        String prefix = plugin.getLuckPermsHook().getPrefix(player);
        line1Raw = line1Raw.replace("{player}", player.getName())
                .replace("{prefix}", prefix)
                .replace("%luckperms_prefix%", prefix);

        line1Raw = plugin.getPlaceholderHook().setPlaceholders(player, line1Raw);

        String line2Raw = plugin.getConfig().getString("nametag.ping-format", "&b{ping}ms");
        line2Raw = line2Raw.replace("{ping}", String.valueOf(player.getPing()))
                .replace("{player}", player.getName());
        line2Raw = plugin.getPlaceholderHook().setPlaceholders(player, line2Raw);

        String fullText = line1Raw + "\n" + line2Raw;
        Component component = ColorUtil.toComponent(fullText);
        display.text(component);
    }

    public void setPlayerVisibility(Player viewer, boolean visible) {
        PlayerDataManager.PlayerSettings settings = plugin.getPlayerDataManager().getSettings(viewer.getUniqueId());
        settings.setVisibility(visible);
        plugin.getPlayerDataManager().saveSettings(viewer.getUniqueId());

        for (Map.Entry<UUID, TextDisplay> entry : displays.entrySet()) {
            if (entry.getKey().equals(viewer.getUniqueId())) continue;
            TextDisplay display = entry.getValue();
            if (display == null || !display.isValid()) continue;

            if (visible) {
                viewer.showEntity(plugin, display);
            } else {
                viewer.hideEntity(plugin, display);
            }
        }
    }

    private void updateVisibilityForViewers(Player target, TextDisplay targetDisplay) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.equals(target)) continue;
            PlayerDataManager.PlayerSettings viewerSettings = plugin.getPlayerDataManager().getSettings(viewer.getUniqueId());
            if (!viewerSettings.isVisibility()) {
                viewer.hideEntity(plugin, targetDisplay);
            } else {
                viewer.showEntity(plugin, targetDisplay);
            }
        }
    }

    private void updateViewerVisibilityForPlayer(Player viewer) {
        PlayerDataManager.PlayerSettings viewerSettings = plugin.getPlayerDataManager().getSettings(viewer.getUniqueId());
        boolean viewerCanSee = viewerSettings.isVisibility();

        for (Map.Entry<UUID, TextDisplay> entry : displays.entrySet()) {
            if (entry.getKey().equals(viewer.getUniqueId())) continue;
            TextDisplay display = entry.getValue();
            if (display == null || !display.isValid()) continue;

            if (viewerCanSee) {
                viewer.showEntity(plugin, display);
            } else {
                viewer.hideEntity(plugin, display);
            }
        }
    }

    public void removeDisplay(UUID uuid) {
        TextDisplay display = displays.remove(uuid);
        if (display != null && display.isValid()) {
            display.remove();
        }
    }

    public void cleanup() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }

        for (TextDisplay display : displays.values()) {
            if (display != null && display.isValid()) {
                display.remove();
            }
        }
        displays.clear();

        if (hiddenTeam != null) {
            try {
                hiddenTeam.unregister();
            } catch (Exception ignored) {}
            hiddenTeam = null;
        }
    }

    public TextDisplay getDisplay(UUID uuid) {
        return displays.get(uuid);
    }
}
