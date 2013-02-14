package cz.cesnet.shongo.joda;

import cz.cesnet.shongo.api.util.Converter;
import cz.cesnet.shongo.fault.FaultException;
import cz.cesnet.shongo.fault.FaultRuntimeException;
import org.hibernate.HibernateException;
import org.joda.time.ReadablePartial;

/**
 * Persist {@link org.joda.time.ReadablePartial} via hibernate.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
public class PersistentReadablePartial extends PersistentStringType
{
    public static final PersistentReadablePartial INSTANCE = new PersistentReadablePartial();

    @Override
    public Class returnedClass()
    {
        return ReadablePartial.class;
    }

    @Override
    protected Object fromNonNullString(String string) throws HibernateException
    {
        try {
            return Converter.Atomic.convertStringToReadablePartial(string);
        }
        catch (FaultRuntimeException exception) {
            throw new HibernateException("Failed to load " + ReadablePartial.class.getName() + " from '" +
                    string.toString() + "'", exception);
        }
    }

    @Override
    protected String toNonNullString(Object value) throws HibernateException
    {
        if (value instanceof ReadablePartial) {
            return value.toString();
        }
        else {
            throw new HibernateException("Cannot save " + value.getClass().getName() + " as " +
                    ReadablePartial.class.getName() + ". Implement it if needed.");
        }
    }
}
