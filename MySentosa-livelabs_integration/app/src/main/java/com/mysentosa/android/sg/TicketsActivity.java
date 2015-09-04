package com.mysentosa.android.sg;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response.Listener;
import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.mysentosa.android.sg.adapter.TicketsAdapter;
import com.mysentosa.android.sg.asynctask.GetPackagesAsyncTask;
import com.mysentosa.android.sg.asynctask.GetTicketEventAsyncTask;
import com.mysentosa.android.sg.asynctask.MyPurchaseAsyncTask;
import com.mysentosa.android.sg.custom_dialog.LoginDialog;
import com.mysentosa.android.sg.helper.AlertDialogHelper;
import com.mysentosa.android.sg.models.GetMyClaimedDealsRequestModel;
import com.mysentosa.android.sg.models.Promotion;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.ContentURIs;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.Queries;
import com.mysentosa.android.sg.request.ClaimSpecialDealRequest;
import com.mysentosa.android.sg.request.GetMyClaimedDealsRequest;
import com.mysentosa.android.sg.request.GetPromotionExclusiveRequest;
import com.mysentosa.android.sg.utils.AlertHelper;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.Const.FlurryStrings;
import com.mysentosa.android.sg.utils.CustomCallback;
import com.mysentosa.android.sg.utils.LogHelper;
import com.mysentosa.android.sg.utils.SentosaUtils;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class TicketsActivity extends BaseActivity implements OnClickListener {

    public static final String TAG = TicketsActivity.class.getSimpleName();

    public static final String TICKET_TYPE = "TICKET_TYPE";
    public static final String START_FROM_DEAL_SCREEN = "DEAL_SCREEN";

    private int itemID = -1;

    int TicketsCode = 0;
    int REQUEST_CODE = 1;
    int REQUEST_SHOPING_CODE = 2;

    private boolean startFromDealScreen = false;
    private Dialog successDialog;
    private int pickedTicketPosition = 0;

    //After purchase successfully, claim all the exclusive deals which point to the purchased ticket
    private ArrayList<Promotion> listOfExclusiveDeal;
    private int currentPage = 1;
    private ArrayList<Promotion> listOfClaimedExclusiveDeal;
    private ArrayList<Integer> listOfTicketID;

    @InjectView(R.id.header_title) TextView headerTitle;
    @InjectView(R.id.header_cart) ImageView headerCart;
    @InjectView(R.id.header_cart_item) TextView headerCartItem;
    @InjectView(R.id.list) ListView list;
    @InjectView(R.id.emptyItems) TextView emptyItemsText;
    @InjectView(R.id.availability) TextView availabilityText;
    @InjectView(R.id.packages) ImageView packagesTab;
    @InjectView(R.id.events) ImageView eventsTab;
    @InjectView(R.id.attractions) ImageView attractionsTab;
    @InjectView(R.id.purchased) ImageView purchasedTab;

    private TicketsAdapter ticketAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tickets_screen);
        ButterKnife.inject(this);

        FlurryAgent.logEvent(FlurryStrings.TicketPage);

        initializeViews();

        if (getIntent().getStringExtra(TICKET_TYPE) == null) {
            availabilityText.setVisibility(View.GONE);
            list.setVisibility(View.GONE);
            emptyItemsText.setVisibility(View.INVISIBLE);
            TicketsCode = -1;
            setGrey();
        } else {
            startFromDealScreen = getIntent().getBooleanExtra(START_FROM_DEAL_SCREEN, false);
            TicketsCode = getTicketCode(getIntent().getStringExtra(TICKET_TYPE));
            itemID = getIntent().getIntExtra(NavigationManagerActivity.ACTIVITY_TO_START_ID, -1);
            fetchRecords();
        }

        EasyTracker easyTracker = EasyTracker.getInstance(this);
        easyTracker.set(Fields.SCREEN_NAME, Const.GAStrings.TICKETS);
        easyTracker.send(MapBuilder
                        .createAppView()
                        .build()
        );
    }

    private void initializeViews() {
        headerTitle.setText(getString(R.string.tickets));

        emptyItemsText.setVisibility(View.GONE);
        availabilityText.setVisibility(View.GONE);

        eventsTab.setVisibility(View.GONE);

        SentosaUtils.addListViewFooter(this, list, true);
        ticketAdapter = new TicketsAdapter(this, TicketsCode);
        list.setAdapter(ticketAdapter);

        setUpSuccessDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Boolean isOnline = SentosaUtils.isOnline(this);
        if (!isOnline) {
            showNoInternetConnectionDialog();
        }
        getBadges();
    }

    public void showNoInternetConnectionDialog() {
        String title = getString(R.string.no_internet_connection_error_title);
        String message = getString(R.string.no_internet_connection_error_message);
        String positiveText = getString(R.string.no_internet_connection_error_button);

        AlertDialogHelper dialogHelper = new AlertDialogHelper(this, title, message, positiveText);
        dialogHelper.show();
    }

    @OnClick(R.id.header_cart)
    public void shoppingCart() {
        startActivityForResult(new Intent(TicketsActivity.this, TicketShopingCartActivity.class), REQUEST_SHOPING_CODE);
    }

    @OnItemClick(R.id.list)
    public void pickTicket(int position) {
        if (TicketsCode != Const.PURCHASE_TICKET_TYPE_CODE) {
            if (!Const.mTicketsItems.get(position).isIslanderExclusive() || SentosaUtils.isUserLogined(TicketsActivity.this)) {
                moveToTicketDetailActivity(position);
            } else {
                pickedTicketPosition = position;
                LoginDialog dialog = new LoginDialog(TicketsActivity.this);
                dialog.show();
            }
        } else {
            startActivityForResult(new Intent(TicketsActivity.this, TicketPurchaseDetailActivity.class).putExtra("Position", position), REQUEST_CODE);
        }
    }

    @OnClick({R.id.packages, R.id.events, R.id.attractions})
    public void clickTab(View view) {
        switch (view.getId()) {
            case R.id.packages:
                TicketsCode = Const.PACKAGE_TICKET_TYPE_CODE;
                fetchRecords();
                break;
            case R.id.events:
                TicketsCode = Const.EVENT_TICKET_TYPE_CODE;
                fetchRecords();
                break;
            case R.id.attractions:
                TicketsCode = Const.ATTRACTION_TICKET_TYPE_CODE;
                fetchRecords();
                break;
        }
    }

    @OnClick(R.id.purchased)
    public void seePurchased() {
        TicketsCode = Const.PURCHASE_TICKET_TYPE_CODE;
        fetchPurchaseRecords();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_more_deal:
                successDialog.dismiss();
                if (startFromDealScreen) {
                    finish();
                }
                break;

            case R.id.bt_checkout:
                successDialog.dismiss();
                shoppingCart();
                break;
        }
    }

    private void setGrey() {
        packagesTab.setImageResource(R.drawable.tab_packages_grey);
        eventsTab.setImageResource(R.drawable.tab_events_grey);
        attractionsTab.setImageResource(R.drawable.tab_attractions_grey);
        purchasedTab.setImageResource(R.drawable.tab_purchased);
        setOrange();
    }

    private void setOrange() {
        switch (TicketsCode) {
            case 0:
                packagesTab.setImageResource(R.drawable.tab_packages_orange);
                break;
            case 1:
                attractionsTab.setImageResource(R.drawable.tab_attractions_orange);
                break;
            case 2:
                eventsTab.setImageResource(R.drawable.tab_events_orange);
                break;
            case 3:
                purchasedTab.setImageResource(R.drawable.tab_purchased_pressed);
                break;

            default:
                break;
        }
    }

    private void fetchRecords() {
        setGrey();

        new GetTicketEventAsyncTask(this, new CustomCallback() {
            @Override
            public void isFnished(boolean isSucceed) {
                if (isSucceed) {
                    eventsTab.setVisibility(View.VISIBLE);
                }
            }
        }).execute();

        GetPackagesAsyncTask getPackAsync = new GetPackagesAsyncTask(
                TicketsActivity.this, new CustomCallback() {
            @Override
            public void isFnished(boolean isSucceed) {
                availabilityText.setVisibility(View.GONE);
                if (isSucceed) {
                    ticketAdapter = new TicketsAdapter(
                            TicketsActivity.this, TicketsCode);
                    list.setAdapter(ticketAdapter);
                    list.setVisibility(View.VISIBLE);
                    emptyItemsText.setVisibility(View.GONE);
                    ticketAdapter.notifyDataSetChanged();
                    if (itemID != -1) {
                        // In case we came from the Deal Exclusive screen and can not found the Ticket ID => finish
                        boolean foundID = false;
                        for (int i = 0; i < Const.mTicketsItems.size(); i++) {
                            if (Const.mTicketsItems.get(i).getId() == itemID) {
                                if (TicketsCode != Const.PURCHASE_TICKET_TYPE_CODE) {
                                    foundID = true;
                                    //reset the itemID incase user press "Back" button at the TicketDetailActivity class
                                    itemID = -1;
                                    moveToTicketDetailActivity(i);
                                    break;
                                }
                            }
                        }
                        if (!foundID && startFromDealScreen) {
                            finish();
                        }
                    }
                } else {
                    list.setVisibility(View.GONE);
                    emptyItemsText.setVisibility(View.INVISIBLE);
                }
            }
        }, TicketsCode);
        getPackAsync.execute();
    }

    private void fetchPurchaseRecords() {
        setGrey();
        MyPurchaseAsyncTask purchaseAsync = new MyPurchaseAsyncTask(TicketsActivity.this, new CustomCallback() {
            @Override
            public void isFnished(boolean isSucceed) {
                if (isSucceed) {
                    ticketAdapter = new TicketsAdapter(TicketsActivity.this, TicketsCode);
                    list.setAdapter(ticketAdapter);
                    list.setVisibility(View.VISIBLE);
                    emptyItemsText.setVisibility(View.GONE);
                    availabilityText.setVisibility(View.VISIBLE);
                    LogHelper.i("crash", "load purchased records");
                } else {
                    list.setVisibility(View.GONE);
                    emptyItemsText.setVisibility(View.VISIBLE);
                }
            }
        });
        purchaseAsync.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogHelper.i("crash", "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_CANCELED && startFromDealScreen) {
            finish();
        } else if (requestCode == LoginDialog.REQUEST_LOGIN_CODE) {
            if (SentosaUtils.isUserLogined(TicketsActivity.this)) {
                moveToTicketDetailActivity(pickedTicketPosition);
            }
        } else if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            successDialog.show();
        } else if (requestCode == REQUEST_SHOPING_CODE) {
            if (resultCode == RESULT_OK) {
                AlertHelper.showPopup(TicketsActivity.this,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                                if (startFromDealScreen) {
                                    finish();
                                }
                                if (TicketsCode == Const.PURCHASE_TICKET_TYPE_CODE)
                                    fetchPurchaseRecords();
                            }
                        }, getString(R.string.purchase_successfully));
                if (SentosaUtils.isUserLogined(this)) {
                    getDealsAndClaim();
                }
            } else if (resultCode == Const.RESULT_CANCEL_CODE) {
                AlertHelper.showPopup(TicketsActivity.this,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                                if (startFromDealScreen) {
                                    finish();
                                }
                            }
                        }, getString(R.string.purchase_unsuccessfully));
            }
        } else {
            if (TicketsCode != Const.PURCHASE_TICKET_TYPE_CODE)
                fetchRecords();
            else
                fetchPurchaseRecords();
        }
    }

    private void getBadges() {
        Cursor cursor = getContentResolver().query(ContentURIs.CART_URI, null, Queries.GET_TOTAL_SHOPPING_CART_ITEM_QUERY, null, Const.MANUAL);

        if (cursor.getCount() > 0) {
            cursor.moveToPosition(0);
            if (cursor.getInt(0) == 0) {
                headerCartItem.setVisibility(View.GONE);
            } else {
                headerCartItem.setVisibility(View.VISIBLE);
            }
            headerCartItem.setText(String.valueOf(cursor.getInt(0)));
        } else {
            headerCartItem.setVisibility(View.GONE);
        }

        cursor.close();
    }

    public int getTicketCode(String ticketName) {
        if (ticketName.equalsIgnoreCase("Event")) {
            return Const.EVENT_TICKET_TYPE_CODE;
        } else if (ticketName.equalsIgnoreCase("Package")) {
            return Const.PACKAGE_TICKET_TYPE_CODE;
        } else if (ticketName.equalsIgnoreCase("Attraction")) {
            return Const.ATTRACTION_TICKET_TYPE_CODE;
        } else {
            return Const.PACKAGE_TICKET_TYPE_CODE;
        }
    }

    private void setUpSuccessDialog() {
        successDialog = new Dialog(TicketsActivity.this, R.style.CustomDialog);
        successDialog.setContentView(R.layout.dialog_ticket_success);

        TextView tvTitle = (TextView) successDialog.findViewById(R.id.tv_title);
        tvTitle.setTypeface(((SentosaApplication) this.getApplication()).myridTypeFace);
        Button btMoreDeal = (Button) successDialog.findViewById(R.id.bt_more_deal);
        btMoreDeal.setTypeface(((SentosaApplication) this.getApplication()).myridTypeFace);
        btMoreDeal.setOnClickListener(TicketsActivity.this);
        Button btCheckOut = (Button) successDialog.findViewById(R.id.bt_checkout);
        btCheckOut.setTypeface(((SentosaApplication) this.getApplication()).myridTypeFace);
        btCheckOut.setOnClickListener(TicketsActivity.this);
    }

    private void moveToTicketDetailActivity(int position) {
        startActivityForResult(
                new Intent(TicketsActivity.this,
                        TicketDetailActivity.class).putExtra(
                        "Position", position).putExtra("TicketsCode",
                        TicketsCode), REQUEST_CODE);
    }

    private void getDealsAndClaim() {
        getPromotionEx();
    }

    private void claimDiscountedTicket() {
        listOfTicketID = SentosaUtils.getListOfBoughtTicketID(this);

        ArrayList<Integer> listOfFailedTicket = SentosaUtils.getListOfClaimedTicketIDFailed(this);

        //We add all the fail tickets last time
        if (listOfFailedTicket != null && listOfFailedTicket.size() > 0) {
            listOfTicketID.addAll(listOfFailedTicket);
        }

        //Check if ticket has been claimed or not
        for (Promotion deal : listOfClaimedExclusiveDeal) {
            if (deal.isDiscountType() && listOfTicketID.contains(deal.getDiscountedTicketEntityId())) {
                listOfTicketID.remove((Object) deal.getDiscountedTicketEntityId());
            }
        }

        if (listOfTicketID != null && listOfTicketID.size() > 0) {
            //Default, all of them will be added to the failed list, if we claim successfully, we'll remove later
            saveListOfTicketClaimedFailed();

            //Claim the 1st ticket, the Response of the API will do the rest
            claimDeal(returnDealIDByTicketID(listOfTicketID.get(0)));
        } else {
            dismissProgressDialog();
        }

    }

    private void getPromotionEx() {
        mPBLoading.show();
        GetPromotionExclusiveRequest request = new GetPromotionExclusiveRequest(this, mResponseListener, mErrorListener);
        SentosaApplication.mRequestQueue.add(request);
    }

    Listener<ArrayList<Promotion>> mResponseListener = new Listener<ArrayList<Promotion>>() {

        @Override
        public void onResponse(ArrayList<Promotion> listResult) {
            listOfExclusiveDeal = listResult;
            getClaimedDeals();
        }
    };


    private void getClaimedDeals() {
        GetMyClaimedDealsRequest request = new GetMyClaimedDealsRequest(this, SentosaUtils.getMemberID(this),
                SentosaUtils.getAccessToken(this), currentPage, mResponseMyClaimedListener, mErrorListener);
        SentosaApplication.mRequestQueue.add(request);
    }

    Listener<GetMyClaimedDealsRequestModel> mResponseMyClaimedListener = new Listener<GetMyClaimedDealsRequestModel>() {

        @Override
        public void onResponse(GetMyClaimedDealsRequestModel result) {
            if (listOfClaimedExclusiveDeal == null) {
                listOfClaimedExclusiveDeal = new ArrayList<Promotion>();
            }
            listOfClaimedExclusiveDeal.addAll(result.listPromotions);
            if (currentPage < result.pageSize) {
                currentPage += 1;
                getClaimedDeals();
            } else {
                claimDiscountedTicket();
            }
        }
    };

    private void claimDeal(long ticketID) {
        boolean found = false;

        //Claim the ticket if it's for Islander
        for (Promotion deal : listOfExclusiveDeal) {
            if (deal.isDiscountType() && ticketID == deal.getId()) {
                found = true;
                ClaimSpecialDealRequest request = new ClaimSpecialDealRequest(this, SentosaUtils.getMemberID(this),
                        SentosaUtils.getAccessToken(this), "", ticketID, mResponseClaimDealListener, mErrorListener);
                SentosaApplication.mRequestQueue.add(request);
                break;
            }
        }

        //in case we can not found the deal, dismiss the dialog
        if (!found) {
            dismissProgressDialog();
        }
    }

    Listener<Boolean> mResponseClaimDealListener = new Listener<Boolean>() {

        @Override
        public void onResponse(Boolean result) {
            if (result) {
                listOfTicketID.remove(0);

                //The successful ticket will be removed here
                saveListOfTicketClaimedFailed();

                if (listOfTicketID.size() > 0) {
                    claimDeal(returnDealIDByTicketID(listOfTicketID.get(0)));
                } else {
                    dismissProgressDialog();
                }
            } else {
                dismissProgressDialog();
            }
        }
    };

    //In case we fail to claim the ticket, we'll save it and claim next times user buy ticket
    private void saveListOfTicketClaimedFailed() {
        SentosaUtils.saveListOfClaimedTicketIDFailed(this, listOfTicketID);
    }

    private long returnDealIDByTicketID(long dealID) {
        for (Promotion deal : listOfExclusiveDeal) {
            if (deal.getDiscountedTicketEntityId() == dealID) {
                return deal.getId();
            }
        }
        return 0;
    }
}
