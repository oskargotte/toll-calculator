import java.time.LocalTime;

public interface TollFeeTimeIntervalPolicy {
    public int getTollFee(LocalTime time);
}
