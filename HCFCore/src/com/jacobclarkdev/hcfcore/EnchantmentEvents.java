package com.jacobclarkdev.hcfcore;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.plugin.Plugin;

/**
 * 
 * @author Jacob Clark
 */

public class EnchantmentEvents implements Listener {
	
	Plugin plugin;
	
	public EnchantmentEvents(Plugin p) {
		
		this.plugin = p;
		
	}
	
	@EventHandler
	public void onEnchant(EnchantItemEvent e) {
		
		File enchantmentFile = new File(plugin.getDataFolder() + "EnchantmentConfig.yml");
		YamlConfiguration enchantmentFileConfig = YamlConfiguration.loadConfiguration(enchantmentFile);
		
		/**
		 * Here I just loop through all the enchantments that are being added, as there could be multiple.
		 * For each enchantment if it is disabled in the config file, simply remove the enchantment.
		 * 
		 * If the server admin set a maximum level lower than the base game's max level, change the level of the enchantment
		 * to the max that is set in the config file.
		 * 
		 * Having trouble with iterating, throwing a ConcurrentModificationException. I am going to change it to use an Iterator
		 * Just need to find out how to dupe Map into Iterator.
		 */
		ArrayList<Enchantment> enchantsToRemove = new ArrayList<Enchantment>();
		Map<Enchantment, Integer> enchantsToChange = new HashMap<Enchantment, Integer>();
		Map<Enchantment, Integer> enchantMap = e.getEnchantsToAdd();
//		for(Iterator<Enchantment, Integer> = enchantMap.ent)
		for(Enchantment enchantment : enchantMap.keySet()) {
			Bukkit.broadcastMessage(enchantment.getKey().getKey().toString());
			Bukkit.broadcastMessage(Integer.toString(enchantMap.get(enchantment)));
			if(!(enchantmentFileConfig.getBoolean(enchantment.getKey().getKey().toString() + ".Enabled"))) {
				enchantsToRemove.add(enchantment);
			} else if(enchantmentFileConfig.getInt(enchantment.getKey().getKey().toString() + ".Max") > enchantMap.get(enchantment)) {
				enchantsToChange.put(enchantment, enchantmentFileConfig.getInt(enchantment.getKey().getKey().toString() + ".Max"));
			}
		}
		
		for(Enchantment enchantment : enchantsToRemove) {
			e.getEnchantsToAdd().remove(enchantment);
		}
		for(Enchantment enchantment : enchantsToChange.keySet()) {
			e.getEnchantsToAdd().put(enchantment, enchantsToChange.get(enchantment));
		}
		Bukkit.broadcastMessage("Test");
		for(Enchantment enchantment: e.getEnchantsToAdd().keySet()) {
			
			Bukkit.broadcastMessage(enchantment.getKey().getKey());
			Bukkit.broadcastMessage(e.getEnchantsToAdd().get(enchantment).toString());
			
		}
	}
	
}