
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class TollCalculator {

    private static final int NUM_MINUTES_OF_FREE_PASSES = 60;
    private TollFeeTimeIntervalPolicy timeIntervalPolicy;

    public TollCalculator(TollFeeTimeIntervalPolicy timeIntervalPolicy) {
        this.timeIntervalPolicy = timeIntervalPolicy;
    }

    /**
   * Calculate the total toll fee for one day
   *
   * @param vehicle - the vehicle
   * @param dates   - date and time of all passes on one day
   * @return - the total toll fee for that day
   */
  public int getTollFee(Vehicle vehicle, LocalDateTime... dates) {
    List<List<LocalDateTime>> datesByChargingIntervals = getDatesByChargingIntervals(dates);
    int totalFee = 0;

    for (List<LocalDateTime> chargingInterval : datesByChargingIntervals) {
      int intervalFee = getMaxFee(chargingInterval, vehicle);
      totalFee += intervalFee;
    }

    return Integer.min(totalFee, this.timeIntervalPolicy.getDailyMaxFee());
  }


  private List<List<LocalDateTime>> getDatesByChargingIntervals(LocalDateTime[] dates) {
    List<List<LocalDateTime>> result = new ArrayList<>();
    result.add(new ArrayList<>());

    List<LocalDateTime> sortedDates = Arrays.stream(dates).sorted().collect(toList());
    LocalDateTime firstDateOfInterval = sortedDates.get(0);

    for (LocalDateTime date : sortedDates) {
      if(date.isBefore(firstDateOfInterval.plusMinutes(NUM_MINUTES_OF_FREE_PASSES))) {
        result.get(result.size() - 1).add(date);
      } else {
        firstDateOfInterval = date;
        result.add(new ArrayList<>(Arrays.asList(date)));
      }
    }

    return result;
  }


  private int getMaxFee(List<LocalDateTime> dates, Vehicle vehicle) {
    return dates.stream()
            .map(date -> getTollFee(date, vehicle))
            .reduce(Integer::max)
            .get();
  }


  public int getTollFee(final LocalDateTime date, Vehicle vehicle) {
    if(isTollFreeDate(date) || vehicle.isTollFree()) return 0;
    return this.timeIntervalPolicy.getTollFee(date.toLocalTime());
  }

  private Boolean isTollFreeDate(LocalDateTime date) {
    int year = date.getYear();
    int month = date.getMonthValue();
    int day = date.getDayOfMonth();

    DayOfWeek dayOfWeek = date.getDayOfWeek();
    if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) return true;

    if (year == 2013) {
      if (month == Calendar.JANUARY && day == 1 ||
          month == Calendar.MARCH && (day == 28 || day == 29) ||
          month == Calendar.APRIL && (day == 1 || day == 30) ||
          month == Calendar.MAY && (day == 1 || day == 8 || day == 9) ||
          month == Calendar.JUNE && (day == 5 || day == 6 || day == 21) ||
          month == Calendar.JULY ||
          month == Calendar.NOVEMBER && day == 1 ||
          month == Calendar.DECEMBER && (day == 24 || day == 25 || day == 26 || day == 31)) {
        return true;
      }
    }
    return false;
  }
}

