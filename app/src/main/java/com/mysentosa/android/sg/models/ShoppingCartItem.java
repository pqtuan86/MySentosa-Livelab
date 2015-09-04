package com.mysentosa.android.sg.models;

public class ShoppingCartItem {

	public int _id;
	public int Cart_id;
	public String Cart_desc;
	public int Cart_Qty;
	public float Cart_Price;
	public float Cart_Amount;
	public int Cart_type;
	public String Cart_name;
	public int Cart_selection_type;
	public int Cart_detail_id;
	public String Cart_date;

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public int getCart_id() {
		return Cart_id;
	}

	public void setCart_id(int cart_id) {
		Cart_id = cart_id;
	}

	public String getCart_desc() {
		return Cart_desc;
	}

	public void setCart_desc(String cart_desc) {
		Cart_desc = cart_desc;
	}

	public int getCart_Qty() {
		return Cart_Qty;
	}

	public void setCart_Qty(int cart_Qty) {
		Cart_Qty = cart_Qty;
	}

	public float getCart_Price() {
		return Cart_Price;
	}

	public void setCart_Price(float cart_Price) {
		Cart_Price = cart_Price;
	}

	public float getCart_Amount() {
		return Cart_Amount;
	}

	public void setCart_Amount(float cart_Amount) {
		Cart_Amount = cart_Amount;
	}

	public int getCart_type() {
		return Cart_type;
	}

	public void setCart_type(int cart_type) {
		Cart_type = cart_type;
	}

	public String getCart_name() {
		return Cart_name;
	}

	public void setCart_name(String cart_name) {
		Cart_name = cart_name;
	}

	public int getCart_selection_type() {
		return Cart_selection_type;
	}

	public void setCart_selection_type(int cart_selection_type) {
		Cart_selection_type = cart_selection_type;
	}

	public int getCart_detail_id() {
		return Cart_detail_id;
	}

	public void setCart_detail_id(int cart_detail_id) {
		Cart_detail_id = cart_detail_id;
	}

	public String getCart_date() {
		return Cart_date;
	}

	public void setCart_date(String cart_date) {
		Cart_date = cart_date;
	}

}
