package ca.vulpovile.interim;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

import javax.net.ssl.SSLServerSocketFactory;

import ca.vulpovile.interim.LogFormatter;
import ca.vulpovile.interim.compression.LZWCompressor;
import ca.vulpovile.interim.protocolv1.ServerHandler;

public class Server {

	Properties prop = new Properties();
	File propertyFile = new File("server.cfg");
	public static final Logger logger = Logger.getLogger("InterIM Logger");
	public static boolean isRunning = false;
	public static String ip = "";
	public static int port = 11011;
	public static int timeout = -1;
	public static String registrationTitle = "InterIM Main Server Registration";
	public static File accountsDir = new File("./accounts");
	static ArrayList<ChatGroup> groups = new ArrayList<ChatGroup>();
	static
	{
		groups.add(new ChatGroup("A test", 0));
		groups.add(new ChatGroup("A test 2", 1));
		groups.add(new ChatGroup("A test 3", 2));
		groups.add(new ChatGroup("A test 4", 3));
	}
	public Server()
	{
		if(!accountsDir.isDirectory())
			accountsDir.mkdir();
		ConsoleHandler handler = new ConsoleHandler();
		handler.setFormatter(new LogFormatter());
		logger.setUseParentHandlers(false);
		logger.addHandler(handler);
		BufferedInputStream fis = null;
		try {
			fis = new BufferedInputStream(new FileInputStream(propertyFile));
			prop.load(fis);
		} catch (FileNotFoundException e) {
			logger.warning("Failed to open properties, generating new");
			e.printStackTrace();
		} catch (IOException e) {
			logger.warning("Failed to read properties, generating new");
			e.printStackTrace();
		}
		finally
		{
			if(fis != null)
				try {
					fis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		try{

			ip = prop.getProperty("ip", ip);
			port = Integer.parseInt(prop.getProperty("port", String.valueOf(port)));
			timeout = Integer.parseInt(prop.getProperty("timeout", String.valueOf(timeout)));
			registrationTitle = prop.getProperty("registration-title", String.valueOf(registrationTitle));
			LZWCompressor.USE_HUFFMAN = Boolean.parseBoolean(prop.getProperty("use-huffman", String.valueOf(LZWCompressor.USE_HUFFMAN)));
		}
		catch(NumberFormatException ex)
		{

			logger.warning("Failed to read properties, generating new");
		}
		prop.setProperty("ip", ip);
		prop.setProperty("port", String.valueOf(port));
		prop.setProperty("timeout", String.valueOf(timeout));
		prop.setProperty("registration-title", String.valueOf(registrationTitle));
		prop.setProperty("use-huffman", String.valueOf(LZWCompressor.USE_HUFFMAN));
		BufferedOutputStream fos = null;
		try
		{
			fos = new BufferedOutputStream(new FileOutputStream(propertyFile));
			prop.store(fos, "InterIM Server Settings");
		}
		catch(FileNotFoundException e)
		{
			logger.warning("Failed to save properties: " + e.getMessage());
			e.printStackTrace();
		}
		catch(IOException e)
		{
			logger.warning("Failed to save properties: " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			if(fos != null)
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		logger.info("Loaded!");
	}
	public void start() {
		isRunning = true;
		try {
			logger.info("Waiting for connections...");
			ServerSocket socket = SSLServerSocketFactory.getDefault().createServerSocket(port);
			while(isRunning)
			{
				Socket sock = socket.accept();
				logger.info("Connecting: " + sock.getInetAddress());
				ServerHandler handler = new ServerHandler(sock);
				handler.start();
			}
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static ChatGroup getGroup(int id)
	{
		for(ChatGroup group : groups)
			if(group.id == id)
				return group;
		return null;
	}
	public static String[] getGroups() {
		String[] names = new String[groups.size()];
		for(int i = 0; i < names.length; i++)
		{
			names[i] = groups.get(i).name;
		}
		return names;
	}

}

