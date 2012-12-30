package com.djo938.bluetoothgps;

import java.util.Set;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity
{
	private int counter;
	
	public MainActivity()
	{
		super();
		this.counter = 1;

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
    	}
    	else
    	{
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
    	}
    	SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    	ret += "\n<>"+sharedPref.getString("location_list", "toto");
    	
    	if(isMyServiceRunning())
    	{
    		ret += "\n<> service started";
    		
    	}
    	else
    	{
    		ret += "\n<> service not started";
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
        
        if(!isMyServiceRunning())
        {
        	Log.v("starting service", "YES");
        	this.startService(new Intent(MainActivity.this, MainService.class));
        }
        else
        {
        	Log.v("starting service", "NO");
        }
        
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
	
	private boolean isMyServiceRunning() 
	{
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
	    {
	        if (MainService.class.getName().equals(service.service.getClassName())) 
	        {
	            return true;
	        }
	    }
	    return false;
	}
}
