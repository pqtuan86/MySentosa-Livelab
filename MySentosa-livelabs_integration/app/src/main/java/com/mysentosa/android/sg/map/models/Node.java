package com.mysentosa.android.sg.map.models;

import java.util.ArrayList;

import org.osmdroid.util.GeoPoint;

public class Node implements Comparable<Node> {

	public static final int MAX_COST = 999999;
	
	private final ArrayList<Edge> outGoingEdges;
	private final String title;
	private final Integer ID;
	private final double latitude, longitude;
	
	//Related to path calculation and plotting only. Need not always be set.
	private Edge sourceEdge = null;
	private boolean isVisited = false;
	private Integer gCostToReachNode = MAX_COST;
	private Integer fCostToReachNode = MAX_COST;

	public Node(Integer ID, String title, double latitude, double longitude) {
		this.title = title;
		this.ID = ID;
		this.latitude = latitude;
		this.longitude = longitude;
		outGoingEdges = new ArrayList<Edge>();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NodeId: " + ID);
        builder.append(" title: " + title);
        builder.append(" latitude: " + latitude);
        builder.append(" longitude: " + longitude);
		return builder.toString();
	}

	public void addOutgoingEdge(Edge edge) {
		this.outGoingEdges.add(edge);
	}
	
	public ArrayList<Edge> getOutGoingEdges() {
		return outGoingEdges;
	}

	public String getTitle() {
		return title;
	}
	
	public int getLatE6() {
		return (int) (this.latitude*1E6);
	}

	public int getLongE6() {
		return (int) (this.longitude*1E6);
	}

	public GeoPoint getGeoPoint() {
		return new GeoPoint(this.getLatE6(),this.getLongE6());
	}
	
	public Integer getID() {
		return this.ID;
	}

	//COMPARATOR PORTION
	@Override
	public int compareTo(Node n) {
		int compareValue = this.fCostToReachNode.compareTo(n.getFCostToReachNode());
		if(compareValue==0) compareValue = this.ID - n.ID;
		return compareValue;
	}

	//RELATED TO DIJKSTRA. NOT A NODE PROPERTY AS SUCH.
	public void resetSourceEdgeAndCost() {
		this.isVisited = false;
		this.sourceEdge = null;
		this.gCostToReachNode = MAX_COST; //just a large value
		this.fCostToReachNode = MAX_COST;
	}
	
	public void setGFCostAndSourceEdge(Integer gCostToReachNode, Integer fCostToReachNode, Edge sourceEdge) {
		this.gCostToReachNode = gCostToReachNode;
		this.fCostToReachNode = fCostToReachNode;
		this.sourceEdge = sourceEdge;
	}
	
	public Integer getGCostToReachNode() {
		return gCostToReachNode;
	}
	
	public Integer getFCostToReachNode() {
		return fCostToReachNode;
	}
	
	public Edge getSourceEdge() {
		return this.sourceEdge;
	}
	
	public void setAsVisited() {
		this.isVisited = true;
	}
	
	public boolean isVisited() {
		return this.isVisited;
	}
	
}