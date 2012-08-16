package cz.cesnet.shongo.fault;

/**
 * Exception to be thrown when an entity validation failed.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
public class EntityValidationException extends FaultException
{
    /**
     * Constructor.
     *
     * @param entityType
     * @param entityIdentifier
     */
    public EntityValidationException(Class entityType, Long entityIdentifier, String format, Object... objects)
    {
        super(CommonFault.ENTITY_VALIDATION, "%s (entity '%s' with identifier '%d')",
                String.format(format, objects), entityType, entityIdentifier);
    }
}
