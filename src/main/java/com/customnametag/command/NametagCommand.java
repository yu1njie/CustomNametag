package com.customnametag.command;

import com.customnametag.CustomNametagPlugin;
import com.customnametag.manager.PlayerDataManager;
import com.customnametag.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class NametagCommand extends Command {

    private final CustomNametagPlugin plugin;

    public NametagCommand(CustomNametagPlugin plugin) {
        super("nametag", "CustomNametag main command", "/nametag [visibility|background|textshadow|reload]", Arrays.asList("cnt"));
        setPermission("customnametag.use");
        this.plugin = plugin;
    }

    private void sendMessage(CommandSender sender, String messageKey, String... replacements) {
        String msg = plugin.getConfig().getString("messages." + messageKey, "");
        if (msg.isEmpty()) return;

        String prefix = plugin.getConfig().getString("messages.prefix", "");
        msg = prefix + msg;

        for (int i = 0; i < replacements.length - 1; i += 2) {
            msg = msg.replace(replacements[i], replacements[i + 1]);
        }

        sender.sendMessage(ColorUtil.toComponent(msg));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        if (sub.equals("reload")) {
            if (!sender.hasPermission("customnametag.admin")) {
                sendMessage(sender, "no-permission");
                return true;
            }
            plugin.reloadConfig();
            plugin.getNametagManager().startUpdateTask();
            for (Player player : Bukkit.getOnlinePlayers()) {
                plugin.getNametagManager().setupPlayer(player);
            }
            sendMessage(sender, "reload-success");
            return true;
        }

        if (!(sender instanceof Player)) {
            sendMessage(sender, "only-players");
            return true;
        }

        Player player = (Player) sender;
        PlayerDataManager.PlayerSettings settings = plugin.getPlayerDataManager().getSettings(player.getUniqueId());

        if (sub.equals("visibility") || sub.equals("toggle") || sub.equals("vis")) {
            boolean current = settings.isVisibility();
            boolean newState = !current;
            plugin.getNametagManager().setPlayerVisibility(player, newState);

            if (newState) {
                sendMessage(player, "visibility-enabled");
            } else {
                sendMessage(player, "visibility-disabled");
            }
            return true;
        }

        if (sub.equals("background") || sub.equals("bg")) {
            if (args.length < 2) {
                sendMessage(player, "background-invalid");
                return true;
            }

            String colorInput = args[1];
            String lower = colorInput.toLowerCase(Locale.ROOT);

            if (lower.equals("transparent") || lower.equals("none")) {
                settings.setBackgroundColor("transparent");
                plugin.getPlayerDataManager().saveSettings(player.getUniqueId());
                plugin.getNametagManager().updateStyle(player);
                sendMessage(player, "background-transparent");
                return true;
            }

            Color parsed = ColorUtil.parseColor(colorInput);
            if (parsed == null && !lower.equals("default")) {
                sendMessage(player, "background-invalid");
                return true;
            }

            settings.setBackgroundColor(colorInput);
            plugin.getPlayerDataManager().saveSettings(player.getUniqueId());
            plugin.getNametagManager().updateStyle(player);
            sendMessage(player, "background-set", "{color}", colorInput);
            return true;
        }

        if (sub.equals("textshadow") || sub.equals("shadow")) {
            boolean current = settings.isTextShadow();
            boolean newState = !current;
            settings.setTextShadow(newState);
            plugin.getPlayerDataManager().saveSettings(player.getUniqueId());
            plugin.getNametagManager().updateStyle(player);

            if (newState) {
                sendMessage(player, "shadow-enabled");
            } else {
                sendMessage(player, "shadow-disabled");
            }
            return true;
        }

        sendHelp(sender);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        List<String> lines = plugin.getConfig().getStringList("messages.help");
        for (String line : lines) {
            sender.sendMessage(ColorUtil.toComponent(line));
        }
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subs = new ArrayList<>(Arrays.asList("visibility", "background", "textshadow"));
            if (sender.hasPermission("customnametag.admin")) {
                subs.add("reload");
            }
            return StringUtil.copyPartialMatches(args[0], subs, completions);
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("background") || args[0].equalsIgnoreCase("bg"))) {
            List<String> colors = Arrays.asList(
                    "transparent", "none", "default", "#000000", "#FFFFFF", "#FF0000", "#00FF00",
                    "#0000FF", "#FFFF00", "black", "white", "red", "blue", "green", "yellow", "gold", "gray"
            );
            return StringUtil.copyPartialMatches(args[1], colors, completions);
        }

        return Collections.emptyList();
    }
}
