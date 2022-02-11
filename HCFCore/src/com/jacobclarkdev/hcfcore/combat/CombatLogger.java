package com.jacobclarkdev.hcfcore.combat;

import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;

public class CombatLogger implements Listener {
	
	Plugin plugin;
	
	
	public CombatLogger(Plugin p) {
		this.plugin = p;
	}
	
	
	
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent e) {

		if(e.getEntity() instanceof Villager) {
			
			//Do villager stuff
			
		}
				
		if(!(e.getEntity() instanceof Player)) { return; }		// If the entity dying isn't a player, return
		
		
		
	}
	
}