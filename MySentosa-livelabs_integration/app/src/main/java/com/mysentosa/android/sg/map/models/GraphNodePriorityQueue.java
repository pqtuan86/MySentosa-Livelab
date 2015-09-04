package com.mysentosa.android.sg.map.models;

import java.util.Collection;
import java.util.PriorityQueue;

/**
 * 
 * @author vsutskever
 * 
 */
public class GraphNodePriorityQueue  {


	private PriorityQueue<Node> pQueue = new PriorityQueue<Node>();

	public GraphNodePriorityQueue(){

	}

	public void add(Node n){
		pQueue.add(n);
	}

	public void add(Collection<Node> nodeCollection){
		this.pQueue.addAll(nodeCollection);
	}

	public Boolean hasMore(){
		return !this.pQueue.isEmpty();
	}

	public Node remove(){
		return this.pQueue.remove();
	}

	public void updateGraphNodeDistance(Node n){
		this.pQueue.remove(n);
		this.pQueue.add(n);
	}

}