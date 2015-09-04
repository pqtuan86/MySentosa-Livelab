package com.mysentosa.android.sg.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.mysentosa.android.sg.BuildConfig;
import com.mysentosa.android.sg.EventsAndPromotionsActivity;
import com.mysentosa.android.sg.HomeActivity;
import com.mysentosa.android.sg.InformationActivity;
import com.mysentosa.android.sg.MapActivity;
import com.mysentosa.android.sg.ThingsToDo_MySentosaActivity;
import com.mysentosa.android.sg.TicketsActivity;
import com.mysentosa.android.sg.models.CartItems;
import com.mysentosa.android.sg.models.FailedItems;
import com.mysentosa.android.sg.models.LocationItem;
import com.mysentosa.android.sg.models.MyPurchasesDetailItem;
import com.mysentosa.android.sg.models.MyPurchasesDetailSplitItem;
import com.mysentosa.android.sg.models.MyPurchasesItem;
import com.mysentosa.android.sg.models.TicketDetailItem;
import com.mysentosa.android.sg.models.TicketEventDetailItem;
import com.mysentosa.android.sg.models.TicketItem;

public class Const {

	// public static final long UPDATE_EVENT_DURATION = 2*60*60; // update event
	// and promotion every 2 hours
	public static final int EVENT_LIST_LOADER = 0;
	public static final int PROMOTION_LIST_LOADER = 1;
	public static final int ACTIVE = 1;
	public static final int INACTIVE = 0;
	public static final String LAST_RETRIEVAL_TIME = "LastRetrievalTime";
	public static final String LAST_ANNOUNCEMENTS_RETRIEVAL_TIME = "LastAnnouncementsRetrievalTime";

	public static final String NODE_ID = "node_id";
	
	// category types
	public static final String ATTRACTION = "Attractions";
	public static final String BUS = "Bus";
	public static final String TRAIN = "Sentosa Express";
	public static final String TRAM = "Tram";
	public static final String CABLE = "Cable";
	public static final String BUS_INT = "Bus Interchange";
	public static final String MRT = "Singapore MRT";
	public static final String CABLEWAY = "Cableway";
	public static final String CARPARK = "Car Park";
	public static final String LOCKER = "Locker";
	public static final String PRAYER_ROOM = "Prayer Room";
	public static final String MOTHERS_ROOM = "Mothers' Room";
	public static final String INFORMATION_COUNTER = "Info Counter";
	public static final String MONEY_CHANGER = "Money Changer";
	public static final String TOILET = "Toilet";
	public static final String TAXI = "Taxi";
	public static final String WIFI = "WiFi";
	public static final String ATM = "ATM";
	public static final String FIRST_AID = "First Aid";
	public static final String SHOPPING = "Shopping";
	public static final String FNB = "Food and Beverages";
	public static final String HOTEL_AND_SPA = "Hotel and Spa";

	// Crittercism key
	public static String CRITTERCISM_KEY;
	static {
		if (BuildConfig.DEBUG)
			CRITTERCISM_KEY = "50e6cf4df716964fbe00001f"; // staging
		else
			CRITTERCISM_KEY = "50e6cf7659e1bd54ad00001d"; // production
	}
	
	//GA Strings
	public static class GAStrings
	{
	    //Screen
		public static final String LANDING = "Landing Page";
		public static final String MAP = "Map";
		public static final String DIRECTIONS = "Directions";
		public static final String TICKETS = "Tickets - Main";
		public static final String CHECKOUT = "Checkout Form";
		public static final String TELEMONEY = "Telemoney Page";
		public static final String THINGS_TO_DO = "Things to do - Main";
		public static final String INFORMATION = "Information";
		public static final String ATTARCTION = "Attraction Details";
		public static final String DEALS_MAIN = "Deals - Main";
		public static final String ISLANDER_MAIN = "Islander - Main"; 
		
		//Event
		public static final String ISLANDER_EVENT_CATEGORY = "islander";
		public static final String ISLANDER_REGISTER = "tapped on islander register";
		public static final String ISLANDER_LOGIN = "tapped on islander login";
		public static final String ISLANDER_RENEW = "tapped on islander renew";
	}

	// Flurry Strings
	public static class FlurryStrings {

		public static final String FLURRY_KEY;
		static {
			if (BuildConfig.DEBUG) {
				FLURRY_KEY = "6PN88D8F665NTJ6GNZXV"; // staging
			} else
				FLURRY_KEY = "BVV62F2NHFQ4KKB2LZUT"; // production
		}

		public static final String MenuLaunch = "Menu Launch";
		public static final String MenuClose = "Menu Close";
		public static final String HomePage = "Home Page";
		public static final String HomePageLaunch = "Home Page Launch";
		public static final String ItineraryList = "Itinerary List";
		public static final String Itinerary = "Itinerary";
		public static final String FlurryEventName = "Event Name";
		public static final String LocationDetailsSourceItinerary = "Location Details - Source Itinerary";
		public static final String LocationDetailsSourceMyBookmarks = "Location Details - Source My Bookmarks";
		public static final String LocationDetailsSourceMap = "Location Details - Source Map";
		public static final String LocationDetailsSourceCategoryList = "Location Details - Source Category List";
		public static final String LocationDetailsSourceEvents = "Location Details - Source Events";
		public static final String LocationDetailsSourcePromos = "Location Details - Source Promos";
		public static final String NameOfLocation = "Name of Location";
		public static final String Category = "Category";
		public static final String MapPage = "Map Page";
		public static final String TicketPage = "Tickets - Main";
		public static final String CheckoutPage = "Checkout Form";
		public static final String TelemoneyPage = "Telemoney Page";
		public static final String MapPageFilter = "Map Page (Filter)";
		public static final String MapPageDirections = "Map Page (Directions)";
		public static final String MapPageFilterCategoriesChosen = "Map Page (Filter Categories Chosen)";
		public static final String CategorySelected = "Category Selected"; // check
																			// this
		public static final String MapPageList = "Map Page (List)";
		public static final String MapPageCategoryList = "Map Page (Category List)";
		public static final String MapPageCurrentLocation = "Map Page (Current Location)";
		public static final String MapPageEraseRoute = "Map Page (Erase Route)";
		public static final String MapRouting = "Map Routing";
		public static final String TypeOfRouting = "Type Of Routing";
		public static final String LocationDetailsCall = "Location Details (Call)";
		public static final String LocationDetailsVideo = "Location Details (Video)";
		public static final String EventDetailsLocationDetails = "Event Details (Location Details)";
		public static final String PromotionDetailsLocationDetails = "Promotion Details (Location Details)";
		public static final String EventsAndPromotionsPage = "Events and Promotions Page";
		public static final String PromotionsPage = "Deals Page";
		public static final String EventsListPage = "Events List Page";
		public static final String PromotionsListPage = "Promotions List Page";
		public static final String EventDetailsEvents = "Event Details (Events)";
		public static final String PromotionsDetailsPromotions = "Promotion Details (Promotions)";
		public static final String EventDetailsCall = "Event Details (Call)";
		public static final String PromotionsDetailsCall = "Promotion Details (Call)";
		public static final String EventDetailsFacebook = "Event Details (Facebook)";
		public static final String PromotionsDetailsFacebook = "Promotion Details (Facebook)";
		public static final String EventDetailsEmail = "Event Details (Facebook)";
		public static final String PromotionsDetailsEmail = "Promotion Details (Facebook)";
		public static final String EventDetailsTwitter = "Event Details (Twitter)";
		public static final String PromotionsDetailsTwitter = "Promotion Details (Twitter)";
		public static final String EventDetailsLinked = "Event Details (LinkedTicket)";
		public static final String PromotionsDetailsLinked = "Promotion Details (LinkedTicket)";
		public static final String InformationPage = "Information Page";
		public static final String ThingsToDoPage = "Things To Do Page";
		public static final String MySentosaPage = "Things To Do Page";
		public static final String AttractionInformation = "Attraction (Information)";
		public static final String FoodAndBeveragesInformation = "Food and Beverages (Information)";
		public static final String HotelsAndSpasInformation = "Hotels and Spas (Information)";
		public static final String EmergencyHotline = "Emergency Hotline";
		public static final String GettingToSentosa = "Getting To Sentosa";
		public static final String TrafficUpdates = "Traffic Updates";
		public static final String IslanderPrivileges = "Islander Privileges";
		public static final String IslanderRegister = "Islander Register";
		public static final String IslanderRenew = "Islander Renew";
		public static final String ProfileAndSettings = "Profile and Settings";
		public static final String ContactUs = "Contact Us";
		public static final String Type = "Type";
		public static final String Email = "Email";
		public static final String Call = "Call";
		public static final String WALK = "WALK";
		public static final String TRANSPORT = "TRANSPORT";

		public static final String ItemInThingsToDo = "Item (Things to do)";
		public static final String LocationDetail = "Location Stats";
		public static final String TopAttraction = "Top Attraction";
		public static final String DirectionsFromLocationDetails = "Directions from Location Details";
		public static final String ShowBookmarksInMap = "Map Page (Show Bookmarks)";
		public static final String ShowByCategoriesInMap = "Map Page (Show by Categories)";
		public static final String LocateMeInMap = "Map Page (Locate Me)";

		public static final String ShowBookmarksInMySentosa = "Show Bookmarks list";
		public static final String ShowProfileAndSettingsInMySentosa = "Show Profile and Settings";

		public static String getEventNameForActivity(String activityName) {
			if (activityName.equals(HomeActivity.class.getName()))
				return HomePage;
			else if (activityName.equals(MapActivity.class.getName()))
				return MapPage;
			else if (activityName.equals(EventsAndPromotionsActivity.class
					.getName()))
				return PromotionsPage;
			else if (activityName.equals(InformationActivity.class.getName()))
				return InformationPage;
			else if (activityName.equals(ThingsToDo_MySentosaActivity.class
					.getName()))
				return ThingsToDoPage;
			else if (activityName.equals(ThingsToDo_MySentosaActivity.class
					.getName()))
				return MySentosaPage;
			else if (activityName.equals(TicketsActivity.class.getName()))
				return TicketPage;
			return "";
		}
	}

	public static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
	public static SimpleDateFormat sdfDate = new SimpleDateFormat(
			"dd MMMM yyyy");

	// TICKETS
	public static ArrayList<TicketItem> mTicketsItems = new ArrayList<TicketItem>();
	public static ArrayList<TicketDetailItem> mTicketsdetailItems = new ArrayList<TicketDetailItem>();
	public static ArrayList<TicketEventDetailItem> mTicketsEventdetailItems = new ArrayList<TicketEventDetailItem>();
	public static StringBuilder sbTerms;

	public static ArrayList<CartItems> mEventCartItems = new ArrayList<CartItems>();
	public static ArrayList<Integer> mFailedTicketsItems = new ArrayList<Integer>();
	public static ArrayList<FailedItems> mFailedTicketsCheckoutItems = new ArrayList<FailedItems>();
	public static ArrayList<MyPurchasesItem> mPurchaseItems = new ArrayList<MyPurchasesItem>();
	public static ArrayList<MyPurchasesDetailItem> mPurchaseDetailItems = new ArrayList<MyPurchasesDetailItem>();
	public static ArrayList<MyPurchasesDetailSplitItem> mPurchaseDetailSplitItems = new ArrayList<MyPurchasesDetailSplitItem>();
	public static ArrayList<LocationItem> mLocationItem = new ArrayList<LocationItem>();

	public static float mBookingFees = -1;
	public static final String MANUAL = "MANUAL";
	
	public static int EVENT_TICKET_TYPE_CODE = 2;
	public static int PACKAGE_TICKET_TYPE_CODE = 0;
	public static int ATTRACTION_TICKET_TYPE_CODE = 1;
	public static int PURCHASE_TICKET_TYPE_CODE = 3;
	public static int RESULT_CANCEL_CODE = 10;

	
	//Notification Type
	public static String NOTIFICATION_TYPE = "NotificationType";
	public static String NOTIFICATION_TICKET_TYPE = "TicketType";
	public static String NOTIFICATION_TYPE_ID = "Id";
	public static String NOTIFICATION_TYPE_EVENT = "event";
	public static String NOTIFICATION_TYPE_DEAL = "deal";
	public static String NOTIFICATION_TYPE_LOCATION = "location";
	public static String NOTIFICATION_TYPE_TICKET = "ticket";
	public static String NOTIFICATION_TYPE_TICKET_EVENT = "event";
	public static String NOTIFICATION_TYPE_TICKET_PACKAGE = "package";
	public static String NOTIFICATION_TYPE_TICKET_ATTRACTION = "attraction";
	
	//API
	public static String API_ISLANDER_BIRTHDAY = "birthdate";
	public static String API_ISLANDER_ACCOUNT_NUMBER = "accountNumber";
	public static String API_ISLANDER_ACCESS_TOKEN = "accessToken";
	public static String API_ISLANDER_ISLANDER_ID = "islanderID";
	public static String API_ISLANDER_QRCODE = "qrCode";
	public static String API_ISLANDER_PROMOTION_ID = "promotionId";
	public static String API_ISLANDER_CLAIMED = "Claimed";
	public static String API_ISLANDER_STATUS_CODE = "StatusCode";
	public static String API_ISLANDER_DATA = "Data";
	public static String API_ISLANDER_ERROR = "Error";
	public static String API_ISLANDER_MESSAGE = "Message";
	public static String API_ISLANDER_PROMOTIONS = "Promotions";
	public static String API_ISLANDER_EMAIL = "email";
	public static String API_ISLANDER_ADDRESS = "address";
	public static String API_ISLANDER_MOBILE = "mobile";
	public static int API_ISLANDER_LOGIN_ACCOUNT_NOT_FOUND = 4011;
	public static int API_ISLANDER_LOGIN_INVALID_BIRTHDAY = 4012;
	public static int API_ISLANDER_LOGIN_MEMBER_NOT_REGISTERED = 4013;
	public static int API_ISLANDER_LOGIN_ACCOUNT_EXPIRED = 4014;
	
	//SHARED PREFERENCE
	public static final String SHARED_PREFERENCE = "Sentosa";
	public static final String MEMBERSHIP_ID = "MembershipID";
	public static final String ACCESS_TOKEN = "AccessToken";
	public static final String DATE_OF_BIRTH = "DateOfBirth";
	public static final String BOUGHT_TICKET = "BoughtTicket";
	public static final String CLAIMED_TICKET_FAILED = "BoughtTicketFailed";
	
	public static final String FIRST_TIME_LOGIN = "FirstTimeLogin";
	
	//Deal Type
	public static final String DEAL_TYPE_FREE_REWARD = "free-reward";
	public static final String DEAL_TYPE_DISCOUTED = "discounted";
	public static final String DEAL_TYPE_ONSITE = "on-site";
	
	//UA tag
	public static final String UA_ISLANDER_TAG = "islander";
}
