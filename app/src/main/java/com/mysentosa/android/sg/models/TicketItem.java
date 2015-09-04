package com.mysentosa.android.sg.models;

public class TicketItem {

	public int Id;
	public String Name;
	public String Description;
	public String Image;
	public String Notes;
	public int ProductId;
	public boolean IslanderExclusive;

	public TicketItem(int id, String name, String description, String image,
			String notes, int productId, boolean islanderExclusive) {
		super();
		Id = id;
		Name = name;
		Description = description;
		Image = image;
		Notes = notes;
		ProductId = productId;
		IslanderExclusive = islanderExclusive;
	}

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

	public String getImage() {
		return Image;
	}

	public void setImage(String image) {
		Image = image;
	}

	public String getNotes() {
		return Notes;
	}

	public void setNotes(String notes) {
		Notes = notes;
	}

	public int getProductId() {
		return ProductId;
	}

	public void setProductId(int productId) {
		ProductId = productId;
	}

    public boolean isIslanderExclusive() {
        return IslanderExclusive;
    }

    public void setIslanderExclusive(boolean islanderExclusive) {
        IslanderExclusive = islanderExclusive;
    }

}
