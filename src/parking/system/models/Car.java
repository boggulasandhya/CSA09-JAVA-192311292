package parking.system.models;

/**
 * Concrete subclass representing four-wheeler passenger cars.
 * Billed at standard rate of Rs 30 per hour.
 */
public class Car extends Vehicle {

    public static final double HOURLY_RATE = 30.0;

    /**
     * Constructs a Car with the specified registration number.
     *
     * @param regNo Vehicle registration number
     */
    public Car(String regNo) {
        super(regNo);
    }

    @Override
    protected double ratePerHour() {
        return HOURLY_RATE;
    }
}
