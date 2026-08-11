package com.learning.shop.repository;

import com.learning.shop.document.CarpoolTrip;
import com.learning.shop.document.TripStatus;
import com.learning.shop.document.TripType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Repository برای سفرهای هم‌پیمایی.
 * <p>
 * از {@link MongoRepository} ارث می‌برد (نه JpaRepository) چون
 * این سرویس از MongoDB استفاده می‌کند. Spring Data MongoDB دقیقاً مثل
 * Spring Data JPA روشِ «method query» را پشتیبانی می‌کند یعنی با تعریف
 * متدی مثل {@code findByStatusAndTripType} خودش کوئری مناسب را می‌سازد.
 */
public interface CarpoolTripRepository extends MongoRepository<CarpoolTrip, String> {

    /**
     * پیدا کردن سفرهای فعال با یک نوع سفر مشخص.
     * برای فیلتر ارزان در الگوریتم matching استفاده می‌شود.
     */
    List<CarpoolTrip> findByStatusAndTripType(TripStatus status, TripType tripType);

    /** پیدا کردن سفر فعال یک کاربر مشخص */
    List<CarpoolTrip> findByUserIdAndStatus(Long userId, TripStatus status);
}
