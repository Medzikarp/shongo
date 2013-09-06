package cz.cesnet.shongo.client.web.models;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.ReadablePartial;
import org.joda.time.format.DateTimeFormat;

import java.util.Locale;

/**
 * Date/time formatter.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
public class DateTimeFormatter
{
    public static final Type SHORT = Type.SHORT;
    public static final Type LONG = Type.LONG;

    /**
     * @param type
     * @return {@link DateTimeFormatter} of given {@code type}
     */
    public static DateTimeFormatter getInstance(Type type)
    {
        return new DateTimeFormatter(type);
    }

    /**
     * @param type
     * @param locale
     * @param dateTimeZone
     * @return {@link DateTimeFormatter} of given {@code type} and for given {@code locale}
     */
    public static DateTimeFormatter getInstance(Type type, Locale locale, DateTimeZone dateTimeZone)
    {
        return new DateTimeFormatter(type).with(locale, dateTimeZone);
    }

    /**
     * Time formatter.
     */
    private final org.joda.time.format.DateTimeFormatter timeFormatter;

    /**
     * Date formatter.
     */
    private final org.joda.time.format.DateTimeFormatter dateFormatter;

    /**
     * Date/time formatter.
     */
    private final org.joda.time.format.DateTimeFormatter dateTimeFormatter;

    /**
     * Timezone.
     */
    private final DateTimeZone dateTimeZone;

    /**
     * Constructor.
     *
     * @param type
     */
    private DateTimeFormatter(Type type)
    {
        switch (type) {
            case SHORT:
                this.timeFormatter = DateTimeFormat.forStyle("-S");
                this.dateFormatter = DateTimeFormat.forStyle("M-");
                this.dateTimeFormatter = DateTimeFormat.forStyle("MS");
                break;
            default:
                this.timeFormatter = DateTimeFormat.forStyle("-M");
                this.dateFormatter = DateTimeFormat.forStyle("M-");
                this.dateTimeFormatter = DateTimeFormat.forStyle("MM");
                break;
        }
        this.dateTimeZone = DateTimeZone.getDefault();
    }

    /**
     * Constructor.
     *
     * @param dateTimeFormatter
     * @param locale
     */
    private DateTimeFormatter(DateTimeFormatter dateTimeFormatter, Locale locale, DateTimeZone dateTimeZone)
    {
        this.timeFormatter = dateTimeFormatter.timeFormatter.withLocale(locale);
        this.dateFormatter = dateTimeFormatter.dateFormatter.withLocale(locale);
        this.dateTimeFormatter = dateTimeFormatter.dateTimeFormatter.withLocale(locale);
        this.dateTimeZone = (dateTimeZone != null ? dateTimeZone : DateTimeZone.getDefault());
    }

    /**
     * @param locale
     * @return {@link DateTimeFormatter} for given {@code locale}
     */
    public DateTimeFormatter with(Locale locale, DateTimeZone dateTimeZone)
    {
        return new DateTimeFormatter(this, locale, dateTimeZone);
    }

    /**
     * @param dateTime
     * @return formatted given {@code dateTime} as time
     */
    public String formatTime(DateTime dateTime)
    {
        return timeFormatter.print(dateTime.withZone(dateTimeZone));
    }

    /**
     * @param dateTime
     * @return formatted given {@code dateTime} as date
     */
    public String formatDate(DateTime dateTime)
    {
        return dateFormatter.print(dateTime.withZone(dateTimeZone));
    }

    /**
     * @param readablePartial
     * @return formatted given {@code readablePartial} as date
     */
    public String formatDate(ReadablePartial readablePartial)
    {
        return dateFormatter.print(readablePartial);
    }

    /**
     * @param dateTime
     * @return formatted given {@code dateTime} as date and time
     */
    public String formatDateTime(DateTime dateTime)
    {
        return dateTimeFormatter.print(dateTime.withZone(dateTimeZone));
    }

    /**
     * @param interval
     * @return formatted given {@code interval}
     */
    public String formatInterval(Interval interval)
    {
        StringBuilder stringBuilder = new StringBuilder();
        DateTime start = interval.getStart().withZone(dateTimeZone);
        DateTime end = interval.getEnd().withZone(dateTimeZone);
        if (start.withTimeAtStartOfDay().equals(end.withTimeAtStartOfDay())) {
            stringBuilder.append(dateFormatter.print(start));
            stringBuilder.append(" ");
            stringBuilder.append(timeFormatter.print(start));
            stringBuilder.append(" - ");
            stringBuilder.append(timeFormatter.print(end));
        }
        else {
            stringBuilder.append(dateTimeFormatter.print(start));
            stringBuilder.append(" - ");
            stringBuilder.append(dateTimeFormatter.print(end));
        }
        return stringBuilder.toString();
    }

    /**
     * @param interval
     * @return formatted given {@code interval} as date range
     */
    public String formatIntervalDate(Interval interval)
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(dateFormatter.print(interval.getStart().withZone(dateTimeZone)));
        stringBuilder.append(" - ");
        stringBuilder.append(dateFormatter.print(interval.getEnd().withZone(dateTimeZone)));
        return stringBuilder.toString();
    }

    /**
     * @param interval
     * @return formatted given {@code interval} as two lines
     */
    public String formatIntervalMultiLine(Interval interval)
    {
        StringBuilder stringBuilder = new StringBuilder();
        DateTime start = interval.getStart().withZone(dateTimeZone);
        DateTime end = interval.getEnd().withZone(dateTimeZone);
        if (start.withTimeAtStartOfDay().equals(end.withTimeAtStartOfDay())) {
            stringBuilder.append("<div class=\"date-time\"><table><tr><td>");
            stringBuilder.append(dateFormatter.print(start));
            stringBuilder.append("</td><td>");
            stringBuilder.append(timeFormatter.print(start));
            stringBuilder.append("</td></tr><tr><td></td><td>");
            stringBuilder.append(timeFormatter.print(end));
            stringBuilder.append("</td></tr></table></div>");
        }
        else {
            stringBuilder.append(dateTimeFormatter.print(start));
            stringBuilder.append("<br/>");
            stringBuilder.append(dateTimeFormatter.print(end));
        }
        return stringBuilder.toString();
    }

    /**
     * Type of the {@link DateTimeFormatter}.
     */
    public static enum Type
    {
        /**
         * Time seconds are invisible.
         */
        SHORT,

        /**
         * Time seconds are visible.
         */
        LONG
    }
}