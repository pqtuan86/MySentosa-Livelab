package com.mysentosa.android.sg.models;

public class MyPurchasesDetailSplitItem {

	public int Group_Id;
	public String Name;
	public long Date;
	public String TicketType;
	public int Id;
	public String Description;
	public String ShortDescription;
	public String Content;
	public float Price;
	public int GroupCode;
	public String DiscountType;
	public int Quantity;
	private long PurchaseDate;
	private String PinCode;
	public String Text;
	public String ImageLink;

	public int getGroup_Id() {
		return Group_Id;
	}

	public void setGroup_Id(int group_Id) {
		Group_Id = group_Id;
	}

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
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
