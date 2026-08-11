package com.learning.shop.document;

/**
 * وضعیت یک مسافر منتظر تاکسی اشتراکی (WaitingPassenger).
 */
public enum WaitingStatus {
    /** مسافر هنوز منتظر وصل شدن به یک سفر فعال است */
    WAITING,
    /** مسافر به یک سفر فعال وصل شده است (پیشنهاد پذیرفته شده) */
    MATCHED,
    /** درخواست مسافر لغو شده است */
    CANCELLED,
    /** درخواست مسافر منقضی شده است */
    EXPIRED
}
