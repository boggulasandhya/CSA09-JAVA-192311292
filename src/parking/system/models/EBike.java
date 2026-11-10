package parking.system.models;

/**
 * Concrete subclass representing Electric Bikes (E-Bikes) introduced in Task 12.
 * E-Bikes incur a flat parking fee (Rs 15.0) regardless of total duration
 * and implement Chargeable for EV charging at Rs 8.0 per kWh.
 */
public class EBike extends Vehicle implements Chargeable {

    public static final double FLAT_PARKING_FEE = 15.0;
    public static final double CHARGING_RATE_PER_KWH = 8.0;

    /**
     * Constructs an EBike with the given registration number.
     *
     * @param regNo Vehicle registration number
     */
    public EBike(String regNo) {
        super(regNo);
    }

    /**
     * E-Bikes are charged a flat rate, so the hourly rate concept returns 0.0.
     */
    @Override
    protected double ratePerHour() {
        return 0.0;
    }

    /**
     * Overrides calculateFee to charge a flat fee of Rs 15.0 for any non-zero duration.
     *
     * @param minutes Total duration parked in minutes
     * @return Flat fee of Rs 15.0 (or 0.0 if duration is 0)
     */
    @Override
    public double calculateFee(int minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException("Duration cannot be negative: " + minutes);
        }
        return (minutes == 0) ? 0.0 : FLAT_PARKING_FEE;
    }

    /**
     * Implements Chargeable interface charging fee calculation.
     *
     * @param kWh Energy consumed in kWh
     * @return Charging fee in Rs
     */
    @Override
    public double chargingFee(int kWh) {
        if (kWh < 0) {
            throw new IllegalArgumentException("Energy consumed cannot be negative: " + kWh);
        }
        return kWh * CHARGING_RATE_PER_KWH;
    }
}
