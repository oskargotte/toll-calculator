import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;

public class CalendarPolicyEvolve implements CalendarPolicy {

  private static CalendarPolicyEvolve singletonInstance;

  private static final int RECURRENT_HOLIDAYS_DEFAULT_LEAP_YEAR = 2000;
  private ArrayList<LocalDate> recurrentHolidays = new ArrayList<>(Arrays.asList(
          LocalDate.of(RECURRENT_HOLIDAYS_DEFAULT_LEAP_YEAR, Month.JANUARY, 1),
          LocalDate.of(RECURRENT_HOLIDAYS_DEFAULT_LEAP_YEAR, Month.APRIL, 30),
          LocalDate.of(RECURRENT_HOLIDAYS_DEFAULT_LEAP_YEAR, Month.MAY, 1),
          LocalDate.of(RECURRENT_HOLIDAYS_DEFAULT_LEAP_YEAR, Month.JUNE, 6),
          LocalDate.of(RECURRENT_HOLIDAYS_DEFAULT_LEAP_YEAR, Month.DECEMBER, 24),
          LocalDate.of(RECURRENT_HOLIDAYS_DEFAULT_LEAP_YEAR, Month.DECEMBER, 25),
          LocalDate.of(RECURRENT_HOLIDAYS_DEFAULT_LEAP_YEAR, Month.DECEMBER, 26),
          LocalDate.of(RECURRENT_HOLIDAYS_DEFAULT_LEAP_YEAR, Month.DECEMBER, 31)
  ));

  private ArrayList<LocalDate> otherHolidays = new ArrayList<>(Arrays.asList(
          // Add holidays specific to 2013
          LocalDate.of(2013, Month.MARCH, 28),
          LocalDate.of(2013, Month.MARCH, 29),
          LocalDate.of(2013, Month.APRIL, 1),
          LocalDate.of(2013, Month.MAY, 8),
          LocalDate.of(2013, Month.MAY, 9),
          LocalDate.of(2013, Month.JUNE, 5),
          LocalDate.of(2013, Month.JUNE, 6),
          LocalDate.of(2013, Month.JUNE, 21),
          LocalDate.of(2013, Month.NOVEMBER, 1)
          // TODO import/add holidays for other relevant years
  ));

  private CalendarPolicyEvolve() {
  }

  public static CalendarPolicy getInstance() {
    if (singletonInstance == null) {
      singletonInstance = new CalendarPolicyEvolve();
    }
    return singletonInstance;
  }

  @Override
  public boolean isTollFreeDay(LocalDate day) {
    return isWeekend(day) || isJuly(day) || isHoliday(day);
  }

  private boolean isWeekend(LocalDate day) {
    DayOfWeek dayOfWeek = day.getDayOfWeek();
    return (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY);
  }

  private boolean isJuly(LocalDate day) {
    return day.getMonth() == Month.JULY;
  }

  private boolean isHoliday(LocalDate day) {
    return isRecurrentHoliday(day) || this.otherHolidays.contains(day);
  }

  private boolean isRecurrentHoliday(LocalDate day) {
    return this.recurrentHolidays.contains(day.withYear(RECURRENT_HOLIDAYS_DEFAULT_LEAP_YEAR));
  }
}
