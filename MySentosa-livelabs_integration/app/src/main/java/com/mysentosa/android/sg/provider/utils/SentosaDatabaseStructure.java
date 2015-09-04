package com.mysentosa.android.sg.provider.utils;

import java.util.Locale;

import android.net.Uri;

import com.mysentosa.android.sg.EventsAndPromotionsDetailActivity;
import com.mysentosa.android.sg.utils.Const;

public interface SentosaDatabaseStructure {

	public static final String SCHEME = "content://";
	public static final String AUTHORITY = "com.mysentosa.android.sg.provider";
	public static final String _ID = "_id";

	public static class ContentURIs {
		// generic uri. will suffice for raw queries
		public static final String SENTOSA_PATH = "SENTOSA";
		public static final Uri SENTOSA_URI = Uri.parse(SCHEME + AUTHORITY
				+ "/" + SENTOSA_PATH);
		public static final int SENTOSA_CODE = 1;

		public static final String EVENTS_PATH = "EVENTS";
		public static final int EVENTS_CODE = 2;
		public static final Uri EVENTS_URI = Uri.parse(SCHEME + AUTHORITY + "/"
				+ EVENTS_PATH);

		public static final String PROMOTIONS_PATH = "PROMOTIONS";
		public static final int PROMOTIONS_CODE = 3;
		public static final Uri PROMOTIONS_URI = Uri.parse(SCHEME + AUTHORITY
				+ "/" + PROMOTIONS_PATH);

		public static final String NODE_DETAILS_PATH = "NODE_DETAILS";
		public static final int NODE_DETAILS_CODE = 4;
		public static final Uri NODE_DETAILS_URI = Uri.parse(SCHEME + AUTHORITY
				+ "/" + NODE_DETAILS_PATH);

		public static final String NODE_DETAILS_TEMP_PATH = "NODE_DETAILS_TEMP";
		public static final int NODE_DETAILS_TEMP_CODE = 5;
		public static final Uri NODE_DETAILS_TEMP_URI = Uri.parse(SCHEME
				+ AUTHORITY + "/" + NODE_DETAILS_TEMP_PATH);

		public static final String NODES_TEMP_PATH = "NODES_TEMP";
		public static final int NODES_TEMP_CODE = 6;
		public static final Uri NODES_TEMP_URI = Uri.parse(SCHEME + AUTHORITY
				+ "/" + NODES_TEMP_PATH);

		public static final String EDGES_TEMP_PATH = "EDGES_TEMP";
		public static final int EDGES_TEMP_CODE = 7;
		public static final Uri EDGES_TEMP_URI = Uri.parse(SCHEME + AUTHORITY
				+ "/" + EDGES_TEMP_PATH);

		public static final String EDGE_OVERLAYS_TEMP_PATH = "EDGE_OVERLAYS_TEMP";
		public static final int EDGE_OVERLAYS_TEMP_CODE = 8;
		public static final Uri EDGE_OVERLAYS_TEMP_URI = Uri.parse(SCHEME
				+ AUTHORITY + "/" + EDGE_OVERLAYS_TEMP_PATH);

		public static final String THINGS_TO_DO_PATH = "THINGS_TO_DO";
		public static final int THINGS_TO_DO_CODE = 9;
		public static final Uri THINGS_TO_DO_URI = Uri.parse(SCHEME + AUTHORITY
				+ "/" + THINGS_TO_DO_PATH);

		public static final String CART_PATH = "CART_ITEMS";
		public static final int CART_CODE = 10;
		public static final Uri CART_URI = Uri.parse(SCHEME + AUTHORITY + "/"
				+ CART_PATH);

	}

	public static class Queries {
		// ----------------------THINGS TO DO RELATED-----------------------
		public static final String THINGS_TO_DO_LIST_QUERY = "SELECT COUNT(*) ,"
				+ ThingsToDoData.NAME_COL
				+ ", "
				+ ThingsToDoData.DESCRIPTION_COL
				+ ", "
				+ ThingsToDoData.ID_COL
				+ ", "
				+ ThingsToDoData.ICON_ID_COL
				+ " FROM "
				+ TABLE_THINGS_TO_DO
				+ " GROUP BY "
				+ ThingsToDoData.NAME_COL
				+ " ORDER BY " + ThingsToDoData.ID_COL;

		public static String searchQueryForThingsToDo(String searchTerm) {
			return "SELECT COUNT(*) ,"
					+ ThingsToDoData.NAME_COL
					+ ", "
					+ ThingsToDoData.DESCRIPTION_COL
					+ ", "
					+ ThingsToDoData.ID_COL
					+ ", "
					+ ThingsToDoData.ICON_ID_COL
					+ " FROM "
					+ TABLE_THINGS_TO_DO
					+ " WHERE "
					+ ThingsToDoData.NAME_COL + " LIKE '%" + searchTerm + "%'"
					+ " GROUP BY "
					+ ThingsToDoData.NAME_COL
					+ " ORDER BY " + ThingsToDoData.ID_COL;
		}

		public static String NODE_DETAILS_FOR_THINGS_TO_DO_TYPE(String category) {
			return "SELECT " + TABLE_NODE_DETAILS + ".*, " + TABLE_NODES + "."
					+ NodeData.LATITUDE_COL + ", " + TABLE_NODES + "."
					+ NodeData.LONGITUDE_COL + " FROM " + TABLE_NODE_DETAILS
					+ " JOIN " + TABLE_THINGS_TO_DO + " ON ("
					+ TABLE_THINGS_TO_DO + "."
					+ ThingsToDoData.DETAIL_NODE_ID_COL + "="
					+ TABLE_NODE_DETAILS + "." + NodeDetailsData.ID_COL
					+ " AND " + TABLE_THINGS_TO_DO + "."
					+ ThingsToDoData.NAME_COL + " LIKE \"" + category
					+ "\") JOIN " + TABLE_NODES + " ON " + TABLE_NODE_DETAILS
					+ "." + NodeDetailsData.NODE_ID_COL + "=" + TABLE_NODES
					+ "." + NodeData.ID_COL + " ORDER BY LENGTH("
					+ NodeDetailsData.SECTION_COL + ") ASC,"
					+ NodeDetailsData.SECTION_COL
					+ " COLLATE LOCALIZED COLLATE NOCASE ASC, "
					+ NodeDetailsData.TITLE_COL
					+ " COLLATE LOCALIZED COLLATE NOCASE ASC";
		}

		public static String ZONES_GROUP_FOR_CATEGORY_QUERY(String category) {
			String additionalClause = " WHERE " + TABLE_THINGS_TO_DO + "."
					+ ThingsToDoData.DETAIL_NODE_ID_COL + "="
					+ TABLE_NODE_DETAILS + "." + NodeDetailsData.ID_COL
					+ " AND " + TABLE_THINGS_TO_DO + "."
					+ ThingsToDoData.NAME_COL + " LIKE \"" + category + "\"";
			additionalClause += " GROUP BY " + NodeDetailsData.SECTION_COL
					+ " ORDER BY LENGTH(" + NodeDetailsData.SECTION_COL
					+ ") ASC, " + NodeDetailsData.SECTION_COL
					+ " COLLATE LOCALIZED COLLATE NOCASE ASC";

			// return list of nodes
			String query = "SELECT " + TABLE_NODE_DETAILS + "."
					+ NodeDetailsData.ID_COL + ", " + TABLE_NODE_DETAILS + "."
					+ NodeDetailsData.SECTION_COL + " FROM "
					+ TABLE_NODE_DETAILS + " JOIN " + TABLE_THINGS_TO_DO
					+ additionalClause;
			return query;
		}

		// ----------------------MAP-ONLY-----------------------
		public static final String NODE_QUERY = "SELECT " + TABLE_NODES + "."
				+ NodeData.ID_COL + ", " + TABLE_NODES + "."
				+ NodeData.LATITUDE_COL + ", " + TABLE_NODES + "."
				+ NodeData.LONGITUDE_COL + ", " + TABLE_NODE_DETAILS + "."
				+ NodeDetailsData.TITLE_COL + " FROM " + TABLE_NODES
				+ " LEFT JOIN " + TABLE_NODE_DETAILS + " ON " + TABLE_NODES
				+ "." + NodeData.ID_COL + " = " + TABLE_NODE_DETAILS + "."
				+ NodeDetailsData.NODE_ID_COL + ";";

		public static final String EDGES_QUERY = "SELECT * FROM " + TABLE_EDGES
				+ ";";

		public static String EDGE_OVERLAYS_QUERY(String sortOrder) {
			return "SELECT " + TABLE_EDGE_OVERLAYS + "."
					+ EdgeOverlayData.LATITUDE_COL + ", " + TABLE_EDGE_OVERLAYS
					+ "." + EdgeOverlayData.LONGITUDE_COL + " FROM "
					+ TABLE_EDGE_OVERLAYS + " WHERE " + TABLE_EDGE_OVERLAYS
					+ "." + EdgeOverlayData.EDGE_ID_COL + "=? ORDER BY "
					+ EdgeOverlayData.ID_COL + " " + sortOrder + ";";
		}

		// ----------------------BOOKMARKS RELATED-----------------------
		public static final String MYBOOKMARKS_QUERY = "SELECT "
				+ TABLE_NODE_DETAILS + "." + NodeDetailsData.TITLE_COL + ", "
				+ TABLE_NODE_DETAILS + "." + NodeDetailsData.NODE_ID_COL + ", "
				+ TABLE_NODE_DETAILS + "." + NodeDetailsData.CATEGORY_COL
				+ ", " + TABLE_NODE_DETAILS + "." + NodeDetailsData.SECTION_COL
				+ ", " + TABLE_NODE_DETAILS + "." + NodeDetailsData.ID_COL
				+ ", " + TABLE_NODES + "." + NodeData.LATITUDE_COL + ", "
				+ TABLE_NODES + "." + NodeData.LONGITUDE_COL + " FROM "
				+ TABLE_NODE_DETAILS + ", " + TABLE_NODES + "  WHERE "
				+ TABLE_NODE_DETAILS + "." + NodeDetailsData.IS_BOOKMARKED_COL
				+ "=1 AND " + TABLE_NODE_DETAILS + "."
				+ NodeDetailsData.NODE_ID_COL + "=" + TABLE_NODES + "."
				+ NodeData.ID_COL + " ORDER BY LENGTH("
				+ NodeDetailsData.SECTION_COL + ") ASC,"
				+ NodeDetailsData.SECTION_COL
				+ " COLLATE LOCALIZED COLLATE NOCASE ASC, "
				+ TABLE_NODE_DETAILS + "." + NodeDetailsData.TITLE_COL
				+ " COLLATE LOCALIZED COLLATE NOCASE ASC";

		public static String ZONES_GROUP_FOR_MYBOOKMARKS_QUERY() {
			String additionalClause = "  WHERE " + TABLE_NODE_DETAILS + "."
					+ NodeDetailsData.IS_BOOKMARKED_COL + "=1";
			additionalClause += " GROUP BY " + NodeDetailsData.SECTION_COL
					+ " ORDER BY LENGTH(" + NodeDetailsData.SECTION_COL
					+ ") ASC, " + NodeDetailsData.SECTION_COL
					+ " COLLATE LOCALIZED COLLATE NOCASE ASC";

			// return list of nodes
			String query = "SELECT " + TABLE_NODE_DETAILS + "."
					+ NodeDetailsData.ID_COL + ", " + TABLE_NODE_DETAILS + "."
					+ NodeDetailsData.SECTION_COL + " FROM "
					+ TABLE_NODE_DETAILS + additionalClause;
			return query;
		}

		// ----------------------BOOKMARKS RELATED-----------------------
		public static final String DIRECTIONS_AUTOCOMPLETE_QUERY = "SELECT "
				+ TABLE_NODE_DETAILS + "." + NodeDetailsData.NODE_ID_COL
				+ " AS _id, " + TABLE_NODE_DETAILS + "."
				+ NodeDetailsData.TITLE_COL + " FROM " + TABLE_NODE_DETAILS
				+ "  WHERE " + TABLE_NODE_DETAILS + "."
				+ NodeDetailsData.TITLE_COL
				+ " LIKE '%--ARGS--%' " // question mark ? was not workign here
				+ " ORDER BY " + TABLE_NODE_DETAILS + "."
				+ NodeDetailsData.TITLE_COL + "  ASC LIMIT 5";

		public static final String DIRECTIONS_AUTOCOMPLETE_DEFAULT_QUERY = "SELECT "
				+ TABLE_NODE_DETAILS
				+ "."
				+ NodeDetailsData.NODE_ID_COL
				+ " AS _id, "
				+ TABLE_NODE_DETAILS
				+ "."
				+ NodeDetailsData.TITLE_COL
				+ " FROM "
				+ TABLE_NODE_DETAILS
				+ "  ORDER BY "
				+ TABLE_NODE_DETAILS
				+ "."
				+ NodeDetailsData.TITLE_COL + " ASC LIMIT 5";

		public static String NODE_DETAILS_QUERY(int selectionArgsLength) {
			String query = "SELECT " + TABLE_NODE_DETAILS + "."
					+ NodeDetailsData.NODE_ID_COL + ", " + TABLE_NODE_DETAILS
					+ "." + NodeDetailsData.TITLE_COL + ", "
					+ TABLE_NODE_DETAILS + "." + NodeDetailsData.CATEGORY_COL
					+ ", " + TABLE_NODES + "." + NodeData.LATITUDE_COL + ", "
					+ TABLE_NODES + "." + NodeData.LONGITUDE_COL + " FROM "
					+ TABLE_NODE_DETAILS + ", " + TABLE_NODES + " WHERE "
					+ TABLE_NODE_DETAILS + "." + NodeDetailsData.NODE_ID_COL
					+ "=" + TABLE_NODES + "." + NodeData.ID_COL + " AND "
					+ TABLE_NODE_DETAILS + "." + NodeDetailsData.CATEGORY_COL
					+ " IN ";
			String matchValues = "(";
			for (int i = 0; i < selectionArgsLength; i++) {
				matchValues += (i == 0 ? "" : ",") + "?";
			}
			matchValues += ");";
			query += matchValues;
			return query;
		}

		public static String CATEGORIES_LIST_QUERY(String selection) {
			String whereClause = "";
			String sort = "";
			if (selection != null) {
				whereClause = " WHERE category like '%" + selection + "%' ";
				sort = " ORDER BY CASE WHEN " + NodeDetailsData.CATEGORY_COL
						+ " NOT LIKE " + "\'" + selection + "%' AND "
						+ NodeDetailsData.CATEGORY_COL + " NOT LIKE " + "\'% "
						+ selection + "%' THEN 3 END, CASE WHEN "
						+ NodeDetailsData.CATEGORY_COL + " LIKE " + "\'"
						+ selection + "%\' THEN 1 ELSE 2 END, "
						+ NodeDetailsData.CATEGORY_COL
						+ " COLLATE LOCALIZED COLLATE NOCASE ASC";
			} else {
				sort += " ORDER BY " + NodeDetailsData.CATEGORY_COL
						+ " COLLATE LOCALIZED COLLATE NOCASE ASC";
			}
			String query = "SELECT " + NodeDetailsData.CATEGORY_COL + ", "
					+ NodeDetailsData.ID_COL + " FROM " + TABLE_NODE_DETAILS
					+ whereClause + " GROUP BY " + NodeDetailsData.CATEGORY_COL
					+ sort;
			return query;
		}

		public static String NODES_LIST(String selection) {
			String additionalClause = new String();
			if (selection != null) {
				additionalClause += " WHERE " + NodeDetailsData.TITLE_COL
						+ " like '%" + selection + "%' ";
				additionalClause += " ORDER BY CASE WHEN "
						+ NodeDetailsData.TITLE_COL + " NOT LIKE " + "\'"
						+ selection + "%' AND " + NodeDetailsData.TITLE_COL
						+ " NOT LIKE " + "\'% " + selection
						+ "%' THEN 3 END, CASE WHEN "
						+ NodeDetailsData.TITLE_COL + " LIKE " + " \'"
						+ selection + "%\' THEN 1 ELSE 2 END, "
						+ NodeDetailsData.TITLE_COL
						+ " COLLATE LOCALIZED COLLATE NOCASE ASC";
			} else {
				additionalClause += " ORDER BY " + NodeDetailsData.TITLE_COL
						+ " COLLATE LOCALIZED COLLATE NOCASE ASC";
			}

			// return list of nodes
			String query = "SELECT " + NodeDetailsData.ID_COL + ", "
					+ NodeDetailsData.NODE_ID_COL + ", "
					+ NodeDetailsData.TITLE_COL + " FROM " + TABLE_NODE_DETAILS
					+ additionalClause;
			return query;
		}

		public static String CATEGORIES_LIST_FILTER_QUERY(
				int selectionArgsLength) {
			String query = "SELECT DISTINCT " + NodeDetailsData.CATEGORY_COL
					+ "," + NodeDetailsData.ID_COL + " FROM "
					+ TABLE_NODE_DETAILS;
			if (selectionArgsLength > 0) {
				query += " WHERE " + NodeDetailsData.CATEGORY_COL + " NOT IN ";
				String matchValues = "(";
				for (int i = 0; i < selectionArgsLength; i++) {
					matchValues += (i == 0 ? "" : ",") + "?";
				}
				matchValues += ") ";
				query += matchValues;
			}
			query += " GROUP BY " + NodeDetailsData.CATEGORY_COL + ";";
			return query;
		}

		public static final String ATTRACTION_DETAIL_QUERY = "SELECT "
				+ TABLE_NODE_DETAILS + "." + NodeDetailsData.TITLE_COL + ", "
				+ TABLE_NODE_DETAILS + "." + NodeDetailsData.ID_COL + ", "
				+ TABLE_NODE_DETAILS + "." + NodeDetailsData.DESCRIPTION_COL + ", "
				+ TABLE_NODE_DETAILS + "." + NodeDetailsData.VIDEO_URL_COL + ", "
				+ TABLE_NODE_DETAILS + "." + NodeDetailsData.IMAGE_NAME_COL + ", "
				+ TABLE_NODE_DETAILS + "." + NodeDetailsData.CATEGORY_COL + ", "
				+ TABLE_NODE_DETAILS + "." + NodeDetailsData.ADMISSION_COL + ", "
				+ TABLE_NODE_DETAILS + "." + NodeDetailsData.OTHER_DETAILS_COL + ", "
				+ TABLE_NODE_DETAILS + "." + NodeDetailsData.SECTION_COL + ", "
				+ TABLE_NODE_DETAILS + "." + NodeDetailsData.EMAIL_COL + ", "
				+ TABLE_NODE_DETAILS + "." + NodeDetailsData.WEBSITE_COL + ", "
				+ TABLE_NODE_DETAILS + "." + NodeDetailsData.IS_BOOKMARKED_COL + ", "
				+ TABLE_NODE_DETAILS + "." + NodeDetailsData.CONTACT_NO_COL + ", "
				+ TABLE_NODE_DETAILS + "." + NodeDetailsData.OPENING_TIMES_COL
				+ " FROM " + TABLE_NODE_DETAILS
				+ " WHERE " + TABLE_NODE_DETAILS + "." + NodeDetailsData.NODE_ID_COL + "=?";
		
		public static final String TITLE_QUERY = "SELECT "
				+ TABLE_NODE_DETAILS + "." + NodeDetailsData.TITLE_COL + " FROM "
						+ TABLE_NODE_DETAILS + " WHERE " + TABLE_NODE_DETAILS + "."
						+ NodeDetailsData.NODE_ID_COL + "= ?";

		public static String EVENTS_PROMOTIONS_LOCATIONS_QUERY(boolean isEvent,
				long id) {
			String TABLE_NAME = isEvent ? TABLE_EVENTS : TABLE_PROMOTIONS;
			String query = "SELECT " + TABLE_NODE_DETAILS + "."
					+ NodeDetailsData.TITLE_COL + ", " + TABLE_NODE_DETAILS
					+ "." + NodeDetailsData.NODE_ID_COL + ", "
					+ TABLE_NODE_DETAILS + "." + NodeDetailsData.ID_COL
					+ " FROM " + TABLE_NODE_DETAILS + " INNER JOIN "
					+ TABLE_NAME + " ON " + TABLE_NODE_DETAILS + "."
					+ NodeDetailsData.ID_COL + "=" + TABLE_NAME + "."
					+ EventsPromotionsBase.LOCATION_IDS_COL + " WHERE "
					+ TABLE_NAME + "." + EventsPromotionsBase.ID_COL + "=" + id;
			return query;
		}

		public static String EVENTS_PROMOTIONS_DETAIL_QUERY(boolean isEvent,
				long id) {
			String TABLE_NAME = isEvent ? TABLE_EVENTS : TABLE_PROMOTIONS;
			String whereClause = String.format(Locale.US, "%s=%d AND %s=%d",
					EventsPromotionsBase.ID_COL, id,
					EventsPromotionsBase.LOCATION_IDS_COL,
					EventsAndPromotionsDetailActivity.INVALID_LOC);
			String query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ whereClause + " ORDER BY "
					+ EventsPromotionsBase.DEFAULT_SORT_ORDER;
			return query;
		}

		public static String EVENTS_PROMOTIONS_LIST_QUERY(boolean isEvent) {
			String TABLE_NAME = isEvent ? TABLE_EVENTS : TABLE_PROMOTIONS;
			String whereClause = EventsPromotionsBase.LOCATION_IDS_COL + "="
					+ EventsPromotionsBase.INVALID_LOC + " AND "
					+ EventsPromotionsBase.IS_ACTIVE_COL + "=" + Const.ACTIVE
					+ " AND " + EventsPromotionsBase.VISIBLE_START_DATE_COL
					+ "<" + System.currentTimeMillis() / 1000;
			String query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ whereClause + " ORDER BY "
					+ EventsPromotionsBase.ORDER_NUMBER_COL + " ASC, "
					+ EventsPromotionsBase.ID_COL + " DESC";
			return query;
		}

		public static String RELATED_EVENTS_PROMOTIONS_QUERY(boolean isEvent,
				long locationId) {
			String TABLE_NAME = isEvent ? TABLE_EVENTS : TABLE_PROMOTIONS;
			String whereClause = String.format(Locale.US, "%s=%d AND %s=%d",
					EventsPromotionsBase.LOCATION_IDS_COL, locationId,
					EventsPromotionsBase.IS_ACTIVE_COL, Const.ACTIVE);
			String query = "SELECT * FROM " + TABLE_NAME + " WHERE "
					+ whereClause + " ORDER BY "
					+ EventsPromotionsBase.DEFAULT_SORT_ORDER;
			return query;
		}

		// ----------------------CART RELATED-----------------------

		public static String GET_SHOPPING_CART_QUERY(int id, int type,
				String date) {

			// String additionalClause = "  WHERE " + CartData.CART_ID_COL +
			// " = "
			// + id + " AND " + CartData.CART_TYPE_COL + " = " + type
			// + " AND " + CartData.CART_DATE_COL + " = '" + date + "' ";

			String additionalClause = "  WHERE " + CartData.CART_DATE_COL
					+ " = '" + date + "' " + " AND " + CartData.CART_TYPE_COL
					+ " = " + type + " AND " + CartData.CART_ID_COL + " = "
					+ id;

			String query = "SELECT " + CartData.ID_COL + ","
					+ CartData.CART_ID_COL + "," + CartData.CART_DESC_COL + ","
					+ CartData.CART_QTY_COL + "," + CartData.CART_PRICE_COL
					+ "," + CartData.CART_AMT_COL + ","
					+ CartData.CART_TYPE_COL + "," + CartData.CART_NAME_COL
					+ "," + CartData.CART_DETAIL_ID_COL + ","
					+ CartData.CART_DATE_COL + " FROM " + TABLE_CART
					+ additionalClause;
			return query;
		}

		// GET CART_ID AND CART_DATE
		public static String GET_SHOPPING_CART_ID_QUERY(int type) {
			String additionalClause = "  WHERE "
					+ CartData.CART_TYPE_COL
					+ " = "
					+ type
					+ " group by "
					+ ((type != Const.EVENT_TICKET_TYPE_CODE) ? CartData.CART_ID_COL
							: CartData.CART_DATE_COL);
			String query = "SELECT " + CartData.CART_ID_COL + " , "
					+ CartData.CART_DATE_COL + " FROM " + TABLE_CART
					+ additionalClause;
			return query;
		}

		// GET ALL RECORDS
		public static String IS_SHOPPING_CART_EXIST_QUERY(int id,
				int TicketsCode, int adult_id) {
			String additionalClause = "  WHERE " + CartData.CART_ID_COL
					+ " = '" + String.valueOf(id) + "' AND "
					+ CartData.CART_TYPE_COL + " = '"
					+ String.valueOf(TicketsCode) + "' AND "
					+ CartData.CART_DETAIL_ID_COL + " = '"
					+ String.valueOf(adult_id) + "'";

			String query = "SELECT " + CartData.ID_COL + ","
					+ CartData.CART_ID_COL + "," + CartData.CART_DESC_COL + ","
					+ CartData.CART_QTY_COL + "," + CartData.CART_PRICE_COL
					+ "," + CartData.CART_AMT_COL + ","
					+ CartData.CART_TYPE_COL + "," + CartData.CART_NAME_COL
					+ "," + CartData.CART_DETAIL_ID_COL + ","
					+ CartData.CART_DATE_COL + " FROM " + TABLE_CART
					+ additionalClause;
			return query;
		}

		// GET ALL RECORDS
		public static String IS_EVENT_SHOPPING_CART_EXIST_QUERY(int id,
				int TicketsCode, int detail_id) {
			String additionalClause = "  WHERE " + CartData.CART_ID_COL
					+ " = '" + String.valueOf(id) + "' AND "
					+ CartData.CART_TYPE_COL + " = '"
					+ String.valueOf(TicketsCode) + "' AND "
					+ CartData.CART_DETAIL_ID_COL + " = '"
					+ String.valueOf(detail_id) + "'";

			String query = "SELECT " + CartData.ID_COL + ","
					+ CartData.CART_ID_COL + "," + CartData.CART_DESC_COL + ","
					+ CartData.CART_QTY_COL + "," + CartData.CART_PRICE_COL
					+ "," + CartData.CART_AMT_COL + ","
					+ CartData.CART_TYPE_COL + "," + CartData.CART_NAME_COL
					+ "," + CartData.CART_DETAIL_ID_COL + ","
					+ CartData.CART_DATE_COL + " FROM " + TABLE_CART
					+ additionalClause;
			return query;
		}

		// GET CART TYPE
		public static String GET_SHOPPING_CART_TYPE_QUERY = "SELECT "
				+ CartData.CART_TYPE_COL + " FROM " + TABLE_CART + " GROUP BY "
				+ CartData.CART_TYPE_COL + " ORDER BY "
				+ CartData.CART_TYPE_COL;

		public static String GET_TOTAL_SHOPPING_CART_ITEM_QUERY = "SELECT COUNT(*) FROM "
				+ TABLE_CART;

		public static String GET_SHOPPING_CART_ITEM_QUERY = "SELECT * FROM "
				+ TABLE_CART;

		public static String GET_FAILED_ITEM_QUERY(int id) {
			String query = "SELECT  * FROM " + TABLE_CART + "  WHERE "
					+ CartData.CART_DETAIL_ID_COL + " = " + id;
			return query;
		}
	}

	public static final String TABLE_THINGS_TO_DO = "THINGS_TO_DO";
	public static final String TABLE_NODE_DETAILS = "NODE_DETAILS";
	public static final String TABLE_NODES = "NODES";
	public static final String TABLE_EDGES = "EDGES";
	public static final String TABLE_EDGE_OVERLAYS = "EDGE_OVERLAY";
	public static final String TABLE_NODE_DETAILS_TEMP = "NODE_DETAILS_TEMP";
	public static final String TABLE_NODES_TEMP = "NODES_TEMP";
	public static final String TABLE_EDGES_TEMP = "EDGES_TEMP";
	public static final String TABLE_EDGE_OVERLAYS_TEMP = "EDGE_OVERLAY_TEMP";
	public static final String TABLE_EVENTS = "EVENTS";
	public static final String TABLE_PROMOTIONS = "PROMOTIONS";
	public static final String TABLE_CART = "CART_ITEMS";

	public static class ThingsToDoData {

		public static final String ID_COL = _ID;
		// links to ids on node details page
		public static final String DETAIL_NODE_ID_COL = "detailNodeId";
		public static final String NAME_COL = "name";
		public static final String ICON_ID_COL = "iconId";
		public static final String DESCRIPTION_COL = "description";

		public static final String ID_JSON = "Id";
		// links to ids on node details page
		public static final String DETAIL_NODE_ID_JSON = "LocationDetailNodeIds";
		public static final String NAME_JSON = "Name";
		public static final String ICON_ID_JSON = "IconId";
		public static final String DESCRIPTION_JSON = "Description";
	}

	public static class NodeDetailsData {

		public static final String ID_COL = _ID;
		public static final String NODE_ID_COL = "node_id";
		public static final String TITLE_COL = "title";
		public static final String CATEGORY_COL = "category";
		public static final String IMAGE_NAME_COL = "image_name";
		public static final String VIDEO_URL_COL = "video_url";
		public static final String DESCRIPTION_COL = "description";
		public static final String ADMISSION_COL = "admission";
		public static final String OTHER_DETAILS_COL = "other_details";
		public static final String SECTION_COL = "section";
		public static final String OPENING_TIMES_COL = "opening_times";
		public static final String CONTACT_NO_COL = "contact_no";
		public static final String EMAIL_COL = "email";
		public static final String WEBSITE_COL = "website";
		public static final String IS_BOOKMARKED_COL = "is_bookmarked";

		public static final String ID_JSON = "detailNodeId";
		public static final String NODE_ID_JSON = "nodeId";
		public static final String TITLE_JSON = "title";
		public static final String CATEGORY_JSON = "category";
//		public static final String IMAGE_NAME_JSON = "imageName";
		public static final String IMAGE_NAME_JSON = "imageNameAndroid";
		public static final String VIDEO_URL_JSON = "videoUrl";
		public static final String DESCRIPTION_JSON = "descriptionText";
		public static final String ADMISSION_JSON = "admission";
		public static final String OTHER_DETAILS_JSON = "otherDetails";
		public static final String SECTION_JSON = "section";
		public static final String OPENING_TIMES_JSON = "openingTimes";
		public static final String CONTACT_NO_JSON = "contactNumber";
		public static final String EMAIL_JSON = "email";
		public static final String WEBSITE_JSON = "website";

	}

	public static class NodeData {
		public static final String ID_COL = _ID;
		public static final String LATITUDE_COL = "lat";
		public static final String LONGITUDE_COL = "lon";
		public static final String TITLE_COL = "title";

		public static final String ID_JSON = "nodeId";
		public static final String LATITUDE_JSON = "lat";
		public static final String LONGITUDE_JSON = "lon";
		public static final String TITLE_JSON = "title";
	}

	public static class EdgeData {
		public static final String ID_COL = _ID;
		public static final String FROM_NODE_COL = "from_node";
		public static final String TO_NODE_COL = "to_node";
		public static final String TYPE_COL = "type";
		public static final String TIME_COL = "time";
		public static final String BIDIRECTIONAL_COL = "bidirectional";

		public static final String ID_JSON = "edgeId";
		public static final String FROM_NODE_JSON = "fromNode";
		public static final String TO_NODE_JSON = "toNode";
		public static final String TYPE_JSON = "type";
		public static final String TIME_JSON = "time";
		public static final String BIDIRECTIONAL_JSON = "biDirectional";
		public static final String LINE_COLOR_JSON = "lineColor";
	}

	public static class EdgeOverlayData {
		public static final String ID_COL = _ID;
		public static final String EDGE_ID_COL = "edgeId";
		public static final String LATITUDE_COL = "lat";
		public static final String LONGITUDE_COL = "lon";

	}

	public static class EventsPromotionsBase {
		// For Promotion Table
		public static final String ID_COL = _ID;
		public static final String TITLE_COL = "Title";
		public static final String DESCRIPTION_COL = "Description";
		public static final String DETAIL_COL = "Detail";
		public static final String VISIBLE_START_DATE_COL = "VisibleStartDate";
		public static final String VISIBLE_END_DATE_COL = "VisibleEndDate";
		public static final String START_DATE_COL = "StartDate";
		public static final String END_DATE_COL = "EndDate";
		public static final String TIME_TEXT_COL = "TimeText";
		public static final String ADMISSION_COL = "Admission";
		public static final String CONTACT_COL = "Contact";
		public static final String EMAIL_COL = "Email";
		public static final String VENUE_COL = "Venue";
		public static final String IMAGE_URL_COL = "ImageURL";
		public static final String EXTERNAL_LINK_COL = "ExternalLink";
		public static final String FACEBOOK_SHARED_COL = "FacebookShared";
		public static final String TWITTER_SHARED_COL = "TwitterShared";
		public static final String SOCIAL_NETWORK_URL_COL = "SocialNetworkURL";
		public static final String IS_ACTIVE_COL = "IsActive";
		public static final String ORDER_NUMBER_COL = "OrderNumber";
		public static final String LINKEDTICKET_COL = "LinkedTicket";
		public static final String CREATED_AT_COL = "CreatedAt";
		public static final String UPDATED_AT_COL = "UpdatedAt";
		public static final String LOCATION_IDS_COL = "LocationIds";
		public static final String BANNER_ID_COL = "BannerId";

		public static final String DEFAULT_SORT_ORDER = "LOWER(" + TITLE_COL
				+ ") ASC";
		public static final int INVALID_LOC = -1;

		// For Json Data
		public static final String ID_JSON = "Id";
		public static final String TITLE_JSON = "Title";
		public static final String DESCRIPTION_JSON = "Description";
		public static final String DETAIL_JSON = "Detail";
		public static final String VISIBLE_START_DATE_JSON = "VisibleStartDate";
		public static final String VISIBLE_END_DATE_JSON = "VisibleEndDate";
		public static final String START_DATE_JSON = "StartDate";
		public static final String END_DATE_JSON = "EndDate";
		public static final String TIME_TEXT_JSON = "TimeText";
		public static final String ADMISSION_JSON = "Admission";
		public static final String CONTACT_JSON = "Contact";
		public static final String EMAIL_JSON = "Email";
		public static final String VENUE_JSON = "Venue";
		public static final String IMAGE_URL_JSON = "ImageURL";
		public static final String EXTERNAL_LINK_JSON = "ExternalURL";
		public static final String FACEBOOK_SHARED_JSON = "FacebookShared";
		public static final String TWITTER_SHARED_JSON = "TwitterShared";
		public static final String SOCIAL_NETWORK_URL_JSON = "SocialNetworkURL";
		public static final String IS_ACTIVE_JSON = "IsActive";
		public static final String ORDER_NUMBER_JSON = "OrderNumber";
		public static final String LINKEDTICKET_JSON = "LinkedTicket";
		public static final String CREATED_AT_JSON = "CreatedAt";
		public static final String UPDATED_AT_JSON = "UpdatedAt";
		public static final String LOCATION_IDS_JSON = "LocationIds";
		public static final String BANNER_ID_JSON = "BannerId";

		public static final String DELETED_PROMOTION_IDS_JSON = "DeletedPromotionIds";
		public static final String DELETED_EVENT_IDS_JSON = "DeletedPromotionIds";

		// For Preference
		public static final String LAST_RETRIEVAL_TIME = "promotion_last_retrival_time";

		private String title, description, img_url;

		public EventsPromotionsBase(String title, String description,
				String img_url) {
			super();
			this.title = title;
			this.description = description;
			this.img_url = img_url;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getImg_url() {
			return img_url;
		}

		public void setImg_url(String img_url) {
			this.img_url = img_url;
		}
	}

	public static class CartData {
		public static final String ID_COL = _ID;
		public static final String CART_ID_COL = "Cart_id";
		public static final String CART_DESC_COL = "Cart_desc";
		public static final String CART_QTY_COL = "Cart_Qty";
		public static final String CART_PRICE_COL = "Cart_Price";
		public static final String CART_AMT_COL = "Cart_Amount";
		public static final String CART_TYPE_COL = "Cart_type";
		public static final String CART_NAME_COL = "Cart_name";
		// public static final String CART_STYPE_COL = "Cart_selection_type";
		public static final String CART_DETAIL_ID_COL = "Cart_detail_id";
		public static final String CART_DATE_COL = "Cart_date";
	}

	public static final String TABLE_THINGS_TO_DO_CREATE = "CREATE TABLE "
			+ TABLE_THINGS_TO_DO + " (" + ThingsToDoData.ID_COL
			+ " INTEGER PRIMARY KEY," + ThingsToDoData.DETAIL_NODE_ID_COL
			+ " INTEGER," + ThingsToDoData.NAME_COL + " TEXT,"
			+ ThingsToDoData.ICON_ID_COL + " TEXT,"
			+ ThingsToDoData.DESCRIPTION_COL + " TEXT)";

	public static final String TABLE_NODE_DETAILS_GENERAL_CREATE = "CREATE TABLE "
			+ "#TABLE_NAME#" + " ("
			+ NodeDetailsData.ID_COL
			+ " INTEGER PRIMARY KEY,"
			+ NodeDetailsData.NODE_ID_COL
			+ " INTEGER, "
			+ NodeDetailsData.TITLE_COL
			+ " TEXT, "
			+ NodeDetailsData.CATEGORY_COL
			+ " TEXT,"
			+ NodeDetailsData.IMAGE_NAME_COL
			+ " TEXT,"
			+ NodeDetailsData.VIDEO_URL_COL
			+ " TEXT,"
			+ NodeDetailsData.DESCRIPTION_COL
			+ " TEXT,"
			+ NodeDetailsData.ADMISSION_COL
			+ " TEXT,"
			+ NodeDetailsData.OTHER_DETAILS_COL
			+ " TEXT,"
			+ NodeDetailsData.SECTION_COL
			+ " TEXT,"
			+ NodeDetailsData.OPENING_TIMES_COL
			+ " TEXT,"
			+ NodeDetailsData.CONTACT_NO_COL
			+ " TEXT,"
			+ NodeDetailsData.EMAIL_COL
			+ " TEXT,"
			+ NodeDetailsData.WEBSITE_COL
			+ " TEXT,"
			+ NodeDetailsData.IS_BOOKMARKED_COL + " INTEGER)";

	public static final String TABLE_NODES_GENERAL_CREATE = "CREATE TABLE "
			+ "#TABLE_NAME#" + " (" + NodeData.ID_COL + " INTEGER PRIMARY KEY,"
			+ NodeData.LATITUDE_COL + " REAL," + NodeData.LONGITUDE_COL
			+ " REAL," + NodeData.TITLE_COL + " TEXT);";

	public static final String TABLE_EDGES_GENERAL_CREATE = "CREATE TABLE "
			+ "#TABLE_NAME#" + " (" + EdgeData.ID_COL + " INTEGER PRIMARY KEY,"
			+ EdgeData.FROM_NODE_COL + " INTEGER," + EdgeData.TO_NODE_COL
			+ " INTEGER," + EdgeData.TIME_COL + " INTEGER," + EdgeData.TYPE_COL
			+ " INTEGER," + EdgeData.BIDIRECTIONAL_COL + " INTEGER);";

	public static final String TABLE_EDGE_OVERLAYS_GENERAL_CREATE = "CREATE TABLE "
			+ "#TABLE_NAME#"
			+ " ("
			+ EdgeOverlayData.ID_COL
			+ " INTEGER PRIMARY KEY,"
			+ EdgeOverlayData.EDGE_ID_COL
			+ " INTEGER,"
			+ EdgeOverlayData.LATITUDE_COL
			+ " REAL,"
			+ EdgeOverlayData.LONGITUDE_COL + " REAL);";

	public static String TABLE_EVENTS_PROMOTIONS_CREATE = "CREATE TABLE "
			+ "#TABLE_NAME#" + " ("
			+ EventsPromotionsBase.ID_COL
			+ " INTEGER,"
			+ EventsPromotionsBase.TITLE_COL
			+ " TEXT,"
			+ EventsPromotionsBase.DESCRIPTION_COL
			+ " TEXT,"
			+ EventsPromotionsBase.DETAIL_COL
			+ " TEXT,"
			+ EventsPromotionsBase.VISIBLE_START_DATE_COL
			+ " INTEGER,"
			+ EventsPromotionsBase.VISIBLE_END_DATE_COL
			+ " INTEGER,"
			+ EventsPromotionsBase.START_DATE_COL
			+ " INTEGER,"
			+ EventsPromotionsBase.END_DATE_COL
			+ " INTEGER,"
			+ EventsPromotionsBase.TIME_TEXT_COL
			+ " TEXT,"
			+ EventsPromotionsBase.ADMISSION_COL
			+ " TEXT,"
			+ EventsPromotionsBase.CONTACT_COL
			+ " TEXT,"
			+ EventsPromotionsBase.EMAIL_COL
			+ " TEXT,"
			+ EventsPromotionsBase.VENUE_COL
			+ " TEXT,"
			+ EventsPromotionsBase.IMAGE_URL_COL
			+ " TEXT,"
			+ EventsPromotionsBase.EXTERNAL_LINK_COL
			+ " TEXT,"
			+ EventsPromotionsBase.FACEBOOK_SHARED_COL
			+ " INTEGER,"
			+ EventsPromotionsBase.TWITTER_SHARED_COL
			+ " INTEGER,"
			+ EventsPromotionsBase.SOCIAL_NETWORK_URL_COL
			+ " TEXT,"
			+ EventsPromotionsBase.IS_ACTIVE_COL
			+ " INTEGER,"
			+ EventsPromotionsBase.ORDER_NUMBER_COL
			+ " INTEGER,"
			+ EventsPromotionsBase.LINKEDTICKET_COL
			+ " TEXT,"
			+ EventsPromotionsBase.CREATED_AT_COL
			+ " INTEGER,"
			+ EventsPromotionsBase.UPDATED_AT_COL
			+ " INTEGER,"
			+ EventsPromotionsBase.LOCATION_IDS_COL
			+ " INTEGER,"
			+ EventsPromotionsBase.BANNER_ID_COL
			+ " INTEGER,"			
			+ " PRIMARY KEY ("
			+ EventsPromotionsBase.ID_COL
			+ ","
			+ EventsPromotionsBase.LOCATION_IDS_COL + ")" + ")";

	public static final String TABLE_CART_GENERAL_CREATE = "CREATE TABLE "
			+ "#TABLE_NAME#" + " (" + CartData.ID_COL + " INTEGER PRIMARY KEY,"
			+ CartData.CART_ID_COL + " INTEGER," + CartData.CART_DESC_COL
			+ " TEXT," + CartData.CART_QTY_COL + " INTEGER,"
			+ CartData.CART_PRICE_COL + " REAL," + CartData.CART_AMT_COL
			+ " REAL," + CartData.CART_TYPE_COL + " INTEGER,"
			+ CartData.CART_NAME_COL + " TEXT," + CartData.CART_DETAIL_ID_COL
			+ " INTEGER," + CartData.CART_DATE_COL + " TEXT);";

	public static String TABLE_EVENTS_CREATE = TABLE_EVENTS_PROMOTIONS_CREATE
			.replace("#TABLE_NAME#", TABLE_EVENTS);
	public static String TABLE_PROMOTIONS_CREATE = TABLE_EVENTS_PROMOTIONS_CREATE
			.replace("#TABLE_NAME#", TABLE_PROMOTIONS);
	public static final String TABLE_NODE_DETAILS_CREATE = TABLE_NODE_DETAILS_GENERAL_CREATE
			.replaceAll("#TABLE_NAME#", TABLE_NODE_DETAILS);
	public static final String TABLE_NODE_DETAILS_TEMP_CREATE = TABLE_NODE_DETAILS_GENERAL_CREATE
			.replaceAll("#TABLE_NAME#", TABLE_NODE_DETAILS_TEMP);
	public static final String TABLE_NODES_CREATE = TABLE_NODES_GENERAL_CREATE
			.replaceAll("#TABLE_NAME#", TABLE_NODES);
	public static final String TABLE_NODES_TEMP_CREATE = TABLE_NODES_GENERAL_CREATE
			.replaceAll("#TABLE_NAME#", TABLE_NODES_TEMP);
	public static final String TABLE_EDGES_CREATE = TABLE_EDGES_GENERAL_CREATE
			.replaceAll("#TABLE_NAME#", TABLE_EDGES);
	public static final String TABLE_EDGES_TEMP_CREATE = TABLE_EDGES_GENERAL_CREATE
			.replaceAll("#TABLE_NAME#", TABLE_EDGES_TEMP);
	public static final String TABLE_EDGE_OVERLAYS_CREATE = TABLE_EDGE_OVERLAYS_GENERAL_CREATE
			.replaceAll("#TABLE_NAME#", TABLE_EDGE_OVERLAYS);
	public static final String TABLE_EDGE_OVERLAYS_TEMP_CREATE = TABLE_EDGE_OVERLAYS_GENERAL_CREATE
			.replaceAll("#TABLE_NAME#", TABLE_EDGE_OVERLAYS_TEMP);
	public static final String TABLE_CART_TEMP_CREATE = TABLE_CART_GENERAL_CREATE
			.replaceAll("#TABLE_NAME#", TABLE_CART);

}
