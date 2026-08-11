package com.learning.shop.document;

/**
 * وضعیت یک پیشنهاد تطابق هم‌پیمایی (CarpoolMatch).
 */
public enum MatchStatus {
    /** پیشنهاد اولیه — فقط به دو طرف معرفی شده، هنوز تأیید نشده */
    SUGGESTED,
    /** دو طرف تأیید کرده‌اند */
    CONFIRMED,
    /** یکی از طرفین رد کرده است */
    REJECTED
}
