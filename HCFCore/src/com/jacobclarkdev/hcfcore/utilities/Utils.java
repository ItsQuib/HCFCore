package com.jacobclarkdev.hcfcore.utilities;

import java.awt.List;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import com.jacobclarkdev.hcfcore.Main;

import io.netty.util.internal.ThreadLocalRandom;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.NBTTagString;

public class Utils {
	
	static Plugin plugin;
	
	static String shopMenuDescriptionHeader;
	static String shopMenuDescriptionBorder;
	public static String chatPrefix;
	
	public static Map<Integer, Integer[]> shopMenuLayoutMap = new HashMap<Integer, Integer[]>();
	
	public Utils(Plugin p) {
		Utils.plugin = p;
		checkConfigs();
		shopMenuLayoutMap.put(1, new Integer[]{13});
		shopMenuLayoutMap.put(2, new Integer[]{12, 14});
		shopMenuLayoutMap.put(3, new Integer[]{11, 13, 15});
		shopMenuLayoutMap.put(4, new Integer[]{10, 12, 14, 16});
		shopMenuLayoutMap.put(5, new Integer[]{10, 12, 13, 14, 16});
		shopMenuLayoutMap.put(6, new Integer[]{10, 11, 12, 14, 15, 16});
		shopMenuLayoutMap.put(7, new Integer[]{10, 11, 12, 13, 14, 15, 16});
		
		File shopFile = new File(plugin.getDataFolder() + "/ShopConfig.yml");
		YamlConfiguration shopConfig = YamlConfiguration.loadConfiguration(shopFile);
		
		shopMenuDescriptionHeader = shopConfig.getString("PageHeader");
		shopMenuDescriptionBorder = shopConfig.getString("PageBorder");
		
		File langFile = new File(plugin.getDataFolder() + "/Language.yml");
		YamlConfiguration langConfig = YamlConfiguration.loadConfiguration(langFile);
		
		chatPrefix = ChatColor.translateAlternateColorCodes('&', langConfig.getString("ChatPrefix"));
		
		
		/*
		 * Have the message prefix and debug prefix in here?
		 * Also learn how to allow &6 &a etc. (Colour Codes)
		 */
		
	}
	
	
	/**
	 * Just a simple method to make saving files easier
	 * @param yamlConfig - The file's YamlConfiguration variable
	 * @param file - The file to save */
    
	public static void saveFile(FileConfiguration yamlConfig, File file) {
		try{
			yamlConfig.save(file);
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * A method to make creating instances of in game items with custom names, definitions and sizes much easier
	 * @param itemMaterial - The item's Material value.
	 * @param name - A custom item name
	 * @param itemSize - How many items are in this stack
	 * @param lore - A description of the item, inserted below the item's name. Can be null
	 * @return - Returns the finished item. */
	
	public static ItemStack makeItem(Material itemMaterial, String name, int itemSize, ArrayList<String> lore) {
		
		ItemStack item = new ItemStack(itemMaterial, itemSize);
		ItemMeta itemMeta = item.getItemMeta();
		if(lore != null) { itemMeta.setLore(lore); }
		itemMeta.setDisplayName(name);
		item.setItemMeta(itemMeta);
		return item;
	}
		
	public static String repeatString(int timesToRepeat, String stringToRepeat) {
		return new String(new char[timesToRepeat]).replace("\0", stringToRepeat);
	}
	
	public static String getShopDescriptionHeader() {
		return shopMenuDescriptionHeader;
	}

	public static String getShopDescriptionBorder() {
		return shopMenuDescriptionBorder;
	}
	
	public static File getShopConfigDir() {
		return new File(plugin.getDataFolder() + "/ShopConfigs/");
	}
	public static File getHillDir() {
		return new File(plugin.getDataFolder() + "/KOTH/Hills/");
	}
	
	public static String getShopConfig(String shopName) {
		File configDir = new File(plugin.getDataFolder() + "/ShopConfigs/");
		for(File pageFile : configDir.listFiles()) {
			YamlConfiguration pageConfig = YamlConfiguration.loadConfiguration(pageFile);
			if(shopName.contains(pageConfig.getString("PageName"))) {
				return pageFile.toString();
			}
		}
		return null;
	}
	
	public static ItemStack addNBTTag(ItemStack item, String tag, String data) {
		
		net.minecraft.server.v1_16_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
		NBTTagCompound itemCompound = (nmsItem.hasTag()) ? nmsItem.getTag() : new NBTTagCompound();
		itemCompound.setString(tag, data);
		nmsItem.setTag(itemCompound);
		ItemStack finishedItem = CraftItemStack.asBukkitCopy(nmsItem);
		return finishedItem;
		
	}
	
	public static Boolean checkNBTTag(ItemStack item, String tag, String data) {
		
		net.minecraft.server.v1_16_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
		if(nmsItem.hasTag()) {
			NBTTagCompound itemCompound = nmsItem.getTag();
			return itemCompound.getString(tag).equals(data);
		} else {
			return false;
		}
	}
	
	public static String getNBTTag(ItemStack item, String tag) {
		
		net.minecraft.server.v1_16_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
		NBTTagCompound itemCompound = nmsItem.getTag();
		try {
			return itemCompound.getString(tag);
		}
		catch(NullPointerException e) {
			return "";
		}
	}
	
	/* I use this to create random numbers as it saves having to Initalise a new instance of Random every time I need a random number
	 * it is also better to use if using between threads */
	
	public static int randomNumber(int min, int max) {
		return ThreadLocalRandom.current().nextInt(min, max);
	}
	
	/**
	 * This is a small method to generate a new file to store data for the HingOfTheHill section of the plugin.
	 * It uses loops to count up from 0 and check every file to see if there is already a file with that ID.
	 * This is designed to make sure no duplicate files are created, and to fill in any holes
	 * (e.g There are hills 1-7, hill 3 gets deleted, the next hill to be created will fill hill 3's slot)
	 * 
	 * @return Returns a new file with the lowest Unique ID */
	
	public static File generateNewHillFile() {
		File hillDir = new File(plugin.getDataFolder() + "/KOTH/Hills/");
		for(int i = 0; i < hillDir.listFiles().length; i++) {
			boolean duplicateName = false;
			for(File f : hillDir.listFiles()) {
				if(f.getName().equals(Integer.toString(i) + ".yml")) {
					duplicateName = true;
					break;
				}
				
			}
			if(!duplicateName) {
				return new File(plugin.getDataFolder() + "/KOTH/Hills/" + Integer.toString(i) + ".yml");
			}
		}
		return new File(plugin.getDataFolder() + "/KOTH/Hills/" + Integer.toString(hillDir.listFiles().length) + ".yml");
	}
	
	
	/*
	 * I have made this method to clean up the Main class. When the plugin is enabled I need to make sure every config
	 * I use exists. If it doesn't exist I create one and put the default config settings in there. */
	
	public void checkConfigs() {
		
		File hillDirectory = new File(plugin.getDataFolder() + "/KOTH/Hills");
		if(!(hillDirectory.exists())) {
			hillDirectory.mkdir();
		}
		
		File langFile = new File(plugin.getDataFolder() + "/Language.yml");
		if(!(langFile.exists())) {
			YamlConfiguration langConfig = YamlConfiguration.loadConfiguration(langFile);
			
			langConfig.set("ChatPrefix", "&8[&aDefault Chat Prefix&8] &c");
			
			saveFile(langConfig, langFile);
		}
		

		File shopFile = new File(plugin.getDataFolder() + "/ShopConfig.yml");
		if(!(shopFile.exists())) {
			YamlConfiguration shopConfig = YamlConfiguration.loadConfiguration(shopFile);
			
			shopConfig.set("PageHeader", "-");
			shopConfig.set("PageBorder", "*");
			
			saveFile(shopConfig, shopFile);
		}

		
		File itemFile = new File(plugin.getDataFolder() + "/ItemConfig.yml");
		if(!(itemFile.exists())) {
			YamlConfiguration itemFileConfig = YamlConfiguration.loadConfiguration(itemFile);
			/*
			 * 
			 * Set File Defaults
			 * 
			 */
			
		}
		
		File enchantFile = new File(plugin.getDataFolder() + "/EnchantConfig.yml");
		if(!(enchantFile.exists())) {
			YamlConfiguration enchantFileConfig = YamlConfiguration.loadConfiguration(enchantFile);
			
			/*
			 * Here I am just looping through all registered enchantments, then entering them in the config file with two sub options:
			 *  - Enabled: Boolean, Should this enchantment be enabled? Default = True
			 *  - Max: Integer, What is the max level allowed? Default = Default Max
			 */
			for(Enchantment e : Enchantment.values()) {
				enchantFileConfig.set(e.getKey().getKey().toString().toUpperCase() + ".Enabled", true);
				enchantFileConfig.set(e.getKey().getKey().toString() + ".Max", e.getMaxLevel());
			}
			saveFile(enchantFileConfig, enchantFile);
		}
		
		
		File combatFile = new File(plugin.getDataFolder() + "/CombatLoggerConfig.yml");
		if(!(combatFile.exists())) {
			YamlConfiguration combatFileConfig = YamlConfiguration.loadConfiguration(combatFile);
			
			/*for(Object o : EntityDamageEvent.DamageCause.values()) {
				
				Bukkit.broadcastMessage(o.toString());
				
			}*/
			
		}
		
		
		File kothFile = new File(plugin.getDataFolder() + "/KingOfTheHill/KOTHConfig.yml");
		if(!(combatFile.exists())) {
			YamlConfiguration kothFileConfig = YamlConfiguration.loadConfiguration(kothFile);
			/*
			 * 
			 * Set File Defaults
			 * 
			 */
			
		}
		
		
		File scoreboardFile = new File(plugin.getDataFolder() + "/ScoreboardConfig.yml");
		if(!(scoreboardFile.exists())) {
			YamlConfiguration scoreboardFileConfig = YamlConfiguration.loadConfiguration(scoreboardFile);
			/*
			 * 
			 * Set File Defaults
			 * 
			 */
			
		}

		
		File materialsShopFile = new File(plugin.getDataFolder() + "/ShopConfigs/MaterialShopConfig.yml");
		if(!(materialsShopFile.exists())) {
			YamlConfiguration materialsShopFileConfig = YamlConfiguration.loadConfiguration(materialsShopFile);
			
			materialsShopFileConfig.set("PageIcon", "DIAMOND");
			materialsShopFileConfig.set("PageName", "Materials Shop");
			materialsShopFileConfig.set("PageDescription", "A shop to buy materials like diamond!!");
			
			materialsShopFileConfig.set("Items.STONE.ItemName", "&aThis is an example item");
			materialsShopFileConfig.set("Items.STONE.Price", 10);
			ArrayList<String> exampleItemArray = new ArrayList<String>();
			exampleItemArray.add("&aThe 'STONE' is the ID of which item you want to be sold in this shop");
			exampleItemArray.add("&aIf you change 'STONE' to 'DIAMOND' this item will be a diamond");
			exampleItemArray.add("&aYou can only sell one instance of each item in the shop");
			materialsShopFileConfig.set("Items.STONE.Lore", exampleItemArray);
			
			
			saveFile(materialsShopFileConfig, materialsShopFile);
			
		}
		
		
		File toolsShopFile = new File(plugin.getDataFolder() + "/ShopConfigs/ToolShopConfig.yml");
		if(!(toolsShopFile.exists())) {
			YamlConfiguration toolsShopFileConfig = YamlConfiguration.loadConfiguration(toolsShopFile);
			
			toolsShopFileConfig.set("PageIcon", "DIAMOND_PICKAXE");
			toolsShopFileConfig.set("PageName", "Tools Shop");
			toolsShopFileConfig.set("PageDescription", "A shop to buy tools!!");
			
			toolsShopFileConfig.set("Items.STONE.ItemName", "&aThis is an example item");
			toolsShopFileConfig.set("Items.STONE.Price", 10);
			ArrayList<String> exampleItemArray = new ArrayList<String>();
			exampleItemArray.add("&aThe 'STONE' is the ID of which item you want to be sold in this shop");
			exampleItemArray.add("&aIf you change 'STONE' to 'DIAMOND' this item will be a diamond");
			exampleItemArray.add("&aYou can only sell one instance of each item in the shop");
			toolsShopFileConfig.set("Items.STONE.Lore", exampleItemArray);
			
			
			saveFile(toolsShopFileConfig, toolsShopFile);
			
		}
		
		
		File blocksShopFile = new File(plugin.getDataFolder() + "/ShopConfigs/BlockShopConfig.yml");
		if(!(blocksShopFile.exists())) {
			YamlConfiguration blocksShopFileConfig = YamlConfiguration.loadConfiguration(blocksShopFile);
			
			blocksShopFileConfig.set("PageIcon", "DIAMOND_BLOCK");
			blocksShopFileConfig.set("PageName", "Blocks Shop");
			blocksShopFileConfig.set("PageDescription", "A shop to buy building blocks!!");
			
			blocksShopFileConfig.set("Items.STONE.ItemName", "&aThis is an example item");
			blocksShopFileConfig.set("Items.STONE.Price", 10);
			ArrayList<String> exampleItemArray = new ArrayList<String>();
			exampleItemArray.add("&aThe STONE is the ID of which item you want to be sold in this shop");
			exampleItemArray.add("&aIf you change STONE to DIAMOND this item will be a diamond");
			exampleItemArray.add("&aYou can only sell one instance of each item in the shop");
			blocksShopFileConfig.set("Items.STONE.Lore", exampleItemArray);
			
			
			saveFile(blocksShopFileConfig, blocksShopFile);
			
		}
	}
	
}