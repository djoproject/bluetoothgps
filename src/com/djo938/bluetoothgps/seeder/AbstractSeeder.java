package com.djo938.bluetoothgps.seeder;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractSeeder 
{
	private List<SeederClientInterface> clients;
	
	protected AbstractSeeder()
	{
		clients = new LinkedList<SeederClientInterface>();
	}
	
	public void addClient(SeederClientInterface client)
	{
		clients.add(client);
	}
	
	public void removeClient(SeederClientInterface client)
	{
		clients.remove(client);
	}
	
	protected void fireNewData(byte data[])
	{
		for(SeederClientInterface sci : clients)
		{
			sci.newData(data);
		}
	}
	
}
