package com.mysentosa.android.sg.models;

import java.util.List;

public class MyPurchasesDetailItem {

	private long PurchaseDate;
	private String PinCode;
	private GroupEntry Group;
	private List<TicketDetailsEntry> TicketDetails;
	private BarcodeEntry Barcode;

	public long getPurchaseDate() {
		return PurchaseDate;
	}

	public void setPurchaseDate(long purchaseDate) {
		PurchaseDate = purchaseDate;
	}

	public String getPinCode() {
		return PinCode;
	}

	public void setPinCode(String pinCode) {
		PinCode = pinCode;
	}

	public GroupEntry getGroup() {
		return Group;
	}

	public void setGroup(GroupEntry group) {
		Group = group;
	}

	public List<TicketDetailsEntry> getTicketDetails() {
		return TicketDetails;
	}

	public void setTicketDetails(List<TicketDetailsEntry> ticketDetails) {
		TicketDetails = ticketDetails;
	}

	public BarcodeEntry getBarcode() {
		return Barcode;
	}

	public void setBarcode(BarcodeEntry barcode) {
		Barcode = barcode;
	}

	public class TicketDetailsEntry {

		public int Id;
		public String Name;
		public String Description;
		public String ShortDescription;
		public String Content;
		public float Price;
		public int GroupCode;
		public String DiscountType;
		public String TicketType;
		public int Quantity;

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

		public String getTicketType() {
			return TicketType;
		}

		public void setTicketType(String ticketType) {
			TicketType = ticketType;
		}

	}

	public class GroupEntry {

		public String Name;
		public int Id;
		public long Date;
		public String TicketType;

		public String getName() {
			return Name;
		}

		public void setName(String name) {
			Name = name;
		}

		public int getId() {
			return Id;
		}

		public void setId(int id) {
			Id = id;
		}

		public long getDate() {
			return Date;
		}

		public void setDate(long date) {
			Date = date;
		}

		public String getTicketType() {
			return TicketType;
		}

		public void setTicketType(String ticketType) {
			TicketType = ticketType;
		}

	}

	public class BarcodeEntry {
		public String Text;
		public String ImageLink;

		public String getText() {
			return Text;
		}

		public void setText(String text) {
			Text = text;
		}

		public String getImageLink() {
			return ImageLink;
		}

		public void setImageLink(String imageLink) {
			ImageLink = imageLink;
		}

	}

}
