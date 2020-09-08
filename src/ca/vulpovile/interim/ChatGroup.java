package ca.vulpovile.interim;

import java.io.IOException;
import java.util.ArrayList;

import ca.vulpovile.interim.protocolv1.NetHandler;
import ca.vulpovile.interim.protocolv1.ServerHandler;
import ca.vulpovile.interim.protocolv1.packets.Packet6Message;
import ca.vulpovile.interim.protocolv1.packets.Packet8ListUsers;

public class ChatGroup {
	public final String name;
	public final int id;
	ArrayList<ServerHandler> users = new ArrayList<ServerHandler>();
	public ChatGroup(String name, int id) {
		this.name = name;
		this.id = id;
	}
	public void sendMessage(ServerHandler exclude, Packet6Message message)
	{
		if(exclude != null)
			message.user = exclude.username.getBytes();
		for(NetHandler user : users)
		{
			if(user != exclude)
				user.sendPacket(message);
		}
	}
	public void sendUserList()
	{
		try{
			String[] userList = new String[users.size()];
			for(int i = 0; i < userList.length; i++)
			{
				userList[i] = users.get(i).username;
			}
			if(users.size() > 0)
			{
				Packet8ListUsers list = new Packet8ListUsers(id, userList);
				for(ServerHandler user : users)
					user.sendPacket(list);
			}
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
			
	}
	public void remove(ServerHandler handler) {
		users.remove(handler);
		sendUserList();
	}
	public void add(ServerHandler handler) {
		users.add(handler);
		sendUserList();
	}
}
