package com.jacobclarkdev.factions;

import java.io.File;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * 
 * Not yet worked on, this class will handle the creation of player Factions.
 * 
 * @author Jacob Clark
 */

public class Factions {
	
	static Plugin plugin;
	
	public Factions(Plugin p) {
		
		this.plugin = p;
		
	}
	
	public static int getTeam(Player p) {
		
		File f = new File(plugin.getDataFolder() + "/Players/" + p.getUniqueId().toString() + ".yml");
		YamlConfiguration fConfig = YamlConfiguration.loadConfiguration(f);
		
		
		return fConfig.getInt("FactionID");
		
	}
	
}