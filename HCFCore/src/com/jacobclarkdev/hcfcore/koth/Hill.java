package com.jacobclarkdev.hcfcore.koth;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.jacobclarkdev.factions.Factions;
import com.jacobclarkdev.hcfcore.utilities.BossBarUtils;
import com.jacobclarkdev.hcfcore.utilities.Utils;

import net.md_5.bungee.api.ChatColor;

/**
 * 
 * 
 * 
 * @author Jacob Clark */

public class Hill extends Thread {
	
	Plugin plugin;
	BossBarUtils bossBarUtilsClass = new BossBarUtils();
	
	int hillId;
	int firstX, firstY, firstZ, secondX, secondY, secondZ;
	
	String world;
	
	ArrayList<Player> playersInHill = new ArrayList<Player>();
	ArrayList<Player> playersAddingToHill = new ArrayList<Player>();
	ArrayList<Player> playersToRemove = new ArrayList<Player>();
	HashMap<UUID, BossBar> bossBarMap = new HashMap<UUID, BossBar>();
	HashMap<Integer, Integer> factionMap = new HashMap<Integer, Integer>();

	
	private volatile boolean threadActive = true;
	
	public Hill(Plugin p, File hillFile) {
		YamlConfiguration hillConfig = YamlConfiguration.loadConfiguration(hillFile);

		this.plugin = p;
		
		this.hillId = Integer.parseInt(hillConfig.getString("Hill.id"));
		
		this.firstX = hillConfig.getInt("Location.1.x");
		this.firstY = hillConfig.getInt("Location.1.y");
		this.firstZ = hillConfig.getInt("Location.1.z");
		this.secondX = hillConfig.getInt("Location.2.x");
		this.secondY = hillConfig.getInt("Location.2.y");
		this.secondZ = hillConfig.getInt("Location.2.z");
		
		this.world = hillConfig.getString("Location.World");
		
	}
	
	public void shutdown() {
		threadActive = false;
	}
	
	public void addBossBar(Player p) {
		
		BossBar bar = Bukkit.createBossBar(ChatColor.LIGHT_PURPLE + " -King Of The Hill- ", BarColor.PURPLE, BarStyle.SEGMENTED_20);
		bar.addPlayer(p);
		bossBarMap.put(p.getUniqueId(), bar);
		
	}
	
	public void removeBossBar(Player p) {
		
		bossBarMap.get(p.getUniqueId()).removePlayer(p);
		bossBarMap.remove(p.getUniqueId());
		
	}
	
	public void addFactionPoint(Integer team) {
		if(factionMap.containsKey(team)) {
			factionMap.put(team, factionMap.get(team) + 1);
		} else {
			factionMap.put(team, 1);
		}
	}
	
	public Boolean checkPlayerLocation(Player p) {
		
		int playerX = (int) p.getLocation().getX();
		int playerY = (int) p.getLocation().getY();
		int playerZ = (int) p.getLocation().getZ();
		
		String playerWorld = p.getLocation().getWorld().getName();
		
		if(!(playerWorld.equals(this.world))) { return false; }
		if(playerX < this.firstX || playerX > this.secondX) { return false; }
		if(playerY < this.firstY || playerY > this.secondY) { return false; }
		if(playerZ < this.firstZ || playerZ > this.secondZ) { return false; }
		
		return true;
	}
	
	public void run() {
		
		Bukkit.broadcastMessage(Utils.chatPrefix + "Hill #" + Integer.toString(hillId) + " is now active!");
		
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if(threadActive) {
					
					for(Player p : Bukkit.getOnlinePlayers()) {
						if(checkPlayerLocation(p)) {
							playersAddingToHill.add(p);
						}
					}
					
					for(Player p : playersInHill) {
						//If player was in the hill last iteration and is still in the hill
						if(playersAddingToHill.contains(p)) {
							
							if(Factions.getTeam(p) != 0) {
								addFactionPoint(Factions.getTeam(p));
							}
							playersAddingToHill.remove(p);
						
						//If player was in the hill last iteration but is no longer (has left the area)
						} else {
							
							//I made a playersToRemove list to avoid a ConcurrentModificationException
							playersToRemove.add(p);
							removeBossBar(p);
							
						}
					}
					//The remaining players are those who are in the hill this iteration but weren't last iteration (Have just entered the area)
					for(Player p : playersAddingToHill) {
						
//						if()
						addBossBar(p);
						playersInHill.add(p);
						
					}
					for(Player p : playersToRemove) {
						playersInHill.remove(p);
					}
					playersAddingToHill.clear();
					playersToRemove.clear();
				}
			}
		}, 0, 100);
	}
}