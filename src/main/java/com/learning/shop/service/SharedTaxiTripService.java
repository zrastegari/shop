package com.learning.shop.service;

import com.learning.shop.document.*;
import com.learning.shop.dto.sharedtaxi.CreateActiveTripRequest;
import com.learning.shop.dto.sharedtaxi.CreateWaitingPassengerRequest;
import com.learning.shop.repository.ActiveSharedTripRepository;
import com.learning.shop.repository.SharedTaxiOfferRepository;
import com.learning.shop.repository.WaitingPassengerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * سرویس مدیریت عملیات پایه‌ی تاکسی اشتراکی (CRUD + پذیرش/رد/تکمیل).
 * <p>
 * در این کلاس ذخیره/بازیابی/تغییر وضعیت انجام می‌شود؛ هسته‌ی تصمیم‌گیری
 * matching در {@link SharedTaxiMatchingService} قرار دارد.
 */
@Service
public class SharedTaxiTripService {

    private static final Logger log = LoggerFactory.getLogger(SharedTaxiTripService.class);

    private final ActiveSharedTripRepository activeTripRepository;
    private final WaitingPassengerRepository waitingPassengerRepository;
    private final SharedTaxiOfferRepository offerRepository;

    public SharedTaxiTripService(ActiveSharedTripRepository activeTripRepository,
                                 WaitingPassengerRepository waitingPassengerRepository,
                                 SharedTaxiOfferRepository offerRepository) {
        this.activeTripRepository = activeTripRepository;
        this.waitingPassengerRepository = waitingPassengerRepository;
        this.offerRepository = offerRepository;
    }

    // ---- ActiveSharedTrip ----

    /**
     * ثبت یک سفر فعال جدید (با ظرفیت اضافه) — وضعیت ACTIVE و createdAt=now.
     */
    public ActiveSharedTrip createActiveTrip(CreateActiveTripRequest request) {
        ActiveSharedTrip trip = new ActiveSharedTrip();
        trip.setDriverId(request.getDriverId());
        trip.setTripType(request.getTripType());
        trip.setExtraCapacity(request.getExtraCapacity());
        trip.setOriginLat(request.getOriginLat());
        trip.setOriginLng(request.getOriginLng());
        trip.setFinalDestLat(request.getFinalDestLat());
        trip.setFinalDestLng(request.getFinalDestLng());
        // موقعیت فعلی ابتدا برابر مبدأ است (یک API دیگر بعداً آپدیت می‌کند)
        trip.setCurrentLat(request.getOriginLat());
        trip.setCurrentLng(request.getOriginLng());
        trip.setStatus(SharedTripStatus.ACTIVE);
        trip.setCreatedAt(LocalDateTime.now());

        ActiveSharedTrip saved = activeTripRepository.save(trip);
        log.info("سفر فعال جدید ثبت شد: id={}, راننده={}, ظرفیت اضافه={}",
                saved.getId(), saved.getDriverId(), saved.getExtraCapacity());
        return saved;
    }

    /**
     * بازیابی یک سفر فعال با شناسه.
     */
    public ActiveSharedTrip getActiveTrip(String id) {
        return activeTripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("سفر فعال با شناسه " + id + " یافت نشد"));
    }

    /**
     * پیدا کردن همه‌ی سفرهای فعال.
     */
    public List<ActiveSharedTrip> findAllActiveTrips() {
        return activeTripRepository.findByStatus(SharedTripStatus.ACTIVE);
    }

    /**
     * علامت‌زدن یک توقف به‌عنوان انجام‌شده (completed=true).
     */
    @Transactional
    public ActiveSharedTrip completeStop(String tripId, String stopId) {
        ActiveSharedTrip trip = getActiveTrip(tripId);
        boolean found = false;
        for (TripStop stop : trip.getStops()) {
            if (stop.getId() != null && stop.getId().equals(stopId)) {
                stop.setCompleted(true);
                found = true;
                break;
            }
        }
        if (!found) {
            throw new RuntimeException("توقف با شناسه " + stopId + " در سفر " + tripId + " یافت نشد");
        }
        ActiveSharedTrip saved = activeTripRepository.save(trip);
        log.info("توقف {} در سفر {} تکمیل شد", stopId, tripId);
        return saved;
    }

    // ---- WaitingPassenger ----

    /**
     * ثبت یک مسافر منتظر جدید — وضعیت WAITING و requestedAt=now.
     */
    public WaitingPassenger createWaitingPassenger(CreateWaitingPassengerRequest request) {
        WaitingPassenger passenger = new WaitingPassenger();
        passenger.setPassengerId(request.getPassengerId());
        passenger.setTripType(request.getTripType());
        passenger.setPickupLat(request.getPickupLat());
        passenger.setPickupLng(request.getPickupLng());
        passenger.setDropoffLat(request.getDropoffLat());
        passenger.setDropoffLng(request.getDropoffLng());
        passenger.setRequestedAt(LocalDateTime.now());
        passenger.setStatus(WaitingStatus.WAITING);

        WaitingPassenger saved = waitingPassengerRepository.save(passenger);
        log.info("مسافر منتظر جدید ثبت شد: id={}, مسافر={}, type={}",
                saved.getId(), saved.getPassengerId(), saved.getTripType());
        return saved;
    }

    /**
     * بازیابی یک مسافر منتظر با شناسه.
     */
    public WaitingPassenger getWaitingPassenger(String id) {
        return waitingPassengerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("مسافر منتظر با شناسه " + id + " یافت نشد"));
    }

    /**
     * پیدا کردن همه‌ی مسافرهای منتظر (WAITING).
     */
    public List<WaitingPassenger> findAllWaitingPassengers() {
        return waitingPassengerRepository.findByStatus(WaitingStatus.WAITING);
    }

    // ---- SharedTaxiOffer ----

    /**
     * بازیابی یک offer با شناسه.
     */
    public SharedTaxiOffer getOffer(String id) {
        return offerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("پیشنهاد با شناسه " + id + " یافت نشد"));
    }

    /**
     * پذیرش یک offer (PENDING -> ACCEPTED):
     * ۱) توقف‌های proposedStops جایگزین stops سفر فعال می‌شوند.
     * ۲) وضعیت WaitingPassenger به MATCHED تغییر می‌کند.
     */
    @Transactional
    public SharedTaxiOffer acceptOffer(String offerId) {
        SharedTaxiOffer offer = getOffer(offerId);
        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new RuntimeException("پیشنهاد " + offerId + " در وضعیت پذیرش نیست (وضعیت: " + offer.getStatus() + ")");
        }

        // جایگزین کردن توقف‌های سفر فعال با ترتیب پیشنهادی
        ActiveSharedTrip trip = getActiveTrip(offer.getActiveTripId());
        trip.setStops(offer.getProposedStops());
        activeTripRepository.save(trip);

        // مارک کردن مسافر به‌عنوان MATCHED
        WaitingPassenger passenger = getWaitingPassenger(offer.getWaitingPassengerId());
        passenger.setStatus(WaitingStatus.MATCHED);
        waitingPassengerRepository.save(passenger);

        offer.setStatus(OfferStatus.ACCEPTED);
        SharedTaxiOffer saved = offerRepository.save(offer);
        log.info("offer {} پذیرفته شد: مسافر {} به سفر {} اضافه شد",
                saved.getId(), passenger.getPassengerId(), trip.getId());
        return saved;
    }

    /**
     * رد یک offer (PENDING -> REJECTED).
     * مسافر WAITING می‌ماند و می‌تواند در جستجوهای بعدی کاندید شود.
     */
    @Transactional
    public SharedTaxiOffer rejectOffer(String offerId) {
        SharedTaxiOffer offer = getOffer(offerId);
        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new RuntimeException("پیشنهاد " + offerId + " در وضعیت رد نیست (وضعیت: " + offer.getStatus() + ")");
        }
        offer.setStatus(OfferStatus.REJECTED);
        SharedTaxiOffer saved = offerRepository.save(offer);
        log.info("offer {} رد شد", offerId);
        return saved;
    }
}
