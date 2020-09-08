package ca.vulpovile.interim.compression;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import ca.vulpovile.interim.compression.Node;


public class Node implements Comparable<Node> {
	public Node left = null;
	public Node right = null;
	public int freq;
	public Byte value;
	
	public void serializeLeafOnly(DataOutputStream dos) throws IOException
	{
		dos.writeBoolean(isTrueLeaf());
		if(isTrueLeaf())
		{
			dos.writeByte(value);
		}
		else
		{
			left.serializeLeafOnly(dos);
			right.serializeLeafOnly(dos);
		}
	}
	
	public void deserializeLeafOnly(DataInputStream dis) throws IOException
	{
		
		if(dis.readBoolean())
		{
			value = dis.readByte();
		}
		else
		{
			left = new Node();
			left.deserializeLeafOnly(dis);
			right = new Node();
			right.deserializeLeafOnly(dis);
		}
	}

	// Constructor, store the value and frequency into the node. Nodes with NULL
	// value are just parent nodes.
	public Node(Byte value, Integer frequency) {
		this.value = value;
		this.freq = frequency;
	}
	public Node() {
	}

	public boolean isLeaf() {
		// Check if the node is a leaf by checking if either of them are null.
		// If one is, the other should be to.
		// Simple check could also be "value != null"
		return left == null || right == null;
	}

	public boolean isTrueLeaf() {
		// Realistic check for leaf node
		return left == null && right == null;
	}

	public int compareTo(Node o) {
		// TODO Auto-generated method stub
		// Comparison function for Comparable<Node>, just return the "size" of
		// the value compared to the other.
		return freq - o.freq;
	}
}
