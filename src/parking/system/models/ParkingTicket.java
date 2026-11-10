package parking.system.models;

import parking.system.facility.SlotLocation;
import parking.system.users.ParkingUser;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Represents an issued parking ticket in the Campus Smart-Parking System.
 * Demonstrates static sequence numbering, overloaded/copy constructors,
 * state encapsulation, and polymorphic fee reconciliation.
 */
public class ParkingTicket {

    // Static counter across all ticket instances to ensure globally unique numbering
    private static int ticketCounter = 1000;

    private final String ticketId;
    private final Vehicle vehicle;
    private final ParkingUser user;
    private final SlotLocation slotLocation;
    private final LocalTime entryTime;

    private LocalTime exitTime;
    private int durationMinutes;
    private int energyConsumedKwh;
    private double parkingFee;
    private double chargingFee;
    private double totalFee;
    private boolean isClosed;

    /**
     * Parameterized primary constructor. Automatically generates ticket ID from static counter.
     *
     * @param vehicle      Parked vehicle reference
     * @param user         Patron reference
     * @param slotLocation Allocated floor and slot coordinates
     * @param entryTime    Time of vehicle arrival
     */
    public ParkingTicket(Vehicle vehicle, ParkingUser user, SlotLocation slotLocation, LocalTime entryTime) {
        Objects.requireNonNull(vehicle, "Vehicle cannot be null.");
        Objects.requireNonNull(user, "User cannot be null.");
        Objects.requireNonNull(slotLocation, "Slot location cannot be null.");
        Objects.requireNonNull(entryTime, "Entry time cannot be null.");

        this.ticketId = "TICK-" + (++ticketCounter);
        this.vehicle = vehicle;
        this.user = user;
        this.slotLocation = slotLocation;
        this.entryTime = entryTime;
        this.isClosed = false;
    }

    /**
     * Overloaded constructor defaulting entry time to current system time.
     */
    public ParkingTicket(Vehicle vehicle, ParkingUser user, SlotLocation slotLocation) {
        this(vehicle, user, slotLocation, LocalTime.now());
    }

    /**
     * Copy constructor for re-issuing duplicate tickets (Task 4 requirement).
     * Creates a deep-enough snapshot for administrative reprinting while preserving identity.
     *
     * @param source Original ticket to replicate
     */
    public ParkingTicket(ParkingTicket source) {
        Objects.requireNonNull(source, "Source ticket to copy cannot be null.");
        this.ticketId = source.ticketId + "-DUP";
        this.vehicle = source.vehicle;
        this.user = source.user;
        this.slotLocation = source.slotLocation;
        this.entryTime = source.entryTime;
        this.exitTime = source.exitTime;
        this.durationMinutes = source.durationMinutes;
        this.energyConsumedKwh = source.energyConsumedKwh;
        this.parkingFee = source.parkingFee;
        this.chargingFee = source.chargingFee;
        this.totalFee = source.totalFee;
        this.isClosed = source.isClosed;
    }

    /**
     * Static accessor for total tickets issued.
     */
    public static int getIssuedTicketCount() {
        return ticketCounter - 1000;
    }

    /**
     * Closes the ticket upon vehicle departure, calculating durations, base vehicle fees,
     * user exemptions/passes, and optional EV charging consumption.
     *
     * @param exitTime Departure time
     * @param kWh      Energy consumed if plugged into EV charging (0 if not used)
     */
    public void closeTicket(LocalTime exitTime, int kWh) {
        if (isClosed) {
            throw new IllegalStateException("Ticket " + ticketId + " has already been settled and closed.");
        }
        Objects.requireNonNull(exitTime, "Exit time cannot be null.");
        if (exitTime.isBefore(entryTime)) {
            throw new IllegalArgumentException("Exit time " + exitTime + " cannot be before entry time " + entryTime);
        }
        if (kWh < 0) {
            throw new IllegalArgumentException("Energy consumed cannot be negative: " + kWh);
        }

        this.exitTime = exitTime;
        this.durationMinutes = (int) Duration.between(entryTime, exitTime).toMinutes();
        this.energyConsumedKwh = kWh;

        // Dynamic dispatch / polymorphic fee calculation
        double rawBaseFee;
        if (vehicle instanceof DeliveryVan van) {
            rawBaseFee = van.calculateFee(durationMinutes, entryTime);
        } else {
            rawBaseFee = vehicle.calculateFee(durationMinutes);
        }

        // Apply institutional pass discount (Student/Staff Monthly Pass or Visitor One-Day Pass)
        this.parkingFee = user.applyParkingDiscount(rawBaseFee);

        // Charging fee calculation via Chargeable interface
        if (vehicle instanceof Chargeable chargeable && kWh > 0) {
            this.chargingFee = chargeable.chargingFee(kWh);
        } else {
            this.chargingFee = 0.0;
        }

        this.totalFee = this.parkingFee + this.chargingFee;
        this.isClosed = true;
    }

    /**
     * Overloaded closeTicket for vehicles without EV charging.
     */
    public void closeTicket(LocalTime exitTime) {
        closeTicket(exitTime, 0);
    }

    public String getTicketId() {
        return ticketId;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public ParkingUser getUser() {
        return user;
    }

    public SlotLocation getSlotLocation() {
        return slotLocation;
    }

    public LocalTime getEntryTime() {
        return entryTime;
    }

    public LocalTime getExitTime() {
        return exitTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public int getEnergyConsumedKwh() {
        return energyConsumedKwh;
    }

    public double getParkingFee() {
        return parkingFee;
    }

    public double getChargingFee() {
        return chargingFee;
    }

    public double getTotalFee() {
        return totalFee;
    }

    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParkingTicket other)) return false;
        return Objects.equals(ticketId, other.ticketId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticketId);
    }

    @Override
    public String toString() {
        return String.format(
                "Ticket #%s | Vehicle: %s (%s) | User: %s (%s) | Slot: %s | Duration: %d min | Fee: Rs %.2f (Parking: Rs %.2f, Charging: Rs %.2f) | Status: %s",
                ticketId, vehicle.getRegNo(), vehicle.getClass().getSimpleName(),
                user.getName(), user.getCategory(), slotLocation,
                durationMinutes, totalFee, parkingFee, chargingFee,
                (isClosed ? "SETTLED" : "ACTIVE")
        );
    }
}
