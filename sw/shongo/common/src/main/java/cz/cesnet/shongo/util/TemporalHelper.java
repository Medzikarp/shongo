package cz.cesnet.shongo.util;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;

/**
 * Helper for manipulating/formatting temporal data types.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
public class TemporalHelper
{
    /**
     * {@link PeriodFormatter} for {@link Period}s.
     */
    private static final PeriodFormatter periodFormatter = PeriodFormat.getDefault();

    /**
     * {@link DateTimeFormatter} for {@link DateTime}s.
     */
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

    /**
     * @param dateTime to be formatted
     * @return formatted given {@code dateTime} to {@link String}
     */
    public static String formatDateTime(DateTime dateTime)
    {
        return dateTimeFormatter.print(dateTime);
    }

    /**
     * @param period to be formatted
     * @return formatted given {@code period} to {@link String}
     */
    public static String formatPeriod(Period period)
    {
        return periodFormatter.print(period);
    }

    /**
     * @param interval to be formatted
     * @return formatted given {@code interval} to string
     */
    public static String formatInterval(Interval interval)
    {
        StringBuilder builder = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
        builder.append(formatter.print(interval.getStart()));
        builder.append(", ");
        builder.append(interval.toPeriod().normalizedStandard().toString());
        return builder.toString();
    }

    /**
     * @param period
     * @param longerThanPeriod
     * @return true if {@code period} is longer than {@code longerThanPeriod},
     *         false otherwise
     */
    public static boolean isPeriodLongerThan(Period period, Period longerThanPeriod)
    {
        if (longerThanPeriod == null) {
            return false;
        }
        return convertPeriodToStandardDuration(period).isLongerThan(convertPeriodToStandardDuration(longerThanPeriod));
    }

    /**
     * @param period to be converted
     * @return given {@code period} converted to standard {@link Duration}
     */
    private static Duration convertPeriodToStandardDuration(Period period)
    {
        if (period.getYears() > 0) {
            period = period.withDays(period.getDays() + 365 * period.getYears()).withYears(0);
        }
        if (period.getMonths() > 0) {
            period = period.withDays(period.getDays() + 31 * period.getMonths()).withMonths(0);
        }
        return period.toStandardDuration();
    }
}
