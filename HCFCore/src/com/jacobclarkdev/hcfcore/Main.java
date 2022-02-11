package com.jacobclarkdev.hcfcore;

import java.io.File;
import java.util.Arrays;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.jacobclarkdev.commands.CreateHill;
import com.jacobclarkdev.commands.FinishHill;
import com.jacobclarkdev.hcfcore.combat.CombatLogger;
import com.jacobclarkdev.hcfcore.inventories.InventoryEvents;
import com.jacobclarkdev.hcfcore.inventories.Shop;
import com.jacobclarkdev.hcfcore.koth.HillCreator;
import com.jacobclarkdev.hcfcore.koth.HillListener;
import com.jacobclarkdev.hcfcore.utilities.Utils;

import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin implements Listener {
	
	
	/**
	 * Initialise the Utils class first, it has a method to check all configs exist and if they don't then
	 * create them with the default settings. This way the first thing that happens on loadup is the configs getting checked. */

	Utils utilsClass = new Utils(this);
	EnchantmentEvents enchantmentEventsClass = new EnchantmentEvents(this);
	Shop shopClass;
	InventoryEvents inventoryEventsClass;
	CombatLogger combatLoggerClass = new CombatLogger(this);
	HillCreator hillCreatorClass = new HillCreator(this);
	HillListener hillListenerClass = new HillListener();
	
	private static Economy econ = null;
    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> econProvider = getServer().getServicesManager().getRegistration(Economy.class);
        if (econProvider != null) {
        	econ = econProvider.getProvider();
        }
        return econ != null;
    }
	
	
	public void onEnable() {
		
		if(setupEconomy()) {
			getLogger().info("Enabled!");
		} else {
			getLogger().info("Disabled! No Vault plugin found.");
			getServer().getPluginManager().disablePlugin(this);
		}
		
		/* Init these after setup Economy otherwise it would send a nulled version of the Econ variable */
		
		shopClass = new Shop(this, econ);
		inventoryEventsClass = new InventoryEvents(this, econ, shopClass);
		
		getServer().getPluginManager().registerEvents(this, this);
		getServer().getPluginManager().registerEvents(new EnchantmentEvents(this), this);
		getServer().getPluginManager().registerEvents(new CombatLogger(this), this);
		getServer().getPluginManager().registerEvents(new InventoryEvents(this, econ, shopClass), this);
		getServer().getPluginManager().registerEvents(new HillCreator(this), this);
		getServer().getPluginManager().registerEvents(new HillListener(), this);
		
		/* Initalising the hills from the KingOfTheHill segment, this starts all necessary threads */
		
		hillListenerClass.initHills(this);
		
		
		/* Setting executors for commands */
		
		getCommand("createhill").setExecutor(new CreateHill(hillCreatorClass));
		getCommand("finishhill").setExecutor(new FinishHill(this, hillCreatorClass));
		
	}
	
	
	
	public void onDisable() {
		
		/* Cleaning up threads used for the hills */
		
		hillListenerClass.removeThreads();
		
	}
	
	
	/* Checking to see if the player has a data file, if not (A new player) I will create one and save it with their unique player ID as the file name. */
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		File playerFile = new File(this.getDataFolder() + "/Players/" + e.getPlayer().getUniqueId().toString() + ".yml");
		if(!(playerFile.exists())) {
			YamlConfiguration playerFileConfig = YamlConfiguration.loadConfiguration(playerFile);
			/**
			 * 
			 * Set File Defaults
			 * 
			 */
			
		}		
	}
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		/* Checking the command sender is a player */
		if(sender instanceof Player) {
			
			Player player = (Player) sender;
			String command = cmd.getName();
			
			if(command.equalsIgnoreCase("shop")) {
				shopClass.openShopMenu(player);
			}
			
			
		} else {
			sender.sendMessage("Must be an in-game player to use this command.");
		}
		return true;
	}
}