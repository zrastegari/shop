package com.learning.shop.model;

public enum Category {
    ELECTRONICS("الکترونیک"),
    CLOTHING("پوشاک"),
    BOOKS("کتاب"),
    HOME_APPLIANCES("لوازم خانگی"),
    SPORTS("ورزشی"),
    OTHER("سایر");

    private final String persianName;

    Category(String persianName) {
        this.persianName = persianName;
    }

    public String getPersianName() {
        return persianName;
    }
}
