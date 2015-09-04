package com.mysentosa.android.sg.provider.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

import com.mysentosa.android.sg.map.models.Edge;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.CartData;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.ContentURIs;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.EdgeData;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.EdgeOverlayData;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.EventsPromotionsBase;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.NodeData;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.NodeDetailsData;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.ThingsToDoData;
import com.mysentosa.android.sg.utils.LogHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JSONParseUtil {
    // This class is basically meant to parse all the json files for nodes, node
    // details, edges, events, promos
    private ContentResolver mResolver;

    public JSONParseUtil(Context c) {
        this.mResolver = c.getContentResolver();
    }

    public void parsingEventPromoJsonData(JSONArray eventPromoArray,
                                          boolean isEvent) throws Exception {
        List<ContentValues> eventPromos = new ArrayList<ContentValues>();
        if (eventPromoArray == null)
            return;
        LogHelper.i("events_promos_length", eventPromoArray.length() + "");
        ContentValues tempEventPromo;
        for (int i = 0; i < eventPromoArray.length(); i++) {
            tempEventPromo = getEventPromoContentValue(eventPromoArray
                    .optJSONObject(i));
            eventPromos.add(tempEventPromo);
            JSONArray eventLocations = eventPromoArray
                    .optJSONObject(i)
                    .optJSONArray(
                            SentosaDatabaseStructure.EventsPromotionsBase.LOCATION_IDS_JSON);
            if (eventLocations != null) {
                for (int j = 0; j < eventLocations.length(); j++) {
                    ContentValues tempEventPromoCopy = new ContentValues(
                            tempEventPromo);
                    tempEventPromoCopy
                            .put(SentosaDatabaseStructure.EventsPromotionsBase.LOCATION_IDS_COL,
                                    eventLocations.optLong(j));
                    eventPromos.add(tempEventPromoCopy);
                }
            }
        }
        if (eventPromos != null && !eventPromos.isEmpty()) {
            if (isEvent)
                mResolver.bulkInsert(ContentURIs.EVENTS_URI, eventPromos
                        .toArray(new ContentValues[eventPromos.size()]));
            else
                mResolver.bulkInsert(ContentURIs.PROMOTIONS_URI, eventPromos
                        .toArray(new ContentValues[eventPromos.size()]));
        }
    }

    // for things to do update from server
    public int parseThingsToDoJSON(String thingsToDoResult)
            throws JSONException {
        List<ContentValues> thingsToDo = new ArrayList<ContentValues>();
        JSONArray thingsToDoData = new JSONObject(thingsToDoResult)
                .getJSONObject("Data").getJSONArray("GroupCategories");
        for (int i = 0; i < thingsToDoData.length(); i++) {
            thingsToDo.addAll(getThingsToDoContentValue(
                    thingsToDoData.optJSONObject(i), thingsToDo.size()));
        }
        return mResolver.bulkInsert(ContentURIs.THINGS_TO_DO_URI,
                thingsToDo.toArray(new ContentValues[thingsToDo.size()]));
        // insert into DB
    }

    public int parseNodesJSON(String nodesResult) throws JSONException {
        List<ContentValues> nodes = new ArrayList<ContentValues>();
        JSONArray nodesData = new JSONArray(nodesResult);
        ContentValues tempNode;
        for (int i = 0; i < nodesData.length(); i++) {
            tempNode = getNodeContentValue(nodesData.optJSONObject(i));
            nodes.add(tempNode);
        }
        return mResolver.bulkInsert(ContentURIs.NODES_TEMP_URI,
                nodes.toArray(new ContentValues[nodes.size()]));
        // insert into DB
    }

    public int parseEdgesJSON(String edgesResult) throws JSONException {
        JSONArray edgesData = new JSONArray(edgesResult);
        List<ContentValues> edges = new ArrayList<ContentValues>();
        ContentValues tempEdge;

        for (int i = 0; i < edgesData.length(); i++) {
            JSONObject edgeObj = edgesData.optJSONObject(i);
            tempEdge = getEdgeContentValue(edgeObj);
            edges.add(tempEdge);

            int TYPE_COL = tempEdge.getAsInteger(EdgeData.TYPE_COL);
            int ID_COL = tempEdge.getAsInteger(EdgeData.ID_COL);

            if (TYPE_COL != Edge.TYPE_WALK && TYPE_COL != Edge.TYPE_WAIT) {
                try {
                    JSONArray mapOverlay = edgeObj.getJSONArray("mapOverlays");
                    List<ContentValues> edgeOverlay = new ArrayList<ContentValues>();
                    for (int j = 0; j < mapOverlay.length(); j++) {
                        JSONArray geoPt = mapOverlay.getJSONArray(j);
                        ContentValues tempEdgeOverlay = new ContentValues();
                        tempEdgeOverlay.put(EdgeOverlayData.EDGE_ID_COL, ID_COL);
                        tempEdgeOverlay.put(EdgeOverlayData.LATITUDE_COL, geoPt.getDouble(0));
                        tempEdgeOverlay.put(EdgeOverlayData.LONGITUDE_COL, geoPt.getDouble(1));
                        edgeOverlay.add(tempEdgeOverlay);
                    }
                    mResolver.bulkInsert(ContentURIs.EDGE_OVERLAYS_TEMP_URI,
                            edgeOverlay.toArray(new ContentValues[edgeOverlay
                                    .size()]));

                } catch (Exception ex) {
                    // do nothing. this means there are no mapOverlays
                }
            }
        }
        return mResolver.bulkInsert(ContentURIs.EDGES_TEMP_URI,
                edges.toArray(new ContentValues[edges.size()]));
    }

    public int parseNodeDetailsJSON(String nodesDetailResult)
            throws JSONException {
        JSONArray nodeDetailsData = new JSONArray(nodesDetailResult);
        List<ContentValues> nodeDetails = new ArrayList<ContentValues>();
        ContentValues tempNodeDetails;
        for (int i = 0; i < nodeDetailsData.length(); i++) {
            tempNodeDetails = getNodeDetailsContentValue(nodeDetailsData
                    .optJSONObject(i));
            nodeDetails.add(tempNodeDetails);
        }
        return mResolver.bulkInsert(ContentURIs.NODE_DETAILS_TEMP_URI,
                nodeDetails.toArray(new ContentValues[nodeDetails.size()]));
    }

    private static int getEdgeType(String type, String color) {
        if (type.equals("WALK"))
            return Edge.TYPE_WALK;
        else if (type.equals("CABLE"))
            return Edge.TYPE_CABLE;
        else if (type.equals("WAIT"))
            return Edge.TYPE_WAIT;
        else if (type.equals("RAIL"))
            return Edge.TYPE_TRAIN;
        else if (type.equals("BUS")) {
            if (color.equals("BLUE"))
                return Edge.TYPE_BUS1;
            else if (color.equals("RED"))
                return Edge.TYPE_BUS2;
            else if (color.equals("YELLOW"))
                return Edge.TYPE_BUS3;
        } else if (type.equals("TRAM")) {
            if (color.equals("PINK"))
                return Edge.TYPE_TRAM1;
            else if (color.equals("ORANGE"))
                return Edge.TYPE_TRAM2;
        }
        return Edge.TYPE_WALK;
    }

    public static ContentValues getEventPromoContentValue(JSONObject data) {
        ContentValues epBase = new ContentValues();
        epBase.put(EventsPromotionsBase.ID_COL,
                data.optLong(EventsPromotionsBase.ID_JSON));
        epBase.put(EventsPromotionsBase.TITLE_COL,
                data.optString(EventsPromotionsBase.TITLE_JSON));
        epBase.put(EventsPromotionsBase.DESCRIPTION_COL,
                data.optString(EventsPromotionsBase.DESCRIPTION_JSON));
        epBase.put(EventsPromotionsBase.DETAIL_COL,
                data.optString(EventsPromotionsBase.DETAIL_JSON));
        epBase.put(EventsPromotionsBase.VISIBLE_START_DATE_COL,
                data.optLong(EventsPromotionsBase.VISIBLE_START_DATE_JSON));
        epBase.put(EventsPromotionsBase.VISIBLE_END_DATE_COL,
                data.optLong(EventsPromotionsBase.VISIBLE_END_DATE_JSON));
        epBase.put(EventsPromotionsBase.START_DATE_COL,
                data.optLong(EventsPromotionsBase.START_DATE_JSON));
        epBase.put(EventsPromotionsBase.END_DATE_COL,
                data.optLong(EventsPromotionsBase.END_DATE_JSON));
        epBase.put(EventsPromotionsBase.TIME_TEXT_COL,
                data.optString(EventsPromotionsBase.TIME_TEXT_JSON));
        epBase.put(EventsPromotionsBase.ADMISSION_COL,
                data.optString(EventsPromotionsBase.ADMISSION_JSON));
        epBase.put(EventsPromotionsBase.CONTACT_COL,
                data.optString(EventsPromotionsBase.CONTACT_JSON));
        epBase.put(EventsPromotionsBase.EMAIL_COL,
                data.optString(EventsPromotionsBase.EMAIL_JSON));
        epBase.put(EventsPromotionsBase.VENUE_COL,
                data.optString(EventsPromotionsBase.VENUE_JSON));
        epBase.put(EventsPromotionsBase.IMAGE_URL_COL,
                data.optString(EventsPromotionsBase.IMAGE_URL_JSON));
        epBase.put(EventsPromotionsBase.EXTERNAL_LINK_COL,
                data.optString(EventsPromotionsBase.EXTERNAL_LINK_JSON));
        epBase.put(EventsPromotionsBase.FACEBOOK_SHARED_COL,
                data.optBoolean(EventsPromotionsBase.FACEBOOK_SHARED_JSON));
        epBase.put(EventsPromotionsBase.TWITTER_SHARED_COL,
                data.optBoolean(EventsPromotionsBase.TWITTER_SHARED_JSON));
        epBase.put(EventsPromotionsBase.SOCIAL_NETWORK_URL_COL,
                data.optString(EventsPromotionsBase.SOCIAL_NETWORK_URL_JSON));
        epBase.put(EventsPromotionsBase.IS_ACTIVE_COL,
                data.optBoolean(EventsPromotionsBase.IS_ACTIVE_COL));
        epBase.put(EventsPromotionsBase.ORDER_NUMBER_COL,
                data.optInt(EventsPromotionsBase.ORDER_NUMBER_JSON));
        epBase.put(EventsPromotionsBase.LINKEDTICKET_COL,
                data.optString(EventsPromotionsBase.LINKEDTICKET_JSON));
        epBase.put(EventsPromotionsBase.CREATED_AT_COL,
                data.optLong(EventsPromotionsBase.CREATED_AT_JSON));
        epBase.put(EventsPromotionsBase.UPDATED_AT_COL,
                data.optLong(EventsPromotionsBase.UPDATED_AT_JSON));
        epBase.put(EventsPromotionsBase.LOCATION_IDS_COL,
                EventsPromotionsBase.INVALID_LOC);
        epBase.put(EventsPromotionsBase.BANNER_ID_COL,
                data.optLong(EventsPromotionsBase.BANNER_ID_JSON));
        return epBase;
    }

    public static ArrayList<ContentValues> getThingsToDoContentValue(
            JSONObject data, int startingSize) {
        ContentValues base = new ContentValues();
        ArrayList<ContentValues> thingsToDoList = new ArrayList<ContentValues>();

        // base.put(ThingsToDoData.ID_COL, data.optInt(ThingsToDoData.ID_JSON));
        base.put(ThingsToDoData.NAME_COL,
                data.optString(ThingsToDoData.NAME_JSON));
        base.put(ThingsToDoData.DESCRIPTION_COL,
                data.optString(ThingsToDoData.DESCRIPTION_JSON));
        base.put(ThingsToDoData.ICON_ID_COL,
                data.optString(ThingsToDoData.ICON_ID_JSON));
        LogHelper.d("test",
                "test name string: " + data.optString(ThingsToDoData.NAME_JSON));
        JSONArray nodeIdList = data
                .optJSONArray(ThingsToDoData.DETAIL_NODE_ID_JSON);
        for (int i = 0; i < nodeIdList.length(); i++) {
            ContentValues temp = new ContentValues(base);
            temp.put(ThingsToDoData.DETAIL_NODE_ID_COL, nodeIdList.optInt(i));
            temp.put(ThingsToDoData.ID_COL, startingSize + i);
            thingsToDoList.add(temp);
        }

        return thingsToDoList;
    }

    public static ContentValues getNodeContentValue(JSONObject data) {
        ContentValues node = new ContentValues();
        node.put(NodeData.ID_COL, data.optInt(NodeData.ID_JSON));
        node.put(NodeData.TITLE_COL, data.optString(NodeData.TITLE_JSON));
        node.put(NodeData.LATITUDE_COL, data.optDouble(NodeData.LATITUDE_JSON));
        node.put(NodeData.LONGITUDE_COL, data.optDouble(NodeData.LONGITUDE_JSON));
        return node;
    }

    public static ContentValues getEdgeContentValue(JSONObject data) {
        ContentValues edge = new ContentValues();
        edge.put(EdgeData.ID_COL, data.optInt(EdgeData.ID_JSON));
        edge.put(EdgeData.FROM_NODE_COL, data.optString(EdgeData.FROM_NODE_JSON));
        edge.put(EdgeData.TO_NODE_COL, data.optDouble(EdgeData.TO_NODE_JSON));
        edge.put(EdgeData.TIME_COL, data.optDouble(EdgeData.TIME_JSON));
        String type = data.optString(EdgeData.TYPE_JSON);
        String line_color = "";
        if (!type.equals("WALK")) {
            line_color = data.optString(EdgeData.LINE_COLOR_JSON);
        }
        edge.put(EdgeData.TYPE_COL, getEdgeType(type, line_color));
        edge.put(EdgeData.BIDIRECTIONAL_COL,
                data.optBoolean("biDirectional") ? 1 : 0);
        return edge;
    }

    public static ContentValues getNodeDetailsContentValue(JSONObject data) {
        ContentValues nodesDetail = new ContentValues();
        nodesDetail.put(NodeDetailsData.ID_COL, data.optInt(NodeDetailsData.ID_JSON));
        nodesDetail.put(NodeDetailsData.NODE_ID_COL, data.optInt(NodeDetailsData.NODE_ID_JSON));
        nodesDetail.put(NodeDetailsData.TITLE_COL, data.optString(NodeDetailsData.TITLE_JSON));
        nodesDetail.put(NodeDetailsData.CATEGORY_COL, data.optString(NodeDetailsData.CATEGORY_JSON));
        nodesDetail.put(NodeDetailsData.IMAGE_NAME_COL, data.optString(NodeDetailsData.IMAGE_NAME_JSON));
        nodesDetail.put(NodeDetailsData.VIDEO_URL_COL, data.optString(NodeDetailsData.VIDEO_URL_JSON));
        nodesDetail.put(NodeDetailsData.DESCRIPTION_COL, data.optString(NodeDetailsData.DESCRIPTION_JSON));
        nodesDetail.put(NodeDetailsData.ADMISSION_COL, data.optString(NodeDetailsData.ADMISSION_JSON));
        nodesDetail.put(NodeDetailsData.OTHER_DETAILS_COL, data.optString(NodeDetailsData.OTHER_DETAILS_JSON));
        nodesDetail.put(NodeDetailsData.SECTION_COL, data.optString(NodeDetailsData.SECTION_JSON).trim());
        nodesDetail.put(NodeDetailsData.OPENING_TIMES_COL, data.optString(NodeDetailsData.OPENING_TIMES_JSON));
        nodesDetail.put(NodeDetailsData.CONTACT_NO_COL, data.optString(NodeDetailsData.CONTACT_NO_JSON));
        nodesDetail.put(NodeDetailsData.EMAIL_COL, data.optString(NodeDetailsData.EMAIL_JSON));
        nodesDetail.put(NodeDetailsData.WEBSITE_COL, data.optString(NodeDetailsData.WEBSITE_JSON));
        return nodesDetail;
    }

    public ContentValues addCartItem(int cid, String desc, int qty, float price,
                                     float amt, int type, String name, /*int selection_type,*/ int detail_id, String date) {
        ContentValues cart = new ContentValues();
        // node.put(CartData.ID_COL, data.optInt(NodeData.ID_JSON));
        cart.put(CartData.CART_ID_COL, cid);
        cart.put(CartData.CART_DESC_COL, desc);
        cart.put(CartData.CART_QTY_COL, qty);
        cart.put(CartData.CART_PRICE_COL, price);
        cart.put(CartData.CART_AMT_COL, amt);
        cart.put(CartData.CART_TYPE_COL, type);
        cart.put(CartData.CART_NAME_COL, name);
        //cart.put(CartData.CART_STYPE_COL, selection_type);
        cart.put(CartData.CART_DETAIL_ID_COL, detail_id);
        cart.put(CartData.CART_DATE_COL, date);
        return cart;
        //mResolver.insert(ContentURIs.CART_URI, cart);
    }

}
