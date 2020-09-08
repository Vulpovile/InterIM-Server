package ca.vulpovile.interim.fileformat;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;

import ca.vulpovile.interim.fileformat.WavData;

public class WavFormat {
	static HashMap<String, Integer> wavHeader = new HashMap<String, Integer>();
	public static WavData readWAV(File wavFile) {
		//Create input stream variables
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream is = null;
		try {
			//Create the input streams
			fis = new FileInputStream(wavFile);
			//Create a buffered stream to read faster
			bis = new BufferedInputStream(fis);
			is = new DataInputStream(bis);
			//Parse the header data
			parseHeader(is);
			//Check if the WAV file is RIFF format
			if(new String(intToBytes(wavHeader.get("ChunkID"))).equalsIgnoreCase("RIFF")
					&& new String(intToBytes(wavHeader.get("Format"))).equalsIgnoreCase("WAVE")
					&& new String(intToBytes(wavHeader.get("SubChunk1ID"))).equalsIgnoreCase("fmt ")
					&& wavHeader.get("AudioFormat").shortValue() == 1
					&& new String(intToBytes(wavHeader.get("SubChunk2ID"))).equalsIgnoreCase("DATA")){
				System.out.println("This is a good wav!");
				//Reverse endian in order to match JVM and get information from the header
				short bitsPerSample = wavHeader.get("BitsPerSample").shortValue();
				//Get samples from other known values
				int samples = (wavHeader.get("SubChunk2Size").intValue() * 8) / (wavHeader.get("NumChannels").shortValue() * bitsPerSample);
				//Create an array to store sample data
				short[] sampleData = new short[samples];
				boolean cango = true;
				for(int currSample = 0; currSample < samples; currSample++)
				{
					switch(bitsPerSample){
						case 8:
							//If the bit depth is 8 bit, store
							sampleData[currSample] = is.readByte();
							break;
						case 16:
							//If the bit depth is 16 bit, reverse the endian and store
							sampleData[currSample] = Short.reverseBytes(is.readShort());
							break;
						default:
							//If the bit depth is not supported, cancel
							cango = false;
							break;
							
					}
				}
				if(cango)
				{
					//If there is no issue, start drawing the data
					return new WavData(wavHeader.get("SampleRate"), sampleData, wavFile.getName());
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally
		{
			//Close all streams if possible
			if(is != null)
			{
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(bis != null)
			{
				try {
					bis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(fis != null)
			{
				try {
					fis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//Nullify the streams
			fis = null;
			bis = null;
			is = null;
			System.out.println("All streams are now closed.");
		}
		return null;
	}
	private static byte[] intToBytes(Integer integer) {
		// TODO Auto-generated method stub
		return BigInteger.valueOf(integer).toByteArray();
	}
	//Parse the header
	private static void parseHeader(DataInputStream data) throws IOException {
		wavHeader.put("ChunkID", data.readInt());
		wavHeader.put("ChunkSize", Integer.reverseBytes(data.readInt()));
		wavHeader.put("Format", data.readInt());
		wavHeader.put("SubChunk1ID", data.readInt());
		wavHeader.put("SubChunk1Size", Integer.reverseBytes(data.readInt()));
		wavHeader.put("AudioFormat", (int) Short.reverseBytes(data.readShort()));
		wavHeader.put("NumChannels", (int) Short.reverseBytes(data.readShort()));
		wavHeader.put("SampleRate", Integer.reverseBytes(data.readInt()));
		wavHeader.put("ByteRate", Integer.reverseBytes(data.readInt()));
		wavHeader.put("BlockAlign", (int) Short.reverseBytes(data.readShort()));
		wavHeader.put("BitsPerSample", (int) Short.reverseBytes(data.readShort()));
		wavHeader.put("SubChunk2ID", data.readInt());
		wavHeader.put("SubChunk2Size", Integer.reverseBytes(data.readInt()));
	}
}
