package com.djo938.bluetoothgps;

import java.io.IOException;
import java.util.Set;

import com.djo938.bluetoothgps.seeder.GPSSystem;
import com.djo938.bluetoothgps.seeder.SeederClientInterface;
import com.djo938.bluetoothgps.server.AbstractServer;
import com.djo938.bluetoothgps.server.BluetoothServer;
import com.djo938.bluetoothgps.server.ServerEvent;
import com.djo938.bluetoothgps.server.TCPIPServer;
import com.djo938.bluetoothgps.server.client.ClientInterface;

import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity implements ServerEvent,SeederClientInterface
{
	private int counter;
	private TCPIPServer tcpipserver;
	private BluetoothServer blueserver;
	private GPSSystem gps;
	
	public MainActivity()
	{
		super();
		this.counter = 1;
		
		/*start TCP/IP server*/
		tcpipserver = new TCPIPServer(1234);
		tcpipserver.addEventClient(this);
		
		try 
		{
			tcpipserver.start();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			Log.e("tcpipserver.start()", e.getMessage(), e);
		}
		
		/*start Bluetooth server*/
		blueserver = new BluetoothServer(BluetoothAdapter.getDefaultAdapter(),"bluetooth gps","f1e7facd-6bf2-4dd0-b96f-5ea70c475c48");
		blueserver.addEventClient(this);
		
		try 
		{
			blueserver.start();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			Log.e("blueserver.start()", e.getMessage(), e);
		}
		
		/*Notify main activity starting*/
		Log.v("MainActivity", "MainActivity");
	}
	
	private String getCompleteMessage()
	{
		
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBluetoothAdapter == null)
        {
			return "<>No Bluetooth Adapter : "+counter++;
        }
    	String ret = mBluetoothAdapter.toString()+" : "+counter++;
        
    	if(!mBluetoothAdapter.isEnabled())
    	{
    		ret += "\n<>device not enabled "+mBluetoothAdapter.getAddress();
    		return ret;
    	}
    	ret += "\n<>device enabled "+mBluetoothAdapter.getAddress();
		
    	Set<BluetoothDevice> paired = mBluetoothAdapter.getBondedDevices();
    	
    	if(paired.isEmpty())
    	{
    		ret += "\n<>no distant paired device";
    		return ret;
    	}
    	
    	for(BluetoothDevice bd : paired)
    	{
    		ret += "\n<>"+bd.getName() + " " + bd.getAddress();
    		//ret += "\n   "+bd.;
    	}
    	
		return ret;
	}
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        TextView textView = new TextView(this);
        textView.setId(42);
        textView.setText(getCompleteMessage());
        
        /*register gps notification, can do it in constructor...*/
		this.gps = new GPSSystem((LocationManager) this.getSystemService(Context.LOCATION_SERVICE));
		this.gps.addClient(this);
        
        Log.v("onCreate", "loading");
        setContentView(textView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) 
    {
    	Log.v("onContextItemSelected", "selected : "+item.getItemId());
    	
    	
    	/*return true;
    	super.onRestart();
    	Log.v("Main ItemSelect", ""+item.getItemId());
    	Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		this.finish();
		return super.onContextItemSelected(item);*/
    	
    	switch (item.getItemId()) 
    	{
    		case R.id.menu_about:
    			AlertDialog.Builder builder = new AlertDialog.Builder(this);
    			builder.setMessage("redirect gps data to a Bluetooth port com.\nAuthor : djo938\n")
    		       .setTitle("About");
    			
    			builder.setPositiveButton("OK", new OnClickListener() {
					@Override public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}});
    			AlertDialog dialog = builder.create();
    			dialog.show();
    			break;
    			
    		case R.id.menu_refresh:
    			
    			/*Intent intent = new Intent(this, MainActivity.class);
    			startActivity(intent);*/
    			
    			TextView last_sync = (TextView)findViewById(42);
    	    	//last_sync.append("plop : "+counter++);
    			last_sync.setText(getCompleteMessage());
    			
    			break;
    		case R.id.menu_settings:
    			Intent intent = new Intent(this, SettingsActivity.class);
    			startActivity(intent);
    			this.finish();
    			break;
    	}
    	return true;
    }

	@Override public void serverStart(AbstractServer server) {Log.v("serverStart ", server.getServerName());}
	@Override public void serverStop(AbstractServer server) {Log.v("serverStop ", server.getServerName());}
	@Override public void serverPause(AbstractServer server) {Log.v("serverPause ", server.getServerName());}
	@Override public void serverRestart(AbstractServer server) {Log.v("serverRestart ", server.getServerName());}
	@Override public void serverAddClient(AbstractServer server, ClientInterface client) {Log.v("serverAddClient ", server.getServerName()+" "+client.getIdentity());}
	@Override public void serverRemoveClient(AbstractServer server, ClientInterface client) {Log.v("serverRemoveClient ", server.getServerName()+" "+client.getIdentity());}
	@Override public void serverError(AbstractServer server, int scale, String message) {Log.v("serverError ", server.getServerName()+" "+scale+" "+message);}

	@Override
	public void newData(byte[] data) 
	{
		tcpipserver.broadcast(data);
		blueserver.broadcast(data);
	}
}
