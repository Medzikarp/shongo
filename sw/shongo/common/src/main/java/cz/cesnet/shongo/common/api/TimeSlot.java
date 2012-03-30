package cz.cesnet.shongo.common.api;

/**
 * Represents a time slot
 *
 * @author Martin Srom
 */
public class TimeSlot
{
    /**
     * Starting Date/Time
     */
    private DateTime dateTime;

    /**
     * Period
     */
    private Period duration;

    public DateTime getDateTime()
    {
        return dateTime;
    }

    public void setDateTime(DateTime dateTime)
    {
        this.dateTime = dateTime;
    }

    public Period getDuration()
    {
        return duration;
    }

    public void setDuration(Period duration)
    {
        this.duration = duration;
    }
}
