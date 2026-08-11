package com.learning.shop.document;

/**
 * وضعیت یک سفر فعال تاکسی اشتراکی (ActiveSharedTrip).
 */
public enum SharedTripStatus {
    /** سفر در حال انجام است و می‌تواند مسافر جدید سوار کند */
    ACTIVE,
    /** سفر به پایان رسیده است */
    COMPLETED,
    /** سفر لغو شده است */
    CANCELLED
}
