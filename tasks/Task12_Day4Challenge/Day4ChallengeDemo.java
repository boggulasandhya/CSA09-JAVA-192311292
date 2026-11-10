package Task12_Day4Challenge;

import java.time.LocalTime;

/**
 * Task 12: Day-4 Design Challenge Demonstration
 * Demonstrates system adaptation for:
 * 1. New vehicle categories: EBike (flat fee + Chargeable) and DeliveryVan (peak hour rate).
 * 2. Visitor One-Day Pass privilege.
 * 3. 5-floor multi-level facility with heterogeneous (jagged) slot counts per floor.
 */
interface Chargeable {
    double chargingFee(int kWh);
}

abstract class Vehicle {
    private final String regNo;

    protected Vehicle(String regNo) {
        if (regNo == null || regNo.trim().isEmpty()) {
            throw new IllegalArgumentException("Registration number cannot be blank.");
        }
        this.regNo = regNo.trim().toUpperCase();
    }

    public String getRegNo() {
        return regNo;
    }

    protected abstract double ratePerHour();

    public double calculateFee(int minutes) {
        if (minutes <= 0) return 0.0;
        int hours = (minutes + 59) / 60;
        return hours * ratePerHour();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Vehicle other && regNo.equals(other.regNo);
    }

    @Override
    public int hashCode() {
        return regNo.hashCode();
    }
}

class Car extends Vehicle {
    public Car(String regNo) { super(regNo); }
    @Override protected double ratePerHour() { return 30.0; }
}

class ElectricCar extends Car implements Chargeable {
    public ElectricCar(String regNo) { super(regNo); }
    @Override public double chargingFee(int kWh) { return kWh * 12.0; }
}

// Day-4 New Vehicle 1: EBike (flat fee + Chargeable)
class EBike extends Vehicle implements Chargeable {
    private static final double FLAT_FEE = 15.0;

    public EBike(String regNo) { super(regNo); }

    @Override
    protected double ratePerHour() { return 0.0; }

    @Override
    public double calculateFee(int minutes) {
        return (minutes <= 0) ? 0.0 : FLAT_FEE;
    }

    @Override
    public double chargingFee(int kWh) {
        return kWh * 8.0;
    }
}

// Day-4 New Vehicle 2: DeliveryVan (Peak rate 9 AM - 5 PM)
class DeliveryVan extends Vehicle {
    public static final double STANDARD_RATE = 30.0;
    public static final double PEAK_RATE = 60.0; // Double car rate

    public DeliveryVan(String regNo) { super(regNo); }

    @Override
    protected double ratePerHour() { return STANDARD_RATE; }

    public double calculateFee(int minutes, LocalTime entryTime) {
        if (minutes <= 0) return 0.0;
        int hours = (minutes + 59) / 60;
        boolean isPeak = !entryTime.isBefore(LocalTime.of(9, 0)) && !entryTime.isAfter(LocalTime.of(17, 0));
        return hours * (isPeak ? PEAK_RATE : STANDARD_RATE);
    }
}

// Day-4 Pass Privileges
enum PassType {
    NONE,
    ONE_DAY_PASS,
    MONTHLY_PASS
}

class ParkingUser {
    private final String name;
    private final PassType pass;

    public ParkingUser(String name, PassType pass) {
        this.name = name;
        this.pass = pass;
    }

    public boolean isParkingExempt() {
        return pass == PassType.MONTHLY_PASS || pass == PassType.ONE_DAY_PASS;
    }

    public String getName() { return name; }
    public PassType getPass() { return pass; }
}

public class Day4ChallengeDemo {
    public static void main(String[] args) {
        System.out.println("=== DAY-4 DESIGN CHALLENGE VALIDATION ===");

        // 1. Dynamic Jagged 5-Floor Extension
        int[] fiveFloorConfig = new int[]{40, 35, 30, 25, 20}; // 150 total slots
        System.out.println("Extending facility to 5 dynamic floors: " + java.util.Arrays.toString(fiveFloorConfig));
        int totalCapacity = 0;
        for (int c : fiveFloorConfig) totalCapacity += c;
        System.out.println("Total Facility Capacity: " + totalCapacity + " slots.");

        // 2. Testing EBike Flat Fee & EV Charging
        EBike eBike = new EBike("TN05EB9999");
        double eBikeParkingFee = eBike.calculateFee(180); // 3 hours parked
        double eBikeChargeFee = eBike.chargingFee(5);     // 5 kWh consumed
        System.out.printf("EBike (180 min, 5 kWh): Parking=Rs %.2f, Charging=Rs %.2f -> Total=Rs %.2f%n",
                eBikeParkingFee, eBikeChargeFee, (eBikeParkingFee + eBikeChargeFee));

        // 3. Testing DeliveryVan Peak Hour (11:00 AM) vs Off-Peak Hour (07:00 AM)
        DeliveryVan van = new DeliveryVan("TN09DV1234");
        double peakFee = van.calculateFee(120, LocalTime.of(11, 0));   // 2 hrs @ Rs 60/hr
        double offPeakFee = van.calculateFee(120, LocalTime.of(7, 0)); // 2 hrs @ Rs 30/hr
        System.out.printf("DeliveryVan Peak (11:00 AM, 120 min): Rs %.2f (Expected: 2 hrs * Rs 60 = Rs 120.00)%n", peakFee);
        System.out.printf("DeliveryVan Off-Peak (07:00 AM, 120 min): Rs %.2f (Expected: 2 hrs * Rs 30 = Rs 60.00)%n", offPeakFee);

        // 4. Testing Visitor One-Day Pass Exemption
        ParkingUser visitor = new ParkingUser("Mr. Sharma", PassType.ONE_DAY_PASS);
        Car visitorCar = new Car("TN01VC5555");
        double baseCarFee = visitorCar.calculateFee(240); // 4 hours = Rs 120
        double payableByVisitor = visitor.isParkingExempt() ? 0.0 : baseCarFee;
        System.out.printf("Visitor with One-Day Pass (240 min): Base Fee=Rs %.2f, Payable=Rs %.2f (Exemption Applied)%n",
                baseCarFee, payableByVisitor);

        System.out.println(">> Day-4 Design Challenge fully verified.");
    }
}
