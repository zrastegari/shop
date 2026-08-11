package com.learning.shop.repository;

import com.learning.shop.document.WaitingPassenger;
import com.learning.shop.document.WaitingStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Repository برای مسافرهای منتظر تاکسی اشتراکی.
 */
public interface WaitingPassengerRepository extends MongoRepository<WaitingPassenger, String> {

    /** پیدا کردن همه مسافرهای منتظر با یک وضعیت مشخص — معمولاً WAITING */
    List<WaitingPassenger> findByStatus(WaitingStatus status);
}
