package net.kaikk.mc.br;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import net.kaikk.mc.br.nbtutils.NBTUtils;

public class BetterRelocation extends JavaPlugin {
	static BetterRelocation instance;
	NBTUtils nbt;
	
	Config config;
	DataStore ds;
	final public static String chatPrefix = "[BetterRelocation] ";
	final public ExecutorService executor = Executors.newSingleThreadExecutor();
	
	private Map<Location,Long> lastLoc = Collections.synchronizedMap(new HashMap<Location,Long>());
	
	@Override
	public void onEnable() {
		instance=this;
		nbt = NBTUtils.getNBT();
		if (nbt == null) {
			this.getLogger().severe("This Minecraft version is not supported yet.");
			return;
		}
		
		config = new Config(instance);
		
		try {
			ds = new DataStore(instance);
			this.getServer().getPluginManager().registerEvents(new EventListener(this), this);
		} catch (Exception e) {
			e.printStackTrace();
			Bukkit.getPluginManager().disablePlugin(instance);
		}
	}
	
	@Override
	public void onDisable() {
		if (ds!=null) {
			ds.dbClose();
		}
		
		instance=null;
	}

	public static BetterRelocation instance() {
		return instance;
	}

	public DataStore dataStore() {
		return ds;
	}
	
	public Config config() {
		return config;
	}
	
	void blockLock(Block block) {
		Long lastAction = lastLoc.get(block.getLocation());
		if (lastAction!=null && System.currentTimeMillis()-lastAction.longValue()<30000) {
			throw new IllegalStateException("There's a pending operation on this block.");
		}
		
		lastLoc.put(block.getLocation(), System.currentTimeMillis());
	}
	
	void blockUnlock(Block block) {
		lastLoc.remove(block.getLocation());
	}
}
