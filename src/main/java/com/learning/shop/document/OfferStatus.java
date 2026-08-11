package com.learning.shop.document;

/**
 * وضعیت یک پیشنهاد تاکسی اشتراکی (SharedTaxiOffer).
 */
public enum OfferStatus {
    /** پیشنهاد در انتظار تأیید/رد است */
    PENDING,
    /** پیشنهاد پذیرفته شده است */
    ACCEPTED,
    /** پیشنهاد رد شده است */
    REJECTED,
    /** پیشنهاد منقضی شده است */
    EXPIRED
}
