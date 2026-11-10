package parking.system.models;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Abstract base class representing a generic vehicle in the Campus Smart-Parking System.
 * Enforces encapsulation with private immutable fields, defines core polymorphic fee calculation,
 * and standardizes object equivalence based on vehicle registration numbers.
 */
public abstract class Vehicle {

    private final String regNo;

    /**
     * Protected constructor to ensure instantiation only via specialized subclasses.
     * Performs defensive validation on vehicle registration number.
     *
     * @param regNo Unique vehicle registration number (e.g., "TN01AB1111")
     * @throws IllegalArgumentException if regNo is null, empty, or whitespace-only
     */
    protected Vehicle(String regNo) {
        if (regNo == null || regNo.trim().isEmpty()) {
            throw new IllegalArgumentException("Registration number cannot be null or empty.");
        }
        this.regNo = regNo.trim().toUpperCase();
    }

    /**
     * Gets the normalized uppercase vehicle registration number.
     *
     * @return Vehicle registration number
     */
    public String getRegNo() {
        return regNo;
    }

    /**
     * Returns the hourly parking rate specific to this vehicle category.
     * Subclasses must override this to supply their distinct rate.
     *
     * @return Rate per hour in Rs
     */
    protected abstract double ratePerHour();

    /**
     * Computes the base parking fee for a given parking duration in minutes.
     * Every started hour is charged in full using integer ceiling arithmetic
     * without floating-point division errors.
     *
     * @param minutes Total duration parked in minutes
     * @return Base parking fee in Rs
     * @throws IllegalArgumentException if minutes is negative
     */
    public double calculateFee(int minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException("Parking duration cannot be negative: " + minutes);
        }
        if (minutes == 0) {
            return 0.0;
        }
        // Integer ceiling arithmetic: (minutes + 59) / 60
        // e.g., 1 min -> 1 hr, 59 min -> 1 hr, 60 min -> 1 hr, 61 min -> 2 hrs, 130 min -> 3 hrs
        int billableHours = (minutes + 59) / 60;
        return billableHours * ratePerHour();
    }

    /**
     * Overloaded method demonstrating compile-time polymorphism (method overloading).
     * Calculates fee between two LocalTime stamps within the same day.
     *
     * @param entryTime Time the vehicle entered the facility
     * @param exitTime  Time the vehicle left the facility
     * @return Base parking fee in Rs
     */
    public double calculateFee(LocalTime entryTime, LocalTime exitTime) {
        Objects.requireNonNull(entryTime, "Entry time cannot be null.");
        Objects.requireNonNull(exitTime, "Exit time cannot be null.");
        if (exitTime.isBefore(entryTime)) {
            throw new IllegalArgumentException("Exit time cannot be before entry time.");
        }
        long minutesBetween = Duration.between(entryTime, exitTime).toMinutes();
        return calculateFee((int) minutesBetween);
    }

    /**
     * Equivalence contract: two vehicles are equal if they belong to the Vehicle hierarchy
     * and possess the identical registration number.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof Vehicle other && Objects.equals(this.regNo, other.regNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(regNo);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [regNo=" + regNo + ", baseRate=" + ratePerHour() + "/hr]";
    }
}
