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
	public void onInventoryClick(InventoryClickEvent e) {
		
		if(!(e.getWhoClicked() instanceof Player)) return;
		
		Player p = (Player) e.getWhoClicked();
		
		/* Only act on clicks in the menu inventory */
		if(e.getView().getTitle().contains("Shop Menu")) {
			
			if(e.getClickedInventory().equals(p.getInventory()) || e.getCurrentItem() == null) {
				e.setCancelled(true);
				return;
			} else if(!(Utils.getShopConfig(e.getCurrentItem().getItemMeta().getDisplayName()) == null)) {
				e.setCancelled(true);
				shopClass.openShopPage(Utils.getShopConfig(e.getCurrentItem().getItemMeta().getDisplayName()), p);
				return;
			}else {
				e.setCancelled(true);
				return;
			}
		}

		/* Only act on clicks in the shop page */
		if(e.getView().getTitle().contains("Shop")) {
			if(e.getClickedInventory().equals(p.getInventory()) || e.getCurrentItem() == null) {
				e.setCancelled(true);
				return;
			} else if(e.getSlot() < 9 && e.getSlot() != 0) {
				e.setCancelled(true);
				return;
			} else if(e.getCurrentItem().getType().equals(Material.BARRIER )) {
				e.setCancelled(true);
				shopClass.openShopMenu(p);
				return;
			}
			
			ItemStack clickedItem = e.getCurrentItem();
			String pageName = e.getView().getTopInventory().getItem(1).getItemMeta().getDisplayName();
			
			/* Iterating through all the shop configs, if the page title matches the PageName in the config, I know it's the correct
			 * config to use. */
			for (File shopFile : shopConfigDir.listFiles()) {
				YamlConfiguration shopConfig = YamlConfiguration.loadConfiguration(shopFile);
				if(pageName.contains(shopConfig.getString("PageName"))) {
					
					int clickedItemsPrice = shopConfig.getInt("Items." + clickedItem.getType().toString() + ".Price");
					
					/* Checking the player has enough money in their account and if they have free space in their inventory */
					if(econ.getBalance(p) >= clickedItemsPrice) {
						if(p.getInventory().firstEmpty() != -1) {
							econ.withdrawPlayer(p, clickedItemsPrice);
							ItemStack itemToGive = new ItemStack(clickedItem.getType());
							p.getInventory().addItem(itemToGive);
							String itemName = itemToGive.getType().name().toLowerCase().replace('_', ' ');
							p.sendMessage(Utils.chatPrefix + "You have bought one " + itemName + " for " + Integer.toString(clickedItemsPrice));
							shopClass.openShopPage(Utils.getShopConfig(pageName), p);
							e.setCancelled(true);
							return;
						} else {
							e.setCancelled(true);
							p.sendMessage(Utils.chatPrefix + "You don't have any space in your Inventory!");
							return;
						}
					} else {
						e.setCancelled(true);
						p.sendMessage(Utils.chatPrefix + "You don't have enough balance to buy this!");
						return;
					}
					
				}
			}
			
		}
		
	}
}