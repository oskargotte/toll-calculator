import java.time.LocalDate;

public interface CalendarPolicy {
    boolean isTollFreeDay(LocalDate day);
}
