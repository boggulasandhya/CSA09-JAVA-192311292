package parking.system.facility;

import parking.system.models.Vehicle;

import java.util.Arrays;
import java.util.Objects;

/**
 * Manages physical parking slot allocation across a multi-floor facility.
 * Uses jagged dynamic arrays (Vehicle[floor][slot]) allowing each floor to hold
 * a distinct number of parking bays.
 * Tracks global campus occupancy via a static counter.
 */
public class ParkingFacility {

    // Multi-floor jagged storage: slots[floor][slot] holds Vehicle reference or null
    private final Vehicle[][] slots;
    private final int totalCapacity;

    // Static counter tracking total currently occupied slots across all facility instances
    private static int totalOccupancy = 0;

    /**
     * Initializes the multi-level parking facility with jagged floor capacities.
     *
     * @param slotsPerFloor Array where index is floor number and value is slot count on that floor
     * @throws IllegalArgumentException if slotsPerFloor is null, empty, or contains non-positive counts
     */
    public ParkingFacility(int[] slotsPerFloor) {
        if (slotsPerFloor == null || slotsPerFloor.length == 0) {
            throw new IllegalArgumentException("Facility must have at least one floor with valid slot capacity.");
        }
        this.slots = new Vehicle[slotsPerFloor.length][];
        int capacityAccumulator = 0;

        for (int f = 0; f < slotsPerFloor.length; f++) {
            if (slotsPerFloor[f] < 0) {
                throw new IllegalArgumentException("Floor " + f + " capacity cannot be negative: " + slotsPerFloor[f]);
            }
            this.slots[f] = new Vehicle[slotsPerFloor[f]];
            capacityAccumulator += slotsPerFloor[f];
        }
        this.totalCapacity = capacityAccumulator;
    }

    /**
     * Static accessor for campus-wide total occupancy.
     *
     * @return Total occupied slots
     */
    public static int getTotalOccupancy() {
        return totalOccupancy;
    }

    /**
     * Resets static occupancy counter (useful during testing suites).
     */
    public static void resetTotalOccupancy() {
        totalOccupancy = 0;
    }

    public int getTotalCapacity() {
        return totalCapacity;
    }

    public int getFloorCount() {
        return slots.length;
    }

    public int getSlotsOnFloor(int floor) {
        validateFloorIndex(floor);
        return slots[floor].length;
    }

    /**
     * Returns true if no free slots exist across all floors.
     */
    public boolean isFull() {
        return getAvailableSlotCount() == 0;
    }

    /**
     * Calculates current available free slots in this facility.
     */
    public int getAvailableSlotCount() {
        int count = 0;
        for (Vehicle[] floor : slots) {
            for (Vehicle v : floor) {
                if (v == null) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Allocates the first available free slot to the vehicle.
     *
     * @param vehicle Vehicle requesting parking
     * @return SlotLocation {floor, slot} or null if facility is full
     * @throws IllegalArgumentException if vehicle is null
     */
    public SlotLocation allocate(Vehicle vehicle) {
        Objects.requireNonNull(vehicle, "Vehicle to allocate cannot be null.");

        // Defensive check: verify vehicle is not already parked in the facility
        if (findVehicleSlot(vehicle.getRegNo()) != null) {
            throw new IllegalStateException("Vehicle " + vehicle.getRegNo() + " is already parked in the facility.");
        }

        for (int f = 0; f < slots.length; f++) {
            for (int s = 0; s < slots[f].length; s++) {
                if (slots[f][s] == null) {
                    slots[f][s] = vehicle;
                    totalOccupancy++;
                    return new SlotLocation(f, s);
                }
            }
        }
        return null; // Facility full
    }

    /**
     * Releases a slot at the specified coordinates.
     *
     * @param floor Zero-indexed floor
     * @param slot  Zero-indexed slot on floor
     * @return true if slot was occupied and successfully freed; false if coordinates invalid or already free
     */
    public boolean release(int floor, int slot) {
        if (!isValidCoordinate(floor, slot)) {
            return false;
        }
        if (slots[floor][slot] == null) {
            return false;
        }
        slots[floor][slot] = null;
        totalOccupancy--;
        return true;
    }

    /**
     * Overloaded convenience method to release using SlotLocation object.
     */
    public boolean release(SlotLocation location) {
        if (location == null) return false;
        return release(location.getFloor(), location.getSlot());
    }

    /**
     * Searches for a vehicle by registration number across all floors.
     *
     * @param regNo Vehicle registration number
     * @return SlotLocation if found, or null otherwise
     */
    public SlotLocation findVehicleSlot(String regNo) {
        if (regNo == null) return null;
        String normalized = regNo.trim().toUpperCase();
        for (int f = 0; f < slots.length; f++) {
            for (int s = 0; s < slots[f].length; s++) {
                if (slots[f][s] != null && slots[f][s].getRegNo().equalsIgnoreCase(normalized)) {
                    return new SlotLocation(f, s);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves the vehicle stationed at a particular coordinate without removing it.
     */
    public Vehicle getVehicleAt(int floor, int slot) {
        if (!isValidCoordinate(floor, slot)) {
            return null;
        }
        return slots[floor][slot];
    }

    /**
     * Validates whether floor and slot coordinates fall within jagged boundaries.
     */
    public boolean isValidCoordinate(int floor, int slot) {
        if (floor < 0 || floor >= slots.length) {
            return false;
        }
        return slot >= 0 && slot < slots[floor].length;
    }

    private void validateFloorIndex(int floor) {
        if (floor < 0 || floor >= slots.length) {
            throw new IndexOutOfBoundsException("Invalid floor index: " + floor + " (Total floors: " + slots.length + ")");
        }
    }

    /**
     * Formats an ASCII floor-by-floor occupancy visualization.
     */
    public String renderOccupancyMap() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("=== Facility Occupancy (Total: %d/%d) ===%n", (totalCapacity - getAvailableSlotCount()), totalCapacity));
        for (int f = 0; f < slots.length; f++) {
            sb.append(String.format("Floor %d [%2d slots]: ", f, slots[f].length));
            for (int s = 0; s < slots[f].length; s++) {
                sb.append(slots[f][s] == null ? "[ Free ] " : String.format("[%s] ", slots[f][s].getRegNo()));
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
