package com.djo938.bluetoothgps.seeder;

import java.util.Date;
import java.util.List;

import android.location.GpsStatus;
import android.location.GpsStatus.NmeaListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.GpsStatus.Listener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.djo938.bluetoothgps.nmea.NMEA;


public class GPSSystem extends AbstractSeeder implements NmeaListener,LocationListener,Listener, Runnable
{
	private LocationManager locationManager;
	private boolean enable;
	private Location lastLocation;
	private Handler timer;

	public GPSSystem(LocationManager locationManager)
	{
		this.locationManager = locationManager;
		enable = false;
		setLastLocation(new Location("bluetooth gps"));
		getLastLocation().setTime(System.currentTimeMillis());
		getLastLocation().setAltitude(0);
		timer = new Handler();
		timer.postDelayed(this, 5000);
	}
	
	public synchronized void disable()
	{
		locationManager.removeUpdates(this);
		locationManager.removeNmeaListener(this);
		locationManager.removeGpsStatusListener(this);
		enable = false;
	}
	
	public synchronized void enableGpsProvider(Looper looper)
	{
		disable();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this,looper);
		locationManager.addNmeaListener(this);
		locationManager.addGpsStatusListener(this);
		enable = true;
	}
	
	public synchronized void enableNetworkProvider(Looper looper)
	{
		disable();
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this,looper);
		locationManager.addNmeaListener(this);
		enable = true;
	}
	
	public synchronized void enablePassiveProvider(Looper looper)
	{
		disable();
		locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 1000, 0, this,looper);
		locationManager.addNmeaListener(this);
		enable = true;
	}

	@Override
	public void onNmeaReceived(long timestamp, String nmea) 
	{
		// TODO Auto-generated method stub
		Log.v("onNmeaReceived", ""+timestamp+" "+nmea);
	}

	@Override
	public void onLocationChanged(Location newLocation) 
	{
		timer.removeCallbacks(this);
		Log.v("onLocationChanged",""+newLocation);
		setLastLocation(newLocation);
		
        sendLocation(newLocation);
        timer.postDelayed(this, 5000);
	}

	@Override
	public void onProviderDisabled(String arg0) 
	{
		// TODO Auto-generated method stub
		Log.v("onProviderDisabled",arg0);
	}

	@Override
	public void onProviderEnabled(String arg0) 
	{
		// TODO Auto-generated method stub
		Log.v("onProviderEnabled",arg0);
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) 
	{
		// TODO Auto-generated method stub
		Log.v("onStatusChanged",arg0+" "+arg1+" "+arg2);
	}
	
	
    private void sendWithChecksum(String line) 
    {
        line = NMEA.decorate(line);
        super.fireNewData((line+"\n").getBytes());
    }

    @SuppressWarnings("deprecation")
	private void sendLocation(Location location) 
    {
        String time = NMEA.formatTime(location);
        String date = NMEA.formatDate(location);
        String position = NMEA.formatPosition(location);

        sendWithChecksum("GPGGA," + time + "," +
                         position + ",1," +
                         NMEA.formatSatellites(location) + "," +
                         location.getAccuracy() + "," +
                         NMEA.formatAltitude(location) + ",,,,");
        sendWithChecksum("GPGLL," + position + "," + time + ",A");
        sendWithChecksum("GPRMC," + time + ",A," +
                         position + "," +
                         NMEA.formatSpeedKt(location) + "," +
                         NMEA.formatBearing(location) + "," +
                         date + ",,");
        Date d = new Date();
        String hour = ""+d.getHours();
        if(hour.length() == 1)
        	hour = "0"+hour;
        
        String minute = ""+d.getMinutes();
        if(minute.length() == 1)
        	minute = "0"+minute;
        
        String seconds = ""+d.getSeconds();
        if(seconds.length() == 1)
        	seconds = "0"+seconds;
        
        String days = ""+d.getDate();
        if(days.length() == 1)
        	days = "0"+days;
        
        String month = ""+(d.getMonth()+1);
        if(month.length() == 1)
        	month = "0"+month;
        
        String year = ""+(1900+d.getYear());
        
        String GPZDA = "GPZDA,"+hour+minute+seconds+".00,"+days+","+month+","+year+",00,00";
        Log.v("GPZDA", GPZDA);
        sendWithChecksum(GPZDA);
        
        //TODO send gpzda
    }
    
	@Override
	public void onGpsStatusChanged(int event) 
	{
		if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS)
            sendSatellite(locationManager.getGpsStatus(null));
		
	}
    
    private void sendSatellite(GpsStatus gps) {
        String gsa = NMEA.formatGpsGsa(gps);
        sendWithChecksum("GPGSA,A," + gsa);

        List<String> gsvs = NMEA.formatGpsGsv(gps);
        for(String gsv : gsvs)
            sendWithChecksum("GPGSV," + gsvs.size() + "," +
                             Integer.toString(gsvs.indexOf(gsv)+1) + "," + gsv);
    }

	public boolean isEnable() 
	{
		return enable;
	}

	@Override
	public void run() 
	{
		timer.postDelayed(this, 5000);
		sendLocation(this.getLastLocation());
		
		Log.v("bluetooth.gps", "timer expire");
	}

	public Location getLastLocation() {
		return lastLocation;
	}

	public void setLastLocation(Location lastLocation) {
		this.lastLocation = lastLocation;
	}
}
