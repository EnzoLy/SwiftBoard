package dev._2lstudios.swiftboard;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import dev._2lstudios.swiftboard.commands.SwiftBoardCommandExecutor;
import dev._2lstudios.swiftboard.listeners.PlayerChangedWorldListener;
import dev._2lstudios.swiftboard.listeners.PlayerJoinListener;
import dev._2lstudios.swiftboard.listeners.PlayerQuitListener;
import dev._2lstudios.swiftboard.scoreboard.Scoreboard;
import dev._2lstudios.swiftboard.scoreboard.ScoreboardManager;
import dev._2lstudios.swiftboard.swift.SwiftHealth;
import dev._2lstudios.swiftboard.swift.SwiftNametag;
import dev._2lstudios.swiftboard.swift.SwiftSidebar;
import dev._2lstudios.swiftboard.swift.config.SwiftConfig;
import dev._2lstudios.swiftboard.swift.config.SwiftHealthConfig;
import dev._2lstudios.swiftboard.swift.config.SwiftNametagConfig;
import dev._2lstudios.swiftboard.swift.config.SwiftSidebarConfig;

public class SwiftBoard extends JavaPlugin {
    private static ScoreboardManager scoreboardManager;
    private static SwiftHealth swiftHealth;
    private static SwiftSidebar swiftSidebar;
    private static SwiftNametag swiftNametag;
    private static final SwiftConfig swiftConfig = new SwiftConfig();

    public static ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public static SwiftHealth getSwiftHealth() {
        return swiftHealth;
    }

    public static SwiftSidebar getSwiftSidebar() {
        return swiftSidebar;
    }

    public static SwiftNametag getSwiftNametag() {
        return swiftNametag;
    }

    public SwiftConfig loadConfig() {
        saveDefaultConfig();

        return swiftConfig.update(getConfig());
    }

    @Override
    public void onEnable() {
        reloadConfig();

        final Server server = getServer();
        final BukkitScheduler scheduler = server.getScheduler();
        final PluginManager pluginManager = server.getPluginManager();
        final SwiftConfig swiftConfig = loadConfig();
        final SwiftHealthConfig swiftHealthConfig = swiftConfig.getSwiftHealthConfig();
        final SwiftNametagConfig swiftNametagConfig = swiftConfig.getSwiftNametagConfig();
        final SwiftSidebarConfig swiftSidebarConfig = swiftConfig.getSwiftSidebarConfig();

        scoreboardManager = new ScoreboardManager();
        swiftHealth = new SwiftHealth(this, scoreboardManager, swiftHealthConfig);
        swiftSidebar = new SwiftSidebar(this, scoreboardManager);
        swiftNametag = new SwiftNametag(this, scoreboardManager);

        for (final Player player : getServer().getOnlinePlayers()) {
            scoreboardManager.create(player);
    
            try {
                if (swiftNametagConfig.isEnabled()) {
                    swiftNametag.init(player);
                }
            } catch (final InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        pluginManager.registerEvents(new PlayerChangedWorldListener(swiftSidebar, swiftSidebarConfig), this);
        pluginManager.registerEvents(
                new PlayerJoinListener(scoreboardManager, swiftSidebar, swiftNametag, swiftNametagConfig, swiftSidebarConfig), this);
        pluginManager.registerEvents(new PlayerQuitListener(scoreboardManager, swiftSidebar, swiftNametag), this);

        if (swiftHealthConfig.isEnabled() && swiftHealthConfig.getUpdateTicks() > 0) {
            scheduler.runTaskTimerAsynchronously(this, swiftHealth, swiftHealthConfig.getUpdateTicks(),
                    swiftHealthConfig.getUpdateTicks());
        }

        if (swiftSidebarConfig.isEnabled() && swiftSidebarConfig.getUpdateTicks() > 0) {
            scheduler.runTaskTimerAsynchronously(this, swiftSidebar, swiftSidebarConfig.getUpdateTicks(),
                    swiftSidebarConfig.getUpdateTicks());
        }

        if (swiftNametagConfig.isEnabled() && swiftNametagConfig.getUpdateTicks() > 0) {
            scheduler.runTaskTimerAsynchronously(this, swiftNametag, swiftNametagConfig.getUpdateTicks(),
                    swiftNametagConfig.getUpdateTicks());
        }

        getCommand("swiftboard").setExecutor(new SwiftBoardCommandExecutor(this));
    }

    @Override
    public void onDisable() {
        final Server server = getServer();
        final BukkitScheduler scheduler = server.getScheduler();

        HandlerList.unregisterAll(this);
        scheduler.cancelTasks(this);

        try {
            for (final Scoreboard scoreboard : scoreboardManager.getScoreboards()) {
                scoreboard.clearObjectives();
                scoreboard.clearTeams();
            }
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}