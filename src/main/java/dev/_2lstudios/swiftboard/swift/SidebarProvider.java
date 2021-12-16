package dev._2lstudios.swiftboard.swift;

import org.bukkit.entity.Player;

import java.util.LinkedList;

public interface SidebarProvider {

    String getTitle(Player player);

    LinkedList<String> getLines(Player player);
}