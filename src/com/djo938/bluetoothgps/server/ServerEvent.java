package com.djo938.bluetoothgps.server;

import com.djo938.bluetoothgps.server.client.ClientInterface;

public interface ServerEvent 
{
	public void serverStart(AbstractServer server);
	public void serverStop(AbstractServer server);
	public void serverPause(AbstractServer server);
	public void serverRestart(AbstractServer server);
	public void serverAddClient(AbstractServer server, ClientInterface client);
	public void serverRemoveClient(AbstractServer server, ClientInterface client);
	public void serverError(AbstractServer server, int scale, String message);
}
