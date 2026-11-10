package parking.system.models;

/**
 * Concrete subclass representing Electric Cars.
 * Demonstrates multilevel inheritance (Vehicle -> Car -> ElectricCar)
 * and interface implementation (Chargeable).
 * Billed at base car rate (Rs 30/hr) plus electrical charging at Rs 12 per kWh.
 */
public class ElectricCar extends Car implements Chargeable {

    public static final double CHARGING_RATE_PER_KWH = 12.0;

    /**
     * Constructs an ElectricCar with registration number, chaining to Car and Vehicle constructors.
     *
     * @param regNo Vehicle registration number
     */
    public ElectricCar(String regNo) {
        super(regNo);
    }

    /**
     * Implements Chargeable interface charging fee calculation.
     *
     * @param kWh Energy consumed in kilowatt-hours
     * @return Charging fee in Rs
     * @throws IllegalArgumentException if kWh is negative
     */
    @Override
    public double chargingFee(int kWh) {
        if (kWh < 0) {
            throw new IllegalArgumentException("Energy consumed cannot be negative: " + kWh);
        }
        return kWh * CHARGING_RATE_PER_KWH;
    }
}
