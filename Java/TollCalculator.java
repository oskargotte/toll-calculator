
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
    private CalendarPolicy calendarPolicy;

    public TollCalculator(TollFeeTimeIntervalPolicy timeIntervalPolicy, CalendarPolicy calendarPolicy) {
        this.timeIntervalPolicy = timeIntervalPolicy;
        this.calendarPolicy = calendarPolicy;
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
    if(this.calendarPolicy.isTollFreeDay(date.toLocalDate()) || vehicle.isTollFree()) return 0;
    return this.timeIntervalPolicy.getTollFee(date.toLocalTime());
  }
}

