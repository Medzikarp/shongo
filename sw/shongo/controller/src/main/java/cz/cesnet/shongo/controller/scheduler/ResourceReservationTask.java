package cz.cesnet.shongo.controller.scheduler;

import cz.cesnet.shongo.controller.cache.Cache;
import cz.cesnet.shongo.controller.cache.ResourceCache;
import cz.cesnet.shongo.controller.reservation.*;
import cz.cesnet.shongo.controller.resource.DeviceResource;
import cz.cesnet.shongo.controller.resource.Resource;
import cz.cesnet.shongo.controller.resource.RoomProviderCapability;
import org.joda.time.Interval;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents {@link cz.cesnet.shongo.controller.scheduler.ReservationTask} for a {@link cz.cesnet.shongo.controller.request.CompartmentSpecification}.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
public class ResourceReservationTask extends ReservationTask
{
    /**
     * {@link Resource} which should be allocated.
     */
    private Resource resource;

    /**
     * Constructor.
     *
     * @param schedulerContext  sets the {@link #schedulerContext}
     * @param resource sets the {@link #resource}
     */
    public ResourceReservationTask(SchedulerContext schedulerContext, Resource resource)
    {
        super(schedulerContext);
        this.resource = resource;
    }

    @Override
    protected SchedulerReport createMainReport()
    {
        return new SchedulerReportSet.AllocatingResourceReport(resource);
    }

    @Override
    protected Reservation allocateReservation() throws SchedulerException
    {
        validateReservationSlot(ResourceReservation.class);

        Interval interval = schedulerContext.getInterval();

        Cache cache = getCache();
        ResourceCache resourceCache = cache.getResourceCache();

        if (schedulerContext.containsReferencedResource(resource)) {
            // Same resource is requested multiple times
            throw new SchedulerReportSet.ResourceMultipleRequestedException(resource);
        }

        // Check resource and parent resources availability
        resourceCache.checkResourceAvailableByParent(resource, schedulerContext);

        // Get available resource reservations
        List<AvailableReservation<ResourceReservation>> availableResourceReservations =
                new LinkedList<AvailableReservation<ResourceReservation>>();
        availableResourceReservations.addAll(schedulerContext.getAvailableResourceReservations(resource));
        sortAvailableReservations(availableResourceReservations);

        // Find matching resource value reservation
        for (AvailableReservation<ResourceReservation> availableResourceReservation : availableResourceReservations) {
            Reservation originalReservation = availableResourceReservation.getOriginalReservation();
            ResourceReservation resourceReservation = availableResourceReservation.getTargetReservation();

            // Reallocatable reservation
            if (availableResourceReservation.isType(AvailableReservation.Type.REALLOCATABLE)) {
                if (this.reallocatableReservation == null) {
                    this.reallocatableReservation = availableResourceReservation;
                }
            }
            // Reusable available reservation
            else {
                // Original reservation slot must contain requested slot
                if (!originalReservation.getSlot().contains(interval)) {
                    continue;
                }

                // Available reservation will be returned so remove it from context (to not be used again)
                schedulerContext.removeAvailableReservation(availableResourceReservation);

                // Return available reservation
                ExistingReservation existingValueReservation;
                if (isReallocatableOriginalReservation(ExistingReservation.class)) {
                    // Reallocate existing resource reservation
                    existingValueReservation = getReallocatableOriginalReservation(ExistingReservation.class);
                    addReport(new SchedulerReportSet.ReservationReallocatingReport(existingValueReservation));
                }
                else {
                    // Create new existing resource reservation
                    existingValueReservation = new ExistingReservation();
                }
                addReport(new SchedulerReportSet.ReservationReusingReport(originalReservation));
                existingValueReservation.setSlot(interval);
                existingValueReservation.setReservation(originalReservation);
                return existingValueReservation;
            }

            // Available reservation will be returned so remove it from context (to not be used again)
            schedulerContext.removeAvailableReservation(availableResourceReservation);

            // Return available reservation
            if (availableResourceReservation.isExistingReservationRequired()) {
                addReport(new SchedulerReportSet.ReservationReusingReport(originalReservation));
                ExistingReservation existingValueReservation = new ExistingReservation();
                existingValueReservation.setSlot(interval);
                existingValueReservation.setReservation(originalReservation);
                return existingValueReservation;
            }
            else {
                addReport(new SchedulerReportSet.ReservationReallocatingReport(originalReservation));
                return originalReservation;
            }
        }

        // Proper instance of new resource reservation
        ResourceReservation resourceReservation = null;

        // If resource is a device
        if (resource instanceof DeviceResource) {
            DeviceResource deviceResource = (DeviceResource) resource;
            // Check that no room is allocated
            RoomProviderCapability roomProviderCapability = deviceResource.getCapability(RoomProviderCapability.class);
            if (roomProviderCapability != null) {
                ReservationManager reservationManager = new ReservationManager(schedulerContext.getEntityManager());
                List<RoomReservation> roomReservations =
                        reservationManager.getRoomReservations(roomProviderCapability, interval);
                schedulerContext.applyRoomReservations(roomProviderCapability.getId(), roomReservations);
                if (roomReservations.size() > 0) {
                    // Requested resource is not available in the requested slot
                    throw new SchedulerReportSet.ResourceAlreadyAllocatedException(resource);
                }
            }
            // Allocate endpoint reservation
            if (deviceResource.isTerminal()) {
                if (isReallocatableOriginalReservationStrict(EndpointReservation.class)) {
                    // Reallocate endpoint reservation
                    resourceReservation = getReallocatableOriginalReservation(EndpointReservation.class);
                    resourceReservation.clearChildReservations();
                    addReport(new SchedulerReportSet.ReservationReallocatingReport(resourceReservation));
                }
                else {
                    // Create new endpoint reservation
                    resourceReservation = new EndpointReservation();
                }
            }
        }

        // Allocate resource reservation
        if (resourceReservation == null) {
            if (isReallocatableOriginalReservationStrict(ResourceReservation.class)) {
                // Reallocate resource reservation
                resourceReservation = getReallocatableOriginalReservation(ResourceReservation.class);
                resourceReservation.clearChildReservations();
                addReport(new SchedulerReportSet.ReservationReallocatingReport(resourceReservation));
            }
            else {
                // Create new resource reservation
                resourceReservation = new ResourceReservation();
            }
        }

        // Set attributes to resource reservation
        resourceReservation.setSlot(interval);
        resourceReservation.setResource(resource);

        // Add resource as referenced to the cache to prevent from multiple checking of the same parent
        schedulerContext.addReferencedResource(resource);

        // Add child reservations for parent resources
        Resource parentResource = resource.getParentResource();
        if (parentResource != null && !schedulerContext.containsReferencedResource(parentResource)) {
            ResourceReservationTask resourceReservationTask = new ResourceReservationTask(schedulerContext, parentResource);
            addChildReservation(resourceReservationTask);
        }

        return resourceReservation;
    }
}
