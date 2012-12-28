package com.djo938.bluetoothgps.server;

import java.io.IOException;
import java.util.UUID;

import com.djo938.bluetoothgps.server.client.BluetoothClient;
import com.djo938.bluetoothgps.server.client.ClientInterface;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.util.Log;

public class BluetoothServer extends AbstractServer
{
	private BluetoothServerSocket mmServerSocket;
	private final BluetoothAdapter adaptater;
	private String uuid_string,appName;
	
	public BluetoothServer(BluetoothAdapter adaptater,String appName,String uuid_string)
	{
		super("Bluetooth Server",1);
		this.adaptater = adaptater;
		this.uuid_string = uuid_string;
		this.appName = appName;
	}

	@Override
	protected ClientInterface acceptClient() throws IOException 
	{
		return new BluetoothClient(this.mmServerSocket.accept());
	}

	@Override
	protected void onStop() throws IOException 
	{
		if(this.mmServerSocket != null)
			this.mmServerSocket.close();
		
		this.mmServerSocket = null;
	}

	@Override
	protected void onStart() throws IOException 
	{
		UUID u = null;
		try
		{
			u = UUID.fromString(uuid_string);
			Log.v("UUID gen", u.toString());
			this.mmServerSocket = adaptater.listenUsingRfcommWithServiceRecord(appName, u);
		}
		catch(IllegalArgumentException iae)
		{
			Log.e("blue.onstart", iae.getMessage()+" : "+u.toString(), iae);
			throw new IOException(iae);
		}
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
