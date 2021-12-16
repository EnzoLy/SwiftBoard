package dev._2lstudios.swiftboard.swift;

import org.bukkit.entity.Player;

public interface NameTagProvider {

    public String getPrefix(Player player);

    public String getSuffix(Player player);

}