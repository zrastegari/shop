package com.learning.shop.repository;

import com.learning.shop.document.SharedTaxiOffer;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository برای پیشنهادهای تاکسی اشتراکی.
 */
public interface SharedTaxiOfferRepository extends MongoRepository<SharedTaxiOffer, String> {
    // متدهای استاندارد (findById, save, ...) از MongoRepository کافی است
}
