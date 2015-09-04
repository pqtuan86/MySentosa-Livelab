package com.mysentosa.android.sg.models;

public class TicketDetailItem {

	public String ShortDescription;
	public String Description;
	public int Id;
	public long Quantity;
	public String Content;
	public String TicketType;
	public float Price;

	public TicketDetailItem(String shortDescription, String description,
			int id, long quantity, String content, String ticketType, float price) {
		super();
		ShortDescription = shortDescription;
		Description = description;
		Id = id;
		Quantity = quantity;
		Content = content;
		TicketType = ticketType;
		Price = price;
	}

	public int getId() {
		return Id;
	}

	public void setId(int id) {
		Id = id;
	}

	public String getDescription() {
		return Description;
	}

	public void setDescription(String description) {
		Description = description;
	}

	public String getContent() {
		return Content;
	}

	public void setContent(String content) {
		Content = content;
	}

	public String getShortDescription() {
		return ShortDescription;
	}

	public void setShortDescription(String shortDescription) {
		ShortDescription = shortDescription;
	}

	public float getPrice() {
		return Price;
	}

	public void setPrice(float price) {
		Price = price;
	}

	public String getTicketType() {
		return TicketType;
	}

	public void setTicketType(String ticketType) {
		TicketType = ticketType;
	}

	public long getQuantity() {
		return Quantity;
	}

	public void setQuantity(long quantity) {
		Quantity = quantity;
	}
	
	public String name;
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}

}
