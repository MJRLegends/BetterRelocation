package net.kaikk.mc.br;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class Utils {
	public static List<String> playerPerms(Player player) {
		List<String> perms = new ArrayList<String>();
		for (PermissionAttachmentInfo i : player.getEffectivePermissions()) {
			if (i.getPermission().startsWith("br.srv.")) {
				perms.add(i.getPermission().substring(7));
			}			
		}
		return perms;
	}

	public static boolean isDoubleChest(Block block) {
		Material chestType = block.getType();
		if (chestType != Material.CHEST && chestType != Material.TRAPPED_CHEST) {
			return false;
		}
		
		Block sb = block.getRelative(BlockFace.EAST);
		if (sb.getType()==chestType) {
			return true;
		}
		
		sb = block.getRelative(BlockFace.WEST);
		if (sb.getType()==chestType) {
			return true;
		}
		
		sb = block.getRelative(BlockFace.NORTH);
		if (sb.getType()==chestType) {
			return true;
		}
		
		sb = block.getRelative(BlockFace.SOUTH);
		if (sb.getType()==chestType) {
			return true;
		}
	
		return false;
	}


	public static boolean isVanillaChestNear(Block block) {
		Block sb = block.getRelative(BlockFace.EAST);
		if (sb.getType()==Material.CHEST || sb.getType()==Material.TRAPPED_CHEST) {
			return true;
		}
		
		sb = block.getRelative(BlockFace.WEST);
		if (sb.getType()==Material.CHEST || sb.getType()==Material.TRAPPED_CHEST) {
			return true;
		}
		
		sb = block.getRelative(BlockFace.NORTH);
		if (sb.getType()==Material.CHEST || sb.getType()==Material.TRAPPED_CHEST) {
			return true;
		}
		
		sb = block.getRelative(BlockFace.SOUTH);
		if (sb.getType()==Material.CHEST || sb.getType()==Material.TRAPPED_CHEST) {
			return true;
		}
	
		return false;
	}

	final static Pattern idStartTag = Pattern.compile("[,\\[\\{]\"[iI][dD]\":[^\"]");
	final static Pattern idEndTag = Pattern.compile("[\\.,\\}]");
	
	public static UUID toUUID(byte[] bytes) {
	    if (bytes.length != 16) {
	        throw new IllegalArgumentException();
	    }
	    int i = 0;
	    long msl = 0;
	    for (; i < 8; i++) {
	        msl = (msl << 8) | (bytes[i] & 0xFF);
	    }
	    long lsl = 0;
	    for (; i < 16; i++) {
	        lsl = (lsl << 8) | (bytes[i] & 0xFF);
	    }
	    return new UUID(msl, lsl);
	}
	
	public static String UUIDtoHexString(UUID uuid) {
		if (uuid==null) return "0x0";
		return "0x"+org.apache.commons.lang.StringUtils.leftPad(Long.toHexString(uuid.getMostSignificantBits()), 16, "0")+org.apache.commons.lang.StringUtils.leftPad(Long.toHexString(uuid.getLeastSignificantBits()), 16, "0");
	}
	
	public static byte[] UUIDtoByteArray(UUID uuid) {
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		return bb.array();
	}
	
	public static boolean isFakePlayer(Player player) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if(player==p) {
				return false;
			}
		}
		return true;
	}
	
	public static int epoch() {
		return (int) (System.currentTimeMillis()/1000);
	}
	

	@SuppressWarnings("deprecation")
	public static String nbtReplaceBukkitNamesWithIds(String json) {
		Matcher m;
		Material material;
		int offset = 0, idStart, idEnd;
		
		StringBuilder sb = new StringBuilder();
		
		for(;;) {
			m = idStartTag.matcher(json.substring(offset));
			
			if (m.find()) {
				idStart = m.start()+6;
				m = idEndTag.matcher(json.substring(offset+idStart));
				if (m.find()) {
					sb.append(json.substring(offset, offset+idStart));
					
					idEnd = m.start();
					String f = json.substring(offset+idStart, offset+idStart+idEnd);
					material = Material.getMaterial(f);
					if (material!=null) {
						sb.append(material.getId());						
					} else {
						sb.append(0);
						Bukkit.getLogger().warning("Missing material: "+f);
					}
					
					
					offset += idStart+idEnd;
				} else {
					break;
				}
			} else {
				break;
			}
		}
		
		return sb.append(json.substring(offset)).toString();
	}
	

	@SuppressWarnings("deprecation")
	public static String nbtReplaceIdsWithBukkitNames(String json) {
		Matcher m;
		Material material;
		int offset = 0, idStart, idEnd;
		
		StringBuilder sb = new StringBuilder();
		
		for(;;) {
			m = idStartTag.matcher(json.substring(offset));
			
			if (m.find()) {
				idStart = m.start()+6;
				m = idEndTag.matcher(json.substring(offset+idStart));
				if (m.find()) {
					sb.append(json.substring(offset, offset+idStart));
					
					idEnd = m.start();
					
					try {
						material = Material.getMaterial(Integer.valueOf(json.substring(offset+idStart, offset+idStart+idEnd)));
						sb.append(material!=null ? material.toString() : "AIR");
					} catch (NumberFormatException e) {
						throw new RuntimeException(e);
					}
					
					offset += idStart+idEnd;
				} else {
					break;
				}
			} else {
				break;
			}
		}
		
		return sb.append(json.substring(offset)).toString();
	}
	
	public static String locationToString(Location location) {
		return "[" + location.getWorld().getName() + ", " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + "]";
	}
	
	public static String timeToString(int seconds) {
		List<String> strs = new ArrayList<String>();

		if (seconds<0) {
			seconds*=-1;
		}

		// seconds
		int secs = seconds % 60;
		if (secs!=0||seconds==0) {
			strs.add(secs+" second"+(secs!=1?"s":""));
		}
		if (seconds<60) {
			return mergeTimeStrings(strs);
		}

		// minutes
		int tmins = (seconds-secs) / 60;
		int mins = tmins % 60;
		if (mins!=0) {
			strs.add(mins+" minute"+(mins!=1?"s":""));
		}
		if (tmins<60) {
			return mergeTimeStrings(strs);
		}

		// hours
		int thours = (tmins-mins) / 60;
		int hours = thours % 24;
		if (hours!=0) {
			strs.add(hours+" hour"+(hours!=1?"s":""));
		}
		if (thours<24) {
			return mergeTimeStrings(strs);
		}

		// days
		int tdays = (thours-hours) / 24;
		if (tdays!=0) {
			strs.add(tdays+" day"+(tdays!=1?"s":""));
		}

		return mergeTimeStrings(strs);
	}
	
	private static String mergeTimeStrings(List<String> strs) {
		StringBuilder sb = new StringBuilder();
		for (int i = strs.size()-1; i >=0; i--) {
			sb.append(strs.get(i));
			sb.append(' ');
		}
		
		int lastChar = sb.length()-1;
		if (lastChar>=0) {
			sb.deleteCharAt(lastChar);
		}
		
		return sb.toString();
	}
	
	public static boolean isEmpty(ItemStack[] inventory) {
		for (ItemStack is : inventory) {
			if (is!=null && is.getType()!=Material.AIR) {
				return false;
			}
		}
		
		return true;
	}
}
