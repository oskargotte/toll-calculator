import java.time.LocalTime;
import java.util.Vector;

public abstract class TollFeeTimeIntervalPolicyBase implements TollFeeTimeIntervalPolicy {
    private Vector<TollFeeTimeInterval> intervals = new Vector<>();

    public static final int NO_TOLL_FEE = 0;

    public void addTollFeeTimeInterval(LocalTime intervalStart, LocalTime intervalEnd, int intervalTollFee) {
        this.intervals.add(new TollFeeTimeInterval(intervalStart, intervalEnd, intervalTollFee));
    }

    @Override
    public int getTollFee(LocalTime time) {
        // Find matching interval for the given time
        for(TollFeeTimeInterval interval : intervals) {
            if (!time.isBefore(interval.getIntervalStart()) && time.isBefore(interval.getIntervalEnd())) {
                return interval.getIntervalTollFee();
            }
        }

        // If no matching interval is found, return no toll fee by default
        return NO_TOLL_FEE;
    }
}