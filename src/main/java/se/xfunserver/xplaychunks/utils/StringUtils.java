package se.xfunserver.xplaychunks.utils;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StringUtils {

    public static String color(@NotNull String msg)  {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static BaseComponent toComponent(@Nullable CommandSender sender, String input) {
        return new TextComponent(
                TextComponent.fromLegacyText(color(input)));
    }

    public static void msg(CommandSender to, BaseComponent msg) {
        to.spigot().sendMessage(msg);
    }

    public static void msg(CommandSender to, String text) {
        msg(to, toComponent(to, text));
    }
}
