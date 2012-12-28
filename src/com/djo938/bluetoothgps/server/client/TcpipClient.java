package com.djo938.bluetoothgps.server.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TcpipClient implements ClientInterface
{
	private Socket sock;
	
	public TcpipClient(Socket sock)
	{
		this.sock = sock;
	}

	@Override
	public boolean isClosed() 
	{
		return this.isClosed();
	}

	@Override
	public void close() throws IOException 
	{
		this.sock.close();		
	}

	@Override
	public InputStream getInputStream() throws IOException 
	{
		return this.sock.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException 
	{
		return this.sock.getOutputStream();
	}

	@Override
	public String getIdentity() 
	{
		return ""+this.sock.getInetAddress().getHostAddress()+":"+this.sock.getPort();
	}

	@Override
	public void closeInput() throws IOException 
	{
		this.sock.shutdownInput();
	}

	@Override
	public void closeOutput() throws IOException 
	{
		this.sock.shutdownOutput();
	}
}
