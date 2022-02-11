package com.jacobclarkdev.hcfcore.koth;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

import com.jacobclarkdev.hcfcore.utilities.BossBarUtils;
import com.jacobclarkdev.hcfcore.utilities.Utils;

/**
 * The HillListener class is for listening to in-game events
 * 
 * @author Jacob Clark
 */
public class HillListener implements Listener {
	
	BossBarUtils bossBarUtilsClass = new BossBarUtils();
	
/*	HashMap<Integer, Hill> hillArray = new HashMap<Integer, Hill>();*/
	ArrayList<Hill> hillsArray = new ArrayList<Hill>();
	ArrayList<Hill> activeHillsArray = new ArrayList<Hill>();
	
/*	HashMap<UUID, Hill> playerInHillMap = new HashMap<UUID, Hill>();*/
	
	public void initHills(Plugin plugin) {
		
		for(File f : Utils.getHillDir().listFiles()) {
			
			Hill hill = new Hill(plugin, f);
			hillsArray.add(hill);
			
		}
	}
	
	public void removeThreads() {
		for(Hill h : activeHillsArray) {
			
			h.shutdown();
			
		}
	}
}