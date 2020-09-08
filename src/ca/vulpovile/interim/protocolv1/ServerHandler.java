package ca.vulpovile.interim.protocolv1;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import ca.vulpovile.interim.ChatGroup;
import ca.vulpovile.interim.Server;
import ca.vulpovile.interim.compression.HuffmanCompressor;
import ca.vulpovile.interim.protocolv1.NetHandler;
import ca.vulpovile.interim.protocolv1.packets.*;

public class ServerHandler extends NetHandler {

	boolean identified = false;
	boolean connected = true;
	HashMap<String, byte[]> regInfo = new HashMap<String, byte[]>();
	Thread timeoutThread = null;
	public String username = null;
	private String login = null;
	ArrayList<ChatGroup> connectedGroups = new ArrayList<ChatGroup>();
	
	
	public ChatGroup getGroup(int id)
	{
		for(ChatGroup group : connectedGroups)
			if(group.id == id)
				return group;
		return null;
	}
	
	
	public ServerHandler(Socket socket) throws IOException {
		super(socket);
	}
	@Override
	public void disconnect() {
		if(connected)
		{
			connected = false;
			Server.logger.info("Client Disconnected: " + socket.getInetAddress() + "  " + socket.getPort());
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(ChatGroup group : connectedGroups)
			{
				group.remove(this);
			}
			connectedGroups.clear();
		}
	}
	
	@Override
	public void disconnect(String message) {
		if(connected)
		{
			Server.logger.info(message);
			this.sendPacket(new Packet0Disconnect(message));
			disconnect();
		}
	}

	@Override
	public void handlePacket(Packet1Identify id) {
		Server.logger.info("Sent identify");
		if(id.minVersion > Packet.VERSION)
			disconnect("Protocol version " + id.minVersion + " is too new, sever version is " + Packet.VERSION);
		if(id.version < Packet.MINVERSION)
			disconnect("Protocol version " + id.version + " is too old, must be at least " + Packet.MINVERSION);
		if(connected)
		{
			login = new String(id.login);
			if(id.extraData)
			{
				try {
					sendRegistration("<img src='http://androdome.com/InterIM.png' />This is the Interim test server.<br>With<br>an HTML<br>Test", "", login, false);
					/*
					sendPacket(new Packet2RegisterField("Dropdown", "Potato\0Tomato\0Bisquit", Type.DROPDOWN));
					sendPacket(new Packet2RegisterField("Radio", "!Potato\0Tomato\0Bisquit", Type.RADIO));
					sendPacket(new Packet2RegisterField("Checkbox", "Potato\0!Tomato\0!Bisquit\0Potato\0Potato\0Potato\0Potato\0Potato\0Potato\0Potato\0Potato\0Potato\0Potato\0Potato\0Potato\0Potato\0Potato\0Potato\0Potato\0Potato\0Potato\0Potato\0Potato\0Potato\0Potato\0Potato\0Potato\0Potato\0Potato\0Potato\0Potato\0Potato\0Potato\0Potato", Type.CHECKBOX));
					*/
					
					if(Server.timeout > 0)
					{
						timeoutThread = new Thread()
						{
							public void run()
							{
								try {
									Thread.sleep(1000*(Server.timeout+2));
								} catch (InterruptedException e) {
								}
								if(timeoutThread != null)
								{
									disconnect("Took too long to respond");
									timeoutThread = null;
								}
							}
						};
						timeoutThread.start();
					}
				} catch (IOException e) {
					disconnect("Internal server error");
					e.printStackTrace();
				}
			}
			else
			{
				doLogin(id.password);
			}
		}
	}

	@Override
	public void handlePacket(Packet2RegisterField id) {
		if(id.type != Packet2RegisterField.Type.TIMEOUT)
			regInfo.put(new String(id.title), id.input);
		else
		{
			doRegiser();
		}
	}
	
	//Taken from https://www.tutorialspoint.com/validate-email-address-in-java
	static boolean isEmailValid(String email) {
	      String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
	      return email.matches(regex);
	   }

	private void doRegiser() {
		if(regInfo.get("Username") == null || regInfo.get("Email (Login)") == null || regInfo.get("Password") == null || regInfo.get("Are you over 13?") == null)
			disconnect("Invalid input");
		else
		{
			try{
				this.username = new String(regInfo.get("Username"));
				this.login = new String(regInfo.get("Email (Login)"));
				byte[] password = regInfo.get("Password");
				byte over13 = regInfo.get("Are you over 13?")[0];
				String errorHTML = "";
				if(username.length() > 32 || username.length() < 2)
					errorHTML += "<font color=red>Your username has to be between 2 and 32 characters long</font><br><br>";
				File userFile = new File(Server.accountsDir, login.toLowerCase());
				if(userFile.exists())
					errorHTML += "<font color=red>Your username is already taken</font><br><br>";
				if(password.length < 4)
					errorHTML += "<font color=red>Your password has to be at least 4 bytes long</font><br><br>";
				if(!isEmailValid(login))
					errorHTML += "<font color=red>Your email is not valid</font><br><br>";
				if(over13 == 0)
				{
					disconnect("You must be over 13 to register");
					return;
				}
				if(errorHTML.trim().length() == 0)
				{
					sendPacket(new Packet3Alert("Success!", "Your account is being created and you will be logged in", Packet3Alert.AlertType.INFO));
					Properties account = new Properties();
					account.setProperty("username", username);
					account.setProperty("password", sha512(password));
					BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(userFile));
					account.store(fos, "");
					fos.close();
					doLogin(password);
				}
				else
					sendRegistration(errorHTML, username, login, over13>0);
			}
			catch (Exception e) {
				disconnect("Internal server error");
				e.printStackTrace();
			}
		}
	}
	
	//From http://oliviertech.com/java/generate-SHA256--SHA512-hash-from-a-String/
	private String sha512(byte[] password) throws NoSuchAlgorithmException {
		  MessageDigest digest = MessageDigest.getInstance("SHA-512");
		  digest.reset();
		  digest.update(password);
		  return String.format("%0128x", new BigInteger(1, digest.digest()));
	}
	
	private void doLogin(byte[] password) {
		try {
			File userFile = new File(Server.accountsDir, login.toLowerCase());
			if(!userFile.exists())
				disconnect("Incorrect username or password");
			Properties account = new Properties();
			BufferedInputStream fos = new BufferedInputStream(new FileInputStream(userFile));
			account.load(fos);
			fos.close();
			if(sha512(password).trim().equalsIgnoreCase(account.getProperty("password").trim()))
			{
				identified = true;
				Thread t = timeoutThread;
				timeoutThread = null;
				username = account.getProperty("username");
				if(t != null)
					t.interrupt();
				sendPacket(new Packet1Identify("InterIM Instant Messenger", username.getBytes(), false));
				sendPacket(new Packet7ListGroups(Server.getGroups()));
			}
			else
				disconnect("Incorrect username or password");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			disconnect("Internal server error");
			e.printStackTrace();
		}
	}
	private void sendRegistration(String html, String name, String email, boolean is13) throws IOException
	{
		Server.logger.info("Sending registration");
		sendPacket(new Packet2RegisterField(Server.registrationTitle, HuffmanCompressor.compress(html.getBytes()), Packet2RegisterField.Type.INTROHTML));
		sendPacket(new Packet2RegisterField("Username", name, Packet2RegisterField.Type.TEXT));
		sendPacket(new Packet2RegisterField("Email (Login)", email, Packet2RegisterField.Type.TEXT));
		sendPacket(new Packet2RegisterField("Password", "", Packet2RegisterField.Type.PASSWORD));
		if(is13)
			sendPacket(new Packet2RegisterField("Are you over 13?", "!Yes", Packet2RegisterField.Type.CHECKBOX));
		else
			sendPacket(new Packet2RegisterField("Are you over 13?", "Yes", Packet2RegisterField.Type.CHECKBOX));
		sendPacket(new Packet2RegisterField("Seconds before disconnect", BigInteger.valueOf(Server.timeout).toByteArray(), Packet2RegisterField.Type.TIMEOUT));
	}
	
	@Override
	public void run() {
		Server.logger.info("Started recieve thread");
		try {
		while(connected && Server.isRunning)
		{
			
				byte opcode = dis.readByte();
				Server.logger.info("Got packet " + opcode);
				Class<?> packetClass = Packet.packets.get(opcode);
				if(packetClass == null)
				{
					Server.logger.severe("Invalid opcode: " + opcode);
					break;
				}
				else
				{
					try {
						Packet packet = (Packet) packetClass.newInstance();
						packet.getPacket(dis);
						packet.handlePacket(this);
					} catch (InstantiationException e) {
						e.printStackTrace();
						break;
					} catch (IllegalAccessException e) {
						e.printStackTrace();
						break;
					}
				}
			} 
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		disconnect();
	}

	@Override
	public void handlePacket(Packet0Disconnect id) {
		Server.logger.info("Client Disconnected: " + new String(id.message));
		disconnect();
	}
	@Override
	public void handlePacket(Packet3Alert id) {
		handlePacket((Packet)id);
	}
	@Override
	public void handlePacket(Packet4JoinGroup id) {
		ChatGroup group = Server.getGroup(id.groupId);
		if(getGroup(id.groupId) == null && group != null)
		{
			sendPacket(id);
			group.add(this);
			this.connectedGroups.add(group);
		}
	}
	@Override
	public void handlePacket(Packet5LeaveGroup id) {
		ChatGroup group = getGroup(id.groupId);
		if(group != null)
		{
			group.remove(this);
			this.connectedGroups.remove(group);
		}
	}
	@Override
	public void handlePacket(Packet6Message id) {
		ChatGroup group = getGroup(id.groupId);
		if(group != null)
		{
			group.sendMessage(this, id);
		}
	}
	@Override
	public void handlePacket(Packet7ListGroups id) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void handlePacket(Packet8ListUsers id) {
		// TODO Auto-generated method stub
		
	}

}
