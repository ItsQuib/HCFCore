package com.jacobclarkdev.hcfcore.koth;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.jacobclarkdev.hcfcore.Main;
import com.jacobclarkdev.hcfcore.utilities.Utils;

import net.md_5.bungee.api.ChatColor;

public class HillCreator implements Listener  {
	
	Plugin plugin;
	
	public HashMap<UUID, Integer> playerMap = new HashMap<UUID, Integer>();
	
//	private File hillFolder = new File(plugin.getDataFolder() + "/KOTH/Hills/");
	
	public HillCreator(Plugin p) {
		
		this.plugin = p;
		
	}
	
	public HashMap<UUID, Integer> getPlayerMap() {
		return playerMap;
	}
	
	public void setPlayerMap(UUID idToAdd, Integer intToAdd) {
		playerMap.put(idToAdd, intToAdd);
	}
	
	public boolean isInPlayerMap(UUID idToCheck) {
		return (playerMap.containsKey(idToCheck)) ? true : false;
	}

	
	public void createHill(Player p, String[] args) {
		
		File hillFile = Utils.generateNewHillFile();
		int fileId = Integer.parseInt(hillFile.getName().replace(".yml", ""));
		
		ItemStack blazeRod = Utils.makeItem(Material.BLAZE_ROD, ChatColor.RED + "Hill ID: " + Integer.toString(fileId) + ChatColor.YELLOW + "   Left click to set pos 1 | Right click to set pos 2", 1, null);
		blazeRod = Utils.addNBTTag(blazeRod, "HillId", Integer.toString(fileId));
		p.getInventory().addItem(blazeRod);
		p.sendMessage(Utils.chatPrefix + "Use this blaze rod to set both corners of the hill you would like to create!");
		
		YamlConfiguration hillConfig = YamlConfiguration.loadConfiguration(hillFile);
		hillConfig.set("Hill.Type", args[0].toUpperCase());
		hillConfig.set("Hill.id", Integer.toString(fileId));
		Utils.saveFile(hillConfig, hillFile);
		playerMap.put(p.getUniqueId(), fileId);
		
	}
	
	public void finishHill(int hillId) {
		
		File hillFile = new File(plugin.getDataFolder() + "/KOTH/Hills/" + Integer.toString(hillId) + ".yml");
		YamlConfiguration hillConfig = YamlConfiguration.loadConfiguration(hillFile);
		List<String> axisList = Arrays.asList("x", "y", "z");
		int[] numList = new int[2];
		
		for(int i = 0; i < 3; i++) {
			numList[0] = hillConfig.getInt("Location.1." + axisList.get(i));
			numList[1] = hillConfig.getInt("Location.2." + axisList.get(i));
			Arrays.sort(numList);
			
			hillConfig.set("Location.1." + axisList.get(i), numList[0]);
			hillConfig.set("Location.2." + axisList.get(i), numList[1]);
		}
		
		Utils.saveFile(hillConfig, hillFile);
		
	}
	
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		
		Player p = (Player) e.getPlayer();
		
		ItemStack heldItem = e.getPlayer().getInventory().getItemInMainHand();
		
		if(heldItem.getType().equals(Material.BLAZE_ROD)) {
			if(!(e.getHand().equals(EquipmentSlot.HAND))) { return; }
			if(!(Utils.getNBTTag(heldItem, "HillId").equals(""))) {
				
				if(e.getAction().equals(Action.LEFT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
					
					int isLeftClick = (e.getAction().equals(Action.LEFT_CLICK_BLOCK)) ? 1 : 2;
					
					int hillId = Integer.parseInt(Utils.getNBTTag(heldItem, "HillId"));
					
					File hillFile = new File(plugin.getDataFolder() + "/KOTH/Hills/" + Integer.toString(hillId) + ".yml");
					YamlConfiguration hillConfig = YamlConfiguration.loadConfiguration(hillFile);
					
					Location blockLoc = e.getClickedBlock().getLocation();
					
					int xAxis = (int) blockLoc.getX();
					int yAxis = (int) blockLoc.getY();
					int zAxis = (int) blockLoc.getZ();
					
					hillConfig.set("Location." + Integer.toString(isLeftClick) + ".x", xAxis);
					hillConfig.set("Location." + Integer.toString(isLeftClick) + ".y", yAxis);
					hillConfig.set("Location." + Integer.toString(isLeftClick) + ".z", zAxis);
					
					if(hillConfig.get("Location.World") != null) {
						if(!(hillConfig.get("Location.World").equals(blockLoc.getWorld().getName()))) {
							p.sendMessage(Utils.chatPrefix + "You cannot create an area in two different worlds!");
							return;
						}
					}else {
						hillConfig.set("Location.World", blockLoc.getWorld().getName());
					}
					
					p.sendMessage(Utils.chatPrefix + "Set position " + Integer.toString(isLeftClick) + " to:  X-" + Integer.toString(xAxis) + " Y-" + Integer.toString(yAxis) + " Z-" + Integer.toString(zAxis));
					
					Utils.saveFile(hillConfig, hillFile);
					e.setCancelled(true);
					return;
				}
				
			}
			
		}
		
		
	}
	
	
	
}