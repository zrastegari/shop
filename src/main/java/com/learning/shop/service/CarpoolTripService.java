package com.learning.shop.service;

import com.learning.shop.document.CarpoolTrip;
import com.learning.shop.document.TripStatus;
import com.learning.shop.dto.carpool.CreateTripRequest;
import com.learning.shop.dto.carpool.TripResponse;
import com.learning.shop.repository.CarpoolTripRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * سرویس مدیریت عملیات پایه‌ی سفرهای هم‌پیمایی (CRUD).
 * <p>
 * در این کلاس فقط ذخیره/بازیابی/لغو سفر انجام می‌شود؛
 * منطقِ اصلی matching در {@link CarpoolMatchingService} قرار دارد.
 */
@Service
public class CarpoolTripService {

    private static final Logger log = LoggerFactory.getLogger(CarpoolTripService.class);

    private final CarpoolTripRepository tripRepository;

    public CarpoolTripService(CarpoolTripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    /**
     * ساخت یک Document سفر از روی درخواست ثبت و ذخیره‌ی آن (status=ACTIVE).
     *
     * @return سفر ذخیره‌شده به صورت Document (برای اجرای matching)
     */
    public CarpoolTrip createTrip(CreateTripRequest request) {
        CarpoolTrip trip = new CarpoolTrip();
        trip.setUserId(request.getUserId());
        trip.setOriginLat(request.getOriginLat());
        trip.setOriginLng(request.getOriginLng());
        trip.setOriginRadiusMeters(request.getOriginRadiusMeters());
        trip.setDestLat(request.getDestLat());
        trip.setDestLng(request.getDestLng());
        trip.setDestRadiusMeters(request.getDestRadiusMeters());
        trip.setTripType(request.getTripType());
        trip.setEarliestDepartureTime(request.getEarliestDepartureTime());
        trip.setLatestDepartureTime(request.getLatestDepartureTime());
        trip.setStatus(TripStatus.ACTIVE);
        trip.setCreatedAt(LocalDateTime.now());

        CarpoolTrip saved = tripRepository.save(trip);
        log.info("سفر جدید ثبت شد: id={}, tripType={}", saved.getId(), saved.getTripType());
        return saved;
    }

    /**
     * بازیابی یک سفر با شناسه.
     *
     * @throws RuntimeException اگر سفر یافت نشود
     */
    public CarpoolTrip getTrip(String id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("سفر با شناسه " + id + " یافت نشد"));
    }

    /**
     * تبدیل سند به {@link TripResponse} — در لایه سرویس برای expose نشدن Document.
     */
    public TripResponse toResponse(CarpoolTrip trip) {
        return TripResponse.from(trip);
    }

    /**
     * لغو (CANCEL) کردن یک سفر — وضعیت به CANCELLED تغییر می‌کند.
     */
    public void cancelTrip(String id) {
        CarpoolTrip trip = getTrip(id);
        trip.setStatus(TripStatus.CANCELLED);
        tripRepository.save(trip);
        log.info("سفر {} لغو شد", id);
    }

    /**
     * تغییر وضعیت هر دو سفر یک match به MATCHED.
     * در هنگام تأیید match استفاده می‌شود.
     */
    public void markBothAsMatched(String tripOneId, String tripTwoId) {
        Map<String, CarpoolTrip> trips = Map.of(
                tripOneId, getTrip(tripOneId),
                tripTwoId, getTrip(tripTwoId)
        );
        for (CarpoolTrip trip : trips.values()) {
            trip.setStatus(TripStatus.MATCHED);
            tripRepository.save(trip);
        }
        log.info("هر دو سفر {} و {} به MATCHED تغییر کردند", tripOneId, tripTwoId);
    }
}
