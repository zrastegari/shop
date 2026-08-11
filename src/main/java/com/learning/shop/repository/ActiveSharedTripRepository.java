package com.learning.shop.repository;

import com.learning.shop.document.ActiveSharedTrip;
import com.learning.shop.document.SharedTripStatus;
import com.learning.shop.document.TripType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Repository برای سفرهای فعال تاکسی اشتراکی.
 * از MongoRepository ارث می‌برد (نه JpaRepository) چون از MongoDB استفاده می‌شود.
 */
public interface ActiveSharedTripRepository extends MongoRepository<ActiveSharedTrip, String> {

    /** پیدا کردن سفرهای فعال با یک نوع خاص — برای فیلتر ارزان در matching */
    List<ActiveSharedTrip> findByStatusAndTripType(SharedTripStatus status, TripType tripType);

    /** پیدا کردن همه سفرهای فعال — برای جستجوی Passenger-initiated */
    List<ActiveSharedTrip> findByStatus(SharedTripStatus status);
}
