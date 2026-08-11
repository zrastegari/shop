package com.learning.shop.repository;

import com.learning.shop.document.CarpoolMatch;
import com.learning.shop.document.MatchStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Repository برای پیشنهادهای تطابق هم‌پیمایی.
 */
public interface CarpoolMatchRepository extends MongoRepository<CarpoolMatch, String> {

    /**
     * پیدا کردن همه پیشنهادهای تطابق مربوط به یک سفر (در هر دو جهت tripOne/tripTwo).
     * برای جلوگیری از ثبت تکراری و نمایشِ match های یک سفر استفاده می‌شود.
     */
    List<CarpoolMatch> findByTripOneId(String tripId);

    List<CarpoolMatch> findByTripTwoId(String tripId);

    /** پیدا کردن یک match خاص بین دو سفر مشخص (برای جلوگیری از ثبت تکراری) */
    boolean existsByTripOneIdAndTripTwoIdAndStatus(String tripOneId, String tripTwoId, MatchStatus status);

    /** پیدا کردن match های یک سفر که هنوز در حالت پیشنهاد هستند */
    List<CarpoolMatch> findByTripOneIdAndStatus(String tripId, MatchStatus status);

    List<CarpoolMatch> findByTripTwoIdAndStatus(String tripId, MatchStatus status);
}
