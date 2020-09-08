package ca.vulpovile.interim.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import ca.vulpovile.interim.compression.HuffmanCompressor;

public class LZWCompressor {
	
	public static boolean USE_HUFFMAN = false;
	//Check if a dictionary contains a byte
	public static boolean dictContains(List<byte[]> dict, byte[] vals)
	{
		//For each entry
		for(byte[] entry : dict)
		{
			//If equal then it contains the value
			if(Arrays.equals(vals, entry))
				return true;
		}
		return false;
	}
	
	//Look for the index
	public static int dictIndex(List<byte[]> dict, byte[] vals)
	{
		//For each entry
		for(int i = 0; i < dict.size(); i++)
		{
			//If found, return index
			if(Arrays.equals(vals, dict.get(i)))
				return i;
		}
		return -1;
	}
	
	
	//Create a new array and append to it
	public static byte[] appendArray(byte[] s, byte val)
	{
		//Create array + 1
		byte[] arr = new byte[s.length+1];
		//Add each item
		for(int i = 0; i < s.length; i++)
			arr[i] = s[i];
		arr[s.length] = val;
		return arr;
	}

	public static byte[] compress(byte[] compressable)
	{
		if(USE_HUFFMAN)
			return HuffmanCompressor.compress(compressable);
		try{
			if(compressable.length < 1)
				return new byte[0];
			ArrayList<byte[]> dictionary = new ArrayList<byte[]>();
			List<Integer> code = new LinkedList<Integer>();
			for(byte i = Byte.MIN_VALUE; i < Byte.MAX_VALUE; i++)
				dictionary.add(new byte[]{i});
			//Storage for codes
			//get the first symbol
			byte[] s = new byte[]{compressable[0]};
			//For each symbol
			for(int i = 1; i < compressable.length; i++)
			{
				//Get the next symbol
				byte c = compressable[i];
				//s + c
				byte[] appended = appendArray(s,c);
				//if s+c is in the dictionary, then s = s + c
				if(dictContains(dictionary, appended))
					s = appended;
				else
				{
					//add the index of s to the output
					code.add(dictIndex(dictionary, s));
					//add s+c to the dictionary
					dictionary.add(appendArray(s,c));
					//set s = c
					s = new byte[]{c};
				}
			}
			code.add(dictIndex(dictionary, s));
			for(byte i = Byte.MIN_VALUE; i < Byte.MAX_VALUE; i++)
				dictionary.remove(0);
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeInt(dictionary.size());
			for(int i = 0; i < dictionary.size(); i++)
			{
				dos.writeInt(dictionary.get(i).length);
				dos.write(dictionary.get(i));
			}
			System.out.println(compressable.length);
			System.out.println(code.size());
			System.out.println(dictionary.size());
			dos.writeInt(code.size());
			for(int i = 0; i < code.size(); i++)
				dos.writeInt(code.get(i));
			dos.close();
			baos.close();
			byte[] result = baos.toByteArray();
			System.out.println("Size after LZW Compression : " + ((float)result.length)/(float)compressable.length + "x the size");
			return result;
		} catch (IOException ex){
			ex.printStackTrace();
			return new byte[0];
		}
	}
	
	public static byte[] decompress(byte[] compressed)
	{
		if(USE_HUFFMAN)
			return HuffmanCompressor.decompress(compressed);
		try{
			if(compressed.length < 1)
				return new byte[0];
			ArrayList<byte[]> dictionary = new ArrayList<byte[]>();
			for(byte i = Byte.MIN_VALUE; i < Byte.MAX_VALUE; i++)
				dictionary.add(new byte[]{i});
			ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
			DataInputStream dis = new DataInputStream(bais);
			int size = dis.readInt();
			for(int i = 0; i < size; i++)
			{
				byte[] arr = new byte[dis.readInt()];
				dis.read(arr);
				dictionary.add(arr);
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			size = dis.readInt();
			for(int i = 0; i < size; i++)
			{
				baos.write(dictionary.get(dis.readInt()));
			}
			dis.close();
			bais.close();
			baos.close();
		return baos.toByteArray();
		}catch (IOException ex){
			ex.printStackTrace();
			return new byte[0];
		}

	}
	/*
	public static void printDictionary(ArrayList<byte[]> dict)
	{
		for(byte[] barr : dict)
		{
			for(byte val : barr)
			{
				System.out.print(val + "  ");	
			}
			System.out.println();
		}
	}*/
}
