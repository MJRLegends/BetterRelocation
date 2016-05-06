package net.kaikk.mc.br;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;


public class Config {
	public String serverName, dbHostname, dbUsername, dbPassword, dbDatabase;
	public Set<Material> allowedBlocks;
	public int timeLimit;
	
	Config(JavaPlugin instance) {
		instance.saveDefaultConfig();
		instance.reloadConfig();
		
		this.serverName=instance.getConfig().getString("ServerName");
		
		allowedBlocks=new HashSet<>();
		List<String> allowedStrList = instance.getConfig().getStringList("AllowedBlocks");
		for (String allowed : allowedStrList) {
			try {
				Material material = Material.matchMaterial(allowed);
				if (material==null) {
					throw new IllegalArgumentException();
				}
				allowedBlocks.add(material);
			} catch (Exception e) {
				BetterRelocation.instance.getLogger().warning("Block material \""+allowed+"\" is not valid.");
			}
		}
		
		this.dbHostname=instance.getConfig().getString("MySQL.Hostname");
		this.dbUsername=instance.getConfig().getString("MySQL.Username");
		this.dbPassword=instance.getConfig().getString("MySQL.Password");
		this.dbDatabase=instance.getConfig().getString("MySQL.Database");
		
		this.timeLimit=instance.getConfig().getInt("TimeLimit");
	}
}
