/*
	Part of the code of this class has been taken from https://github.com/rutgerkok/BetterEnderChest/blob/7e0c400c3581ba4dbe9c6f64d4f835345e567b76/src/main/java/nl/rutgerkok/betterenderchest/nms/SimpleNMSHandler.java
	Original license:
	Copyright (c) 2013, Rutger Kok
	All rights reserved.
	Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
	 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
	 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. Neither the name of the owner nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/


package net.kaikk.mc.br.nbtutils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.v1_7_R4.NBTBase;
import net.minecraft.server.v1_7_R4.NBTNumber;
import net.minecraft.server.v1_7_R4.NBTTagByteArray;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.NBTTagDouble;
import net.minecraft.server.v1_7_R4.NBTTagInt;
import net.minecraft.server.v1_7_R4.NBTTagIntArray;
import net.minecraft.server.v1_7_R4.NBTTagList;
import net.minecraft.server.v1_7_R4.NBTTagLong;
import net.minecraft.server.v1_7_R4.NBTTagString;

public class NBTUtils17R4 extends NBTUtils {
	@SuppressWarnings("deprecation")
	public String toString(Block block) throws Exception {
		// Get block info
		NBTTagCompound baseTag = new NBTTagCompound();
		//NBTTagCompound blockTag = toNBT(block.getState().getData().toItemStack());
		NBTTagCompound blockTag = new NBTTagCompound();
		blockTag.setInt("id", block.getState().getTypeId());
		blockTag.setByte("data", block.getState().getRawData());
		
		// Get inventory from the block
		if (block.getState() instanceof InventoryHolder) {
			NBTTagList listTag = new NBTTagList();
			Inventory inventory = ((InventoryHolder) block.getState()).getInventory();
			for (int i=0; i<inventory.getSize(); i++) {
				ItemStack is = inventory.getItem(i);
				if (is!=null) {
					NBTTagCompound item = toNBT(is);
					item.setInt("slotpos", i);
					listTag.add(item);
				}
			}
			// set inventory data
			baseTag.set("inventory", listTag);
		}
		
		// set block data
		baseTag.set("block", blockTag);

		Map<String, Object> map = toMap(baseTag);
		return JSONObject.toJSONString(map);
	}

	
	@SuppressWarnings("deprecation")
	public void toBlock(Block block, String json) throws Exception {
		NBTTagCompound baseTag = toTag(json);
		NBTTagCompound blockTag = baseTag.getCompound("block");
		
		block.setTypeIdAndData(blockTag.getInt("id"), blockTag.getByte("data"), false);
		
		try {
			if (baseTag.hasKey("inventory") && block.getState() instanceof InventoryHolder) {
				NBTTagList inventoryTag = baseTag.getList("inventory", TagType.COMPOUND);
				
				Inventory inventory = ((InventoryHolder) block.getState()).getInventory();
				inventory.clear();
				
				for(int i=0; i<inventoryTag.size(); i++) {
					NBTTagCompound itemTag = inventoryTag.get(i);
					int pos = itemTag.getInt("slotpos");
					ItemStack is = toItemStack(itemTag);
					
					inventory.setItem(pos, is);
				}
			}
		} catch (Exception e) {
			block.setTypeIdAndData(0, (byte) 0, false);
			throw e;
		}
	}
	
	public ItemStack toItemStack(NBTTagCompound nbt) {
		return CraftItemStack.asCraftMirror(net.minecraft.server.v1_7_R4.ItemStack.createStack(nbt));
	}
	
	public static NBTTagCompound toNBT(ItemStack itemStack) {
		return CraftItemStack.asNMSCopy(itemStack).save(new NBTTagCompound());
	}
	
	/** not implemented yet */
	@Deprecated
	public static String toString(ItemStack itemStack) {
		return null;
	}
	
	/** not implemented yet */
	@Deprecated
	public static ItemStack toItemStack(String json) {
		return null;
	}
	

    static final NBTBase javaTypeToNBTTag(Object object) throws IOException {
        // Handle compounds
        if (object instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, ?> map = (Map<String, ?>) object;

            Object byteArrayValue = map.get(BYTE_ARRAY);
            if (byteArrayValue instanceof List) {
                // The map is actually a byte array, not a compound tag
                @SuppressWarnings("unchecked")
                List<Number> boxedBytes = (List<Number>) byteArrayValue;
                return new NBTTagByteArray(unboxBytes(boxedBytes));
            }

            NBTTagCompound tag = new NBTTagCompound();
            for (Entry<String, ?> entry : map.entrySet()) {
                NBTBase value = javaTypeToNBTTag(entry.getValue());
                if (value != null) {
                    tag.set(entry.getKey(), value);
                }
            }
            return tag;
        }
        // Handle numbers
        if (object instanceof Number) {
            Number number = (Number) object;
            if (number instanceof Integer || number instanceof Long) {
                // Whole number
                if (number.intValue() == number.longValue()) {
                    // Fits in integer
                    return new NBTTagInt(number.intValue());
                }
                return new NBTTagLong(number.longValue());
            } else {
                return new NBTTagDouble(number.doubleValue());
            }
        }
        // Handle strings
        if (object instanceof String) {
            return new NBTTagString((String) object);
        }
        // Handle lists
        if (object instanceof List) {
            List<?> list = (List<?>) object;
            NBTTagList listTag = new NBTTagList();

            if (list.isEmpty()) {
                // Don't deserialize empty lists - we have no idea what
                // type it should be. The methods on NBTTagCompound will
                // now return empty lists of the appropriate type
                return null;
            }

            // Handle int arrays
            Object firstElement = list.get(0);
            if (firstElement instanceof Integer || firstElement instanceof Long) {
                // Ints may be deserialized as longs, even if the numbers
                // are small enough for ints
                @SuppressWarnings("unchecked")
                List<Number> intList = (List<Number>) list;
                return new NBTTagIntArray(unboxIntegers(intList));
            }

            // Other lists
            for (Object entry : list) {
                NBTBase javaType = javaTypeToNBTTag(entry);
                if (javaType != null) {
                    listTag.add(javaType);
                }
            }
            return listTag;
        }
        if (object == null) {
            return null;
        }
        throw new IOException("Unknown object: (" + object.getClass() + ") " + object + "");
    }

    private static final Object nbtTagToJavaType(NBTBase tag) throws IOException {
        if (tag instanceof NBTTagCompound) {
            return toMap((NBTTagCompound) tag);
        } else if (tag instanceof NBTTagList) {
            // Add all children
            NBTTagList listTag = (NBTTagList) tag;
            List<Object> objects = new ArrayList<Object>();
            for (int i = 0; i < listTag.size(); i++) {
                objects.add(tagInNBTListToJavaType(listTag, i));
            }
            return objects;
        } else if (tag instanceof NBTNumber) {
            // Check for whole or fractional number (we don't care about
            // the difference between int/long or double/float, in JSON
            // they look the same)
            NBTNumber nbtNumber = (NBTNumber) tag;
            if (nbtNumber instanceof NBTTagInt || nbtNumber instanceof NBTTagLong) {
                // Whole number
                return nbtNumber.c();
            } else {
                // Fractional number
                return nbtNumber.g();
            }
        } else if (tag instanceof NBTTagString) {
            String value = ((NBTTagString) tag).a_();
            return value.replace("\"", "'");
        } else if (tag instanceof NBTTagByteArray) {
            // Byte arrays are placed in a map, see comment for BYTE_ARRAY
            return ImmutableMap.of(BYTE_ARRAY, boxBytes(((NBTTagByteArray) tag).c()));
        } else if (tag instanceof NBTTagIntArray) {
            return boxIntegers(((NBTTagIntArray) tag).c());
        }

        throw new IOException("Unknown tag: " + tag);
    }

    /**
     * Converts the object at the specified position in the list to a Map,
     * List, double, float or String.
     * 
     * @param tagList
     *            The list to convert an element from.
     * @param position
     *            The position in the list.
     * @return The converted object.
     * @throws IOException
     *             If the tag type is unknown.
     */
    private static final Object tagInNBTListToJavaType(NBTTagList tagList, int position) throws IOException {
        switch (tagList.d()) {
            case TagType.COMPOUND:
                NBTTagCompound compoundValue = tagList.get(position);
                return nbtTagToJavaType(compoundValue);
            case TagType.INT_ARRAY:
                return boxIntegers(tagList.c(position));
            case TagType.DOUBLE:
                double doubleValue = tagList.d(position);
                return doubleValue;
            case TagType.FLOAT:
                float floatValue = tagList.e(position);
                return floatValue;
            case TagType.STRING:
                String stringValue = tagList.getString(position);
                return stringValue;
        }
        throw new IOException("Unknown list (type " + tagList.getTypeId() + "): " + tagList);
    }

    /**
     * Converts the compound tag to a map. All values in the tag will also
     * have their tags converted to String//primitives/maps/Lists.
     * 
     * @param tagCompound
     *            The compound tag.
     * @return The map.
     * @throws IOException
     *             In case an unknown tag was encountered in the NBT tag.
     */
    @SuppressWarnings("unchecked")
	static final Map<String, Object> toMap(NBTTagCompound tagCompound) throws IOException {
        Collection<String> tagNames = tagCompound.c();

        // Add all children
        Map<String, Object> jsonObject = new HashMap<String, Object>(tagNames.size());
        for (String subTagName : tagNames) {
            NBTBase subTag = tagCompound.get(subTagName);
            jsonObject.put(subTagName, nbtTagToJavaType(subTag));
        }
        return jsonObject;
    }

    /**
     * Turns the given json-formatted string back into a NBTTagCompound.
     * Mojangson formatting is also accepted.
     * 
     * @param jsonString
     *            The json string to parse.
     * @return The parsed json string.
     * @throws IOException
     *             If the string cannot be parsed.
     */
    static final NBTTagCompound toTag(String jsonString) throws IOException {
        try {
            return (NBTTagCompound) javaTypeToNBTTag(new JSONParser().parse(jsonString));
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
