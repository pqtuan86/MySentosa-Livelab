package com.mysentosa.android.sg.models;

public class TicketDetailItemWrapper {
	public static final int TYPE_ITEM = 0;
	public static final int TYPE_HEADER = 1;
	
	public int type;
	public String headerContent;
	public TicketDetailItem item;
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getHeaderContent() {
		return headerContent;
	}
	public void setHeaderContent(String headerContent) {
		this.headerContent = headerContent;
	}
	public TicketDetailItem getItem() {
		return item;
	}
	public void setItem(TicketDetailItem item) {
		this.item = item;
	}
	
	
}
