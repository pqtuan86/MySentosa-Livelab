package com.mysentosa.android.sg.map.models;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Color;

import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.ContentURIs;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.Queries;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class Edge {
    public static final int TYPE_WALK = 0, TYPE_BUS1 = 1, TYPE_BUS2 = 2,
            TYPE_BUS3 = 3, TYPE_TRAM1 = 4, TYPE_TRAM2 = 5, TYPE_TRAIN = 6,
            TYPE_WAIT = 7, TYPE_CABLE = 8;

    public static final int DUMMY_EDGE_COST = -5;
    // black, blue, red, yellow, pink, orange, purple
    private static final int[] colorForType = {
            Color.BLACK,
            Color.parseColor("#9acd32"),
            Color.parseColor("#6E1819"),
            Color.parseColor("#FFA100"),
            Color.parseColor("#A55BDE"),
            Color.parseColor("#FFA100"),
            Color.parseColor("#55DEED"),
            Color.TRANSPARENT,
            Color.GREEN
    };
    // used on directions screen
    private static final int[] colorForDirection = {
            Color.parseColor("#818181"),
            Color.parseColor("#9acd32"), Color.parseColor("#6E1819"),
            Color.parseColor("#FFA100"), Color.parseColor("#A55BDE"),
            Color.parseColor("#FFA100"), Color.parseColor("#55DEED"),
            Color.TRANSPARENT};

    private static final String[] lineNames = {
            "Alight at #NODE_NAME# and walk",
            "Board Bus 1 at #NODE_NAME#",
            "Board Bus 2 at #NODE_NAME#",
            "Board Bus 3 at #NODE_NAME#",
            "Board tram at #NODE_NAME#",
            "Board orange line tram at #NODE_NAME#",
            "Board train at #NODE_NAME#"};
    // just line label
    private static final String[] lineLabels = {"Walk", "Bus 1",
            "Bus 2", "Bus 3", "Tram",
            "Orange line tram", "Train"};

    private static final int[] speed_meter_per_hour = {4000, 40000, 40000,
            40000, 15000, 15000, 50000};

    private static final int[] waiting_time_mins = {0, 10, 10, 10, 10, 10, 5};

    private Node fromNode, toNode;
    private int cost;
    private int type;
    private int edgeID; //this is not unique as 2 different edge objects pointing in opposite directions can share the same id
    private boolean isReversed; //used only to change order of overlays
//	private ArrayList<GeoPoint> edgeContourList;

    public Edge(Node fromNode, Node toNode, int edgeID, int cost, int type, boolean isReversed) {
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.cost = cost;
        this.type = type;
        this.edgeID = edgeID;
        this.isReversed = isReversed;
    }

    public Node getToNode() {
        return toNode;
    }

    public Node getFromNode() {
        return fromNode;
    }

    public int getCostForEdge() {
        return cost;
    }

    public int getEdgeType() {
        return type;
    }

    public int getEdgeColor() {
        return colorForType[type];
    }

    public int getEdgeDirectionColor() { // used on directions screen
        return colorForDirection[type];
    }

    public String getLineName() {
        return lineNames[type];
    }

    public String getLineLabel() {
        return lineLabels[type];
    }

    public int getSpeed() {
        return speed_meter_per_hour[type];
    }

    public int getWaitingTime() {
        return waiting_time_mins[type];
    }

    public ArrayList<GeoPoint> getEdgeContourList(ContentResolver cr) {
        ArrayList<GeoPoint> pathContourList = null;
        if (type != Edge.TYPE_WALK && type != Edge.TYPE_WAIT) {
            String sortOrder = isReversed ? "ASC" : "DESC";
            Cursor overlayCursor = cr.query(ContentURIs.SENTOSA_URI, null, Queries.EDGE_OVERLAYS_QUERY(sortOrder), new String[]{"" + edgeID}, null);
            if (overlayCursor.moveToFirst()) {
                pathContourList = new ArrayList<GeoPoint>();
                while (!overlayCursor.isAfterLast()) {
                    int latE6 = (int) Math.floor(overlayCursor.getDouble(0) * (1e6));
                    int lonE6 = (int) Math.floor(overlayCursor.getDouble(1) * (1e6));
                    pathContourList.add(new GeoPoint(latE6, lonE6));
                    overlayCursor.moveToNext();
                }
            }
            overlayCursor.close();
        }
        return pathContourList;
    }

    public static Edge getDummyEdge(Node node, GeoPoint point) {
        return new Edge(new Node(-1, "Starting Point", point.getLatitudeE6() / 1E6, point.getLongitudeE6() / 1E6), node, -1, DUMMY_EDGE_COST, TYPE_WALK, false);
    }

    public String getFromNodeTitle() {
        return fromNode.getTitle();
    }

    public String getToNodeTitle() {
        return toNode.getTitle();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EdgeId: " + edgeID);
        if (fromNode != null) {
            builder.append("\nfromNode: " + fromNode.toString());
        }
        if (toNode != null) {
            builder.append("\ntoNode: " + toNode.toString());
        }
        builder.append("\ncost: " + cost + " type: " + type + " isReversed: " + isReversed);
        return builder.toString();
    }
}