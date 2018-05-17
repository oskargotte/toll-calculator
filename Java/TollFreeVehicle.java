public class TollFreeVehicle implements Vehicle {
    private final Vehicle.VehicleType type;

    public TollFreeVehicle(Vehicle.VehicleType type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return type.toString();
    }

    @Override
    public boolean isTollFree() {
        return true;
    }
}