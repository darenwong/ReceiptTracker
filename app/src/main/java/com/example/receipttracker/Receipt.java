package com.example.receipttracker;

import android.net.Uri;

import java.util.Date;

public class Receipt implements Comparable<Receipt> {
    private Integer id;
    private Float amount;
    private String currency;
    private Date date;
    private String title;
    private String category;
    private String note;
    private String image;
    private Boolean isSummary;
    private Boolean isChecked;

    public Receipt(Integer id, Float amount, String currency, Date date, String title, String category, String note, String image, Boolean isSummary, Boolean isChecked) {
        this.id = id;
        this.amount = amount;
        this.currency = currency;
        this.date = date;
        this.title = title;
        this.category = category;
        this.note = note;
        this.image = image;
        this.isSummary = isSummary;
        this.isChecked = isChecked;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Boolean getSummary() {
        return isSummary;
    }

    public void setSummary(Boolean summary) {
        isSummary = summary;
    }

    public Boolean getChecked() {
        return isChecked;
    }

    public void setChecked(Boolean checked) {
        isChecked = checked;
    }

    @Override
    public int compareTo(Receipt o) {
        return this.getDate().compareTo(o.getDate());
    }
}
