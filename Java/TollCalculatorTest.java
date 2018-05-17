import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TollCalculatorTest {

    private static final int HIGH_TOLL_FEE = 18;
    private static final int MEDIUM_TOLL_FEE = 13;
    private static final int LOW_TOLL_FEE = 8;

    private static final int NO_FEE = 0;

    private static final LocalDate WEEKDAY_NON_HOLIDAY = LocalDate.of(2018, 5, 2);
    private static final Vehicle NORMAL_FEE_VEHICLE = new Car();

    @Test
    void ShouldBeHighFeeOnRushHours() {
        LocalTime morningRushHourStart   = LocalTime.parse("07:00");
        LocalTime morningRushHourEnd     = LocalTime.parse("08:00");
        LocalTime afternoonRushHourStart = LocalTime.parse("15:30");
        LocalTime afternoonRushHourEnd   = LocalTime.parse("17:00");

        AssertSameFeeForTimeInterval(HIGH_TOLL_FEE, morningRushHourStart, morningRushHourEnd);
        AssertSameFeeForTimeInterval(HIGH_TOLL_FEE, afternoonRushHourStart, afternoonRushHourEnd);
    }

    @Test
    void ShouldBeMidFeeCloseToRushHours() {
        AssertSameFeeForTimeInterval(MEDIUM_TOLL_FEE, LocalTime.parse("06:30"), LocalTime.parse("07:00"));
        AssertSameFeeForTimeInterval(MEDIUM_TOLL_FEE, LocalTime.parse("08:00"), LocalTime.parse("08:30"));
        AssertSameFeeForTimeInterval(MEDIUM_TOLL_FEE, LocalTime.parse("15:00"), LocalTime.parse("15:30"));
        AssertSameFeeForTimeInterval(MEDIUM_TOLL_FEE, LocalTime.parse("17:00"), LocalTime.parse("18:00"));
    }

    @Test
    void ShouldBeLowFeeOnLowTrafficHours() {
        AssertSameFeeForTimeInterval(LOW_TOLL_FEE, LocalTime.parse("06:00"), LocalTime.parse("06:30"));
        AssertSameFeeForTimeInterval(LOW_TOLL_FEE, LocalTime.parse("08:30"), LocalTime.parse("15:00"));
        AssertSameFeeForTimeInterval(LOW_TOLL_FEE, LocalTime.parse("18:00"), LocalTime.parse("18:30"));
    }

    @Test
    void ShouldBeNoFeeOnEveningAndNight() {
        AssertSameFeeForTimeInterval(NO_FEE, LocalTime.parse("18:30"), LocalTime.parse("00:00"));
        AssertSameFeeForTimeInterval(NO_FEE, LocalTime.parse("00:00"), LocalTime.parse("06:00"));
    }

    void AssertSameFeeForTimeInterval(int expectedFee, LocalTime intervalStart, LocalTime intervalEnd) {
        TollCalculator tollCalculator = new TollCalculator();

        for(LocalTime entryTime = intervalStart;
            !entryTime.equals(intervalEnd);
            entryTime = entryTime.plusMinutes(1)){
            Date entryTimeOnNormalFeeDay = toDate(WEEKDAY_NON_HOLIDAY, entryTime);

            int fee = tollCalculator.getTollFee(entryTimeOnNormalFeeDay, NORMAL_FEE_VEHICLE);

            assertEquals(expectedFee, fee, String.format("Fee should be %s kr at %s", expectedFee, entryTime));
        }
    }

    Date toDate(LocalDate day, LocalTime timeOfDay) {
        return Date.from(timeOfDay.atDate(day)
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }
}