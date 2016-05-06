package net.kaikk.mc.br;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

class RetrieveBlockTask implements Runnable {
	final BetterRelocation instance;
	final Player player;
	final String playerPerms;
	final Block block;
	
	public RetrieveBlockTask(BetterRelocation instance, Player player, Block block, String playerPerms) {
		this.instance = instance;
		this.player = player;
		this.block = block;
		this.playerPerms = playerPerms;		
	}
	
	@Override
	public void run() {
		try {
			Statement statement = instance.ds.statement();
			String conditions = " player="+Utils.UUIDtoHexString(player.getUniqueId())+" AND srv IN ("+playerPerms+") AND storetime < "+(Utils.epoch()+instance.config.timeLimit)+" LIMIT 1";
			statement.executeUpdate("START TRANSACTION");
			ResultSet rs = statement.executeQuery("SELECT id, dat FROM betterrelocation WHERE"+conditions);
			if (rs.next()) {
				final int id = rs.getInt(1);
				final String json = Utils.nbtReplaceBukkitNamesWithIds(rs.getString(2));
				
				new BukkitRunnable() {
					@Override
					public void run() {
						try {
							instance.nbt.toBlock(block, json);
							player.sendMessage(ChatColor.GREEN+BetterRelocation.chatPrefix+"Block retrieved!");
							instance.getLogger().info(player.getName()+" retrieved a "+block.getType()+" (remote id: "+id+") @ "+Utils.locationToString(block.getLocation()));
							statement.executeUpdate("DELETE FROM betterrelocation WHERE id = "+id);
							statement.executeUpdate("COMMIT");
							instance.blockUnlock(block);
						} catch (Exception e) {
							try {
								statement.executeUpdate("ROLLBACK");
							} catch (SQLException e1) {
								e1.printStackTrace();
							}
							e.printStackTrace();
							player.sendMessage(ChatColor.RED+BetterRelocation.chatPrefix+"An error occurred while retrieving the block!");
							block.setType(Material.AIR);
						}
					}
				}.runTask(instance);
			} else {
				player.sendMessage(ChatColor.RED+BetterRelocation.chatPrefix+"No available blocks!");
				instance.blockUnlock(block);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			block.setType(Material.AIR);
		}
	}

}