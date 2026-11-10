package parking.system.models;

/**
 * Interface representing vehicles capable of electric charging.
 * Provides abstract method for charging cost computation and a default
 * diagnostic method for charging specifications.
 */
public interface Chargeable {

    /**
     * Computes the charging fee based on energy consumed.
     *
     * @param kWh Energy consumed in kilowatt-hours (must be non-negative)
     * @return Charging fee in currency units (Rs)
     */
    double chargingFee(int kWh);

    /**
     * Default method demonstrating interface evolution in modern Java (Java 8+).
     * Provides an estimate of standard charging duration based on power rating.
     *
     * @param kWh Energy needed
     * @param chargingRateKw Charger power delivery in kW
     * @return Approximate charging duration in minutes
     */
    default int estimateChargingTimeMinutes(int kWh, double chargingRateKw) {
        if (chargingRateKw <= 0) {
            throw new IllegalArgumentException("Charging rate must be greater than zero.");
        }
        return (int) Math.ceil((kWh / chargingRateKw) * 60.0);
    }
}
