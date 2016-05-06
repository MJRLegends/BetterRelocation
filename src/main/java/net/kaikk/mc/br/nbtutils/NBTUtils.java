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

import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Block;

public abstract class NBTUtils {
	public static NBTUtils getNBT() {
		try {
			Class.forName("net.minecraft.server.v1_7_R4.NBTBase");
			return new NBTUtils17R4();
		} catch (ClassNotFoundException e) { }
		
		try {
			Class.forName("net.minecraft.server.v1_8_R3.NBTBase");
			return new NBTUtils18R3();
		} catch (ClassNotFoundException e) { }
		
		return null;
	}
	
	public abstract String toString(Block block) throws Exception;
	public abstract void toBlock(Block block, String json) throws Exception;
	
	/**
     * Byte arrays are stored as {{@value #BYTE_ARRAY}: [0,1,3,etc.]}, ints
     * simply as [0,1,3,etc]. Storing byte arrays this way preserves their
     * type. So when reading a map, check for this value to see whether you
     * have a byte[] or a compound tag.
     */
	protected static final String BYTE_ARRAY = "byteArray";

    /**
     * Boxes all the values of the array for consumption by JSONSimple.
     * 
     * @param byteArray
     *            Array to box.
     * @return The boxed array.
     */
	protected static final List<Byte> boxBytes(byte[] byteArray) {
        List<Byte> byteList = new ArrayList<Byte>(byteArray.length);
        for (byte aByte : byteArray) {
            byteList.add(aByte); // Wraps
        }
        return byteList;
    }

    /**
     * Boxes all the values of the array for consumption by JSONSimple.
     * 
     * @param intArray
     *            Array to box.
     * @return The boxed array.
     */
    protected static final List<Integer> boxIntegers(int[] intArray) {
        List<Integer> integerList = new ArrayList<Integer>(intArray.length);
        for (int anInt : intArray) {
            integerList.add(anInt); // Wraps
        }
        return integerList;
    }


    /**
     * Converts from a List<Number>, as found in the JSON, to byte[].
     *
     * @param boxed
     *            List from the JSON. return The byte array.
     * @return The unboxed bytes.
     */
    protected static final byte[] unboxBytes(List<Number> boxed) {
        byte[] bytes = new byte[boxed.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = boxed.get(i).byteValue();
        }
        return bytes;
    }

    /**
     * Converts from a List<Number>, as found in the JSON, to int[].
     *
     * @param boxed
     *            List from the JSON. return The int array.
     * @return The unboxed ints.
     */
    protected static final int[] unboxIntegers(List<Number> boxed) {
        int[] ints = new int[boxed.size()];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = boxed.get(i).intValue();
        }
        return ints;
    }
    
	/**
	 * Constants for some NBT tag types.
	 */
    protected static class TagType {
    	protected static final int COMPOUND = 10;
    	protected static final int DOUBLE = 6;
    	protected static final int FLOAT = 5;
    	protected static final int INT_ARRAY = 11;
    	protected static final int STRING = 8;
	}
}
