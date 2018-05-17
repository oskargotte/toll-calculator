import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.*;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TollCalculatorTest {

    TollCalculator tollCalculator = new TollCalculator(new TollFeeTimeIntervalPolicyEvolve());
    TollCalculator tollCalculatorHighFeeAllDay;

    private static final int HIGH_TOLL_FEE = TollFeeTimeIntervalPolicyEvolve.HIGH_TOLL_FEE_EVOLVE;
    private static final int MEDIUM_TOLL_FEE = TollFeeTimeIntervalPolicyEvolve.MEDIUM_TOLL_FEE_EVOLVE;
    private static final int LOW_TOLL_FEE = TollFeeTimeIntervalPolicyEvolve.LOW_TOLL_FEE_EVOLVE;

    private static final int NO_FEE = TollFeeTimeIntervalPolicyBase.NO_TOLL_FEE;

    private static final LocalDate WEEKDAY_NON_HOLIDAY = LocalDate.of(2018, 5, 2);
    private static final Vehicle NORMAL_FEE_VEHICLE = new Car();

    @BeforeEach
    void beforeEach() {
        TollFeeTimeIntervalPolicy highFeeAllDayPolicy = Mockito.mock(TollFeeTimeIntervalPolicyBase.class);
        Mockito.when(highFeeAllDayPolicy.getTollFee(Mockito.any())).thenReturn(HIGH_TOLL_FEE);

        tollCalculatorHighFeeAllDay = new TollCalculator(highFeeAllDayPolicy);
    }

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


    @Test
    void AVehicleShouldBeChargedForEachEntryIfMoreThanAnHourApart() {
        Date[] entryTimesMoreThanAnHourApart = {
                toDate(WEEKDAY_NON_HOLIDAY, LocalTime.parse("15:30")),
                toDate(WEEKDAY_NON_HOLIDAY, LocalTime.parse("16:31")),
        };

        int fee = tollCalculatorHighFeeAllDay.getTollFee(NORMAL_FEE_VEHICLE, entryTimesMoreThanAnHourApart);

        assertEquals(HIGH_TOLL_FEE * 2, fee);
    }

    @Test
    void AVehicleShouldOnlyBeChargeOnceAnHour() {
        Date[] entryTimesWithinAnHour = {
                toDate(WEEKDAY_NON_HOLIDAY, LocalTime.parse("06:45")),
                toDate(WEEKDAY_NON_HOLIDAY, LocalTime.parse("07:45")),
                toDate(WEEKDAY_NON_HOLIDAY, LocalTime.parse("15:30")),
                toDate(WEEKDAY_NON_HOLIDAY, LocalTime.parse("16:30")),
        };
        int fee = tollCalculatorHighFeeAllDay.getTollFee(NORMAL_FEE_VEHICLE, entryTimesWithinAnHour);

        assertEquals(HIGH_TOLL_FEE * 2, fee);
    }

    @Test
    void AVehicleShouldOnlyBeChargeOnceAnHourWhenPassingAtDifferentDays() {
        Date[] entryTimesWithinAnHour = {
                toDate(WEEKDAY_NON_HOLIDAY, LocalTime.parse("23:45")),
                toDate(WEEKDAY_NON_HOLIDAY.plusDays(1), LocalTime.parse("00:15")),
        };
        int fee = tollCalculatorHighFeeAllDay.getTollFee(NORMAL_FEE_VEHICLE, entryTimesWithinAnHour);

        assertEquals(HIGH_TOLL_FEE, fee);
    }

    @Test
    void ShouldBeHighestFeeWhenMultiplePassesPerHour() {
        /* TODO: Is this really a requirement?? */
        Date[] entryTimesWithDifferentFeesWithinAnHour = {
                toDate(WEEKDAY_NON_HOLIDAY, LocalTime.parse("15:15")), // 13 kr
                toDate(WEEKDAY_NON_HOLIDAY, LocalTime.parse("15:45")), // 18 kr
        };
        int fee = tollCalculator.getTollFee(NORMAL_FEE_VEHICLE, entryTimesWithDifferentFeesWithinAnHour);

        assertEquals(HIGH_TOLL_FEE, fee);
    }

    void AssertSameFeeForTimeInterval(int expectedFee, LocalTime intervalStart, LocalTime intervalEnd) {
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