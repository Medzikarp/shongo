package cz.cesnet.shongo.controller.api;

import cz.cesnet.shongo.AliasType;
import cz.cesnet.shongo.Technology;
import cz.cesnet.shongo.api.Alias;
import cz.cesnet.shongo.api.DataMap;
import cz.cesnet.shongo.api.IdentifiedComplexType;
import cz.cesnet.shongo.controller.ReservationRequestPurpose;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.*;

/**
 * Summary for all types of {@link AbstractReservationRequest}.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
public class ReservationRequestSummary extends IdentifiedComplexType
{
    /**
     * @see ReservationRequestType
     */
    private ReservationRequestType type;

    /**
     * Date/time when the {@link AbstractReservationRequest} was created.
     */
    private DateTime dateTime;

    /**
     * User-id of the user who created the {@link AbstractReservationRequest}.
     */
    private String userId;

    /**
     * @see AbstractReservationRequest#purpose
     */
    private ReservationRequestPurpose purpose;

    /**
     * @see AbstractReservationRequest#description
     */
    private String description;

    /**
     * The earliest requested date/time slot.
     */
    private Interval earliestSlot;

    /**
     * Number of slots in future (except the earliest date/time slot).
     */
    private Integer futureSlotCount;

    /**
     * {@link AllocationState} of the reservation request for the earliest requested date/time slot.
     */
    private AllocationState allocationState;

    /**
     * {@link ExecutableState} of an executable allocated for the reservation request for the earliest requested date/time slot.
     */
    private ExecutableState executableState;

    /**
     * @see cz.cesnet.shongo.controller.api.ReservationRequestSummary.Specification
     */
    private Specification specification;

    /**
     * Technologies which are .
     */
    private Set<Technology> specificationTechnologies = new HashSet<Technology>();

    /**
     * Reused reservation request identifier.
     */
    private String reusedReservationRequestId;

    /**
     * Last allocated reservation id.
     */
    private String lastReservationId;

    /**
     * {@link ExecutableState} of an executable allocated for a reservation request which reused this
     * reservation request and whose slot is active.
     */
    private ExecutableState usageExecutableState;

    /**
     * @return {@link #type}
     */
    public ReservationRequestType getType()
    {
        return type;
    }

    /**
     * @param type sets the {@link #type}
     */
    public void setType(ReservationRequestType type)
    {
        this.type = type;
    }

    /**
     * @return {@link #dateTime}
     */
    public DateTime getDateTime()
    {
        return dateTime;
    }

    /**
     * @param dateTime sets the {@link #dateTime}
     */
    public void setDateTime(DateTime dateTime)
    {
        this.dateTime = dateTime;
    }

    /**
     * @return {@link #userId}
     */
    public String getUserId()
    {
        return userId;
    }

    /**
     * @param userId sets the {@link #userId}
     */
    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    /**
     * @return {@link #purpose}
     */
    public ReservationRequestPurpose getPurpose()
    {
        return purpose;
    }

    /**
     * @param purpose sets the {@link #purpose}
     */
    public void setPurpose(ReservationRequestPurpose purpose)
    {
        this.purpose = purpose;
    }

    /**
     * @return {@link #description}
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * @param description sets the {@link #description}
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @return {@link #earliestSlot}
     */
    public Interval getEarliestSlot()
    {
        return earliestSlot;
    }

    /**
     * @param earliestSlot sets the {@link #earliestSlot}
     */
    public void setEarliestSlot(Interval earliestSlot)
    {
        this.earliestSlot = earliestSlot;
    }

    /**
     * @return {@link #futureSlotCount}
     */
    public Integer getFutureSlotCount()
    {
        return futureSlotCount;
    }

    /**
     * @param futureSlotCount sets the {@link #futureSlotCount}
     */
    public void setFutureSlotCount(Integer futureSlotCount)
    {
        this.futureSlotCount = futureSlotCount;
    }

    /**
     * @return {@link #allocationState}
     */
    public AllocationState getAllocationState()
    {
        return allocationState;
    }

    /**
     * @param allocationState sets the {@link #allocationState}
     */
    public void setAllocationState(AllocationState allocationState)
    {
        this.allocationState = allocationState;
    }

    /**
     * @return {@link #executableState}
     */
    public ExecutableState getExecutableState()
    {
        return executableState;
    }

    /**
     * @param executableState {@link #executableState}
     */
    public void setExecutableState(ExecutableState executableState)
    {
        this.executableState = executableState;
    }

    /**
     * @return {@link #specification}
     */
    public Specification getSpecification()
    {
        return specification;
    }

    /**
     * @param specification sets the {@link #specification}
     */
    public void setSpecification(Specification specification)
    {
        this.specification = specification;
    }

    /**
     * @return {@link #specificationTechnologies}
     */
    public Set<Technology> getSpecificationTechnologies()
    {
        return specificationTechnologies;
    }

    /**
     * @param specificationTechnologies sets the {@link #specificationTechnologies}
     */
    public void setSpecificationTechnologies(Set<Technology> specificationTechnologies)
    {
        this.specificationTechnologies = specificationTechnologies;
    }

    /**
     * @param technology to be added to the {@link #specificationTechnologies}
     */
    public void addSpecificationTechnology(Technology technology)
    {
        this.specificationTechnologies.add(technology);
    }

    /**
     * @return {@link #reusedReservationRequestId}
     */
    public String getReusedReservationRequestId()
    {
        return reusedReservationRequestId;
    }

    /**
     * @param reusedReservationRequestId sets the {@link #reusedReservationRequestId}
     */
    public void setReusedReservationRequestId(String reusedReservationRequestId)
    {
        this.reusedReservationRequestId = reusedReservationRequestId;
    }

    /**
     * @return {@link #lastReservationId}
     */
    public String getLastReservationId()
    {
        return lastReservationId;
    }

    /**
     * @param reservationId sets the {@link #lastReservationId}
     */
    public void setLastReservationId(String reservationId)
    {
        this.lastReservationId = reservationId;
    }

    /**
     * @return {@link #usageExecutableState}
     */
    public ExecutableState getUsageExecutableState()
    {
        return usageExecutableState;
    }

    /**
     * @param usageExecutableState sets the {@link #usageExecutableState}
     */
    public void setUsageExecutableState(ExecutableState usageExecutableState)
    {
        this.usageExecutableState = usageExecutableState;
    }

    private static final String TYPE = "type";
    private static final String DATETIME = "dateTime";
    private static final String USER_ID = "userId";
    private static final String PURPOSE = "purpose";
    private static final String DESCRIPTION = "description";
    private static final String EARLIEST_SLOT = "earliestSlot";
    private static final String FUTURE_SLOT_COUNT = "futureSlotCount";
    private static final String ALLOCATION_STATE = "allocationState";
    private static final String EXECUTABLE_STATE = "executableState";
    private static final String SPECIFICATION = "specification";
    private static final String SPECIFICATION_TECHNOLOGIES = "specificationTechnologies";
    private static final String REUSED_RESERVATION_REQUEST_ID = "reusedReservationRequestId";
    private static final String LAST_RESERVATION_ID = "lastReservationId";
    private static final String USAGE_EXECUTABLE_STATE = "usageExecutableState";

    @Override
    public DataMap toData()
    {
        DataMap dataMap = super.toData();
        dataMap.set(TYPE, type);
        dataMap.set(DATETIME, dateTime);
        dataMap.set(USER_ID, userId);
        dataMap.set(PURPOSE, purpose);
        dataMap.set(DESCRIPTION, description);
        dataMap.set(EARLIEST_SLOT, earliestSlot);
        dataMap.set(FUTURE_SLOT_COUNT, futureSlotCount);
        dataMap.set(ALLOCATION_STATE, allocationState);
        dataMap.set(EXECUTABLE_STATE, executableState);
        dataMap.set(SPECIFICATION, specification);
        dataMap.set(SPECIFICATION_TECHNOLOGIES, specificationTechnologies);
        dataMap.set(REUSED_RESERVATION_REQUEST_ID, reusedReservationRequestId);
        dataMap.set(LAST_RESERVATION_ID, lastReservationId);
        dataMap.set(USAGE_EXECUTABLE_STATE, usageExecutableState);
        return dataMap;
    }

    @Override
    public void fromData(DataMap dataMap)
    {
        super.fromData(dataMap);
        type = dataMap.getEnum(TYPE, ReservationRequestType.class);
        dateTime = dataMap.getDateTime(DATETIME);
        userId = dataMap.getString(USER_ID);
        purpose = dataMap.getEnum(PURPOSE, ReservationRequestPurpose.class);
        description = dataMap.getString(DESCRIPTION);
        earliestSlot = dataMap.getInterval(EARLIEST_SLOT);
        futureSlotCount = dataMap.getInteger(FUTURE_SLOT_COUNT);
        allocationState = dataMap.getEnum(ALLOCATION_STATE, AllocationState.class);
        executableState = dataMap.getEnum(EXECUTABLE_STATE, ExecutableState.class);
        specification = dataMap.getComplexType(SPECIFICATION, Specification.class);
        specificationTechnologies = dataMap.getSet(SPECIFICATION_TECHNOLOGIES, Technology.class);
        reusedReservationRequestId = dataMap.getString(REUSED_RESERVATION_REQUEST_ID);
        lastReservationId = dataMap.getString(LAST_RESERVATION_ID);
        usageExecutableState = dataMap.getEnum(USAGE_EXECUTABLE_STATE, ExecutableState.class);
    }

    /**
     * Type of {@link AbstractReservationRequest}.
     */
    public abstract static class Specification extends IdentifiedComplexType
    {
    }

    /**
     * {@link cz.cesnet.shongo.controller.api.ReservationRequestSummary.Specification} that represents a reservation request for a resource.
     */
    public static class ResourceSpecification extends Specification
    {
        /**
         * {@link Resource#getId()}
         */
        private String resourceId;

        /**
         * @return {@link #resourceId}
         */
        public String getResourceId()
        {
            return resourceId;
        }

        /**
         * @param resourceId sets the {@link #resourceId}
         */
        public void setResourceId(String resourceId)
        {
            this.resourceId = resourceId;
        }

        private static final String RESOURCE_ID = "resourceId";

        @Override
        public DataMap toData()
        {
            DataMap dataMap = super.toData();
            dataMap.set(RESOURCE_ID, resourceId);
            return dataMap;
        }

        @Override
        public void fromData(DataMap dataMap)
        {
            super.fromData(dataMap);
            resourceId = dataMap.getString(RESOURCE_ID);
        }
    }

    /**
     * {@link cz.cesnet.shongo.controller.api.ReservationRequestSummary.Specification} that represents a reservation request for a virtual room.
     */
    public static class RoomSpecification extends Specification
    {
        /**
         * Requested participant count for the room.
         */
        private Integer participantCount;

        /**
         * @return {@link #participantCount}
         */
        public Integer getParticipantCount()
        {
            return participantCount;
        }

        /**
         * @param participantCount sets the {@link #participantCount}
         */
        public void setParticipantCount(Integer participantCount)
        {
            this.participantCount = participantCount;
        }

        private static final String PARTICIPANT_COUNT = "participantCount";

        @Override
        public DataMap toData()
        {
            DataMap dataMap = super.toData();
            dataMap.set(PARTICIPANT_COUNT, participantCount);
            return dataMap;
        }

        @Override
        public void fromData(DataMap dataMap)
        {
            super.fromData(dataMap);
            participantCount = dataMap.getInteger(PARTICIPANT_COUNT);
        }
    }

    /**
     * {@link cz.cesnet.shongo.controller.api.ReservationRequestSummary.Specification} that represents a reservation request for a {@link Alias}.
     */
    public static class AliasSpecification extends Specification
    {
        /**
         * Requested {@link cz.cesnet.shongo.AliasType} for the {@link Alias}.
         */
        private cz.cesnet.shongo.AliasType aliasType;

        /**
         * Requested value for the {@link Alias}.
         */
        private String value;

        /**
         * @return {@link #aliasType}
         */
        public AliasType getAliasType()
        {
            return aliasType;
        }

        /**
         * @param aliasType sets the {@link #aliasType}
         */
        public void setAliasType(AliasType aliasType)
        {
            this.aliasType = aliasType;
        }

        /**
         * @return {@link #value}
         */
        public String getValue()
        {
            return value;
        }

        /**
         * @param value sets the {@link #value}
         */
        public void setValue(String value)
        {
            this.value = value;
        }

        private static final String ALIAS_TYPE = "aliasType";
        private static final String VALUE = "value";

        @Override
        public DataMap toData()
        {
            DataMap dataMap = super.toData();
            dataMap.set(ALIAS_TYPE, aliasType);
            dataMap.set(VALUE, value);
            return dataMap;
        }

        @Override
        public void fromData(DataMap dataMap)
        {
            super.fromData(dataMap);
            aliasType = dataMap.getEnum(ALIAS_TYPE, AliasType.class);
            value = dataMap.getString(VALUE);
        }
    }
}
