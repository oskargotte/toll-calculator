import java.time.LocalTime;

public class TollFeeTimeInterval {
    private LocalTime intervalStart;
    private LocalTime intervalEnd;
    private int intervalTollFee;

    public TollFeeTimeInterval(LocalTime intervalStart, LocalTime intervalEnd, int intervalTollFee) {
        this.intervalStart = intervalStart;
        this.intervalEnd = intervalEnd;
        this.intervalTollFee = intervalTollFee;
    }

    public LocalTime getIntervalStart() {
        return intervalStart;
    }

    public LocalTime getIntervalEnd() {
        return intervalEnd;
    }

    public int getIntervalTollFee() {
        return intervalTollFee;
    }
}
