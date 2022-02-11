package com.jacobclarkdev.hcfcore.inventories;

import java.io.File;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.jacobclarkdev.hcfcore.utilities.Utils;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;

/**
 * This class listens for inventory click events and acts accordingly. If they click an item in the shop's Menu I send them to
 * the correct page based on what they clicked.
 * If they click on an item that is being sold in the shop I check they have enough balance and inventory space to collect the item
 * then take the balance off them and give them the item.
 * 
 * @author Jacob Clark
 */

public class InventoryEvents implements Listener {
	
	Plugin plugin;
	Economy econ;
	Shop shopClass;
	
	private File shopConfigDir = Utils.getShopConfigDir();
	
	public InventoryEvents(Plugin p, Economy e, Shop s) {
		this.plugin = p;
		this.econ = e;
		this.shopClass = s;
	}
	
	
	/**
	 * Everything here gets called whenever someone clicks in an inventory. This gets called a lot so I have to do some checks to
	 * narrow it down, by checking the clicked inventory's title is the same as the title I gave them in Shop.java.
	 * 
	 * Every time an item is clicked in the shop inventories the event needs to be cancelled and returned out of, this is so players
	 * can't take items out of the shop inventory and into their own, getting free items. */
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		
		if(!(event.getWhoClicked() instanceof Player)) return;
		
		Player player = (Player) event.getWhoClicked();
		ItemStack clickedItem = event.getCurrentItem();
		
		
		switch(event.getView().getTitle()) {
			
		case("Shop Menu"):
			
			
			if(event.getClickedInventory().equals(player.getInventory()) || clickedItem == null) {
				event.setCancelled(true);
				return;
			} else if(!(Utils.getShopConfig(clickedItem.getItemMeta().getDisplayName()) == null)) {
				event.setCancelled(true);
				shopClass.openShopPage(Utils.getShopConfig(clickedItem.getItemMeta().getDisplayName()), player);
				return;
			}else {
				event.setCancelled(true);
				return;
			}
		
		
		case("Shop"):
			
			if(event.getClickedInventory().equals(player.getInventory()) || clickedItem == null) {
				event.setCancelled(true);
				return;
			} else if(event.getSlot() < 9 && event.getSlot() != 0) {
				event.setCancelled(true);
				return;
			} else if(event.getCurrentItem().getType().equals(Material.BARRIER )) {
				event.setCancelled(true);
				shopClass.openShopMenu(player);
				return;
			}
			
			String pageName = event.getView().getTopInventory().getItem(1).getItemMeta().getDisplayName();
			
			/* Iterating through all the shop configs, if the page title matches the PageName in the config, I know it's the correct
			 * config to use. */
			for (File shopFile : shopConfigDir.listFiles()) {
				YamlConfiguration shopConfig = YamlConfiguration.loadConfiguration(shopFile);
				if(pageName.contains(shopConfig.getString("PageName"))) {
					
					int clickedItemPrice = shopConfig.getInt("Items." + clickedItem.getType().toString() + ".Price");
					
					/* Checking the player has enough money in their account and if they have free space in their inventory */
					if(econ.getBalance(player) >= clickedItemPrice) {
						/* Checking the player has space in their inventory */
						if(player.getInventory().firstEmpty() != -1) {
							econ.withdrawPlayer(player, clickedItemPrice);
							ItemStack itemToGive = new ItemStack(clickedItem.getType());
							player.getInventory().addItem(itemToGive);
							String itemName = itemToGive.getType().name().toLowerCase().replace('_', ' ');
							player.sendMessage(Utils.chatPrefix + "You have bought one " + itemName + " for " + Integer.toString(clickedItemPrice));
							shopClass.openShopPage(Utils.getShopConfig(pageName), player);
							event.setCancelled(true);
							return;
						} else {
							event.setCancelled(true);
							player.sendMessage(Utils.chatPrefix + "You don't have any space in your Inventory!");
							return;
						}
					} else {
						event.setCancelled(true);
						player.sendMessage(Utils.chatPrefix + "You don't have enough balance to buy this!");
						return;
					}
					
				}
			}
			break;
		}
	}
}