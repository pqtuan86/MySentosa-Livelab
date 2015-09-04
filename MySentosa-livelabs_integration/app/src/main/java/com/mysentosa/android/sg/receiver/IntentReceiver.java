/*
Copyright 2009-2011 Urban Airship Inc. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE URBAN AIRSHIP INC ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
EVENT SHALL URBAN AIRSHIP INC OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mysentosa.android.sg.receiver;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mysentosa.android.sg.EventsAndPromotionsActivity;
import com.mysentosa.android.sg.HomeActivity;
import com.mysentosa.android.sg.NavigationManagerActivity;
import com.mysentosa.android.sg.SplashScreenActivity;
import com.mysentosa.android.sg.ThingsToDoCategoryListActivity;
import com.mysentosa.android.sg.TicketsActivity;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.LogHelper;
import com.urbanairship.UAirship;
import com.urbanairship.push.PushManager;

public class IntentReceiver extends BroadcastReceiver {

	private static final String logTag = "IntentReceiver";

	private Intent launchActivity;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		LogHelper.i(logTag, " testing Received intent: " + intent.toString());
		String action = intent.getAction();

		if (action.equals(PushManager.ACTION_PUSH_RECEIVED)) {

			int id = intent.getIntExtra(PushManager.EXTRA_NOTIFICATION_ID, 0);

			LogHelper.i(logTag, "Received push notification. Alert: " 
					+ intent.getStringExtra(PushManager.EXTRA_ALERT)
					+ " [NotificationID="+id+"]");

			logPushExtras(intent);

		} else if (action.equals(PushManager.ACTION_NOTIFICATION_OPENED)) {
		    launchActivity = new Intent();
		    
			logPushExtras(intent);
			
			String notificationType = intent.getStringExtra(Const.NOTIFICATION_TYPE);
			String stringID = intent.getStringExtra(Const.NOTIFICATION_TYPE_ID);
			int id = stringID==null?-1:Integer.parseInt(stringID);
			
			String toClass = "";
			if (notificationType == null) {
			    toClass = HomeActivity.class.getName();
			} else if (notificationType.equals(Const.NOTIFICATION_TYPE_EVENT)) {
			    toClass = EventsAndPromotionsActivity.class.getName();
			    launchActivity.putExtra(EventsAndPromotionsActivity.CURRENT_TYPE, EventsAndPromotionsActivity.TYPE_EVENT);
			} else if (notificationType.equals(Const.NOTIFICATION_TYPE_DEAL)) {
			    toClass = EventsAndPromotionsActivity.class.getName();
                launchActivity.putExtra(EventsAndPromotionsActivity.CURRENT_TYPE, EventsAndPromotionsActivity.TYPE_PROMOTION);
			} else if (notificationType.equals(Const.NOTIFICATION_TYPE_LOCATION)) {
			    toClass = ThingsToDoCategoryListActivity.class.getName();
			    launchActivity.putExtra(ThingsToDoCategoryListActivity.CATEGORY_NAME_FOR_NODES, Const.ATTRACTION);
			} else if (notificationType.equals(Const.NOTIFICATION_TYPE_TICKET)) {
			    toClass = TicketsActivity.class.getName();
			    String ticketType = intent.getStringExtra(Const.NOTIFICATION_TICKET_TYPE);
			    if (ticketType.equals(Const.NOTIFICATION_TYPE_TICKET_EVENT)) {
			        launchActivity.putExtra(TicketsActivity.TICKET_TYPE, Const.NOTIFICATION_TYPE_TICKET_EVENT);
	            } else if (ticketType.equals(Const.NOTIFICATION_TYPE_TICKET_PACKAGE)) {
	                launchActivity.putExtra(TicketsActivity.TICKET_TYPE, Const.NOTIFICATION_TYPE_TICKET_PACKAGE);
                } else if (ticketType.equals(Const.NOTIFICATION_TYPE_TICKET_ATTRACTION)) {
                    launchActivity.putExtra(TicketsActivity.TICKET_TYPE, Const.NOTIFICATION_TYPE_TICKET_ATTRACTION);
                } 
            }
			navigateToActivity(NavigationManagerActivity.isAlive(), toClass, id);

		} else if (action.equals(PushManager.ACTION_REGISTRATION_FINISHED)) {
			LogHelper.i(logTag, " testing Registration complete. APID:" + intent.getStringExtra(PushManager.EXTRA_APID)
					+ ". Valid: " + intent.getBooleanExtra(PushManager.EXTRA_REGISTRATION_VALID, false));
		}
	}

	/**
	 * Log the values sent in the payload's "extra" dictionary.
	 * 
	 * @param intent A PushManager.ACTION_NOTIFICATION_OPENED or ACTION_PUSH_RECEIVED intent.
	 */
	private void logPushExtras(Intent intent) {
		Set<String> keys = intent.getExtras().keySet();
		for (String key : keys) {

			//ignore standard C2DM extra keys
			List<String> ignoredKeys = Arrays.asList(
					"collapse_key",//c2dm collapse key
					"from",//c2dm sender
					PushManager.EXTRA_NOTIFICATION_ID,//int id of generated notification (ACTION_PUSH_RECEIVED only)
					PushManager.EXTRA_PUSH_ID,//internal UA push id
					PushManager.EXTRA_ALERT);//ignore alert
			if (ignoredKeys.contains(key)) {
				continue;
			}
			LogHelper.i(logTag, "Push Notification Extra: ["+key+" : " + intent.getStringExtra(key) + "]");
		}
	}
	
	private void navigateToActivity(boolean navigationManagerActivityIsAlive, String toClass, int id) {
	    
	    NavigationManagerActivity.STARTED_FROM_NOTIFICATION = true;
	    
	    launchActivity.putExtra(NavigationManagerActivity.ACTIVITY_TO_START, toClass);
	    launchActivity.putExtra(NavigationManagerActivity.ACTIVITY_TO_START_ID, id);
	    if (!navigationManagerActivityIsAlive) {
	        launchActivity.setClass(UAirship.shared().getApplicationContext(), SplashScreenActivity.class);
	        launchActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            UAirship.shared().getApplicationContext().startActivity(launchActivity);
	    } else {
	        launchActivity.setAction(NavigationManagerActivity.PUSH_INTENT_ACTION);
	        UAirship.shared().getApplicationContext().sendBroadcast(launchActivity);
	    }
	    
	    
	}
}
