package com.learning.shop.document;

/**
 * وضعیت یک سفر هم‌پیمایی (CarpoolTrip).
 */
public enum TripStatus {
    /** سفر فعال و در انتظار پیدا کردن هم‌مسیر */
    ACTIVE,
    /** سفر با یک هم‌مسیر دیگر جفت شده و match تأیید شده است */
    MATCHED,
    /** سفر منقضی شده (بازه زمانی سپری شده) */
    EXPIRED,
    /** سفر لغو شده توسط کاربر */
    CANCELLED
}
