package net.kaikk.mc.br;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

class EventListener implements Listener {
	private BetterRelocation instance;
	

	EventListener(BetterRelocation instance) {
		this.instance = instance;
	}
	
	@EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
	void onInteract(PlayerInteractEvent event) {
		if (event.getAction()!=Action.RIGHT_CLICK_BLOCK || event.getPlayer().getItemInHand()==null || event.getPlayer().getItemInHand().getType()!=Material.FEATHER) {
			return;
		}
		
		Player player = event.getPlayer();
		
		// ignore fake players
		if (Utils.isFakePlayer(player)) {
			return;
		}
		
		Block block = event.getClickedBlock();
		// ignore doors, trapdoors
		if (block.getType()==Material.WOOD_DOOR || block.getType()==Material.WOODEN_DOOR || block.getType()==Material.TRAP_DOOR) {
			return;
		}
		
		// check permissions
		if (!player.hasPermission("br."+(player.isSneaking() ? "store" : "retrieve"))) {
			player.sendMessage(ChatColor.RED+BetterRelocation.chatPrefix+"Permission denied");
			return;
		}
		
		// when retrieving a block, it's like you are placing a chest, so use the relative block
		if (!player.isSneaking()) { 
			block = event.getClickedBlock().getRelative(event.getBlockFace());
		}
		
		// check build permissions
		BlockBreakEvent bbevent = new BlockBreakEvent(block, player);
		Bukkit.getPluginManager().callEvent(bbevent);
		if (bbevent.isCancelled()) {
			player.sendMessage(ChatColor.RED+BetterRelocation.chatPrefix+"Permission denied");
			return;
		}
		
		if (player.isSneaking()) {
			// store block
			try {
				if (block==null) {
					throw new NullPointerException("The block is null");
				} else if (!instance.config.allowedBlocks.contains(block.getType())) {
					throw new IllegalArgumentException("The block is not allowed.");
				}
				
				// no double chests
				if (Utils.doubleChestCheck(block)) {
					throw new Exception("Double chests are not allowed.");
				}
				
				
				if (block.getState() instanceof InventoryHolder) {
					InventoryHolder inventoryHolder = (InventoryHolder) block.getState();
					Inventory inventory = inventoryHolder.getInventory();
					if (inventory!=null) {
						// be sure no one is looking into this inventory
						if (!inventory.getViewers().isEmpty()) {
							throw new IllegalStateException("The block is in use.");
						}
						
						// do not store empty inventories
						if (Utils.isEmpty(inventory.getContents())) {
							throw new IllegalStateException("The inventory is empty.");
						}
					}
				}
				
				// disallow any other BetterRelocation operations on this block until the store is completed
				instance.blockLock(block);
				
				// call event
				StoreEvent storeEvent = new StoreEvent(player, block);
				Bukkit.getPluginManager().callEvent(storeEvent);
				if (storeEvent.isCancelled()) {
					throw new Exception("An event has cancelled this operation");
				}

				final String json;
				try {
					json = instance.nbt.toString(block);
				} catch (Exception e1) {
					e1.printStackTrace();
					throw new Exception("An error occurred while retrieving NBT data for this block");
				}
				
				// asynchronously store the block
				player.sendMessage(ChatColor.GREEN+BetterRelocation.chatPrefix+"Please wait while we process your request...");
				instance.executor.submit(new StoreBlockTask(instance, player, block, json));
			} catch (Exception e) {
				player.sendMessage(ChatColor.RED+BetterRelocation.chatPrefix+e.getMessage());
			}
			event.setCancelled(true);
		} else {
			// retrieve block
			try {
				// check per server permissions
				StringBuilder playerPerms = new StringBuilder();
				for (String srv : Utils.playerPerms(player)) {
					playerPerms.append("\""+srv+"\",");
				}
				if (playerPerms.length()==0) {
					player.sendMessage(ChatColor.RED+BetterRelocation.chatPrefix+"You don't have permissions to retrieve blocks!");
					return;
				}
				playerPerms.setLength(playerPerms.length()-1);
				
				// disallow any other BetterRelocation operations on this block until the retrieve is completed
				instance.blockLock(block);
				
				// call event
				RetrieveEvent storeEvent = new RetrieveEvent(player, block);
				Bukkit.getPluginManager().callEvent(storeEvent);
				if (storeEvent.isCancelled()) {
					throw new Exception("An event has cancelled this operation");
				}
				
				if (Utils.doubleChestCheck(block)) {
					throw new Exception("You can't place this chest near other vanilla chests.");
				}

				// asynchronously retrieve the block
				player.sendMessage(ChatColor.GREEN+BetterRelocation.chatPrefix+"Please wait while we process your request...");
				instance.executor.submit(new RetrieveBlockTask(instance, player, block, playerPerms.toString()));
			} catch (Exception e) {
				player.sendMessage(ChatColor.RED+BetterRelocation.chatPrefix+e.getMessage());
			}
			
			if (block.getState() instanceof Chest) {
				event.setCancelled(true);
			}
		}
	}
}

