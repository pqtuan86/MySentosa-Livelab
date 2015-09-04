/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mysentosa.android.sg.location;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;

import com.urbanairship.location.LastLocationFinder;

public class LocationFinder {

	protected static String TAG = "LocationFinder";

	protected LocationManager locationManager;

	protected boolean needFasterLocation = true;
	protected Context context;
	protected ILastLocationFinder lastLocationFinder;
	protected LocationUpdateRequester locationUpdateRequester;


	public static final long DEFAULT_WAITING_TIME = 10000;
	Timer locationTaskTimer;
	boolean gps_enabled = false;
	boolean network_enabled = false;
	LocationNotifier locationNotifier;
	Handler handler = new Handler();
	boolean findingInProgress;

	private long waitingTime;

	public LocationFinder(Context context, LocationNotifier locationNotifier) {
		this.context = context;
		this.locationNotifier = locationNotifier;
		this.waitingTime = LocationFinder.DEFAULT_WAITING_TIME;

		// Get references to the managers
		locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);

		// Instantiate a LastLocationFinder class.
		// This will be used to find the last known location when the
		// application starts.
		lastLocationFinder = PlatformSpecificImplementationFactory
				.getLastLocationFinder(context);
		lastLocationFinder.setChangedLocationListener(fastLocationUpdateListener);

		// Instantiate a Location Update Requester class based on the available
		// platform version.
		// This will be used to request location updates.
		locationUpdateRequester = PlatformSpecificImplementationFactory
				.getLocationUpdateRequester(locationManager);
	}

	public LocationFinder(Context context, LocationNotifier locationNotifier, long waitingTime) {
		this(context, locationNotifier);
		this.waitingTime = waitingTime;
		
	}

	/**
	 * Start listening for location updates.
	 */
	
	public boolean isStillFinding(){
		return findingInProgress;
	}
	

	public boolean requestLocationUpdates() {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		String providerTest = locationManager.getBestProvider(criteria, true);
		
		if(providerTest == null || providerTest.length() == 0)
		{
			return false;
		}
		findingInProgress = true;
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		
		// it will give faster location for first call.
		if (needFasterLocation) {
			 Location l = lastLocationFinder.getLastBestLocation(PlacesConstants.MAX_DISTANCE, PlacesConstants.MAX_TIME);
			 if(l != null) { // got last location.
				 locationNotifier.updatedLocation(l);
			 }

		} 

		// Normal updates while activity is visible.
		locationUpdateRequester.requestLocationUpdates(0, 0,
				criteria, bestLocationUpdateListener);



		// Register a receiver that listens for when a better provider than I'm
		// using becomes available.
		String bestProvider = locationManager.getBestProvider(criteria, false);
		String bestAvailableProvider = locationManager.getBestProvider(
				criteria, true);
		if (bestProvider != null && !bestProvider.equals(bestAvailableProvider)) {
			locationManager.requestLocationUpdates(bestProvider, 0, 0,
					bestInactiveLocationProviderListener);
		}
		
		
		locationTaskTimer = new Timer();
		locationTaskTimer.schedule(new CancleLocationListeners(), waitingTime);
		
		return true;
	}

	/**
	 * Stop listening for location updates
	 */
	public void disableLocationUpdates() {
		locationManager.removeUpdates(bestInactiveLocationProviderListener);
		locationManager.removeUpdates(fastLocationUpdateListener);
		locationManager.removeUpdates(bestLocationUpdateListener);
		
		lastLocationFinder.cancel();
		findingInProgress = false;
		if(locationTaskTimer != null){
			locationTaskTimer.cancel();
			locationTaskTimer.purge();
		}
		
	}

	/**
	 * One-off location listener that receives updates from the
	 * {@link LastLocationFinder}. This is triggered where the last known
	 * location is outside the bounds of our maximum distance and latency.
	 */
	protected LocationListener fastLocationUpdateListener = new LocationListener() {
		public void onLocationChanged(Location l) {
			locationNotifier.updatedLocation(l);
			locationManager.removeUpdates(this);
			
			// Got a fast location, try for a finer location now..
			LocationFinder.this.needFasterLocation = false;
			// already called the GPS in parallel
			//requestLocationUpdates();
		}

		public void onProviderDisabled(String provider) {
			requestLocationUpdates();
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {}
		public void onProviderEnabled(String provider) {}
	};
	
	/**
	 * Look for finer location updates
	 */
	protected LocationListener bestLocationUpdateListener = new LocationListener() {
		public void onLocationChanged(Location l) {
			locationNotifier.updatedLocation(l);
			disableLocationUpdates();
		}

		public void onProviderDisabled(String provider) {
			requestLocationUpdates();
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {}
		public void onProviderEnabled(String provider) {}
	};

	/**
	 * If the best Location Provider (usually GPS) is not available when we
	 * request location updates, this listener will be notified if / when it
	 * becomes available. It calls requestLocationUpdates to re-register the
	 * location listeners using the better Location Provider.
	 */
	protected LocationListener bestInactiveLocationProviderListener = new LocationListener() {
		public void onLocationChanged(Location l) {}
		public void onProviderDisabled(String provider) {}
		public void onStatusChanged(String provider, int status, Bundle extras) {}

		public void onProviderEnabled(String provider) {
			// Re-register the location listeners using the better Location	 Provider.
			requestLocationUpdates();
		}
	};
	
	
	class CancleLocationListeners extends TimerTask {
		@Override
		public void run() {
			LocationFinder.this.disableLocationUpdates();
			 handler.post(new Runnable(){
				public void run(){	
					locationNotifier.listenersDisabled();
				}
			});
			
		}
	}
}