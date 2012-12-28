package com.djo938.bluetoothgps.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import com.djo938.bluetoothgps.server.client.ClientInterface;
import com.djo938.bluetoothgps.server.client.TcpipClient;

public class TCPIPServer extends AbstractServer
{
	private ServerSocket server;
	private int port;
	
	public TCPIPServer(int port) 
	{
		super("TCP/IP Server",2);
		
		server = null;
		this.port = port;
	}

	@Override
	public void onStop() throws IOException 
	{
		if(server != null)
			server.close();
		
		server = null;
	}

	@Override
	public void onStart() throws IOException 
	{
		server = new ServerSocket();
		server.setReuseAddress(true);
		server.bind(new InetSocketAddress(this.port));
	}

	@Override
	public ClientInterface acceptClient() throws IOException 
	{
		Socket client = server.accept();
		//client.shutdownInput();
		return new TcpipClient(client);
	}
	
	@Override
	protected void onPause() throws IOException 
	{
		onStop();
	}

	@Override
	protected void onRestart() throws IOException 
	{
		onStart();
	}
}
