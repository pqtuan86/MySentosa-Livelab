package com.mysentosa.android.sg;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

public class NavigationManagerActivity extends FragmentActivity {

	/*
	 * The purpose of this activity is primarily to act as a base activity on
	 * top of which all other activities are stacked When we return from any of
	 * the other activities on top of this in the stack, we just quit this
	 * activity When this activity is started with the final destination
	 * activity as an extra in the intent, it simply starts that from here
	 */
	public static final String ACTIVITY_TO_START = "ACTIVITY_TO_START",
			PUSH_INTENT_ACTION = "com.mysentosa.android.sg.push_received";
	
	public static boolean STARTED_FROM_NOTIFICATION = false;
	public static final String ACTIVITY_TO_START_ID = "ID"; //Use when the notification is opened
	
	public static final int QUIT_NAVIGATION_MANAGER_ACTIVITY = 43291;
	private final int reqCode = 1001;
	private static boolean isAlive = false;
	private PushNotificationListener pushNotificationListener;

	public static boolean isAlive() {
		return isAlive;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		isAlive = true;
		super.onCreate(savedInstanceState);
		this.pushNotificationListener = new PushNotificationListener();
		this.setContentView(new View(this));
		this.registerReceiver(pushNotificationListener, new IntentFilter(
				PUSH_INTENT_ACTION));
		String className = this.getIntent().getStringExtra(ACTIVITY_TO_START);		
		if (className == null)
			finish();
		startBaseActivity(className, this.getIntent(), false);
	}

	@Override
	public void onNewIntent(Intent intent) {
		String className = intent.getStringExtra(ACTIVITY_TO_START);	
		if (className == null)
			finish();
		startBaseActivity(className, intent, true);
	}

	public void startBaseActivity(String className, Intent intent, boolean isExistingActivity) {
		Intent mIntent = new Intent();
		mIntent.setClassName(this, className);
		int eventId = intent.getIntExtra(ACTIVITY_TO_START_ID, -1);

		if (className.equals(MapActivity.class.getName())) {
			int nodeId = getIntent().getIntExtra(MapActivity.ROUTE_TO_NODE, -1);
			String nodeTitle = getIntent().getStringExtra(MapActivity.ROUTE_TO_NODE_TEXT);
			boolean isWalkOnly = getIntent().getBooleanExtra(MapActivity.IS_WALK_ONLY, true);

			if (nodeId != -1 && !isExistingActivity) {
				mIntent.putExtra(MapActivity.ROUTE_TO_NODE, nodeId);
				mIntent.putExtra(MapActivity.IS_WALK_ONLY, isWalkOnly);
				mIntent.putExtra(MapActivity.ROUTE_TO_NODE_TEXT, nodeTitle);
			}
		}

		if (className.equals(EventsAndPromotionsActivity.class.getName())) {
			if (eventId != -1) {
				mIntent.putExtra(EventsAndPromotionsDetailActivity.ID,eventId);
			}
			int type = intent.getIntExtra(EventsAndPromotionsActivity.CURRENT_TYPE,EventsAndPromotionsActivity.TYPE_EVENT);
			mIntent.putExtra(EventsAndPromotionsActivity.CURRENT_TYPE, type);
		}
		if (className.equals(ThingsToDo_MySentosaActivity.class.getName())) {
			int type = intent.getIntExtra(ThingsToDo_MySentosaActivity.CURRENT_TYPE,ThingsToDo_MySentosaActivity.TYPE_THINGSTODO);
			mIntent.putExtra(ThingsToDo_MySentosaActivity.CURRENT_TYPE, type);
		}
		if (className.equals(TicketsActivity.class.getName())) {			
			mIntent.putExtra(TicketsActivity.TICKET_TYPE, intent.getStringExtra(TicketsActivity.TICKET_TYPE));
			if (eventId != -1) {
                mIntent.putExtra(NavigationManagerActivity.ACTIVITY_TO_START_ID,eventId);
            }
		}
		if (className.equals(ThingsToDoCategoryListActivity.class.getName())) {          
		    mIntent.putExtra(ThingsToDoCategoryListActivity.CATEGORY_NAME_FOR_NODES,intent.getStringExtra(ThingsToDoCategoryListActivity.CATEGORY_NAME_FOR_NODES));
		    if (eventId != -1) {
                mIntent.putExtra(EventsAndPromotionsDetailActivity.ID,eventId);
            }
        }
		
		mIntent.putExtra(BaseActivity.ACTIVITY_BOTTOM_OF_STACK, true);
		startActivityForResult(mIntent, reqCode);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (STARTED_FROM_NOTIFICATION & isTaskRoot()) {
		    Intent intent = new Intent(NavigationManagerActivity.this, HomeActivity.class);
		    startActivity(intent);
		    STARTED_FROM_NOTIFICATION = false;
		}
		this.finish();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		this.unregisterReceiver(pushNotificationListener);
		isAlive = false;
	}

	private class PushNotificationListener extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
		    intent.setClass(NavigationManagerActivity.this,NavigationManagerActivity.class);
		    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            NavigationManagerActivity.this.startActivity(intent);
		}
	}
}