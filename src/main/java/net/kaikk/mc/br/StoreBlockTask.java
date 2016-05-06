package net.kaikk.mc.br;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;

import com.mysql.jdbc.Statement;

public class StoreBlockTask implements Runnable {
	private BetterRelocation instance;
	private Player player;
	private Block block;
	private String json;
	
	StoreBlockTask(BetterRelocation instance, Player player, Block block, String json) {
		this.instance = instance;
		this.player = player;
		this.block = block;
		this.json = json;
	}

	@Override
	public void run() {
		try {
			String parsedJson;
			try {
				parsedJson = Utils.nbtReplaceIdsWithBukkitNames(json);
			} catch (Exception e1) {
				e1.printStackTrace();
				throw new Exception("An error occurred while parsing NBT data for this block");
			}
			
			if (parsedJson.length()>524288) {
				throw new Exception("You're trying to store more than 512KiB of data with a single block! Split your chest content in multiple chests!");
			}
			
			PreparedStatement ps = instance.ds.db.prepareStatement("INSERT INTO betterrelocation (player, srv, storetime, dat) VALUES(?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			ps.setBytes(1, Utils.UUIDtoByteArray(player.getUniqueId()));
			ps.setString(2, instance.config.serverName);
			ps.setInt(3, Utils.epoch());
			ps.setString(4, parsedJson);
			
			ps.executeUpdate();
			
			ResultSet rs = ps.getGeneratedKeys();
			rs.next();
			int id = rs.getInt(1);
			
			instance.getLogger().info(player.getName()+" stored a "+block.getType()+" (remote id: "+id+") @ "+Utils.locationToString(block.getLocation()));
			
			new BukkitRunnable() {
				@Override
				public void run() {
					if (block.getState() instanceof InventoryHolder) {
						((InventoryHolder) block.getState()).getInventory().clear();
					}
					block.setType(Material.AIR);
					player.sendMessage(ChatColor.GREEN+BetterRelocation.chatPrefix+"Block stored! It will expire in "+Utils.timeToString(instance.config.timeLimit)+", do not forget to retrieve it!");
					instance.blockUnlock(block);
				}
			}.runTask(instance);
		} catch (SQLException e) {
			player.sendMessage(ChatColor.RED+BetterRelocation.chatPrefix+"I'm not able to store this block, probably the block or an item on this container has invalid data. Or there was a database error.");
			e.printStackTrace();
		} catch (Exception e) {
			player.sendMessage(ChatColor.RED+BetterRelocation.chatPrefix+"There was an error! "+e.getMessage());
			e.printStackTrace();
		}
	}
}
