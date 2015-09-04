package com.mysentosa.android.sg.map.algorithms;

import java.util.ArrayList;

import org.osmdroid.util.GeoPoint;

import android.location.Location;
import android.util.Log;

import com.mysentosa.android.sg.map.models.Edge;
import com.mysentosa.android.sg.map.models.Graph;
import com.mysentosa.android.sg.map.models.GraphNodePriorityQueue;
import com.mysentosa.android.sg.map.models.Node;
import com.mysentosa.android.sg.utils.LogHelper;

public class AStarPathFinder {
	//contains list of nodes each of which contain a list of edges
	private Graph graph;
	private final int MAX_SPEED = 25, MAX_SPEED_WALK = 2; //in METER/SEC
	private final int MIN_DISTANCE = 20;
	public static boolean CHANGE_TO_DIJKSTRA = false;
	
	//we add all nodes in graph to unsettledNodes. If for a node, we determine the shortest path, we simply remove it from unsettled nodes. graph object will still
	//have a reference
	private GraphNodePriorityQueue unsettledNodes;

	public AStarPathFinder (Graph g){
		this.graph  = g;
	}
	
	
	
	/**
	 * Actual algorithm
	 */
	public ArrayList<Edge> computePath(GeoPoint startPoint, Node targetNode, boolean walkOnly){
		this.graph.resetNodesCostAndSource();
		this.unsettledNodes = new GraphNodePriorityQueue();
		
		Node startNode = this.graph.getNearestNode(startPoint);
		LogHelper.d("Dijkstra","Dijkstra: star node id:"+startNode.getID()+startNode.getTitle()+" destination node id:"+targetNode.getID()+targetNode.getTitle());
		ArrayList<Edge> path = new ArrayList<Edge>();
		
		if(startNode==null || targetNode==null)
			return null;
		
		if(targetNode.getID()==startNode.getID()) {
			//IF TARGET NODE IS NEAREST NODE, just create a walking edge to the nearest node and return
			Edge startingWalkEdge = Edge.getDummyEdge(startNode, startPoint);
			path.add(0, startingWalkEdge);
			return path;
		}
		
		Location targetLocation = new Location("target");
		targetLocation.setLatitude(targetNode.getLatE6()/1E6);
		targetLocation.setLongitude(targetNode.getLongE6()/1E6);
		
		startNode.setGFCostAndSourceEdge(0, 0, Edge.getDummyEdge(startNode, startPoint));
		this.unsettledNodes.updateGraphNodeDistance(startNode);
		
		Node currentLeastCostNode = null, destinationNode = null;
		
		while (this.unsettledNodes.hasMore()){
			currentLeastCostNode = this.unsettledNodes.remove();
			currentLeastCostNode.setAsVisited();
		
			if(currentLeastCostNode.getID()==targetNode.getID()) {
				destinationNode = currentLeastCostNode;
				break; //this means we have reached the destination node
			}
			
			for (Edge e: currentLeastCostNode.getOutGoingEdges()){
				if(walkOnly && e.getEdgeType()!=Edge.TYPE_WALK) 
					continue; 
				Node adjNode = e.getToNode();
				if(adjNode.isVisited()) continue;
				
				Integer newPossibleGCost = e.getCostForEdge()+currentLeastCostNode.getGCostToReachNode();
				Integer newPossibleFCost = newPossibleGCost + getHCost(targetLocation, adjNode.getGeoPoint(), walkOnly);
				LogHelper.d("Dijkstra","Dijkstra: edge type: "+e.getEdgeType()+": from node: "+e.getFromNode().getID()+" to node: "+e.getToNode().getID()+" gcost: "+newPossibleGCost+" fcost: "+newPossibleFCost);
				if (newPossibleFCost<adjNode.getFCostToReachNode()){ //verify if these needs to be g or f cost
					adjNode.setGFCostAndSourceEdge(newPossibleGCost, newPossibleFCost, e);
					this.unsettledNodes.updateGraphNodeDistance(adjNode);
				}
			}
		}
		
		//If destinationNode's source edge is null or cost is integer.maxvalue, it means there is no path found. unlikely. destinationNode==null is even less likely
		if(destinationNode==null || destinationNode.getSourceEdge()==null) {
			LogHelper.d("Dijkstra","Dijkstra: destination node not found");
			return null;
		}
		
		//Portion to reverse calculate the path
		Edge iterator = destinationNode.getSourceEdge();
		while(iterator.getFromNode().getID()!=startNode.getID()) {
			path.add(0, iterator);
			iterator = iterator.getFromNode().getSourceEdge();
		}
		
		
		if(distanceBetweenGeoPoints(startPoint,iterator.getFromNode().getGeoPoint())>MIN_DISTANCE) {
			//if we have to walk more than 20 meters, create a new node at the start point, create an edge from start point to start node and add it to list
			Edge startingWalkEdge =Edge.getDummyEdge(iterator.getFromNode(), startPoint);
			iterator.getFromNode().setGFCostAndSourceEdge(0, 0, startingWalkEdge);
			path.add(0, iterator); 
			path.add(0, startingWalkEdge);
		} else {
			path.add(0, iterator); //just add the start node to the path
		}
		
		return path;
	}
	
	public Integer getHCost(Location targetLocation, GeoPoint p2, boolean isWalkOnly) {
		if(CHANGE_TO_DIJKSTRA) return 0;
		
		Location l2 = new Location("l2");  
		l2.setLatitude(p2.getLatitudeE6() / 1E6);  
		l2.setLongitude(p2.getLongitudeE6() / 1E6);  

		int speed = isWalkOnly?MAX_SPEED_WALK:MAX_SPEED;
		return (int) (targetLocation.distanceTo(l2)/speed);
	}
	
	public double distanceBetweenGeoPoints(GeoPoint p1, GeoPoint p2) {
		Location l1 = new Location("l1");  
		l1.setLatitude(p1.getLatitudeE6() / 1E6);  
		l1.setLongitude(p1.getLongitudeE6() / 1E6);  

		Location l2 = new Location("l2");  
		l2.setLatitude(p2.getLatitudeE6() / 1E6);  
		l2.setLongitude(p2.getLongitudeE6() / 1E6);  

		return l1.distanceTo(l2);
	}
	
//	private int getWaitingTime(int sourceEdgeType, int currentEdgeType) {
//		if(currentEdgeType==Edge.TYPE_WALK || sourceEdgeType==currentEdgeType)
//			return 0;
//		else 
//			return COST_WAITING_TIME;
//	}
}
