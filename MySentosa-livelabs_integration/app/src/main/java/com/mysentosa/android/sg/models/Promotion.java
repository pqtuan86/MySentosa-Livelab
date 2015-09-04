package com.mysentosa.android.sg.models;

import com.mysentosa.android.sg.utils.Const;

public class Promotion {
    long Id;
    String Title;
    String DealType;
    int DiscountedTicketEntityId;
    String DicountedTicketEntityType;
    int RewardQuantity;
    int RewardQuantityCount;
    boolean RewardDisplayMessaged;
    String Description;
    String Detail;
    long VisibleStartDate;
    long VisibleEndDate;
    String ImageURL;
    boolean IsActive;
    String CreatedAt;
    String UpdatedAt;
    public long getId() {
        return Id;
    }
    public void setId(long id) {
        Id = id;
    }
    public String getTitle() {
        return Title;
    }
    public void setTitle(String title) {
        Title = title;
    }
    public String getDealType() {
        return DealType;
    }
    public void setDealType(String dealType) {
        DealType = dealType;
    }
    public int getDiscountedTicketEntityId() {
        return DiscountedTicketEntityId;
    }
    public void setDiscountedTicketEntityId(int discountedTicketEntityId) {
        DiscountedTicketEntityId = discountedTicketEntityId;
    }
    public String getDicountedTicketEntityType() {
        return DicountedTicketEntityType;
    }
    public void setDicountedTicketEntityType(String dicountedTicketEntityType) {
        DicountedTicketEntityType = dicountedTicketEntityType;
    }
    public int getRewardQuantity() {
        return RewardQuantity;
    }
    public void setRewardQuantity(int rewardQuantity) {
        RewardQuantity = rewardQuantity;
    }
    public int getRewardQuantityCount() {
        return RewardQuantityCount;
    }
    public void setRewardQuantityCount(int rewardQuantityCount) {
        RewardQuantityCount = rewardQuantityCount;
    }
    public boolean isRewardDisplayMessaged() {
        return RewardDisplayMessaged;
    }
    public void setRewardDisplayMessaged(boolean rewardDisplayMessaged) {
        RewardDisplayMessaged = rewardDisplayMessaged;
    }
    public String getDescription() {
        return Description;
    }
    public void setDescription(String description) {
        Description = description;
    }
    public String getDetail() {
        return Detail;
    }
    public void setDetail(String detail) {
        Detail = detail;
    }
    public long getVisibleStartDate() {
        return VisibleStartDate;
    }
    public void setVisibleStartDate(long visibleStartDate) {
        VisibleStartDate = visibleStartDate;
    }
    public long getVisibleEndDate() {
        return VisibleEndDate;
    }
    public void setVisibleEndDate(long visibleEndDate) {
        VisibleEndDate = visibleEndDate;
    }
    public String getImageURL() {
        return ImageURL;
    }
    public void setImageURL(String imageURL) {
        ImageURL = imageURL;
    }
    public boolean isIsActive() {
        return IsActive;
    }
    public void setIsActive(boolean isActive) {
        IsActive = isActive;
    }
    public String getCreatedAt() {
        return CreatedAt;
    }
    public void setCreatedAt(String createdAt) {
        CreatedAt = createdAt;
    }
    public String getUpdatedAt() {
        return UpdatedAt;
    }
    public void setUpdatedAt(String updatedAt) {
        UpdatedAt = updatedAt;
    }
    
    public boolean isOnSiteType() {
        return getDealType().equalsIgnoreCase(Const.DEAL_TYPE_ONSITE);
    }
    
    public boolean isFreeRewardType() {
        return getDealType().equalsIgnoreCase(Const.DEAL_TYPE_FREE_REWARD);
    }
    
    public boolean isDiscountType() {
        return getDealType().equalsIgnoreCase(Const.DEAL_TYPE_DISCOUTED);
    }
}
