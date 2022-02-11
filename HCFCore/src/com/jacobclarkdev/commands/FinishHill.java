package com.jacobclarkdev.commands;

import java.io.File;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.jacobclarkdev.hcfcore.koth.HillCreator;
import com.jacobclarkdev.hcfcore.utilities.Utils;

public class FinishHill implements CommandExecutor {
	
	Plugin plugin;
	HillCreator hillCreatorClass;
	
	public FinishHill(Plugin p, HillCreator hc) {
		
		plugin = p;
		hillCreatorClass = hc;
		
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player)) { return true; }
		Player player = (Player) sender;
		
		if(cmd.getName().equalsIgnoreCase("FinishHill")) {
			if(!(player.hasPermission("HCFCore.Admin"))) {
				player.sendMessage(Utils.chatPrefix + "You don't have the required permission to do this command!");
				return true;
			}
			
			for(ItemStack item : player.getInventory().getContents()) {
				if(!(Utils.getNBTTag(item, "HillId").equals(""))) {
					String hillId = Utils.getNBTTag(item, "HillId");
					File hillFile = new File(plugin.getDataFolder() + "/KOTH/Hills/" + hillId + ".yml");
					YamlConfiguration hillConfig = YamlConfiguration.loadConfiguration(hillFile);
					if(!(hillConfig.get("Location.1.x").equals(null) || hillConfig.get("Location.2.x").equals(null))) {
						player.sendMessage(Utils.chatPrefix + "Hill ID: " + hillId + " created!");
						player.getInventory().remove(item);
						hillCreatorClass.finishHill(Integer.parseInt(hillId));
						return true;
					} else {
						player.sendMessage(Utils.chatPrefix + "You must select the two opposite points for the hill!");
						return true;
					}
				}
			}
		}
		return false;
	}
	
	
}