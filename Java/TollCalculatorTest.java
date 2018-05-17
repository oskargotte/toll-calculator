import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.time.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TollCalculatorTest {

    TollCalculator tollCalculator = new TollCalculator(new TollFeeTimeIntervalPolicyEvolve(), CalendarPolicyEvolve.getInstance());
    TollCalculator tollCalculatorHighFeeAllDay;

    private static final int HIGH_TOLL_FEE = TollFeeTimeIntervalPolicyEvolve.HIGH_TOLL_FEE_EVOLVE;
    private static final int MEDIUM_TOLL_FEE = TollFeeTimeIntervalPolicyEvolve.MEDIUM_TOLL_FEE_EVOLVE;
    private static final int LOW_TOLL_FEE = TollFeeTimeIntervalPolicyEvolve.LOW_TOLL_FEE_EVOLVE;

    private static final int NO_FEE = TollFeeTimeIntervalPolicyBase.NO_TOLL_FEE;
    private static final int MAX_FEE = 100;

    private static final LocalDate WEEKDAY_NON_HOLIDAY = LocalDate.of(2018, 5, 2);
    private static final LocalDate WEEKEND_DAY = LocalDate.of(2018, 5, 5);
    private static final LocalDate WEEKDAY_HOLIDAY = LocalDate.of(2018, 5, 1);
    private static final LocalDate GOOD_FRIDAY_2013 = LocalDate.of(2013, 3, 29);
    private static final Vehicle NORMAL_FEE_VEHICLE = new Car();

    @BeforeEach
    void beforeEach() {
        TollFeeTimeIntervalPolicy highFeeAllDayPolicy = Mockito.mock(TollFeeTimeIntervalPolicyBase.class);
        Mockito.when(highFeeAllDayPolicy.getTollFee(Mockito.any())).thenReturn(HIGH_TOLL_FEE);
        Mockito.when(highFeeAllDayPolicy.getDailyMaxFee()).thenReturn(MAX_FEE);

        tollCalculatorHighFeeAllDay = new TollCalculator(highFeeAllDayPolicy, CalendarPolicyEvolve.getInstance());
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
        LocalDateTime[] entryTimesMoreThanAnHourApart = {
                toDate(WEEKDAY_NON_HOLIDAY, LocalTime.parse("15:30")),
                toDate(WEEKDAY_NON_HOLIDAY, LocalTime.parse("16:31")),
        };

        int fee = tollCalculatorHighFeeAllDay.getTollFee(NORMAL_FEE_VEHICLE, entryTimesMoreThanAnHourApart);

        assertEquals(HIGH_TOLL_FEE * 2, fee);
    }

    @Test
    void AVehicleShouldOnlyBeChargeOnceAnHour() {
        LocalDateTime[] entryTimesWithinAnHour = {
                toDate(WEEKDAY_NON_HOLIDAY, LocalTime.parse("06:45")), // Start of first chargeable period
                toDate(WEEKDAY_NON_HOLIDAY, LocalTime.parse("07:45")), // No fee since passing is within an hour
                toDate(WEEKDAY_NON_HOLIDAY, LocalTime.parse("07:46")), // Start of second chargeable period
                toDate(WEEKDAY_NON_HOLIDAY, LocalTime.parse("07:47")), // No fee since passing is within an hour
        };
        int fee = tollCalculatorHighFeeAllDay.getTollFee(NORMAL_FEE_VEHICLE, entryTimesWithinAnHour);

        assertEquals(HIGH_TOLL_FEE * 2, fee);
    }

    @Test
    void AVehicleShouldOnlyBeChargeOnceAnHourWhenPassingAtDifferentDays() {
        LocalDateTime[] entryTimesWithinAnHour = {
                toDate(WEEKDAY_NON_HOLIDAY, LocalTime.parse("23:45")),
                toDate(WEEKDAY_NON_HOLIDAY.plusDays(1), LocalTime.parse("00:15")),
        };
        int fee = tollCalculatorHighFeeAllDay.getTollFee(NORMAL_FEE_VEHICLE, entryTimesWithinAnHour);

        assertEquals(HIGH_TOLL_FEE, fee);
    }

    @Test
    void ShouldBePossibleToReportDateUnordered() {
        LocalDateTime[] entryTimesWithinAnHour = {
                toDate(WEEKDAY_NON_HOLIDAY, LocalTime.parse("07:45")),
                toDate(WEEKDAY_NON_HOLIDAY, LocalTime.parse("07:00")),
        };
        int fee = tollCalculatorHighFeeAllDay.getTollFee(NORMAL_FEE_VEHICLE, entryTimesWithinAnHour);

        assertEquals(HIGH_TOLL_FEE, fee);
    }

    @Test
    void ShouldBeHighestFeeWhenMultiplePassesPerHour() {
        /* TODO: Is this really a requirement?? */
        LocalDateTime[] entryTimesWithDifferentFeesWithinAnHour = {
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
            LocalDateTime entryTimeOnNormalFeeDay = toDate(WEEKDAY_NON_HOLIDAY, entryTime);

            int fee = tollCalculator.getTollFee(entryTimeOnNormalFeeDay, NORMAL_FEE_VEHICLE);

            assertEquals(expectedFee, fee, String.format("Fee should be %s kr at %s", expectedFee, entryTime));
        }
    }

    @ParameterizedTest
    @EnumSource(
            value = Vehicle.VehicleType.class,
            names = {"MOTORBIKE", "DIPLOMAT", "EMERGENCY", "FOREIGN", "MILITARY", "TRACTOR"})
    void ShouldBeNoFeeForSpecialVehicles(Vehicle.VehicleType vehicleType) {
        LocalDateTime entryTime = toDate(WEEKDAY_NON_HOLIDAY, LocalTime.parse("07:00"));

        Vehicle tollFreeVehicleMock = new VehicleFactoryEvolve().createVehicle(vehicleType);

        int fee = tollCalculator.getTollFee(entryTime, tollFreeVehicleMock);

        assertEquals(NO_FEE, fee, String.format("%s should be toll free", tollFreeVehicleMock.getType()));
    }

    @Test
    void ShouldBeAMaxFeePerDay() {
        int maxFee = 50;
        int tollFee = 30;
        TollFeeTimeIntervalPolicy tollFeePolicyMock = Mockito.mock(TollFeeTimeIntervalPolicyBase.class);
        Mockito.when(tollFeePolicyMock.getTollFee(Mockito.any())).thenReturn(tollFee);
        Mockito.when(tollFeePolicyMock.getDailyMaxFee()).thenReturn(maxFee);
        TollCalculator tollCalculatorWithMockedPolicy = new TollCalculator(tollFeePolicyMock, CalendarPolicyEvolve.getInstance());

        LocalDateTime[] entryTimes = {
                toDate(WEEKDAY_NON_HOLIDAY, LocalTime.parse("06:00")), // tollFee = 30 kr
                toDate(WEEKDAY_NON_HOLIDAY, LocalTime.parse("08:00")), // tollFee = 30 kr
        };

        int fee = tollCalculatorWithMockedPolicy.getTollFee(NORMAL_FEE_VEHICLE, entryTimes); // 60 kr without max limit

        assertEquals(maxFee, fee);
    }

    @Test
    void WeekendsShouldBeTollFree() {
        LocalDateTime entryTime = toDate(WEEKEND_DAY, LocalTime.parse("07:00"));

        int fee = tollCalculator.getTollFee(entryTime, NORMAL_FEE_VEHICLE);

        assertEquals(NO_FEE, fee);
    }

    @Test
    void WeekdayHolidayShouldBeTollFree() {
        LocalDateTime entryTime = toDate(WEEKDAY_HOLIDAY, LocalTime.parse("07:00"));

        int fee = tollCalculator.getTollFee(entryTime, NORMAL_FEE_VEHICLE);

        assertEquals(NO_FEE, fee);
    }

    @Test
    void GoodFridayShouldBeTollFree() {
        LocalDateTime entryTime = LocalDateTime.of(GOOD_FRIDAY_2013, LocalTime.parse("07:00"));

        int fee = tollCalculator.getTollFee(entryTime, NORMAL_FEE_VEHICLE);

        assertEquals(NO_FEE, fee);
    }

    LocalDateTime toDate(LocalDate day, LocalTime timeOfDay) {
        return day.atTime(timeOfDay);
    }
}