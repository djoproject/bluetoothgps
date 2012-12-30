package com.djo938.bluetoothgps.seeder;

import java.util.List;

import android.location.GpsStatus;
import android.location.GpsStatus.NmeaListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.GpsStatus.Listener;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import com.djo938.bluetoothgps.nmea.NMEA;


public class GPSSystem extends AbstractSeeder implements NmeaListener,LocationListener,Listener
{
	private LocationManager locationManager;
	private boolean enable;

	public GPSSystem(LocationManager locationManager)
	{
		this.locationManager = locationManager;
		enable = false;
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
		Log.v("onLocationChanged",""+newLocation);
        sendLocation(newLocation);
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
}
