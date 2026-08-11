package com.learning.shop.document;

/**
 * نوع سفر هم‌پیمایی.
 * <p>
 * سفرهای داخل‌شهری و برون‌شهری باید کاملاً جدا از هم بررسی شوند.
 */
public enum TripType {
    /** سفر داخل شهر (درون‌شهری) */
    IN_CITY,
    /** سفر برون‌شهری (بین شهرها) */
    OUT_CITY
}
