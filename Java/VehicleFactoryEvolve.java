public class VehicleFactoryEvolve implements VehicleFactory{
    @Override
    public Vehicle createVehicle(Vehicle.VehicleType type) {
        switch (type) {
            case CAR: return new Car();
            case MOTORBIKE:
            case FOREIGN:
            case TRACTOR:
            case DIPLOMAT:
            case MILITARY:
            case EMERGENCY: return new TollFreeVehicle(type);
            default: return new Car();
        }
    }
}
