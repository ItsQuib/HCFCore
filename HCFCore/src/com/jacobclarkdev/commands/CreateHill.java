package com.jacobclarkdev.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.jacobclarkdev.hcfcore.koth.HillCreator;
import com.jacobclarkdev.hcfcore.utilities.Utils;

public class CreateHill implements CommandExecutor {
	
	HillCreator hillCreatorClass;
	
	public CreateHill(HillCreator hc) {
		
		hillCreatorClass = hc;
		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if(!(sender instanceof Player)) { return true; }
		Player player = (Player) sender;
		
		if(!(player.hasPermission("HCFCore.admin"))) {
			player.sendMessage(Utils.chatPrefix + "You don't have the required permission to do this command!");
			return true;
		} else if(hillCreatorClass.isInPlayerMap(player.getUniqueId())) {
			player.sendMessage(Utils.chatPrefix + "You are already in the process of creating a hill!");
			return true;
		}
		
		/* Checking that the player entered an argument with the command, we need either "Army" or "Monument" */
		if(args.length == 1) {
			if(Arrays.asList("army", "monument").contains(args[0].toLowerCase())) {
				
				/* Checking if the player has space in their inventory, if full it returns -1 */
				if(player.getInventory().firstEmpty() == -1) {
					player.sendMessage(Utils.chatPrefix + "You need space in your inventory to do this command!");
					return true;
				}
				
				hillCreatorClass.createHill(player, args);
				
			} else {
				player.sendMessage("Invalid command, usage: /CreateHill [Army OR Monument]");
			}
		} else {
			player.sendMessage("Invalid command, usage: /CreateHill [Army OR Monument]");
		}
		
		return false;
	}
	
	
	
}