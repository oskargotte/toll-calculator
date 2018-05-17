public interface Vehicle {
  public enum VehicleType {
    CAR,
    MOTORBIKE,
    TRACTOR,
    EMERGENCY,
    DIPLOMAT,
    FOREIGN,
    MILITARY,
  }

  public String getType();

  public boolean isTollFree();
}
