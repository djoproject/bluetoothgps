package com.djo938.bluetoothgps.seeder;

import android.location.GpsStatus.NmeaListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.GpsStatus.Listener;
import android.os.Bundle;
import android.util.Log;
import com.djo938.bluetoothgps.nmea.NMEA;


public class GPSSystem extends AbstractSeeder implements NmeaListener,LocationListener,Listener
{
	//LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

	public GPSSystem(LocationManager locationManager)
	{
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		locationManager.addNmeaListener(this);
		Log.v("GPSSystem","start");
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
}
