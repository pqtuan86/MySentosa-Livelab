package com.mysentosa.android.sg.models;

import java.util.List;

public class TicketEventDetailItem  {

	private String Notes;
	private List<EventDetailsEntry> EventDetails;

	public String getNotes() {
		return Notes;
	}

	public void setNotes(String notes) {
		Notes = notes;
	}

	public List<EventDetailsEntry> getEventDetail() {
		return EventDetails;
	}

	public void setEventDetail(List<EventDetailsEntry> eventDetail) {
		this.EventDetails = eventDetail;
	}

	public class EventDetailsEntry {

		public int Id;
		public String Name;
		public String EventDate;
		List<itemEntry> Items;

		public int getId() {
			return Id;
		}

		public void setId(int id) {
			Id = id;
		}

		public String getName() {
			return Name;
		}

		public void setName(String name) {
			Name = name;
		}

		public String getEventDate() {
			return EventDate;
		}

		public void setEventDate(String eventDate) {
			EventDate = eventDate;
		}

		public List<itemEntry> getItems() {
			return Items;
		}

		public void setItems(List<itemEntry> Items) {
			this.Items = Items;
		}
	}

	public static class itemEntry {
		public int Id;
		public String Description;
		public String ShortDescription;
		public String Content;
		public float Price;
		public int GroupCode;
		public String DiscountType;
		public int Quantity;

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

		public String getShortDescription() {
			return ShortDescription;
		}

		public void setShortDescription(String shortDescription) {
			ShortDescription = shortDescription;
		}

		public String getContent() {
			return Content;
		}

		public void setContent(String content) {
			Content = content;
		}

		public float getPrice() {
			return Price;
		}

		public void setPrice(float price) {
			Price = price;
		}

		public int getGroupCode() {
			return GroupCode;
		}

		public void setGroupCode(int groupCode) {
			GroupCode = groupCode;
		}

		public String getDiscountType() {
			return DiscountType;
		}

		public void setDiscountType(String discountType) {
			DiscountType = discountType;
		}

		public int getQuantity() {
			return Quantity;
		}

		public void setQuantity(int quantity) {
			Quantity = quantity;
		}

	}


}
