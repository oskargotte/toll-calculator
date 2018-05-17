import java.time.LocalTime;

public class TollFeeTimeIntervalPolicyEvolve extends TollFeeTimeIntervalPolicyBase {
    public static final int HIGH_TOLL_FEE_EVOLVE = 18;
    public static final int MEDIUM_TOLL_FEE_EVOLVE = 13;
    public static final int LOW_TOLL_FEE_EVOLVE = 8;

    private static final int DAILY_MAX_FEE = 60;

    public TollFeeTimeIntervalPolicyEvolve() {
        this.addTollFeeTimeInterval(LocalTime.parse("06:00"), LocalTime.parse("06:30"), LOW_TOLL_FEE_EVOLVE);
        this.addTollFeeTimeInterval(LocalTime.parse("06:30"), LocalTime.parse("07:00"), MEDIUM_TOLL_FEE_EVOLVE);
        this.addTollFeeTimeInterval(LocalTime.parse("07:00"), LocalTime.parse("08:00"), HIGH_TOLL_FEE_EVOLVE);
        this.addTollFeeTimeInterval(LocalTime.parse("08:00"), LocalTime.parse("08:30"), MEDIUM_TOLL_FEE_EVOLVE);
        this.addTollFeeTimeInterval(LocalTime.parse("08:30"), LocalTime.parse("15:00"), LOW_TOLL_FEE_EVOLVE);
        this.addTollFeeTimeInterval(LocalTime.parse("15:00"), LocalTime.parse("15:30"), MEDIUM_TOLL_FEE_EVOLVE);
        this.addTollFeeTimeInterval(LocalTime.parse("15:30"), LocalTime.parse("17:00"), HIGH_TOLL_FEE_EVOLVE);
        this.addTollFeeTimeInterval(LocalTime.parse("17:00"), LocalTime.parse("18:00"), MEDIUM_TOLL_FEE_EVOLVE);
        this.addTollFeeTimeInterval(LocalTime.parse("18:00"), LocalTime.parse("18:30"), LOW_TOLL_FEE_EVOLVE);
    }

    @Override
    public int getDailyMaxFee() {
        return DAILY_MAX_FEE;
    }
}
