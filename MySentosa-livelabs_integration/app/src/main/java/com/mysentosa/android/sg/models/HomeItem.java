package com.mysentosa.android.sg.models;


public class HomeItem {

	public static final String ID_COL="_id";
	public static final String TITLE_COL="title";
	private String imgUrl;
	private int nodeID = -1;
	private int ID = -1;
	private String title = "";
	private String description = "";

	public HomeItem(int nodeID, String imgUrl, String title, String description) {
		super();
		this.nodeID = nodeID;
		this.imgUrl = imgUrl;
		this.title = title;
		this.description = description;
	}
	
	public HomeItem(int ID, int nodeID, String imgUrl, String title, String description) {
		this(nodeID, imgUrl, title, description);
		this.ID = ID;
	}
	
	public int getID() {
		return ID;
	}

	public String getImgUrl() {
		return imgUrl;
	}
	public int getNodeID() {
		return nodeID;
	}
	public String getTitle() {
		return title;
	}
	public String getDescription() {
		return description;
	}	
}