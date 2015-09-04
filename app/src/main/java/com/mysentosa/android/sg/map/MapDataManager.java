// Created by plusminus on 21:46:22 - 25.09.2008
package com.mysentosa.android.sg.map;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.widget.Toast;

import com.mysentosa.android.sg.MapActivity;
import com.mysentosa.android.sg.NodeDetailActivity;
import com.mysentosa.android.sg.R;
import com.mysentosa.android.sg.map.algorithms.AStarPathFinder;
import com.mysentosa.android.sg.map.custom_views.CustomBalloonOverlay;
import com.mysentosa.android.sg.map.custom_views.CustomPathOverlay;
import com.mysentosa.android.sg.map.custom_views.CustomPathOverlay.PathPoint;
import com.mysentosa.android.sg.map.models.Direction;
import com.mysentosa.android.sg.map.models.Edge;
import com.mysentosa.android.sg.map.models.Graph;
import com.mysentosa.android.sg.map.models.Node;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.ContentURIs;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.Queries;
import com.mysentosa.android.sg.utils.Const;
import com.mysentosa.android.sg.utils.DrawableUtils;
import com.mysentosa.android.sg.utils.LogHelper;
import com.mysentosa.android.sg.utils.RepoTools;

import org.osmdroid.ResourceProxy;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.DirectionLocationOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayItem.HotspotPlace;

import java.util.ArrayList;
import java.util.List;


public class MapDataManager implements MapListener {

    private ResourceProxy mResourceProxy;
    public LoadMapDataTask loadMapTask;
    private Context mContext;

    /////////////////////////////////CONSTANTS////////////////////////////////////////
    private final float STROKE_WIDTH = 9, MARKER_SCALE = 0.55f;
    private int MARKER_BASE_OFFSET = 0;
    private final int PATH_TRANSPARENCY = 200;
    public static final double MAP_CENTER_LATITUDE = 1.2538366, MAP_CENTER_LONGITUDE = 103.81875;
    public boolean walkOnly = true, IS_DEBUG = false;
    private static final int CENTER_ON_TIMEOUT = 123;

    /////////////////////////////////MAP OVERLAYS////////////////////////////////////////
    private CustomBalloonOverlay balloonOverlay; //this is the balloon popup displaying info on an attraction point
    private ItemizedIconOverlay<OverlayItem> markerPointsOverlay; //this is the overlay for list of attractions points
    private DirectionLocationOverlay locationOverlay; //this is the overlay for current location
    private CustomPathOverlay pathOverlay; //this is the overlay that actually draws the path
    private ItemizedIconOverlay<OverlayItem> pathIndicatorOverlays; //these show how to get to a destination. display A->B->C etc...

    /////////////////////////////////OVERLAY DATA////////////////////////////////////////
    private ArrayList<PathPoint> pathPointList = new ArrayList<PathPoint>();
    private ArrayList<OverlayItem> pathIndicatorList = new ArrayList<OverlayItem>();
    private ArrayList<String> categoriesDisplayed = new ArrayList<String>();

    //////////////////FLAGS OF ACTIONS WAITING FOR MAP DATA LOADING/////////////////////
    // routeTo:
    private int destinationNodeId = -1;
    private int startNodeId = -1;
    private boolean isPathToBeDisplayed = false, isWalkOnly = true;
    // centerOn:
    private boolean mToBeCenterOn = false;
    private long mCenterOnDelay = 0;
    private int mCenterOnNodeId = -1;

    //////////////////////////////////PATH CALCULATION RELATED///////////////////////////
    private AStarPathFinder mPathFinder;
    private Graph mGraph;
    /////////////////////////////////MAPVIEW REFERENCE///////////////////////////////////
    private MapView mapView = null;

    /////////////////////////////////CONSTRUCTOR///////////////////////////////////
    GeoPoint testStartPoint = new GeoPoint(MAP_CENTER_LATITUDE, MAP_CENTER_LONGITUDE);

    public MapDataManager(MapView mMapView, ResourceProxy rProxy) {
        mapView = mMapView;
        mContext = mMapView.getContext();
        mResourceProxy = rProxy;
        initializeOverlays(mContext);
        mapView.setMapListener(this);
        loadMapTask = new LoadMapDataTask();
        loadMapTask.execute(); //load graph and nodes to display
    }

    // constructor used in directions class
    public MapDataManager(Context context, ResourceProxy rProxy) {
        mContext = context;
        mResourceProxy = rProxy;
        loadMapTask = new LoadMapDataTask();
        loadMapTask.execute(); //load graph and nodes to display
    }

    /////////////////////////////////THIS INITIALIZES ALL THE MAP OVERLAYS///////////////////////////////////
    private void initializeOverlays(final Context c) {
        //set up location overlay
        locationOverlay = new DirectionLocationOverlay(c, mapView, mResourceProxy);
        locationOverlay.setLocationUpdater(locationUpdateListener);

        //set up path overlay to draw the path as required
        pathOverlay = new CustomPathOverlay(c);
        pathOverlay.setStrokeWidth(STROKE_WIDTH);
        pathOverlay.setTransparency(PATH_TRANSPARENCY);

        MARKER_BASE_OFFSET = (int) Math.round(-1 * mapView.getResources().getDrawable(R.drawable.path_direction_marker).getIntrinsicHeight() / 1.5);
        pathIndicatorOverlays = new ItemizedIconOverlay<OverlayItem>(new ArrayList<OverlayItem>(),
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        balloonOverlay.setMarkerBaseOffset(MARKER_BASE_OFFSET);
                        balloonOverlay.setBalloonOverlay(item, 2);
                        mapView.getController().animateTo(item.mGeoPoint);
                        mapView.postInvalidate();
                        return true;
                    }

                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return false;
                    }
                }, mResourceProxy, 1);

        //three default categories displayed. change/add/modify later as reqd
        categoriesDisplayed.add(Const.ATTRACTION);
        categoriesDisplayed.add(Const.BUS);
        categoriesDisplayed.add(Const.TRAM);
        categoriesDisplayed.add(Const.TRAIN);
        categoriesDisplayed.add(Const.CABLE);
        categoriesDisplayed.add(Const.BUS_INT);
        categoriesDisplayed.add(Const.MRT);
        categoriesDisplayed.add(Const.CABLEWAY);

        //set up the poi markers as required
        float densityFactor = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, mContext.getResources().getDisplayMetrics());
        markerPointsOverlay = new ItemizedIconOverlay<OverlayItem>(new ArrayList<OverlayItem>(),
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        balloonOverlay.setMarkerBaseOffset(0);
                        balloonOverlay.setBalloonOverlay(item, 1);
                        mapView.getController().animateTo(item.mGeoPoint);
                        mapView.postInvalidate();
                        return true;
                    }

                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        if (IS_DEBUG) {
                            testStartPoint = item.getPoint();
                            Toast.makeText(c, "selected point: " + item.getPoint().getLatitudeE6() + "," + item.getPoint().getLongitudeE6(), Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }
                }, mResourceProxy, MARKER_SCALE / densityFactor);

        //set up the balloon overlay to display whenever an attraction/interchange point is tapped
        balloonOverlay = new CustomBalloonOverlay(c, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            @Override
            public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                if (IS_DEBUG)
                    Toast.makeText(c, "Tapped blue arrow '" + item.mTitle + "' (index=" + index + ") got single tapped up", Toast.LENGTH_LONG).show();
                if (item.getUid().contains("path_indicator")) {
                    try {
                        String[] idData = item.getUid().split(":");
                        if (idData.length > 0) {
                            ((MapActivity) mContext).directionsPosition = Integer.parseInt(idData[0]);
                        }

                        if (((MapActivity) mContext).mapDirections != null)
                            ((MapActivity) mContext).mapDirections.performClick();
                    } catch (Exception e) {
                    }
                    return true; //return if it is a path indicator balloon
                }
                Intent intent = new Intent(c, NodeDetailActivity.class);
                intent.putExtra(Const.NODE_ID, Integer.parseInt(item.getUid()));
                intent.putExtra(Const.FlurryStrings.FlurryEventName, Const.FlurryStrings.LocationDetailsSourceMap);
                ((MapActivity) c).startActivityForResult(intent, MapActivity.ROUTE_DESTINATION_REQUEST_CODE);
                return true;
            }

            @Override
            public boolean onItemLongPress(final int index, final OverlayItem item) {
                return false;
            }
        });

        //Adding overlays
        mapView.getOverlays().add(pathOverlay);
        mapView.getOverlays().add(markerPointsOverlay);
        mapView.getOverlays().add(pathIndicatorOverlays);
        mapView.getOverlays().add(balloonOverlay);
        mapView.getOverlays().add(locationOverlay);

    }

    ////////////////////THIS IS FOR MAPACTIVITY TO SHOW THE PATH TO THE DESTINATION NODE/////////////////////////////
    public void routeTo(int nodeId, boolean walkOnly, boolean centerMap) {
        GeoPoint p = getCurrentUserLocation();
        if (p == null || !this.loadMapTask.isCompleted) {
            this.isPathToBeDisplayed = true;
            this.destinationNodeId = nodeId;
            this.isWalkOnly = walkOnly;
            Toast.makeText(mContext, (p == null ? "Please wait while location is determined." : "Please wait, loading data."), Toast.LENGTH_LONG).show();
            return;
        } else
            routeTo(nodeId, walkOnly, p, centerMap);
    }

    private void routeTo(int nodeId, boolean walkOnly, GeoPoint fromLoc, boolean centerMap) {
        if (IS_DEBUG)
            fromLoc = testStartPoint;
        if (!MapActivity.BOUNDING_BOX.contains(fromLoc)) {
            Toast.makeText(mContext, "Feature available within Sentosa.", Toast.LENGTH_LONG).show();
            return;
        }
        ArrayList<Edge> edges = mPathFinder.computePath(fromLoc, mGraph.getNode(nodeId), walkOnly);
        if (edges == null || edges.size() == 0) {
            Toast.makeText(mContext, "Unable to determine a route to your destination, please try again", Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(mContext, "Route found", Toast.LENGTH_LONG).show();
        showPathForEdges(edges);
        //centerOnUserLocation();
        if (centerMap)
            centerOn(fromLoc, 500);
    }

    public void routeTo(int toNodeId, boolean walkOnly, int fromNodeId, boolean centerMap) {
        if (!this.loadMapTask.isCompleted) {
            this.isPathToBeDisplayed = true;
            this.destinationNodeId = toNodeId;
            this.startNodeId = fromNodeId;
            this.isWalkOnly = walkOnly;
            Toast.makeText(mContext, ("Please wait for data to load."), Toast.LENGTH_LONG).show();
            return;
        }
        Node fromNode = mGraph.getNode(fromNodeId);
        GeoPoint fromLoc = new GeoPoint(fromNode.getLatE6(), fromNode.getLongE6());
        routeTo(toNodeId, walkOnly, fromLoc, centerMap);
    }

    public void centerOn(final GeoPoint centerLoc, final long delayMilliSecs) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CENTER_ON_TIMEOUT:
                        mapView.getController().animateTo(centerLoc);
                }
            }
        };

        Thread splash = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(delayMilliSecs);
                } catch (InterruptedException e) {
                } finally {
                    handler.sendEmptyMessage(CENTER_ON_TIMEOUT);
                }

            }
        };
        splash.start();
    }

    public void centerOn(int nodeId, long delayMilliSecs) {
        if (loadMapTask.isCompleted == false) {
            this.mToBeCenterOn = true;
            this.mCenterOnNodeId = nodeId;
            this.mCenterOnDelay = delayMilliSecs;
            return;
        }

        Node node = mGraph.getNode(nodeId);
        GeoPoint loc = new GeoPoint(node.getLatE6(), node.getLongE6());
        centerOn(loc, delayMilliSecs);
    }

    public ArrayList<Direction> directionsTo(int nodeId, boolean walkOnly, GeoPoint fromLoc) {
        if (IS_DEBUG)
            fromLoc = testStartPoint;
        if (!MapActivity.BOUNDING_BOX.contains(fromLoc)) {
            Toast.makeText(mContext, "Routing service available only on Sentosa island.", Toast.LENGTH_LONG).show();
            return null;
        }
        ArrayList<Edge> edges = mPathFinder.computePath(fromLoc, mGraph.getNode(nodeId), walkOnly);
        if (edges == null || edges.size() == 0) {
            Toast.makeText(mContext, "Could not determine a route to your destination!", Toast.LENGTH_LONG).show();
            return null;
        }
        return getDirectionsFromEdges(edges);
    }

    public ArrayList<Direction> directionsTo(int toNodeId, boolean walkOnly, int fromNodeId) {
        Node fromNode = mGraph.getNode(fromNodeId);
        GeoPoint fromLoc = new GeoPoint(fromNode.getLatE6(), fromNode.getLongE6());

        return directionsTo(toNodeId, walkOnly, fromLoc);
    }

    ////////////////////THIS LOADS ALL THE GRAPH DATA FROM DATABASE FOR ROUTING AND DISPLAYS RELEVANT NODES/////////////////////////////
    public class LoadMapDataTask extends AsyncTask<Void, Void, Boolean> {
        //for displaying the overlay items
        private ArrayList<OverlayItem> overlayList;
        private boolean isCompleted = false;

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                isCompleted = true;

                // do pending routeTo call
                if (isPathToBeDisplayed && startNodeId != -1) {
                    routeTo(destinationNodeId, isWalkOnly, startNodeId, true);
                    isPathToBeDisplayed = false;
                    destinationNodeId = -1;
                    startNodeId = -1;
                    mapView.invalidate();
                }

                //do pending centerOn call
                if (mToBeCenterOn) {
                    centerOn(mCenterOnNodeId, mCenterOnDelay);
                    mToBeCenterOn = false;
                    mCenterOnDelay = 0;
                    mCenterOnNodeId = -1;
                }
            }
        }

        @Override
        protected void onProgressUpdate(Void... params) {
            displayNodes(overlayList); //displaying in UI thread
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //DISPLAYING NODES ON MAP
            overlayList = loadOverlayNodesData();
            if (overlayList != null && overlayList.size() > 0)
                publishProgress();

            //GRAPH CREATION
            mGraph = new Graph();
            mGraph.loadMap(mContext);
            mPathFinder = new AStarPathFinder(mGraph);

            return true;
        }

        public boolean isCompleted() {
            return isCompleted;
        }
    }

    //loading the nodes to display on map from the database
    public ArrayList<OverlayItem> loadOverlayNodesData() {
        String[] categoriesList = new String[categoriesDisplayed.size()];
        categoriesDisplayed.toArray(categoriesList);

        Resources mResource = mContext.getResources();
        ArrayList<OverlayItem> overlayList = new ArrayList<OverlayItem>();

        Cursor cursor = mContext.getContentResolver().query(ContentURIs.SENTOSA_URI, null, Queries.NODE_DETAILS_QUERY(categoriesList.length), categoriesList, null);

        if (RepoTools.isRowAvailable(cursor)) {
            while (!cursor.isAfterLast()) {
                double latitude = RepoTools.getDouble(cursor, SentosaDatabaseStructure.NodeData.LATITUDE_COL);
                double longitude = RepoTools.getDouble(cursor, SentosaDatabaseStructure.NodeData.LONGITUDE_COL);

                GeoPoint point = new GeoPoint(latitude, longitude);
                String iconName = RepoTools.getString(cursor, SentosaDatabaseStructure.NodeDetailsData.CATEGORY_COL);
                iconName = "cat_" + iconName.replace(" ", "").replace("'", "").toLowerCase();

                int resID = mResource.getIdentifier(iconName, "drawable", mContext.getPackageName());
                Drawable drawable = mResource.getDrawable(resID);

                int nodeId = RepoTools.getInt(cursor, SentosaDatabaseStructure.NodeDetailsData.NODE_ID_COL);
                String title = RepoTools.getString(cursor, SentosaDatabaseStructure.NodeDetailsData.TITLE_COL);

                overlayList.add(new OverlayItem(Integer.toString(nodeId), null, title, point, drawable));
                cursor.moveToNext();
            }
        }

        cursor.close();
        return overlayList;
    }

    //helper for displaying nodes. separated as this must be done on UI thread
    public void displayNodes(ArrayList<OverlayItem> overlayList) {
        markerPointsOverlay.removeAllItems();
        for (OverlayItem o : overlayList) {
            markerPointsOverlay.addItem(o);
        }
    }


    public void displayIconsOnMap(List<String> iconCategories) {
        categoriesDisplayed.clear();
        for (String category : iconCategories) {
            categoriesDisplayed.add(category);
        }
        displayNodes(loadOverlayNodesData());
    }

    public ArrayList<String> getCategoriesDisplayed() {
        return (ArrayList<String>) categoriesDisplayed.clone();
    }

    /////////////////////THIS IS TO DISPLAY PATH WHEN A* ALGORITHM DETERMINES THE PATH/////////////////////////////
    public void showPathForEdges(ArrayList<Edge> pathData) {
        if (pathData == null || pathData.size() == 0)
            return;

        //HERE WE OBTAIN THE POINTS WHICH TOGETHER REPRESENT THE PATH
        pathPointList.clear();
        ArrayList<GeoPoint> edgeContourList;
        int color = 0;
        Edge edge;


        for (int i = (pathData.size() - 1); i >= 0; i--) {
            edge = pathData.get(i);
            color = edge.getEdgeColor();
            pathPointList.add(new PathPoint(edge.getToNode().getLatE6(), edge.getToNode().getLongE6(), color)); //this is the destination node's coords

            edgeContourList = edge.getEdgeContourList(mContext.getContentResolver());
            if (edgeContourList != null) {
                for (GeoPoint p : edgeContourList) {
                    pathPointList.add(new PathPoint(p.getLatitudeE6(), p.getLongitudeE6(), color));
                }
            }

            if (i == 0) {
                //add the data of the first point of the first node
                pathPointList.add(new PathPoint(edge.getFromNode().getLatE6(), edge.getFromNode().getLongE6(), color));
            }
        }

        //HERE WE GET THE PATH INDICATORS LIST
        pathIndicatorList.clear();
        pathIndicatorOverlays.removeAllItems();
        Node n;
        int currentType = Edge.TYPE_WALK;
        Resources r = mapView.getResources();
        int indicatorCount = 0;
        int prevEdgeType = -1;
        String title = "";
        for (int i = 0; i < pathData.size(); i++) {
            try {
                edge = pathData.get(i);

                if (edge.getEdgeType() == Edge.TYPE_WAIT) {
                    //special case: if the final destination is a bus/tram stop
                    //we add a final indicator for destination node
                    if (i == (pathData.size() - 1)) { //last edge. must display destination
                        n = edge.getToNode();
                        indicatorCount++;
                        OverlayItem item = new OverlayItem(indicatorCount + ":path_indicator" + n.getID(), null, "Reach destination at " + n.getTitle(), n.getGeoPoint(), DrawableUtils.numberedPathDirectionDrawable(r, indicatorCount));
                        item.setMarkerHotspot(HotspotPlace.BOTTOM_CENTER);
                        pathIndicatorOverlays.addItem(item);
                        break;
                    }

                    if (edge.getCostForEdge() > 0)
                        title = edge.getFromNode().getTitle();
                    continue;
                }

                if (i == 0) { //first edge. must display start point
                    currentType = edge.getEdgeType();
                    n = edge.getFromNode();
                    if (n.getTitle() != null) title = n.getTitle();
                    indicatorCount++;
                    String descText = edge.getLineName();
                    if (edge.getEdgeType() == Edge.TYPE_WALK) {
                        if (edge.getCostForEdge() == Edge.DUMMY_EDGE_COST)
                            descText = "Walk from your starting location";
                        else
                            descText = descText.replace("Alight", "Start").replace("#NODE_NAME#", title);
                    } else {
                        descText = descText.replace("Board", "Start at " + title + " and board").replace("#NODE_NAME#", "");
                    }

                    OverlayItem item = new OverlayItem(indicatorCount + ":path_indicator" + n.getID(), null, descText, n.getGeoPoint(), DrawableUtils.numberedPathDirectionDrawable(r, indicatorCount));
                    item.setMarkerHotspot(HotspotPlace.BOTTOM_CENTER);
                    pathIndicatorOverlays.addItem(item);
                } else {
                    if (edge.getEdgeType() != currentType) {
                        //if there is a change in the type of edge
                        currentType = edge.getEdgeType();
                        n = edge.getFromNode();
                        if (n.getTitle() != null) title = n.getTitle();
                        indicatorCount++;
                        String descText = edge.getLineName();
                        if (prevEdgeType != Edge.TYPE_WALK && edge.getEdgeType() != Edge.TYPE_WALK)
                            descText = descText.replace("Board", "Alight and change to");
                        descText = descText.replace("#NODE_NAME#", title);
                        OverlayItem item = new OverlayItem(indicatorCount + ":path_indicator" + n.getID(), null, descText, n.getGeoPoint(), DrawableUtils.numberedPathDirectionDrawable(r, indicatorCount));
                        item.setMarkerHotspot(HotspotPlace.BOTTOM_CENTER);
                        pathIndicatorOverlays.addItem(item);
                    }
                }

                //add a final indicator for destination node
                if (i == (pathData.size() - 1)) { //last edge. must display destination
                    n = edge.getToNode();
                    indicatorCount++;
                    OverlayItem item = new OverlayItem(indicatorCount + ":path_indicator" + n.getID(), null, "Reach destination at " + n.getTitle(), n.getGeoPoint(), DrawableUtils.numberedPathDirectionDrawable(r, indicatorCount));
                    item.setMarkerHotspot(HotspotPlace.BOTTOM_CENTER);
                    pathIndicatorOverlays.addItem(item);
                }

                prevEdgeType = edge.getEdgeType();
            } catch (Exception ignore) {
                ignore.printStackTrace();
            }
        }

        pathOverlay.setPoints(pathPointList);
        mapView.postInvalidate();
    }

    public ArrayList<Direction> getDirectionsFromEdges(ArrayList<Edge> pathData) {
        if (pathData == null || pathData.size() == 0)
            return null;
        Node n;
        Edge edge;
        ArrayList<Direction> directions = new ArrayList<Direction>();
        int currentType = Edge.TYPE_WALK;
        Direction direction = null;

        int prevEdgeType = -1;
        String title = "";
        for (int i = 0; i < pathData.size(); i++) {
            try {
                edge = pathData.get(i);
                if (edge.getEdgeType() == Edge.TYPE_WAIT) {
                    //special case: if the final destination is a bus/tram stop
                    //we add a final indicator for destination node
                    if (i == (pathData.size() - 1)) { //last edge. must display destination
                        n = edge.getToNode();
                        direction = new Direction(edge, "Reach destination at " + n.getTitle());
                        directions.add(direction);
                        direction.setFrom(edge.getFromNodeTitle());
                        LogHelper.d("2359", "Edge: Direction added: " + "Reach destination at " + n.getTitle());
                        break;
                    }

                    if (edge.getCostForEdge() > 0)
                        title = edge.getFromNode().getTitle();
                    continue;
                }

                if (i == 0) { //first edge. must display start point
                    currentType = edge.getEdgeType();
                    n = edge.getFromNode();
                    if (n.getTitle() != null) title = n.getTitle();
                    String descText = edge.getLineName();
                    if (edge.getEdgeType() == Edge.TYPE_WALK) {
                        if (edge.getCostForEdge() == Edge.DUMMY_EDGE_COST)
                            descText = "Walk from your starting location";
                        else
                            descText = descText.replace("Alight", "Start").replace("#NODE_NAME#", title);
                    } else {
                        descText = descText.replace("Board", "Start at " + title + " and board").replace("#NODE_NAME#", "");
                    }
                    direction = new Direction(edge, descText);
                    directions.add(direction);
                    direction.setFrom(edge.getFromNodeTitle());
                    LogHelper.d("2359", "Edge: Direction added: " + descText);

                } else {
                    if (edge.getEdgeType() != currentType) {
                        //if there is a change in the type of edge
                        currentType = edge.getEdgeType();
                        n = edge.getFromNode();
                        if (n.getTitle() != null) title = n.getTitle();
                        String descText = edge.getLineName();
                        if (prevEdgeType != Edge.TYPE_WALK && edge.getEdgeType() != Edge.TYPE_WALK)
                            descText = descText.replace("Board", "Alight and change to");
                        descText = descText.replace("#NODE_NAME#", title);
                        direction = new Direction(edge, descText);
                        directions.add(direction);
                        direction.setFrom(edge.getFromNodeTitle());
                        LogHelper.d("2359", "Edge: Direction added: " + descText);
                    }
                }

                //add a final indicator for destination node
                if (i == (pathData.size() - 1)) { //last edge. must display destination
                    n = edge.getToNode();
                    direction = new Direction(edge, "Reach destination at " + n.getTitle());
                    directions.add(direction);
                    direction.setFrom(edge.getFromNodeTitle());
                    LogHelper.d("2359", "Edge: Direction added: " + "Reach destination at " + n.getTitle());
                }

                prevEdgeType = edge.getEdgeType();

                float results[] = {0};
                Location.distanceBetween(edge.getFromNode().getLatE6() / 1E6,
                        edge.getFromNode().getLongE6() / 1E6,
                        edge.getToNode().getLatE6() / 1E6,
                        edge.getToNode().getLongE6() / 1E6,
                        results);

                LogHelper.d("2359", "Edge: From:" + edge.getFromNodeTitle()
                        + ", To: " + edge.getToNodeTitle()
                        + ", Type: " + edge.getLineLabel()
                        + ", Distance: " + results[0] + " m");
                if (direction != null) {
                    direction.setDistance(direction.getDistance() + results[0]);
                    direction.setTo(edge.getToNodeTitle());
                    if (edge.getEdgeType() != Edge.TYPE_WAIT && edge.getEdgeType() != Edge.TYPE_WALK) {
                        direction.setStops(direction.getStops() + 1);
                    }
                }
            } catch (Exception ignore) {
                ignore.printStackTrace();
            }
        }
        return directions;
    }

    public void clearExistingPath() {
        pathPointList.clear();
        pathOverlay.setPoints(pathPointList);
        pathIndicatorOverlays.removeAllItems();
        mapView.postInvalidate();
    }

    public int getPathPointSize() {
        return pathPointList.size();
    }

    public void hideBalloonOverlay() {
        balloonOverlay.hideBalloonOverlay();
    }

    // ===========================================================
    // User location related methods
    // ===========================================================
    private DirectionLocationOverlay.LocationUpdateListener locationUpdateListener = new DirectionLocationOverlay.LocationUpdateListener() {
        @Override
        public void onLocationUpdated(GeoPoint currentLoc) {
            if (isPathToBeDisplayed && loadMapTask.isCompleted()) {
                routeTo(destinationNodeId, isWalkOnly, currentLoc, true);
                isPathToBeDisplayed = false;
                destinationNodeId = -1;
                mapView.invalidate();
            }
        }
    };

    public void enableLocationOverlay() {
        locationOverlay.enableMyLocation();
        locationOverlay.enableCompass();
    }

    public void disableLocationOverlays() {
        locationOverlay.disableMyLocation();
        locationOverlay.disableCompass();
    }

    public GeoPoint getCurrentUserLocation() {
        return locationOverlay.getMyLocation();
    }

    public void centerOnUserLocation() {
        GeoPoint p = locationOverlay.getMyLocation();
        if (p == null) {
            p = new GeoPoint(MAP_CENTER_LATITUDE, MAP_CENTER_LONGITUDE);
            Toast.makeText(mContext, "Sorry! Could not determine your location.", Toast.LENGTH_SHORT);
        }
        mapView.getController().animateTo(p);
    }

    // ===========================================================
    // Methods from MapListener Interface
    // ===========================================================
    @Override
    public boolean onScroll(ScrollEvent event) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onZoom(ZoomEvent event) {
        return false;
    }

    @Override
    public boolean onSingleTapNotHitOverlays() {
        // TODO Auto-generated method stub
        hideBalloonOverlay();
        mapView.postInvalidate();
        return false;
    }

}
