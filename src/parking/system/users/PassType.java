package parking.system.users;

/**
 * Enumeration representing available parking passes and subscription tiers.
 */
public enum PassType {
    NONE(0.0),
    ONE_DAY_PASS(80.0),    // Flat fee for full day campus access for visitors
    MONTHLY_PASS(500.0);   // Prepaid institutional pass offering 100% parking fee exemption

    private final double cost;

    PassType(double cost) {
        this.cost = cost;
    }

    public double getCost() {
        return cost;
    }
}
