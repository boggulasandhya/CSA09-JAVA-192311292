package parking.system.users;

import java.util.Objects;

/**
 * Represents a campus parking system patron (Student, Staff, or Visitor).
 * Encapsulates identification, category, pass status, and fee exemption logic.
 */
public class ParkingUser {

    private final String userId;
    private final String name;
    private final UserCategory category;
    private PassType passType;

    /**
     * Parameterized constructor with validation.
     *
     * @param userId   Unique patron ID (e.g., student ID, employee ID, or visitor badge)
     * @param name     Patron full name
     * @param category UserCategory classification
     * @param passType Current parking pass type
     */
    public ParkingUser(String userId, String name, UserCategory category, PassType passType) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be null or empty.");
        }
        this.userId = userId.trim();
        this.name = name.trim();
        this.category = Objects.requireNonNull(category, "User category cannot be null.");
        this.passType = (passType == null) ? PassType.NONE : passType;
    }

    /**
     * Overloaded convenience constructor default to PassType.NONE.
     */
    public ParkingUser(String userId, String name, UserCategory category) {
        this(userId, name, category, PassType.NONE);
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public UserCategory getCategory() {
        return category;
    }

    public PassType getPassType() {
        return passType;
    }

    public void setPassType(PassType passType) {
        this.passType = (passType == null) ? PassType.NONE : passType;
    }

    /**
     * Checks if the user is exempt from regular parking fees.
     * Staff and students with valid Monthly Pass park free of charge.
     * Visitors with a One-Day Pass also have parking fee covered.
     *
     * @return true if fee exemption applies
     */
    public boolean isParkingFeeExempt() {
        if ((category == UserCategory.STAFF || category == UserCategory.STUDENT)
                && passType == PassType.MONTHLY_PASS) {
            return true;
        }
        return category == UserCategory.VISITOR && passType == PassType.ONE_DAY_PASS;
    }

    /**
     * Applies discount / exemption to computed hourly parking fee.
     *
     * @param standardFee Base parking fee calculated from vehicle rate and duration
     * @return Final parking fee payable by this user
     */
    public double applyParkingDiscount(double standardFee) {
        if (isParkingFeeExempt()) {
            return 0.0;
        }
        return standardFee;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParkingUser other)) return false;
        return Objects.equals(userId, other.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "ParkingUser{" +
                "userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", category=" + category +
                ", passType=" + passType +
                ", isExempt=" + isParkingFeeExempt() +
                '}';
    }
}
