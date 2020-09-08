package ca.vulpovile.interim.fileformat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ca.vulpovile.interim.fileformat.WavData;

public class WavData {
	public final int sampleRate;
	public final byte[] samples;
	public final String fileName;
	public WavData(int sampleRate, short[] samples, String fileName)
	{
		this.sampleRate = sampleRate;
		this.fileName = fileName;
		this.samples = new byte[samples.length * 2];
		ByteBuffer.wrap(this.samples).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(samples);
	}
	
	public WavData(int sampleRate, byte[] samples, String fileName)
	{
		this.sampleRate = sampleRate;
		this.fileName = fileName;
		this.samples = samples;
	}
	
	public byte[] serializeDetails() throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		dos.writeInt(sampleRate);
		byte[] bytes = fileName.getBytes();
		dos.writeInt(bytes.length);
		dos.write(bytes);
		dos.close();
		baos.close();
		return baos.toByteArray();
	}
	
	public static WavData unSerializeDetails(byte[] samples, byte[] data) throws IOException
	{
		ByteArrayInputStream baos = new ByteArrayInputStream(data);
		DataInputStream dos = new DataInputStream(baos);
		int sampleRate = dos.readInt();
		byte[] bytes = new byte[dos.readInt()];
		dos.read(bytes);
		String fileName = new String(bytes);
		dos.close();
		baos.close();
		return new WavData(sampleRate, samples, fileName);
	}
}
