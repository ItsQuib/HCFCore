package com.jacobclarkdev.hcfcore.inventories;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.jacobclarkdev.hcfcore.utilities.TextUtils;
import com.jacobclarkdev.hcfcore.utilities.Utils;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;

/**
 * In this class I am creating menus for the in game shop, I achieve this by creating an Inventory and filling it with items
 * In the InventoryEvents.java class I then use the onInventoryClick event to stop users from taking the items from the inventory,
 * they instead act like buttons, with players being able to click them to do certain actions like buying an item or going to
 * a different page.
 * 
 * In this case the items that go in the shop are the choice of the server admin, so I use .yml files as configuration files
 * in the plugin directory.
 * 
 * @author Jacob Clark
 */


public class Shop {
	
	Plugin plugin;
	Economy econ;
	
	/* Creating the inventories. */
	
	Inventory shopMenuInventory = Bukkit.getServer().createInventory(null, 27, "Shop Menu");
	Inventory shopPageInventory = Bukkit.getServer().createInventory(null, 54, "Shop");
	
	private File shopConfigDir = Utils.getShopConfigDir();
	
	public Shop(Plugin p, Economy e) {
		this.plugin = p;
		this.econ = e;
	}
	

	/**
	 * This opens the menu, in the menu there is an item for each shop, which the server admin sets up in the configuration files
	 * I check each file and create the correct item to put into the menu. */
	
	public void openShopMenu(Player p) {
		/**
		 * TEST TO SEE IF THIS CLEARS SOMEONE ELSE'S SHOP MENU WHEN THEY HAVE IT OPEN
		 */
		
		shopMenuInventory.clear();
		
		ArrayList<ItemStack> itemArray = new ArrayList<ItemStack>();
		
		String descHeader = Utils.getShopDescriptionHeader();
		String descBorder = Utils.getShopDescriptionBorder();		

		ItemStack glassBorder = Utils.makeItem(Material.YELLOW_STAINED_GLASS_PANE, " ", 1, null);
		
		/* Checking there aren't more then 7 files */
		
		if(shopConfigDir.listFiles().length > 7) {
			p.sendMessage(Utils.chatPrefix + "Error! Too many shop configs");
			return;
		}

		/**
		 * This loops through all the files in the ShopConfigs directory, creating an item for that config with the
		 * appropriate name and description and placing it into the menu
		 */
		
		for(File pageFile : shopConfigDir.listFiles()) {
			YamlConfiguration pageConfig = YamlConfiguration.loadConfiguration(pageFile);
			//Need to add a try/except to catch any errors thrown if PageIcon and PageName are invalid
			ArrayList<String> itemDescription = new ArrayList<String>();
			int charCounter = pageConfig.getString("PageDescription").length() + 4;
			itemDescription.add(ChatColor.GOLD + TextUtils.getCenteredMessage(Utils.repeatString(charCounter, descHeader)));
			itemDescription.add(ChatColor.GOLD + TextUtils.getCenteredMessage(descBorder + "  " + pageConfig.getString("PageDescription") + "  " + descBorder));
			itemDescription.add(ChatColor.GOLD + TextUtils.getCenteredMessage(Utils.repeatString(charCounter, descHeader)));
			ItemStack pageItem = Utils.makeItem(Material.getMaterial(pageConfig.getString("PageIcon").toUpperCase()), ChatColor.RED + pageConfig.getString("PageName"), 1, itemDescription);
			itemArray.add(pageItem);
		}
		
		/* This puts a border around the items, making the menu look more presentable */
		
		for(int i = 0; i < 10; i++) {
			shopMenuInventory.setItem(i, glassBorder);
		}
		for(int i = 17; i < 27; i++) {
			shopMenuInventory.setItem(i, glassBorder);
		}
		
		/**
		 * Here I am getting the correct list from shopMenuLayoutMap in my Utils class. This list contains the correct slots to
		 * put the items depending on how many there are, I then just loop through the list and add the items to their
		 * respective slots. This keeps them even and tidy looking regardless of how many items there are.
		 */
		
		Integer[] shopLayout = Utils.shopMenuLayoutMap.get(shopConfigDir.listFiles().length);
		for(int i = 0; i < shopLayout.length ; i++) {
			shopMenuInventory.setItem(shopLayout[i], itemArray.get(i));
		}
		
		p.openInventory(shopMenuInventory);
	}
	
	/*
	 * TODO:
	 * - setCancelled etc.etc. if clicked in own inventory, make it secure
	 */
	
	
	/**
	 * Here I'm opening a shop page, first I check if the inventory object is empty, if it isn't clear it.
	 * I then loop through all the items in that page's config file, for every item add the lore then create the item.
	 * 
	 * The counter starts at 9 because the first 9 slots contains the header. If the counter gets to 53 I break as there aren't
	 * any more slots left in the inventory.
	 * 
	 * @param shopFileString - The path for the config file to open
	 */
	
	public void openShopPage(String shopFilePath, Player p) {
		File shopFile = new File(shopFilePath);
		YamlConfiguration shopConfig = YamlConfiguration.loadConfiguration(shopFile);
		
		if(!(shopPageInventory.isEmpty())) { shopPageInventory.clear(); }
		
		ItemStack glassBorder = Utils.makeItem(Material.YELLOW_STAINED_GLASS_PANE, ChatColor.RED + shopConfig.getString("PageName"), 1, null);
		
		for(int i = 1; i < 8; i++) {
			shopPageInventory.setItem(i, glassBorder);
		}
		
		ItemStack backButton = Utils.makeItem(Material.BARRIER, ChatColor.RED + "Back To Menu", 1, null);
		shopPageInventory.setItem(0, backButton);
				
		int userBalance = (int) econ.getBalance(p);
		
		ItemStack balanceButton = Utils.makeItem(Material.GOLD_INGOT, ChatColor.YELLOW + "Your Balance: " + Integer.toString(userBalance), 1, null);
		shopPageInventory.setItem(8, balanceButton);
		
		int counter = 9;
		for(String key : shopConfig.getConfigurationSection("Items").getKeys(false)) {
			
			ArrayList<String> loreList = new ArrayList<String>();
			for(String loreLine : shopConfig.getStringList("Items." + key + ".Lore")) {
				loreList.add(ChatColor.translateAlternateColorCodes('&', loreLine));
			}
			loreList.add(" ");
			loreList.add(ChatColor.RED + "|  " + ChatColor.YELLOW + "Price: " + ChatColor.GOLD + Integer.toString(shopConfig.getInt("Items." + key + ".Price")));
			loreList.add(ChatColor.RED + "|  " + ChatColor.YELLOW + "Click to buy!");
			ItemStack item = Utils.makeItem(Material.getMaterial(key.toUpperCase()), ChatColor.translateAlternateColorCodes('&', shopConfig.getString("Items." + key + ".ItemName")), 1, loreList);
			
			shopPageInventory.setItem(counter, item);
			counter++;
			
			if(counter == 53) {
				break;
			}
		}
		p.openInventory(shopPageInventory);
	}
}