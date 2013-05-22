package cz.cesnet.shongo.controller.scheduler;

import cz.cesnet.shongo.controller.cache.Cache;
import cz.cesnet.shongo.controller.cache.ResourceCache;
import cz.cesnet.shongo.controller.reservation.ExistingReservation;
import cz.cesnet.shongo.controller.reservation.FilteredValueReservation;
import cz.cesnet.shongo.controller.reservation.Reservation;
import cz.cesnet.shongo.controller.reservation.ValueReservation;
import cz.cesnet.shongo.controller.resource.Capability;
import cz.cesnet.shongo.controller.resource.ResourceManager;
import cz.cesnet.shongo.controller.resource.value.FilteredValueProvider;
import cz.cesnet.shongo.controller.resource.value.ValueProvider;
import org.joda.time.Interval;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Represents {@link cz.cesnet.shongo.controller.scheduler.ReservationTask} for a {@link cz.cesnet.shongo.controller.reservation.AliasReservation}.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
public class ValueReservationTask extends ReservationTask
{
    /**
     * {@link ValueProvider} to be used.
     */
    private ValueProvider valueProvider;

    /**
     * To be allocated.
     */
    private String requestedValue;

    /**
     * Constructor.
     *
     * @param schedulerContext
     * @param valueProvider
     * @param requestedValue
     */
    public ValueReservationTask(SchedulerContext schedulerContext, ValueProvider valueProvider, String requestedValue)
    {
        super(schedulerContext);
        this.valueProvider = valueProvider;
        this.requestedValue = requestedValue;
    }

    @Override
    protected SchedulerReport createMainReport()
    {
        return new SchedulerReportSet.AllocatingValueReport(
                valueProvider.getTargetValueProvider().getCapabilityResource());
    }

    @Override
    protected Reservation allocateReservation() throws SchedulerException
    {
        validateReservationSlot(ValueReservation.class);

        final Interval interval = getInterval();
        final Cache cache = getCache();
        final ResourceCache resourceCache = cache.getResourceCache();

        // Check if resource can be allocated and if it is available in the future
        Capability capability = valueProvider.getCapability();
        resourceCache.checkCapabilityAvailable(capability, schedulerContext);

        // Check target value provider
        ValueProvider targetValueProvider = valueProvider.getTargetValueProvider();
        if (targetValueProvider != valueProvider) {
            // Check whether target value provider can be allocated
            capability = targetValueProvider.getCapability();
            resourceCache.checkCapabilityAvailable(capability, schedulerContext);
        }

        // Already used values for targetValueProvider in the interval
        Set<String> usedValues = getUsedValues(targetValueProvider, interval);

        // Get available value reservations
        List<AvailableReservation<ValueReservation>> availableValueReservations =
                new LinkedList<AvailableReservation<ValueReservation>>();
        availableValueReservations.addAll(schedulerContext.getAvailableValueReservations(targetValueProvider));
        sortAvailableReservations(availableValueReservations);

        // Find matching available value reservation
        for (AvailableReservation<ValueReservation> availableValueReservation : availableValueReservations) {
            // Check available value reservation
            Reservation originalReservation = availableValueReservation.getOriginalReservation();
            ValueReservation valueReservation = availableValueReservation.getTargetReservation();

            // Only reusable available reservations
            if (!availableValueReservation.isType(AvailableReservation.Type.REUSABLE)) {
                continue;
            }

            // Original reservation slot must contain requested slot
            if (!originalReservation.getSlot().contains(interval)) {
                continue;
            }

            // Value must match requested value
            if (requestedValue != null && !valueReservation.getValue().equals(requestedValue)) {
                continue;
            }

            // Available reservation will be returned so remove it from context (to not be used again)
            schedulerContext.removeAvailableReservation(availableValueReservation);

            // Create new existing value reservation
            addReport(new SchedulerReportSet.ReservationReusingReport(originalReservation));
            ExistingReservation existingValueReservation = new ExistingReservation();
            existingValueReservation.setSlot(interval);
            existingValueReservation.setReservation(originalReservation);
            return existingValueReservation;
        }

        // Allocate value reservation
        try {
            String value;
            ValueReservation valueReservation;
            // Create new value reservation
            if (valueProvider instanceof FilteredValueProvider) {
                valueReservation = new FilteredValueReservation(requestedValue);
            }
            else {
                valueReservation = new ValueReservation();
            }
            // Generate new value
            if (requestedValue != null) {
                value = valueProvider.generateValue(usedValues, requestedValue);
            }
            else {
                value = valueProvider.generateValue(usedValues);
            }
            valueReservation.setSlot(interval);
            valueReservation.setValueProvider(targetValueProvider);
            valueReservation.setValue(value);
            return valueReservation;
        }
        catch (ValueProvider.InvalidValueException exception) {
            throw new SchedulerReportSet.ValueInvalidException(requestedValue);
        }
        catch (ValueProvider.ValueAlreadyAllocatedException exception) {
            throw new SchedulerReportSet.ValueAlreadyAllocatedException(requestedValue);
        }
        catch (ValueProvider.NoAvailableValueException exception) {
            throw new SchedulerReportSet.ValueNotAvailableException();
        }
    }

    /**
     * @param valueProvider for which the used values should be returned
     * @param interval      for which interval
     * @return set of used values for given {@code valueProvider} in given {@code interval}
     */
    private Set<String> getUsedValues(ValueProvider valueProvider, Interval interval)
    {
        Set<String> usedValues;
        ResourceManager resourceManager = new ResourceManager(schedulerContext.getEntityManager());
        Long valueProviderId = valueProvider.getId();
        List<ValueReservation> allocatedValues =
                resourceManager.listValueReservationsInInterval(valueProviderId, interval);
        schedulerContext.applyValueReservations(valueProviderId, allocatedValues);
        usedValues = new HashSet<String>();
        for (ValueReservation allocatedValue : allocatedValues) {
            usedValues.add(allocatedValue.getValue());
        }
        return usedValues;
    }
}
