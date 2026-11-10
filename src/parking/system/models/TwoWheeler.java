package parking.system.models;

/**
 * Concrete subclass representing motorized two-wheelers (motorcycles, scooters).
 * Billed at standard rate of Rs 10 per hour.
 */
public class TwoWheeler extends Vehicle {

    public static final double HOURLY_RATE = 10.0;

    /**
     * Constructs a TwoWheeler with the given registration number.
     *
     * @param regNo Vehicle registration number
     */
    public TwoWheeler(String regNo) {
        super(regNo);
    }

    @Override
    protected double ratePerHour() {
        return HOURLY_RATE;
    }
}
