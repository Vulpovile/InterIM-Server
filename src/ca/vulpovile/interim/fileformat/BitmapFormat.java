package ca.vulpovile.interim.fileformat;


import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;

import javax.swing.JOptionPane;

//From assignments
public class BitmapFormat {
	static HashMap<String, Integer> bmpHeader = new HashMap<String, Integer>();
	public static short[][][] readBMP(File bmpFile) {
		//Create the input stream variables
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream is = null;
		try {
			//Create the input streams
			fis = new FileInputStream(bmpFile);
			//Create a buffered stream to read faster
			bis = new BufferedInputStream(fis);
			is = new DataInputStream(bis);
			//Parse the header data
			parseHeader(is);
			//Check if the header is correct for the bitmap
			if(!new String(intToBytes(bmpHeader.get("FileType"))).equals("BM"))
			{
				//Something is wrong with the bitmap, show an error
				JOptionPane.showMessageDialog(null, "The selected file has a malformed header", "Error", JOptionPane.ERROR_MESSAGE);
			}
			else
			{
				//Start reading the bitmap
				System.out.println("This is a good bitmap!");		
				//is.read(new byte[Integer.reverseBytes(bmpHeader.get("PixelDataOffset"))-(14+40)]); //Remove Offset
				//Get the width
				int width = bmpHeader.get("ImageWidth");
				//Get the height
				int height = bmpHeader.get("ImageHeight");
				//Some files have some padding bytes after each line of pixels, this calculates their size so they can be removed
				int paddedBytes = bmpHeader.get("ImageSize") - width*height*3;
				paddedBytes /= height;
				//Short because there are no unsigned values in Java
				short[][][] rgbbytes = new short[height][width][3];
				//For each pixel in the bitmap
				for(int i = 0; i < height; i++)
				{
					for(int j = 0; j < width; j++)
					{
						//Read backwards because bitmaps are BGR
						for(int k = 2; k >= 0; k--)
						{
							rgbbytes[i][j][k] = is.readByte();
							rgbbytes[i][j][k] &= 0xff;
						}
					}
					for(int pad = 0; pad < paddedBytes; pad++)
					{
						is.readByte(); //discard padding
					}
				}
				return rgbbytes;
				
			}
		} catch (FileNotFoundException e) {
			//File should not reach a file not found error, either you don't have the permission to open this file
			//or the file is locked some other way
			JOptionPane.showMessageDialog(null, "The selected file is invald", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} catch (IOException e) {
			//Show an error
			JOptionPane.showMessageDialog(null, "The selected file could not be read", "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		finally
		{
			//Close input streams where possible
			if(is != null)
			{
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(bis != null)
			{
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(fis != null)
			{
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			//Clear the input streams
			fis = null;
			bis = null;
			is = null;
		}
		return null;
	}
	private static byte[] intToBytes(Integer integer) {
		// TODO Auto-generated method stub
		return BigInteger.valueOf(integer).toByteArray();
	}
	private static void parseHeader(DataInputStream is) throws IOException {
		bmpHeader.put("FileType", (int) is.readShort());
		bmpHeader.put("FileSize", is.readInt());
		bmpHeader.put("Reserved1", (int) is.readShort());
		bmpHeader.put("Reserved2", (int) is.readShort());
		bmpHeader.put("PixelDataOffset", is.readInt());
		bmpHeader.put("HeaderSize", is.readInt());
		//Reverse the bytes to flip the endian
		bmpHeader.put("ImageWidth", Integer.reverseBytes(is.readInt()));
		bmpHeader.put("ImageHeight", Integer.reverseBytes(is.readInt()));
		bmpHeader.put("Planes", (int) is.readShort());
		bmpHeader.put("BitsPerPixel", (int) is.readShort());
		bmpHeader.put("Compression", is.readInt());
		//Reverse the bytes to flip the endian
		bmpHeader.put("ImageSize", Integer.reverseBytes(is.readInt()));
		bmpHeader.put("XpixelsPerMeter", is.readInt());
		bmpHeader.put("YpixelsPerMeter", is.readInt());
		bmpHeader.put("TotalColours", is.readInt());
		bmpHeader.put("ImportantColours", is.readInt());
	}
}
