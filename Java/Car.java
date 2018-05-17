
public class Car implements Vehicle {
  @Override
  public String getType() {
    return VehicleType.CAR.toString();
  }

  @Override
  public boolean isTollFree() {
    return false;
  }
}
