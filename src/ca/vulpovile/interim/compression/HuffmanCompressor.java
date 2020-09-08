package ca.vulpovile.interim.compression;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import ca.vulpovile.interim.compression.Node;

public class HuffmanCompressor {
	

	public static byte[] compress(byte[] data) {
		try{
			HashMap<Byte, Integer> map = getMap(data);
			//Create a list of values
			ArrayList<Byte> vals = new ArrayList<Byte>();
			//Create a list of frequencies
			ArrayList<Integer> freqs = new ArrayList<Integer>();
			//For each item in the map, add vales and frequencies to their respective lists
			Iterator<Byte> it = map.keySet().iterator();
			while(it.hasNext())
			{
				Byte key = it.next();
				vals.add(key);
				freqs.add(map.get(key));
			}
			it = null;
			//Make a huffman tree
			Node tree = makeHuffmanTree(vals, freqs);
			//Create a map from the tree
			HashMap<Byte, String> huffmanMap = new HashMap<Byte, String>();
			huffmanMap = recursiveCount(tree, "", huffmanMap);
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeInt(data.length);
			tree.serializeLeafOnly(dos);
			
			int length = 0;
			for(int i = 0; i < data.length; i++)
			{
				String path = huffmanMap.get(data[i]);
				length += path.length();
			}
			byte[] arr = new byte[length/8 + (length % 8 == 0 ? 0 : 1)];
			long bit = 0;
			for(int i = 0; i < data.length; i++)
			{
				String path = huffmanMap.get(data[i]);
				for(int j = 0; j < path.length(); j++)
				{
					int offset = (int) (bit % 8);
					int index = (int) (bit / 8);
					if(offset == 0)
						arr[index] = 0;
					if(path.charAt(j) == '1')
						arr[index] |= (1 << offset);
					bit++;
				}
			}
			dos.write(arr);
			dos.close();
			baos.close();
			byte[] result = baos.toByteArray();
			System.out.println("Size after Huffman Compression : " + ((float)result.length)/(float)data.length + "x the size");
			return result;
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
		return new byte[0];
	}

	public static byte[] decompress(byte[] data) {
		try{
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			DataInputStream dis = new DataInputStream(bais);
			byte uncompressed[] = new byte[dis.readInt()];
			Node tree = new Node();
			tree.deserializeLeafOnly(dis);
			long bit = 0;
			byte currByte = 0;
			for(int i = 0; i < uncompressed.length; i++)
			{
				Node currNode = tree;
				while(!currNode.isLeaf())
				{
					int offset = (int) (bit % 8);
					if(offset == 0)
					{
						currByte = dis.readByte();
					}
					int currBit = (currByte & (1 << offset));
					if(currBit != 0)
						currNode = currNode.right;
					else
						currNode = currNode.left;
					bit++;
				}
				uncompressed[i] = currNode.value;
			}
			dis.close();
			bais.close();
			return uncompressed;
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
		return new byte[0];
	}

	// Create a map of values and their frequencies
	public static HashMap<Byte, Integer> getMap(byte[] symbols) {
		HashMap<Byte, Integer> map = new HashMap<Byte, Integer>();
		// For each value, size determined by numChar (1 or 2)
		for (int i = 0; i < symbols.length; i++) {
			// Get the value from the input string
			Byte value = symbols[i];
			// If the value does not exist in the map create it and set it to 1,
			// otherwise add 1 to it's frequency.
			if (map.get(value) == null)
				map.put(value, 1);
			else
				map.put(value, map.get(value) + 1);
		}
		// Return the map
		return map;
	}

	public static HashMap<Byte, String> recursiveCount(Node tree, String path, HashMap<Byte, String> map) {
		// If the tree is a leaf node, then you have reached the bottom and
		// learned it's path
		if (tree.isLeaf()) {
			// Put it's path into the map of paths
			if (path.length() == 0)
				path = "0";
			map.put(tree.value, path);

		} else {
			// Otherwise, recursively traverse the left and right nodes, adding
			// 0 and 1 to their paths respectivley
			recursiveCount(tree.left, path + "0", map);
			recursiveCount(tree.right, path + "1", map);
		}
		return map;
	}

	public static Node makeHuffmanTree(ArrayList<Byte> vals, ArrayList<Integer> freq) {
		// Based on the methods from Slide 28 of lossless compression
		// First, make a list of nodes. A queue could've been used, but the list
		// had to be sorted
		ArrayList<Node> nodes = new ArrayList<Node>();
		for (int i = 0; i < vals.size(); i++)
			nodes.add(new Node(vals.get(i), freq.get(i)));
		Collections.sort(nodes);// Sort the nodes
		while (nodes.size() > 1) {
			Node left = nodes.remove(0); // Get the first node
			Node right = nodes.remove(0); // Get the second node
			// Create a parent node with their combined frequencies
			Node parent = new Node(null, left.freq + right.freq);
			// Add the nodes to the parent node
			parent.left = left;
			parent.right = right;
			// Add the parent to the list
			nodes.add(parent);
			// Sort the list again after adding the parent node
			Collections.sort(nodes);
		}
		// Return the Huffman tree
		return nodes.get(0);
	}

}
