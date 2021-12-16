package dev._2lstudios.swiftboard.swift;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import dev._2lstudios.swiftboard.hooks.PlaceholderAPIHook;
import dev._2lstudios.swiftboard.scoreboard.HealthDisplay;
import dev._2lstudios.swiftboard.scoreboard.Scoreboard;
import dev._2lstudios.swiftboard.scoreboard.ScoreboardManager;
import net.md_5.bungee.api.ChatColor;

public class SwiftSidebar implements Runnable {
    private final Plugin plugin;
    private final ScoreboardManager scoreboardManager;
    private final Map<Player, List<String>> currentLines = new ConcurrentHashMap<>();
    private SidebarProvider provider;

    public SwiftSidebar(final Plugin plugin, final ScoreboardManager scoreboardManager) {
        this.plugin = plugin;
        this.scoreboardManager = scoreboardManager;
    }

    public void setProvider(SidebarProvider provider) {
        this.provider = provider;
    }

    public SidebarProvider getProvider() {
        return provider;
    }

    private String format(final Player player, String text) {
        text = ChatColor.translateAlternateColorCodes('&', PlaceholderAPIHook.setPlaceholders(player, text));

        if (text.length() > 40) {
            text = text.substring(0, 40);
        }

        return text;
    }

    private String sendLine(final Scoreboard scoreboard, final String line, final int index)
            throws InvocationTargetException {
        if (line.isEmpty()) {
            final StringBuilder emptyBuilder = new StringBuilder();

            for (int y = 0; y < index; y++) {
                emptyBuilder.append(ChatColor.RESET);
            }

            final String formattedLine = emptyBuilder.toString();

            scoreboard.updateScore("swiftsidebar", formattedLine, index);

            return formattedLine;
        } else {
            final Player player = scoreboard.getPlayer();
            final String formattedLine = format(player, line);

            scoreboard.updateScore("swiftsidebar", formattedLine, index);

            return formattedLine;
        }
    }

    private void sendLines(final Player player) throws InvocationTargetException {
        if(provider == null) return;
        if (scoreboardManager.hasScoreboard(player)) {
            final Scoreboard scoreboard = scoreboardManager.getScoreboard(player);

            if (!provider.getLines(player).isEmpty()) {
                final LinkedList<String> lines = provider.getLines(player);

                Collections.reverse(lines); //IDK why but it works

                if (!lines.isEmpty()) {
                    final List<String> sentLines = new ArrayList<>();
                    final String title = format(player, provider.getTitle(player));

                    if (scoreboard.containsObjective("swiftsidebar")) {
                        if (!title.equals(scoreboard.getObjective("swiftsidebar").getDisplayName())) {
                            scoreboard.updateObjective("swiftsidebar", title, HealthDisplay.INTEGER);
                        }
                    } else {
                        scoreboard.createObjective("swiftsidebar", title, HealthDisplay.INTEGER);
                        scoreboard.displayObjective(1, "swiftsidebar");
                    }

                    try {
                        for (int i = 0; i < lines.size(); i++) {
                            final String sentLine = sendLine(scoreboard, lines.get(i), i);

                            sentLines.add(sentLine);
                        }
                    } finally {
                        try {
                            if (currentLines.containsKey(player)) {
                                for (final String currentLine : currentLines.get(player)) {
                                    if (!sentLines.contains(currentLine)) {
                                        scoreboard.removeScore("swiftsidebar", currentLine);
                                    }
                                }
                            }
                        } finally {
                            currentLines.put(player, sentLines);
                        }
                    }
                } else {
                    scoreboard.removeObjective("swiftsidebar");
                }
            } else {
                scoreboard.removeObjective("swiftsidebar");
            }
        }
    }

    public void sendLines() throws InvocationTargetException {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            sendLines(player);
        }
    }

    @Override
    public void run() {
        try {
            sendLines();
        } catch (final InvocationTargetException e) {
            plugin.getLogger().info("Failed to send SwiftBoard to players!");
        }
    }
}
