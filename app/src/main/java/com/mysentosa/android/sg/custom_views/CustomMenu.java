package com.mysentosa.android.sg.custom_views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.mysentosa.android.sg.EventsAndPromotionsActivity;
import com.mysentosa.android.sg.HomeActivity;
import com.mysentosa.android.sg.InformationActivity;
import com.mysentosa.android.sg.IslanderActivity;
import com.mysentosa.android.sg.MapActivity;
import com.mysentosa.android.sg.NavigationManagerActivity;
import com.mysentosa.android.sg.PromotionsActivity;
import com.mysentosa.android.sg.R;
import com.mysentosa.android.sg.ThingsToDo_MySentosaActivity;
import com.mysentosa.android.sg.TicketsActivity;
import com.mysentosa.android.sg.custom_views.SlidingDrawer.OnDrawerCloseListener;
import com.mysentosa.android.sg.custom_views.SlidingDrawer.OnDrawerOpenListener;
import com.mysentosa.android.sg.custom_views.SlidingDrawer.OnScrollListener;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.Const.FlurryStrings;

import sg.edu.smu.livelabs.integration.PromotionActivity;

public class CustomMenu {

    public static Intent createNavigatingIntent(Context context, String className) {
        Intent intent = new Intent(context, NavigationManagerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(NavigationManagerActivity.ACTIVITY_TO_START, className);
        return intent;
    }

    private SlidingDrawer sd;
    private RotateImageView mainButton, menuArrow;
    private Context context;
    private boolean isExpanded;

    //Sync this order with the one in base activity
    private int imgResIDs[] = {
            R.id.menu_home,
            R.id.menu_things_to_do,
            R.id.menu_map,
            R.id.menu_tickets,
            R.id.menu_mysentosa,
            R.id.menu_deals,
            R.id.menu_coupons,
            R.id.menu_islander,
            R.id.menu_information
    };

    private String classNames[] = {
            HomeActivity.class.getName(),
            ThingsToDo_MySentosaActivity.class.getName(),
            MapActivity.class.getName(),
            TicketsActivity.class.getName(),
            ThingsToDo_MySentosaActivity.class.getName(),
            EventsAndPromotionsActivity.class.getName(),
            PromotionsActivity.class.getName(),
            IslanderActivity.class.getName(),
            InformationActivity.class.getName()
    };

    private final boolean CLOSE = true, OPEN = false;


    public CustomMenu(Context context, boolean isExpanded) {
        this.context = context;
        this.isExpanded = isExpanded;
        initializeView();
    }


    private void initializeView() {
        sd = (SlidingDrawer) LayoutInflater.from(context).inflate(R.layout.custom_menu, null);

        mainButton = (RotateImageView) sd.getHandle().findViewById(R.id.iv_menu_main);
        mainButton.setTag(CLOSE);

        menuArrow = (RotateImageView) sd.getHandle().findViewById(R.id.iv_menu_arrow);

        sd.setmOnScrollListener(new OnScrollListener() {
            @Override
            public void onScroll(int length, int distance) {
                float degree = 180 - (float) 180 * distance / length;
                menuArrow.rotateTo(degree);
                mainButton.rotateTo(degree);
            }
        });

        sd.setOnDrawerOpenListener(new OnDrawerOpenListener() {

            @Override
            public void onDrawerOpened() {
                FlurryAgent.logEvent(FlurryStrings.MenuLaunch);
            }
        });

        sd.setOnDrawerCloseListener(new OnDrawerCloseListener() {

            @Override
            public void onDrawerClosed() {
                FlurryAgent.logEvent(FlurryStrings.MenuClose);
            }
        });


        for (int i = 0; i < imgResIDs.length; i++) {
            View v = sd.findViewById(imgResIDs[i]);
            v.setOnClickListener(menuItemsClickListener);
            v.setTag(i);
        }
    }

    private OnClickListener menuItemsClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            sd.close();
            int index = (Integer) v.getTag();
            String className = classNames[index];
            FlurryAgent.logEvent(FlurryStrings.getEventNameForActivity(className));
            Activity currentActivity = (Activity) v.getContext();

            if (!currentActivity.getClass().getName().equals(className)) {
                Intent intent = new Intent();
                intent.setClassName(currentActivity, className);

                if (className.equals(HomeActivity.class.getName())) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                }

                if (className.equals(EventsAndPromotionsActivity.class.getName())) {
                    intent.putExtra(EventsAndPromotionsActivity.CURRENT_TYPE, EventsAndPromotionsActivity.TYPE_PROMOTION);

                    EasyTracker easyTracker = EasyTracker.getInstance(context);
                    easyTracker.set(Fields.SCREEN_NAME, Const.GAStrings.DEALS_MAIN);
                    easyTracker.send(MapBuilder
                                    .createAppView()
                                    .build()
                    );
                }

                if (className.equals(ThingsToDo_MySentosaActivity.class.getName()))
                    intent.putExtra(ThingsToDo_MySentosaActivity.CURRENT_TYPE, index == 1 ? ThingsToDo_MySentosaActivity.TYPE_THINGSTODO : ThingsToDo_MySentosaActivity.TYPE_MYSENTOSA);

                if (className.equals(TicketsActivity.class.getName()))
                    intent.putExtra(TicketsActivity.TICKET_TYPE, "Package");

                if (className.equals(IslanderActivity.class.getName())) {
                    EasyTracker easyTracker = EasyTracker.getInstance(context);
                    easyTracker.set(Fields.SCREEN_NAME, Const.GAStrings.ISLANDER_MAIN);
                    easyTracker.send(MapBuilder
                                    .createAppView()
                                    .build()
                    );
                }

                if(className.equals(PromotionsActivity.class.getName())) {
                    intent.putExtra("Notification", false);
                }

                currentActivity.startActivity(intent);

            } else {
                //we have an exception here, EVENTS and PROMOTIONS use a same activity => code above will not run in case moving from EVENTS to PROMOTIONS screen
                if (className.equals(EventsAndPromotionsActivity.class.getName())) {
                    if (((EventsAndPromotionsActivity) currentActivity).isEvent()) {
                        //Can not move from PROMOTIONS to EVENTS, only 1 way: EVENTS to PROMOTIONS
                        Intent intent = new Intent();
                        intent.setClassName(currentActivity, className);

                        intent.putExtra(EventsAndPromotionsActivity.CURRENT_TYPE, EventsAndPromotionsActivity.TYPE_PROMOTION);

                        EasyTracker easyTracker = EasyTracker.getInstance(context);
                        easyTracker.set(Fields.SCREEN_NAME, Const.GAStrings.DEALS_MAIN);
                        easyTracker.send(MapBuilder
                                        .createAppView()
                                        .build()
                        );

                        currentActivity.startActivity(intent);
                    }
                }

            }
        }
    };

    public SlidingDrawer getMenu() {
        return sd;
    }
}