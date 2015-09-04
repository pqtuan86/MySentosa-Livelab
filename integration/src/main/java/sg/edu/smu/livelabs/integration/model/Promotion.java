package sg.edu.smu.livelabs.integration.model;

import java.io.Serializable;
import java.net.URL;
import java.util.Date;

/**
 * This is the model class for promotion.
 * Created by Le Gia Hai on 8/6/2015.
 * Edited by John on 1/7/2015
 */
public class Promotion implements Serializable {
    private static final long serialVersionUID = -1267301040013412895L;
    private int id;
    private String title;
    private String details;
    private String description;
    private String campaignName;
    private Date startTime;
    private Date endTime;
    private URL image;
    private String workingHours;
    private String status;
    private String merchantName;
    private String merchantLocation;
    private String merchantPhone;
    private String merchantEmail;
    private String merchantWeb;
    private int campaignId;

    private int width, height;

    public Promotion() {}

    public Promotion(int id, String title, String details, String description, String campaignName,
                     Date startTime, Date endTime,
                     URL image, String workingHours, String status, String merchantName, String merchantLocation,
                     String merchantPhone, String merchantEmail, String merchantWeb, int campaignId) {
        this.id = id;
        this.title = title;
        this.details = details;
        this.description = description;
        this.campaignName = campaignName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.image = image;
        this.workingHours = workingHours;
        this.status = status;
        this.merchantName = merchantName;
        this.merchantLocation = merchantLocation;
        this.merchantPhone = merchantPhone;
        this.merchantEmail = merchantEmail;
        this.merchantWeb = merchantWeb;
        this.campaignId = campaignId;
    }

    public int getCampaignId(){
        return campaignId;
    }


    public String getMerchantLocation(){
        return merchantLocation;
    }

    public String getMerchantPhone(){
        return merchantPhone;
    }

    public String getMerchantEmail(){
        return merchantEmail;
    }

    public String getMerchantWeb(){
        return merchantWeb;
    }

    public String getMerchantName(){
        return merchantName;
    }

    public String getWorkingHours(){
        return workingHours;
    }

    public String getStatus(){
        return status;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDetails() {
        return details;
    }

    public String getCampaignName() {
        return campaignName;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public URL getImage() {
        return image;
    }


    public String getDescription(){
        return description;
    }
}
