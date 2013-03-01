package cz.cesnet.shongo.controller.resource;

import cz.cesnet.shongo.AbstractManager;
import cz.cesnet.shongo.controller.fault.PersistentEntityNotFoundException;
import cz.cesnet.shongo.controller.reservation.AliasReservation;
import cz.cesnet.shongo.controller.reservation.ResourceReservation;
import cz.cesnet.shongo.controller.reservation.RoomReservation;
import cz.cesnet.shongo.controller.reservation.ValueReservation;
import cz.cesnet.shongo.controller.resource.value.FilteredValueProvider;
import cz.cesnet.shongo.controller.resource.value.ValueProvider;
import cz.cesnet.shongo.controller.util.DatabaseFilter;
import cz.cesnet.shongo.fault.FaultException;
import org.joda.time.Interval;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Manager for {@link Resource}.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 * @see AbstractManager
 */
public class ResourceManager extends AbstractManager
{
    /**
     * Constructor.
     *
     * @param entityManager
     */
    public ResourceManager(EntityManager entityManager)
    {
        super(entityManager);
    }

    /**
     * Create a new resource in the database.
     *
     * @param resource
     * @throws FaultException when the creating fail
     */
    public void create(Resource resource) throws FaultException
    {
        resource.validate();
        super.create(resource);
    }

    /**
     * Update existing resource in the database.
     *
     * @param resource
     * @throws FaultException when the updating fail
     */
    public void update(Resource resource) throws FaultException
    {
        resource.validate();
        super.update(resource);
    }

    /**
     * Delete existing resource in the database
     *
     * @param resource
     */
    public void delete(Resource resource)
    {
        for (Capability capability : resource.getCapabilities()) {
            if (capability instanceof AliasProviderCapability) {
                AliasProviderCapability aliasProviderCapability = (AliasProviderCapability) capability;
                ValueProvider valueProvider = aliasProviderCapability.getValueProvider();
                deleteValueProvider(valueProvider, capability);
            }
            else if (capability instanceof ValueProviderCapability) {
                ValueProviderCapability valueProviderCapability = (ValueProviderCapability) capability;
                ValueProvider valueProvider = valueProviderCapability.getValueProvider();
                deleteValueProvider(valueProvider, capability);
            }
        }
        super.delete(resource);
    }

    /**
     * Delete given {@code valueProvider} if it should be deleted while deleting the {@code capability}.
     *
     * @param valueProvider to be deleted
     * @param capability    which is being deleted
     */
    public void deleteValueProvider(ValueProvider valueProvider, Capability capability)
    {
        if (valueProvider instanceof FilteredValueProvider) {
            FilteredValueProvider filteredValueProvider = (FilteredValueProvider) valueProvider;
            deleteValueProvider(filteredValueProvider.getValueProvider(), capability);
        }
        if (valueProvider.getCapability().equals(capability)) {
            super.delete(valueProvider);
        }
    }

    /**
     * @return list of all resources in the database
     */
    public List<Resource> list(String userId)
    {
        DatabaseFilter filter = new DatabaseFilter("resource");
        filter.addUserId(userId);
        TypedQuery<Resource> query = entityManager.createQuery("SELECT resource FROM Resource resource"
                + " WHERE " + filter.toQueryWhere(),
                Resource.class);
        filter.fillQueryParameters(query);
        List<Resource> resourceList = query.getResultList();
        return resourceList;
    }

    /**
     * @param resourceId
     * @return {@link Resource} with given {@code resourceId}
     * @throws cz.cesnet.shongo.controller.fault.PersistentEntityNotFoundException
     *          when resource doesn't exist
     */
    public Resource get(Long resourceId) throws PersistentEntityNotFoundException
    {
        try {
            Resource resource = entityManager.createQuery(
                    "SELECT resource FROM Resource resource WHERE resource.id = :id",
                    Resource.class).setParameter("id", resourceId)
                    .getSingleResult();
            return resource;
        }
        catch (NoResultException exception) {
            throw new PersistentEntityNotFoundException(Resource.class, resourceId);
        }
    }

    /**
     * @param deviceResourceId
     * @return {@link DeviceResource} with given {@code deviceResourceId}
     * @throws cz.cesnet.shongo.controller.fault.PersistentEntityNotFoundException
     *          when device resource doesn't exist
     */
    public DeviceResource getDevice(Long deviceResourceId) throws PersistentEntityNotFoundException
    {
        try {
            DeviceResource deviceResource = entityManager.createQuery(
                    "SELECT device FROM DeviceResource device WHERE device.id = :id",
                    DeviceResource.class).setParameter("id", deviceResourceId)
                    .getSingleResult();
            return deviceResource;
        }
        catch (NoResultException exception) {
            throw new PersistentEntityNotFoundException(DeviceResource.class, deviceResourceId);
        }
    }

    /**
     * @param capabilityType
     * @return list of all {@link Resource}s which have capability with given {@code capabilityType}
     */
    public List<Resource> listResourcesWithCapability(Class<? extends Capability> capabilityType)
    {
        List<Resource> resources = entityManager.createQuery("SELECT resource FROM Resource resource"
                + " WHERE resource.id IN("
                + "  SELECT resource.id FROM Resource resource"
                + "  INNER JOIN resource.capabilities capability"
                + "  WHERE TYPE(capability) = :capability"
                + "  GROUP BY resource.id"
                + " )", Resource.class)
                .setParameter("capability", capabilityType)
                .getResultList();
        return resources;
    }

    /**
     * @return list of all managed device resource in the database
     */
    public List<DeviceResource> listManagedDevices()
    {
        List<DeviceResource> resourceList = entityManager
                .createQuery("SELECT device FROM DeviceResource device WHERE device.mode.class = ManagedMode",
                        DeviceResource.class)
                .getResultList();
        return resourceList;
    }

    /**
     * @param agentName
     * @return managed device resource which has assigned given {@code agentName}
     *         in the {@link ManagedMode#connectorAgentName} or null if it doesn't exist
     */
    public DeviceResource getManagedDeviceByAgent(String agentName)
    {
        try {
            DeviceResource deviceResource = entityManager.createQuery(
                    "SELECT device FROM DeviceResource device " +
                            "WHERE device.mode.class = ManagedMode AND device.mode.connectorAgentName = :name",
                    DeviceResource.class).setParameter("name", agentName)
                    .getSingleResult();
            return deviceResource;
        }
        catch (NoResultException exception) {
            return null;
        }
    }

    /**
     * @param capabilityType
     * @return list of all {@link Capability}s of given {@code capabilityType}
     */
    public <T extends Capability> List<T> listCapabilities(Class<T> capabilityType)
    {
        List<T> capabilities = entityManager.createQuery("SELECT capability"
                + " FROM " + capabilityType.getSimpleName() + " capability", capabilityType)
                .getResultList();
        return capabilities;
    }

    /**
     * @return list of all {@link cz.cesnet.shongo.controller.resource.value.PatternValueProvider}s
     */
    public List<ValueProvider> listValueProviders()
    {
        List<ValueProvider> valueProviders = entityManager.createQuery("SELECT valueProvider"
                + " FROM ValueProvider valueProvider", ValueProvider.class)
                .getResultList();
        return valueProviders;
    }

    /**
     * @param resourceId
     * @param interval
     * @return list of all {@link ResourceReservation}s for {@link Resource} with given {@code resourceId} which
     *         intersects given {@code interval}
     */
    public List<ResourceReservation> listResourceReservationsInInterval(Long resourceId, Interval interval)
    {
        List<ResourceReservation> resourceReservations = entityManager.createQuery("SELECT reservation"
                + " FROM ResourceReservation reservation"
                + " WHERE reservation.resource.id = :id"
                + " AND NOT(reservation.slotStart >= :end OR reservation.slotEnd <= :start)"
                + " ORDER BY reservation.slotStart", ResourceReservation.class)
                .setParameter("id", resourceId)
                .setParameter("start", interval.getStart())
                .setParameter("end", interval.getEnd())
                .getResultList();
        return resourceReservations;
    }

    /**
     * @param valueProviderId
     * @param interval
     * @return list of all {@link ValueReservation}s for value provider with given {@code valueProviderId}
     *         which intersects given {@code interval}
     */
    public List<ValueReservation> listValueReservationsInInterval(Long valueProviderId, Interval interval)
    {
        List<ValueReservation> valueReservations = entityManager.createQuery("SELECT reservation"
                + " FROM ValueReservation reservation"
                + " WHERE reservation.valueProvider.id = :id"
                + " AND NOT(reservation.slotStart >= :end OR reservation.slotEnd <= :start)"
                + " ORDER BY reservation.slotStart", ValueReservation.class)
                .setParameter("id", valueProviderId)
                .setParameter("start", interval.getStart())
                .setParameter("end", interval.getEnd())
                .getResultList();
        return valueReservations;
    }

    /**
     * @param roomProviderCapabilityId
     * @param interval
     * @return list of all {@link RoomReservation}s for room provider with given {@code roomProviderCapabilityId}
     *         which intersects given {@code interval}
     */
    public List<RoomReservation> listRoomReservationsInInterval(Long roomProviderCapabilityId, Interval interval)
    {
        List<RoomReservation> roomReservations = entityManager.createQuery("SELECT reservation"
                + " FROM RoomReservation reservation"
                + " WHERE reservation.roomProviderCapability.id = :id"
                + " AND NOT(reservation.slotStart >= :end OR reservation.slotEnd <= :start)"
                + " ORDER BY reservation.slotStart", RoomReservation.class)
                .setParameter("id", roomProviderCapabilityId)
                .setParameter("start", interval.getStart())
                .setParameter("end", interval.getEnd())
                .getResultList();
        return roomReservations;
    }

    /**
     * @param aliasProviderCapabilityId
     * @param interval
     * @return list of all {@link AliasReservation}s for alias provider with given {@code aliasProviderCapabilityId}
     *         which intersects given {@code interval}
     */
    public List<AliasReservation> listAliasReservationsInInterval(Long aliasProviderCapabilityId, Interval interval)
    {
        List<AliasReservation> aliasReservations = entityManager.createQuery("SELECT reservation"
                + " FROM AliasReservation reservation"
                + " WHERE reservation.aliasProviderCapability.id = :id"
                + " AND NOT(reservation.slotStart >= :end OR reservation.slotEnd <= :start)"
                + " ORDER BY reservation.slotStart", AliasReservation.class)
                .setParameter("id", aliasProviderCapabilityId)
                .setParameter("start", interval.getStart())
                .setParameter("end", interval.getEnd())
                .getResultList();
        return aliasReservations;
    }
}
