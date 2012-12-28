package com.djo938.bluetoothgps.server.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;

public class BluetoothClient implements ClientInterface
{
	private BluetoothSocket sock;
	
	public BluetoothClient(BluetoothSocket sock)
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
		return this.sock.getRemoteDevice().getAddress();
	}

	@Override
	public void closeInput() {}

	@Override
	public void closeOutput() {}
}
