package parking.system.models;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Concrete subclass representing Commercial Delivery Vans introduced in Task 12.
 * Pricing rule: Charged at double the Car rate (Rs 60/hr) during peak hours (09:00 to 17:00),
 * and at normal Car rate (Rs 30/hr) outside this window.
 */
public class DeliveryVan extends Vehicle {

    public static final double STANDARD_RATE = 30.0;
    public static final double PEAK_RATE = 60.0; // Double the car rate

    private static final LocalTime PEAK_START = LocalTime.of(9, 0);
    private static final LocalTime PEAK_END = LocalTime.of(17, 0);

    /**
     * Constructs a DeliveryVan with the specified registration number.
     *
     * @param regNo Vehicle registration number
     */
    public DeliveryVan(String regNo) {
        super(regNo);
    }

    @Override
    protected double ratePerHour() {
        return STANDARD_RATE;
    }

    /**
     * Overloaded calculateFee method incorporating entry time to determine peak vs off-peak rates.
     *
     * @param minutes   Duration parked in minutes
     * @param entryTime Time the delivery van entered
     * @return Calculated fee according to time-of-day rule
     */
    public double calculateFee(int minutes, LocalTime entryTime) {
        if (minutes < 0) {
            throw new IllegalArgumentException("Minutes cannot be negative.");
        }
        if (minutes == 0) {
            return 0.0;
        }
        int billableHours = (minutes + 59) / 60;
        double applicableRate = isPeakHour(entryTime) ? PEAK_RATE : STANDARD_RATE;
        return billableHours * applicableRate;
    }

    /**
     * Overloaded calculateFee between two LocalTime stamps.
     */
    @Override
    public double calculateFee(LocalTime entryTime, LocalTime exitTime) {
        Objects.requireNonNull(entryTime, "Entry time cannot be null.");
        Objects.requireNonNull(exitTime, "Exit time cannot be null.");
        if (exitTime.isBefore(entryTime)) {
            throw new IllegalArgumentException("Exit time cannot be before entry time.");
        }
        int minutes = (int) Duration.between(entryTime, exitTime).toMinutes();
        return calculateFee(minutes, entryTime);
    }

    /**
     * Helper to verify if entry time falls within peak delivery hours [09:00, 17:00].
     *
     * @param time Entry time to check
     * @return true if between 09:00 and 17:00 inclusive
     */
    public static boolean isPeakHour(LocalTime time) {
        if (time == null) return false;
        return !time.isBefore(PEAK_START) && !time.isAfter(PEAK_END);
    }
}
