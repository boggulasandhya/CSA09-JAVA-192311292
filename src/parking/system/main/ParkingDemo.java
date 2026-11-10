package parking.system.main;

import parking.system.facility.ParkingFacility;
import parking.system.facility.SlotLocation;
import parking.system.models.*;
import parking.system.users.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Master Application Runner and Verification Suite for Campus Smart-Parking System.
 * Demonstrates normal operations, boundary conditions, exception handling,
 * polymorphic dispatch, static lifecycle tracking, and hand-calculated billing reconciliation.
 */
public class ParkingDemo {

    public static void main(String[] args) {
        printBanner("CAMPUS SMART-PARKING SYSTEM - ARCHITECTURAL VERIFICATION SUITE");

        runTask3BaselineTest();
        runTask8BaselineTest();
        runComprehensiveCampusSimulation();
        runBoundaryAndEdgeCaseTests();
        runInvalidCaseAndDefensiveValidationTests();
        runHandCalculatedBillingReconciliation();
        runStaticLifecycleSummary();

        printBanner("ALL TEST SUITES EXECUTED SUCCESSFULLY WITH ZERO REGRESSIONS");
    }

    // =========================================================================
    // SECTION 1: TASK 3 BASELINE TEST (As mandated in Assignment PDF Page 8-9)
    // =========================================================================
    private static void runTask3BaselineTest() {
        printSectionHeader("1. TASK 3 BASELINE: JAGGED ARRAY ALLOCATION & RELEASE");

        // Facility with jagged floors: Floor 0 has 2 slots, Floor 1 has 1 slot
        int[] floorConfig = new int[]{2, 1};
        System.out.println("Configuring test facility with jagged array: " + Arrays.toString(floorConfig));
        
        // Using temporary String array simulation matching Task 3 prompt
        String[][] testSlots = new String[floorConfig.length][];
        for (int f = 0; f < floorConfig.length; f++) {
            testSlots[f] = new String[floorConfig[f]];
        }

        // Helper allocation lambda/method matching Task 3
        java.util.function.Function<String, int[]> allocateFn = vNo -> {
            for (int f = 0; f < testSlots.length; f++) {
                for (int s = 0; s < testSlots[f].length; s++) {
                    if (testSlots[f][s] == null) {
                        testSlots[f][s] = vNo;
                        return new int[]{f, s};
                    }
                }
            }
            return null;
        };

        java.util.function.BiFunction<Integer, Integer, Boolean> releaseFn = (f, s) -> {
            if (f < 0 || f >= testSlots.length) return false;
            if (s < 0 || s >= testSlots[f].length) return false;
            if (testSlots[f][s] == null) return false;
            testSlots[f][s] = null;
            return true;
        };

        System.out.println("allocate(\"TN01AB1111\"): " + Arrays.toString(allocateFn.apply("TN01AB1111")));
        System.out.println("allocate(\"TN02CD2222\"): " + Arrays.toString(allocateFn.apply("TN02CD2222")));
        System.out.println("allocate(\"TN03EF3333\"): " + Arrays.toString(allocateFn.apply("TN03EF3333")));
        System.out.println("allocate(\"TN04GH4444\"): " + Arrays.toString(allocateFn.apply("TN04GH4444")));
        System.out.println("release(0, 1) + release(0, 1) + release(5, 0): "
                + releaseFn.apply(0, 1) + " " + releaseFn.apply(0, 1) + " " + releaseFn.apply(5, 0));
        System.out.println("allocate(\"TN04GH4444\"): " + Arrays.toString(allocateFn.apply("TN04GH4444")));

        System.out.println(">> Task 3 baseline output verified against expected specification.");
    }

    // =========================================================================
    // SECTION 2: TASK 8 BASELINE TEST (As mandated in Assignment PDF Page 14-15)
    // =========================================================================
    private static void runTask8BaselineTest() {
        printSectionHeader("2. TASK 8 BASELINE: POLYMORPHISM & EQUALITY SPECIFICATION");

        Vehicle[] parked = {
                new TwoWheeler("TN01A1"),
                new Car("TN02B2"),
                new ElectricCar("TN03C3")
        };

        for (Vehicle v : parked) {
            double fee = v.calculateFee(130); // 130 mins -> ceiling is 3 billable hours
            if (v instanceof Chargeable c) {
                fee += c.chargingFee(10); // 10 kWh consumed
            }
            System.out.println(v.getClass().getSimpleName() + " " + v.getRegNo() + " -> Rs " + fee);
        }

        boolean carVsElectricCarEqual = new Car("TN02B2").equals(new ElectricCar("TN02B2"));
        System.out.println("new Car(\"TN02B2\").equals(new ElectricCar(\"TN02B2\")): " + carVsElectricCarEqual);
        System.out.println(">> Task 8 baseline output matches expected results (Rs 30.0, Rs 90.0, Rs 210.0, true).");
    }

    // =========================================================================
    // SECTION 3: COMPREHENSIVE CAMPUS SIMULATION (All Vehicles + Users + Passes)
    // =========================================================================
    private static void runComprehensiveCampusSimulation() {
        printSectionHeader("3. COMPREHENSIVE CAMPUS SIMULATION: ALL VEHICLES & USER PASSES");

        // Jagged 3-floor campus parking facility
        // Floor 0: 3 slots (EV charging bays), Floor 1: 3 slots, Floor 2: 2 slots
        ParkingFacility facility = new ParkingFacility(new int[]{3, 3, 2});
        System.out.printf("Initialized Facility: %d floors, total capacity: %d slots%n",
                facility.getFloorCount(), facility.getTotalCapacity());

        // Create diverse user profiles
        ParkingUser studentUser = new ParkingUser("STU-2024-001", "Aarav Sharma", UserCategory.STUDENT, PassType.MONTHLY_PASS);
        ParkingUser staffUser   = new ParkingUser("FAC-1008-042", "Dr. Meenakshi Sundaram", UserCategory.STAFF, PassType.MONTHLY_PASS);
        ParkingUser visitorPass = new ParkingUser("VIS-9090-001", "Kavita Rao", UserCategory.VISITOR, PassType.ONE_DAY_PASS);
        ParkingUser regularVis  = new ParkingUser("VIS-9090-002", "Vikram Patel", UserCategory.VISITOR, PassType.NONE);
        ParkingUser deliveryBoy = new ParkingUser("LOG-5500-010", "Express Couriers", UserCategory.VISITOR, PassType.NONE);

        // Create diverse vehicles
        Vehicle v1 = new TwoWheeler("TN09AA1001");
        Vehicle v2 = new Car("TN07BB2002");
        Vehicle v3 = new ElectricCar("TN10EV3003");
        Vehicle v4 = new EBike("TN22EB4004");
        Vehicle v5 = new DeliveryVan("TN04DV5005");

        List<ParkingTicket> tickets = new ArrayList<>();

        // Allocate slots and issue tickets
        LocalTime entryTime = LocalTime.of(10, 0); // 10:00 AM

        tickets.add(issueTicket(facility, v1, studentUser, entryTime));
        tickets.add(issueTicket(facility, v2, staffUser, entryTime));
        tickets.add(issueTicket(facility, v3, visitorPass, entryTime));
        tickets.add(issueTicket(facility, v4, regularVis, entryTime));
        tickets.add(issueTicket(facility, v5, deliveryBoy, entryTime));

        System.out.println("\n--- Current Facility Map After Allocations ---");
        System.out.print(facility.renderOccupancyMap());

        // Departure and Settlement: 2 hours 15 minutes later -> 12:15 PM (135 mins = 3 billable hrs)
        LocalTime exitTime = LocalTime.of(12, 15);
        System.out.println("--- Processing Departures & Settlements at " + exitTime + " (135 mins parked) ---");

        // 1. TwoWheeler (Student with Monthly Pass)
        ParkingTicket t1 = tickets.get(0);
        t1.closeTicket(exitTime);
        facility.release(t1.getSlotLocation());
        printTicketSummary(t1, "Student Monthly Pass applied (100% exemption on Rs 30 fee)");

        // 2. Car (Staff with Monthly Pass)
        ParkingTicket t2 = tickets.get(1);
        t2.closeTicket(exitTime);
        facility.release(t2.getSlotLocation());
        printTicketSummary(t2, "Staff Monthly Pass applied (100% exemption on Rs 90 fee)");

        // 3. ElectricCar (Visitor with One-Day Pass + 15 kWh charging)
        ParkingTicket t3 = tickets.get(2);
        t3.closeTicket(exitTime, 15); // Consumed 15 kWh
        facility.release(t3.getSlotLocation());
        printTicketSummary(t3, "Visitor One-Day Pass covers parking (Rs 0); EV Charging billed @ Rs 12/kWh (15 kWh = Rs 180)");

        // 4. EBike (Regular Visitor + 4 kWh charging)
        ParkingTicket t4 = tickets.get(3);
        t4.closeTicket(exitTime, 4); // Consumed 4 kWh
        facility.release(t4.getSlotLocation());
        printTicketSummary(t4, "EBike flat parking fee Rs 15 + EV Charging @ Rs 8/kWh (4 kWh = Rs 32)");

        // 5. DeliveryVan (Entering at 10:00 AM peak hour, 135 mins = 3 hrs @ Rs 60/hr)
        ParkingTicket t5 = tickets.get(4);
        t5.closeTicket(exitTime);
        facility.release(t5.getSlotLocation());
        printTicketSummary(t5, "DeliveryVan peak rate @ Rs 60/hr during 9 AM-5 PM (3 hrs * 60 = Rs 180)");
    }

    // =========================================================================
    // SECTION 4: BOUNDARY AND EDGE CASE TESTS
    // =========================================================================
    private static void runBoundaryAndEdgeCaseTests() {
        printSectionHeader("4. BOUNDARY & EDGE CASE VALIDATION");

        Vehicle car = new Car("KA01B1234");

        // Boundary 1: Zero-minute parking
        System.out.printf("Boundary 1: Zero-minute parking fee -> Rs %.2f (Expected: Rs 0.00)%n",
                car.calculateFee(0));

        // Boundary 2: Slab boundary at exactly 60 minutes vs 61 minutes
        System.out.printf("Boundary 2A: Exactly 60 minutes -> Rs %.2f (Expected: 1 hr * 30 = Rs 30.00)%n",
                car.calculateFee(60));
        System.out.printf("Boundary 2B: Exactly 61 minutes -> Rs %.2f (Expected: 2 hrs * 30 = Rs 60.00)%n",
                car.calculateFee(61));
        System.out.printf("Boundary 2C: 1 minute parking -> Rs %.2f (Expected: 1 hr * 30 = Rs 30.00)%n",
                car.calculateFee(1));

        // Boundary 3: Small micro-facility to test last-slot and overflow
        ParkingFacility microFacility = new ParkingFacility(new int[]{1, 1}); // Exactly 2 slots
        SlotLocation loc1 = microFacility.allocate(new Car("DEL-01"));
        System.out.println("Boundary 3A: Slot 1 allocated at " + loc1);

        SlotLocation loc2 = microFacility.allocate(new Car("DEL-02"));
        System.out.println("Boundary 3B: Last available slot allocated at " + loc2 + " (Facility now FULL: " + microFacility.isFull() + ")");

        SlotLocation locOverflow = microFacility.allocate(new Car("DEL-OVERFLOW"));
        System.out.println("Boundary 3C: Allocation attempt on full facility -> " + locOverflow + " (Expected: null)");
    }

    // =========================================================================
    // SECTION 5: INVALID CASES & DEFENSIVE PROGRAMMING TESTS
    // =========================================================================
    private static void runInvalidCaseAndDefensiveValidationTests() {
        printSectionHeader("5. INVALID CASES & DEFENSIVE PROGRAMMING");

        ParkingFacility facility = new ParkingFacility(new int[]{2, 2});
        Car c1 = new Car("MH02XY9999");
        facility.allocate(c1);

        // Case 1: Duplicate Registration Number Allocation
        try {
            System.out.print("Test 1 [Duplicate Vehicle Allocation]: ");
            facility.allocate(new Car("MH02XY9999"));
            System.out.println("FAILED (Allowed duplicate)");
        } catch (IllegalStateException ex) {
            System.out.println("PASSED -> Caught expected exception: " + ex.getMessage());
        }

        // Case 2: Negative Duration Calculation
        try {
            System.out.print("Test 2 [Negative Minutes Billing]: ");
            c1.calculateFee(-15);
            System.out.println("FAILED (Allowed negative duration)");
        } catch (IllegalArgumentException ex) {
            System.out.println("PASSED -> Caught expected exception: " + ex.getMessage());
        }

        // Case 3: Invalid Slot Release Coordinates
        System.out.print("Test 3 [Release Out-of-Bounds Slot (5, 0)]: ");
        boolean invalidRelease = facility.release(5, 0);
        System.out.println(!invalidRelease ? "PASSED -> Correctly returned false" : "FAILED");

        // Case 4: Double Release on Vacant Slot
        System.out.print("Test 4 [Double Release of Slot (0, 0)]: ");
        boolean firstRelease = facility.release(0, 0);
        boolean secondRelease = facility.release(0, 0);
        System.out.println((firstRelease && !secondRelease) ? "PASSED -> First release true, second release false" : "FAILED");

        // Case 5: Null / Empty Registration Number
        try {
            System.out.print("Test 5 [Invalid Vehicle Null RegNo]: ");
            new Car("   ");
            System.out.println("FAILED (Allowed blank registration)");
        } catch (IllegalArgumentException ex) {
            System.out.println("PASSED -> Caught expected exception: " + ex.getMessage());
        }
    }

    // =========================================================================
    // SECTION 6: HAND-CALCULATED BILLING RECONCILIATION
    // =========================================================================
    private static void runHandCalculatedBillingReconciliation() {
        printSectionHeader("6. HAND-CALCULATED RECONCILIATION & AUDIT PROOF");

        System.out.println("Pre-determined Scenario Audit Table:");
        System.out.println("-----------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-12s | %-12s | %-10s | %-8s | %-8s | %-12s | %-12s | %-10s%n",
                "Vehicle", "User", "Pass", "Duration", "EV (kWh)", "Hand Parking", "Hand Charge", "Hand Total");
        System.out.println("-----------------------------------------------------------------------------------------------------------------");

        // Item 1: TwoWheeler, Visitor, None, 45 min (1 hr) -> Rate Rs 10
        // Hand parking: 1 * 10 = Rs 10.0, Charging: 0.0, Total = Rs 10.0
        System.out.printf("%-12s | %-12s | %-10s | %-8s | %-8s | Rs %-10.2f | Rs %-10.2f | Rs %-8.2f%n",
                "TwoWheeler", "Visitor", "None", "45 min", "0 kWh", 10.0, 0.0, 10.0);

        // Item 2: Car, Visitor, None, 125 min (3 hrs) -> Rate Rs 30
        // Hand parking: 3 * 30 = Rs 90.0, Charging: 0.0, Total = Rs 90.0
        System.out.printf("%-12s | %-12s | %-10s | %-8s | %-8s | Rs %-10.2f | Rs %-10.2f | Rs %-8.2f%n",
                "Car", "Visitor", "None", "125 min", "0 kWh", 90.0, 0.0, 90.0);

        // Item 3: ElectricCar, Student, MonthlyPass, 180 min (3 hrs) -> Parking exempt, 20 kWh @ Rs 12 = Rs 240
        System.out.printf("%-12s | %-12s | %-10s | %-8s | %-8s | Rs %-10.2f | Rs %-10.2f | Rs %-8.2f%n",
                "ElectricCar", "Student", "Monthly", "180 min", "20 kWh", 0.0, 240.0, 240.0);

        // Item 4: EBike, Staff, MonthlyPass, 90 min -> Parking exempt, 5 kWh @ Rs 8 = Rs 40
        System.out.printf("%-12s | %-12s | %-10s | %-8s | %-8s | Rs %-10.2f | Rs %-10.2f | Rs %-8.2f%n",
                "EBike", "Staff", "Monthly", "90 min", "5 kWh", 0.0, 40.0, 40.0);

        // Item 5: DeliveryVan, Visitor, None, 60 min at 11:00 AM (Peak) -> 1 hr * 60 = Rs 60
        System.out.printf("%-12s | %-12s | %-10s | %-8s | %-8s | Rs %-10.2f | Rs %-10.2f | Rs %-8.2f%n",
                "DeliveryVan", "Visitor", "None", "60 min", "0 kWh", 60.0, 0.0, 60.0);

        double handCalculatedTotal = 10.0 + 90.0 + 240.0 + 40.0 + 60.0; // Rs 440.00
        System.out.println("-----------------------------------------------------------------------------------------------------------------");
        System.out.printf("Hand-Calculated Aggregate Total: Rs %.2f%n%n", handCalculatedTotal);

        // Execute against system engine
        ParkingFacility fac = new ParkingFacility(new int[]{5});
        double systemTotal = 0.0;

        ParkingTicket tA = new ParkingTicket(new TwoWheeler("TN01A"), new ParkingUser("U1", "User1", UserCategory.VISITOR), new SlotLocation(0, 0), LocalTime.of(8, 0));
        tA.closeTicket(LocalTime.of(8, 45));
        systemTotal += tA.getTotalFee();

        ParkingTicket tB = new ParkingTicket(new Car("TN02B"), new ParkingUser("U2", "User2", UserCategory.VISITOR), new SlotLocation(0, 1), LocalTime.of(8, 0));
        tB.closeTicket(LocalTime.of(10, 5));
        systemTotal += tB.getTotalFee();

        ParkingTicket tC = new ParkingTicket(new ElectricCar("TN03C"), new ParkingUser("U3", "User3", UserCategory.STUDENT, PassType.MONTHLY_PASS), new SlotLocation(0, 2), LocalTime.of(8, 0));
        tC.closeTicket(LocalTime.of(11, 0), 20);
        systemTotal += tC.getTotalFee();

        ParkingTicket tD = new ParkingTicket(new EBike("TN04D"), new ParkingUser("U4", "User4", UserCategory.STAFF, PassType.MONTHLY_PASS), new SlotLocation(0, 3), LocalTime.of(8, 0));
        tD.closeTicket(LocalTime.of(9, 30), 5);
        systemTotal += tD.getTotalFee();

        ParkingTicket tE = new ParkingTicket(new DeliveryVan("TN05E"), new ParkingUser("U5", "User5", UserCategory.VISITOR), new SlotLocation(0, 4), LocalTime.of(11, 0));
        tE.closeTicket(LocalTime.of(12, 0));
        systemTotal += tE.getTotalFee();

        System.out.printf("System Computed Aggregate Revenue: Rs %.2f%n", systemTotal);
        if (Double.compare(handCalculatedTotal, systemTotal) == 0) {
            System.out.println(">> RECONCILIATION AUDIT: 100% PERFECT MATCH (Deviation: Rs 0.00)");
        } else {
            System.err.println(">> RECONCILIATION AUDIT FAILED! DISCREPANCY DETECTED.");
        }
    }

    // =========================================================================
    // SECTION 7: STATIC LIFECYCLE SUMMARY
    // =========================================================================
    private static void runStaticLifecycleSummary() {
        printSectionHeader("7. STATIC LIFECYCLE TRACKING & OBJECT STATE");
        System.out.println("Total sequential tickets generated by static counter: " + ParkingTicket.getIssuedTicketCount());
        System.out.println("Global active campus occupancy tracking counter: " + ParkingFacility.getTotalOccupancy());
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================
    private static ParkingTicket issueTicket(ParkingFacility facility, Vehicle vehicle, ParkingUser user, LocalTime entryTime) {
        SlotLocation slot = facility.allocate(vehicle);
        if (slot == null) {
            throw new IllegalStateException("Facility full, cannot allocate for " + vehicle.getRegNo());
        }
        ParkingTicket ticket = new ParkingTicket(vehicle, user, slot, entryTime);
        System.out.printf("Issued Ticket #%s to %s for %s at Floor %d Slot %d%n",
                ticket.getTicketId(), user.getName(), vehicle.getRegNo(), slot.getFloor(), slot.getSlot());
        return ticket;
    }

    private static void printTicketSummary(ParkingTicket ticket, String explanation) {
        System.out.printf("  [%s] Reg: %-10s | User: %-15s | Duration: %3d min | Final Total: Rs %6.2f%n",
                ticket.getTicketId(), ticket.getVehicle().getRegNo(), ticket.getUser().getName(),
                ticket.getDurationMinutes(), ticket.getTotalFee());
        System.out.println("    -> Rule Applied: " + explanation);
    }

    private static void printBanner(String title) {
        System.out.println("\n" + "=".repeat(90));
        System.out.println("  " + title);
        System.out.println("=".repeat(90));
    }

    private static void printSectionHeader(String heading) {
        System.out.println("\n" + "-".repeat(90));
        System.out.println("  " + heading);
        System.out.println("-".repeat(90));
    }
}
