package parking.system.facility;

import java.util.Objects;

/**
 * Immutable value object representing a physical slot location within the multi-level facility.
 */
public final class SlotLocation {

    private final int floor;
    private final int slot;

    /**
     * Constructs a SlotLocation coordinate.
     *
     * @param floor Zero-indexed floor number
     * @param slot  Zero-indexed slot index on that floor
     */
    public SlotLocation(int floor, int slot) {
        if (floor < 0 || slot < 0) {
            throw new IllegalArgumentException("Floor and slot indices must be non-negative.");
        }
        this.floor = floor;
        this.slot = slot;
    }

    public int getFloor() {
        return floor;
    }

    public int getSlot() {
        return slot;
    }

    /**
     * Converts to an int array {floor, slot} matching the format expected by Task 3.
     */
    public int[] toArray() {
        return new int[]{floor, slot};
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SlotLocation other)) return false;
        return floor == other.floor && slot == other.slot;
    }

    @Override
    public int hashCode() {
        return Objects.hash(floor, slot);
    }

    @Override
    public String toString() {
        return "[" + floor + ", " + slot + "]";
    }
}
