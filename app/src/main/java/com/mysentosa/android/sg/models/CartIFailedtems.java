package com.mysentosa.android.sg.models;

public class CartIFailedtems {

	public int Id;
	public int Qty;
	public String Name;
	public String Desc;
	public float Price;
	public float Amt;
	public String Date;
	public String Type;

	public int getId() {
		return Id;
	}

	public void setId(int id) {
		Id = id;
	}

	public int getQty() {
		return Qty;
	}

	public void setQty(int qty) {
		Qty = qty;
	}

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public float getPrice() {
		return Price;
	}

	public void setPrice(float price) {
		Price = price;
	}

	public String getDesc() {
		return Desc;
	}

	public void setDesc(String desc) {
		Desc = desc;
	}

	public float getAmt() {
		return Amt;
	}

	public void setAmt(float amt) {
		Amt = amt;
	}

	public String getDate() {
		return Date;
	}

	public void setDate(String date) {
		Date = date;
	}

	public String getType() {
		return Type;
	}

	public void setType(String type) {
		Type = type;
	}

}
