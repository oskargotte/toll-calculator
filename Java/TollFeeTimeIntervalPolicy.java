import java.time.LocalTime;

public interface TollFeeTimeIntervalPolicy {
  int getTollFee(LocalTime time);
  int getDailyMaxFee();
}
