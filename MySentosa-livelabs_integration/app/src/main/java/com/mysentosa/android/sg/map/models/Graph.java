// Created by plusminus on 23:11:31 - 22.09.2008
package com.mysentosa.android.sg.map.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.osmdroid.util.GeoPoint;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.mysentosa.android.sg.provider.SentosaContentProvider;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.ContentURIs;
import com.mysentosa.android.sg.provider.utils.SentosaDatabaseStructure.Queries;


public class Graph  {

	private static final String TAG = Graph.class.getSimpleName();

	private Map <Integer,Node> nodeMap;
	private ArrayList<Node> nodeList;
	public static final String FILE_NAME = "graph.bin";

	//---------------------------CONSTRUCTOR---------------------------------------
	public Graph() {
		this.nodeMap = new HashMap<Integer,Node>();
		this.nodeList = new ArrayList<Node>();
	}
	//-----------------------------------------------------------------------------



	//---------------------------ADD/GET NODES---------------------------------------
	//add a node
	public void addNode(Node n) {
		this.nodeList.add(n);
		this.nodeMap.put(n.getID(), n);
	}

	//method to get all the nodes
	public ArrayList<Node> getAllNodes(){
		return this.nodeList;
	}

	//to get specific node
	public Node getNode(Integer NodeID) {
		return nodeMap.get(NodeID);
	}
	//-----------------------------------------------------------------------------



	//---------------------------ADD EDGES-------------------------------------------
	public void addEdge(Edge edge) {
		Node n = nodeMap.get(edge.getFromNode().getID());
		n.addOutgoingEdge(edge);
	}
	//-----------------------------------------------------------------------------



	//---------------------------LOAD THE GRAPH FROM DATABASE-------------------------------------------
	//to parse the json into map data - called from LoadMapAsyncTask
	public void loadMap(Context ctx) {
		addNodes(ctx);
		addEdges(ctx);
	}


	//add a list of nodes from database
	public void addNodes(Context ctx) {
		Node n;
		Cursor c = ctx.getContentResolver().query(ContentURIs.SENTOSA_URI, null, Queries.NODE_QUERY, null, null);
		if(c.moveToFirst()){
			int id_col = c.getColumnIndex(SentosaContentProvider.NodeData.ID_COL);
			int title_col = c.getColumnIndex(SentosaContentProvider.NodeDetailsData.TITLE_COL);
			int lat_col = c.getColumnIndex(SentosaContentProvider.NodeData.LATITUDE_COL);
			int lon_col = c.getColumnIndex(SentosaContentProvider.NodeData.LONGITUDE_COL);

			while(!c.isAfterLast()){
				String title = c.getString(title_col);
//				if(title==null) title = "";
				n = new Node(c.getInt(id_col), title, c.getDouble(lat_col), c.getDouble(lon_col));
				this.addNode(n);
				c.moveToNext();
			}
		}
		c.close();
	}

	//add edges data from database
	public void addEdges(Context ctx) {
		Edge e;
		ContentResolver cr = ctx.getContentResolver();

		Cursor edgeCursor = cr.query(ContentURIs.SENTOSA_URI, null, Queries.EDGES_QUERY, null, null);
		if(edgeCursor.moveToFirst()){
			int id_col = edgeCursor.getColumnIndex(SentosaContentProvider.EdgeData.ID_COL);
			int time_col = edgeCursor.getColumnIndex(SentosaContentProvider.EdgeData.TIME_COL);
			int type_col = edgeCursor.getColumnIndex(SentosaContentProvider.EdgeData.TYPE_COL);
			int bidirectional_col = edgeCursor.getColumnIndex(SentosaContentProvider.EdgeData.BIDIRECTIONAL_COL);
			int from_node_col = edgeCursor.getColumnIndex(SentosaContentProvider.EdgeData.FROM_NODE_COL);
			int to_node_col = edgeCursor.getColumnIndex(SentosaContentProvider.EdgeData.TO_NODE_COL);
			while(!edgeCursor.isAfterLast()){
				int edgeId = edgeCursor.getInt(id_col);
				boolean biDirectional = (edgeCursor.getInt(bidirectional_col)==1);
				int edgeType = edgeCursor.getInt(type_col);
				int edgeCost = edgeCursor.getInt(time_col);
				Node node1 = getNode(edgeCursor.getInt(from_node_col));
				Node node2 = getNode(edgeCursor.getInt(to_node_col));

				e = new Edge(node1, node2, edgeId, edgeCost, edgeType, false);
//				Log.d(TAG, "first edge\n" + e.toString());
				addEdge(e);

				if(biDirectional) {
					e = new Edge(node2, node1, edgeId, edgeCost, edgeType, true);
//					Log.d(TAG, "second edge\n" + e.toString());
					addEdge(e);
				}

				edgeCursor.moveToNext();
			}
		}
		edgeCursor.close();

	}
	//-------------------------------------------------------------------------------


	//---------------------------RESET ALL NODES WHILE CALCULATING THE DIJKSTRA PATH---------------------------------------
	public void resetNodesCostAndSource() {
		for(Node node:nodeList) {
			node.resetSourceEdgeAndCost();
		}
	}
	//---------------------------------------------------------------------------------------------------------------------



	//---------------------------FOR DETERMINING NEAREST NODE TO LOCATION---------------------------------------
	public Node getNearestNode(GeoPoint p) {
		NodeComparator c = new NodeComparator(p);
		return Collections.min(this.nodeList, c);
	}

	private class NodeComparator implements Comparator<Node>{
		GeoPoint p;

		public NodeComparator(GeoPoint p) {
			this.p = p;
		}

		@Override
		public int compare(Node n0, Node n1) {
			int sizeN0 = n0.getOutGoingEdges().size();
			int sizeN1 = n1.getOutGoingEdges().size();

			if(sizeN0==0 && sizeN1>0)
				return 1;
			if(sizeN1==0 && sizeN0>0)
				return -1;

			return (Math.abs(n0.getLatE6()-p.getLatitudeE6())  + Math.abs(n0.getLongE6()-p.getLongitudeE6()))
					- (Math.abs(n1.getLatE6()-p.getLatitudeE6())  + Math.abs(n1.getLongE6()-p.getLongitudeE6()));
		}
	}
	//------------------------------------------------------------------------------------------------------------
}
