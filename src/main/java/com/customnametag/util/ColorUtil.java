package com.customnametag.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Color;

import java.util.Locale;

public final class ColorUtil {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private ColorUtil() {}

    public static Component toComponent(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        return LEGACY_SERIALIZER.deserialize(text);
    }

    public static String colorize(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static Color parseColor(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        String val = input.trim().toLowerCase(Locale.ROOT);

        if (val.equals("transparent") || val.equals("none")) {
            return Color.fromARGB(0, 0, 0, 0);
        }

        if (val.equals("default")) {
            return null;
        }

        if (val.startsWith("#")) {
            try {
                String hex = val.substring(1);
                if (hex.length() == 6) {
                    int r = Integer.parseInt(hex.substring(0, 2), 16);
                    int g = Integer.parseInt(hex.substring(2, 4), 16);
                    int b = Integer.parseInt(hex.substring(4, 6), 16);
                    return Color.fromARGB(255, r, g, b);
                } else if (hex.length() == 8) {
                    int a = Integer.parseInt(hex.substring(0, 2), 16);
                    int r = Integer.parseInt(hex.substring(2, 4), 16);
                    int g = Integer.parseInt(hex.substring(4, 6), 16);
                    int b = Integer.parseInt(hex.substring(6, 8), 16);
                    return Color.fromARGB(a, r, g, b);
                }
            } catch (NumberFormatException ignored) {}
        }

        switch (val) {
            case "black":
                return Color.fromRGB(0, 0, 0);
            case "white":
                return Color.fromRGB(255, 255, 255);
            case "red":
                return Color.fromRGB(255, 0, 0);
            case "dark_red":
                return Color.fromRGB(170, 0, 0);
            case "green":
                return Color.fromRGB(85, 255, 85);
            case "dark_green":
                return Color.fromRGB(0, 170, 0);
            case "blue":
                return Color.fromRGB(85, 85, 255);
            case "dark_blue":
                return Color.fromRGB(0, 0, 170);
            case "yellow":
                return Color.fromRGB(255, 255, 85);
            case "gold":
                return Color.fromRGB(255, 170, 0);
            case "aqua":
                return Color.fromRGB(85, 255, 255);
            case "dark_aqua":
                return Color.fromRGB(0, 170, 170);
            case "purple":
                return Color.fromRGB(170, 0, 170);
            case "pink":
                return Color.fromRGB(255, 85, 255);
            case "gray":
                return Color.fromRGB(170, 170, 170);
            case "dark_gray":
                return Color.fromRGB(85, 85, 85);
            case "orange":
                return Color.fromRGB(255, 128, 0);
            default:
                return null;
        }
    }
}
