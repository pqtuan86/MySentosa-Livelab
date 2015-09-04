package com.mysentosa.android.sg.models;

import java.util.List;

public class MyPurchasesItem {

	private String ReservationId;
	private List<PurchasesEntry> Purchases;
	private long ReserveTime;

	public String getReservationId() {
		return ReservationId;
	}

	public void setReservationId(String reservationId) {
		ReservationId = reservationId;
	}

	public List<PurchasesEntry> getPurchases() {
		return Purchases;
	}

	public void setPurchases(List<PurchasesEntry> purchases) {
		Purchases = purchases;
	}

	public long getReserveTime() {
		return ReserveTime;
	}

	public void setReserveTime(long reserveTime) {
		ReserveTime = reserveTime;
	}

	public class PurchasesEntry {

		public String PinCode;
		List<GroupEntry> Group;

		public String getPinCode() {
			return PinCode;
		}

		public void setPinCode(String pinCode) {
			PinCode = pinCode;
		}

		public List<GroupEntry> getGroup() {
			return Group;
		}

		public void setGroup(List<GroupEntry> group) {
			Group = group;
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

}
