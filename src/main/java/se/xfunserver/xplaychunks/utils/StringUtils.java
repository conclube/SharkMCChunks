package se.xfunserver.xplaychunks.utils;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public class StringUtils {

    public static String color(@NotNull String msg)  {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
