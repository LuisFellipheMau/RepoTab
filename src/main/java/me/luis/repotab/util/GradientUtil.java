package me.luis.repotab.util;

import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GradientUtil {

    private static final Pattern GRADIENT_PATTERN = Pattern.compile(
            "<gradient:(#[A-Fa-f0-9]{6}):(#[A-Fa-f0-9]{6})>(.*?)</gradient>",
            Pattern.CASE_INSENSITIVE
    );

    public static String apply(String input) {
        Matcher matcher = GRADIENT_PATTERN.matcher(input);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String start = matcher.group(1);
            String end = matcher.group(2);
            String content = matcher.group(3).replace("&", "§");
            String gradient = createGradient(content, start, end);
            matcher.appendReplacement(buffer, gradient);
        }

        return matcher.appendTail(buffer).toString();
    }

    private static String createGradient(String text, String startHex, String endHex) {
        java.awt.Color start = java.awt.Color.decode(startHex);
        java.awt.Color end = java.awt.Color.decode(endHex);
        StringBuilder result = new StringBuilder();
        List<ChatColor> activeStyles = new ArrayList<>();
        int length = text.length();

        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            if (c == ChatColor.COLOR_CHAR && i + 1 < length) {
                handleFormatCode(text.charAt(i + 1), activeStyles);
                result.append(ChatColor.COLOR_CHAR).append(text.charAt(i + 1));
                i++;
                continue;
            }

            float ratio = length > 1 ? (float) i / (length - 1) : 0f;
            ChatColor color = interpolateColor(start, end, ratio);
            appendStyledCharacter(result, color, activeStyles, c);
        }

        return result.toString();
    }

    private static void handleFormatCode(char code, List<ChatColor> activeStyles) {
        ChatColor color = ChatColor.getByChar(code);
        if (color == null) return;

        if (color == ChatColor.RESET) {
            activeStyles.clear();
        } else if (isFormattingCode(color)) {
            if (!activeStyles.contains(color)) {
                activeStyles.add(color);
            }
        } else {
            activeStyles.clear();
            activeStyles.add(color);
        }
    }

    private static boolean isFormattingCode(ChatColor color) {
        return color == ChatColor.BOLD ||
                color == ChatColor.ITALIC ||
                color == ChatColor.UNDERLINE ||
                color == ChatColor.STRIKETHROUGH ||
                color == ChatColor.MAGIC;
    }

    private static ChatColor interpolateColor(java.awt.Color start, java.awt.Color end, float ratio) {
        int red = (int) (start.getRed() + (end.getRed() - start.getRed()) * ratio);
        int green = (int) (start.getGreen() + (end.getGreen() - start.getGreen()) * ratio);
        int blue = (int) (start.getBlue() + (end.getBlue() - start.getBlue()) * ratio);
        return ChatColor.of(new java.awt.Color(red, green, blue));
    }

    private static void appendStyledCharacter(StringBuilder result, ChatColor color,
                                              List<ChatColor> activeStyles, char character) {
        result.append(color);
        activeStyles.stream()
                .filter(GradientUtil::isFormattingCode)
                .forEach(result::append);
        result.append(character);
    }
}