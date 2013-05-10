package cz.cesnet.shongo.controller.usecase;

import cz.cesnet.shongo.AliasType;
import cz.cesnet.shongo.Technology;
import cz.cesnet.shongo.TodoImplementException;
import cz.cesnet.shongo.controller.AbstractControllerTest;
import cz.cesnet.shongo.controller.ReservationRequestPurpose;
import cz.cesnet.shongo.controller.api.*;
import cz.cesnet.shongo.controller.util.DatabaseHelper;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * TODO:
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
public class ReallocationTest extends AbstractControllerTest
{
    @Test
    public void testValueModification() throws Exception
    {
        Resource valueProvider = new Resource();
        valueProvider.setName("valueProvider");
        valueProvider.setAllocatable(true);
        valueProvider.addCapability(new ValueProviderCapability("{number:0:100}").withAllowedAnyRequestedValue());
        String valueProviderId = getResourceService().createResource(SECURITY_TOKEN, valueProvider);

        // Allocate value
        ReservationRequest reservationRequest = new ReservationRequest();
        reservationRequest.setSlot("2014-01-01T00:00", "2014-06-01T00:00");
        reservationRequest.setPurpose(ReservationRequestPurpose.SCIENCE);
        reservationRequest.setSpecification(new ValueSpecification(valueProviderId, "1"));
        String reservationRequestId = allocate(reservationRequest);
        ValueReservation valueReservation1 = (ValueReservation) checkAllocated(reservationRequestId);

        Assert.assertEquals("Value should be allocated.", "1", valueReservation1.getValue());

        // Modify allocated value
        reservationRequest = (ReservationRequest) getReservationService().getReservationRequest(
                SECURITY_TOKEN, reservationRequestId);
        reservationRequest.setSpecification(new ValueSpecification(valueProviderId, "2"));
        ValueReservation valueReservation2 = (ValueReservation) allocateAndCheck(reservationRequest);

        Assert.assertEquals("The same (only modified) reservation should be allocated.",
                valueReservation1.getId(), valueReservation2.getId());
        Assert.assertEquals("Modified value should be allocated.", "2", valueReservation2.getValue());
    }

    @Test
    public void testValueExtension() throws Exception
    {
        Resource valueProvider = new Resource();
        valueProvider.setName("valueProvider");
        valueProvider.setAllocatable(true);
        valueProvider.addCapability(new ValueProviderCapability("{number:0:100}").withAllowedAnyRequestedValue());
        String valueProviderId = getResourceService().createResource(SECURITY_TOKEN, valueProvider);

        // Allocate a value (#1)
        ReservationRequest reservationRequest1 = new ReservationRequest();
        reservationRequest1.setSlot("2014-01-01T00:00", "2014-06-01T00:00");
        reservationRequest1.setPurpose(ReservationRequestPurpose.SCIENCE);
        reservationRequest1.setSpecification(new ValueSpecification(valueProviderId));
        String reservationRequest1Id = allocate(reservationRequest1);
        ValueReservation valueReservation1 = (ValueReservation) checkAllocated(reservationRequest1Id);

        Assert.assertEquals("First number from range should be allocated.", "0", valueReservation1.getValue());

        // Allocate same value (#2) before
        ReservationRequest reservationRequest2 = new ReservationRequest();
        reservationRequest2.setSlot("2013-01-01T00:00", "2013-06-01T00:00");
        reservationRequest2.setPurpose(ReservationRequestPurpose.SCIENCE);
        reservationRequest2.setSpecification(new ValueSpecification(valueProviderId));
        String reservationRequest2Id = allocate(reservationRequest2);
        ValueReservation valueReservation2_1 = (ValueReservation) checkAllocated(reservationRequest2Id);

        Assert.assertEquals("Same number should be allocated.", "0", valueReservation2_1.getValue());

        // Extend #2 to not intersect #1
        reservationRequest2 = (ReservationRequest) getReservationService().getReservationRequest(
                SECURITY_TOKEN, reservationRequest2Id);
        reservationRequest2.setSlot("2013-01-01T00:00", "2014-01-01T00:00");
        ValueReservation valueReservation2_2 = (ValueReservation) allocateAndCheck(reservationRequest2);

        Assert.assertEquals("The same (only modified) reservation should be allocated.",
                valueReservation2_1.getId(), valueReservation2_2.getId());
        Assert.assertEquals("Value should not be changed.",
                valueReservation2_1.getValue(), valueReservation2_2.getValue());

        // Extend #2 to intersect #1
        reservationRequest2 = (ReservationRequest) getReservationService().getReservationRequest(
                SECURITY_TOKEN, reservationRequest2Id);
        reservationRequest2.setSlot("2013-01-01T00:00", "2014-06-01T00:00");
        ValueReservation valueReservation2_3 = (ValueReservation) allocateAndCheck(reservationRequest2);

        Assert.assertEquals("The same (only modified) reservation should be allocated.",
                valueReservation2_1.getId(), valueReservation2_2.getId());
        Assert.assertEquals("Second number from range should be allocated.", "1", valueReservation2_3.getValue());
    }

    @Test
    public void testAliasModification() throws Exception
    {
        Resource aliasProvider = new Resource();
        aliasProvider.setName("aliasProvider");
        aliasProvider.setAllocatable(true);
        aliasProvider.addCapability(new AliasProviderCapability("{number:0:100}", AliasType.ROOM_NAME));
        getResourceService().createResource(SECURITY_TOKEN, aliasProvider);

        // Allocate alias
        ReservationRequest reservationRequest = new ReservationRequest();
        reservationRequest.setSlot("2014-01-01T00:00", "2014-06-01T00:00");
        reservationRequest.setPurpose(ReservationRequestPurpose.SCIENCE);
        reservationRequest.setSpecification(new AliasSpecification(AliasType.ROOM_NAME).withValue("1"));
        String reservationRequestId = allocate(reservationRequest);
        AliasReservation aliasReservation1 = (AliasReservation) checkAllocated(reservationRequestId);

        Assert.assertEquals("Alias should be allocated.", "1", aliasReservation1.getValue());

        // Modify allocated alias
        reservationRequest = (ReservationRequest) getReservationService().getReservationRequest(
                SECURITY_TOKEN, reservationRequestId);
        reservationRequest.setSpecification(new AliasSpecification(AliasType.ROOM_NAME).withValue("2"));
        AliasReservation aliasReservation2 = (AliasReservation) allocateAndCheck(reservationRequest);

        Assert.assertEquals("The same (only modified) reservation should be allocated.",
                aliasReservation1.getId(), aliasReservation2.getId());
        Assert.assertEquals("Modified Alias should be allocated.", "2", aliasReservation2.getValue());
        Assert.assertFalse("New value reservation should be allocated.",
                aliasReservation1.getValueReservation().getId().equals(
                        aliasReservation2.getValueReservation().getId()));
    }

    @Test
    public void testAliasExtension() throws Exception
    {
        Resource aliasProvider = new Resource();
        aliasProvider.setName("aliasProvider");
        aliasProvider.setAllocatable(true);
        aliasProvider.setMaximumFuture(Period.parse("P1Y"));
        aliasProvider.addCapability(new AliasProviderCapability("{number:0:100}", AliasType.ROOM_NAME));
        getResourceService().createResource(SECURITY_TOKEN, aliasProvider);

        // In 2013
        setWorkingInterval(Interval.parse("2013-01-01T00:00/2013-02-01T00:00"));
        // Allocate a new alias reservation
        ReservationRequest reservationRequest = new ReservationRequest();
        reservationRequest.setSlot("2013-01-01T00:00", "P1Y");
        reservationRequest.setPurpose(ReservationRequestPurpose.SCIENCE);
        reservationRequest.setSpecification(new AliasSpecification(AliasType.ROOM_NAME));
        String reservationRequestId = allocate(reservationRequest);
        AliasReservation aliasReservation1 = (AliasReservation) checkAllocated(reservationRequestId);

        // In 2014
        setWorkingInterval(Interval.parse("2014-01-01T00:00/2014-02-01T00:00"));
        // Extend the validity of the alias reservation
        reservationRequest = (ReservationRequest) getReservationService().getReservationRequest(
                SECURITY_TOKEN, reservationRequestId);
        reservationRequest.setSlot("2013-01-01T00:00", "P2Y");
        AliasReservation aliasReservation2 = (AliasReservation) allocateAndCheck(reservationRequest);

        Assert.assertEquals("Value reservation identifiers should be same (the reservation should be only extended)",
                aliasReservation1.getValueReservation().getId(), aliasReservation2.getValueReservation().getId());
        Assert.assertEquals("Alias reservation identifiers should be same (the reservation should be only extended)",
                aliasReservation1.getId(), aliasReservation2.getId());
    }

    @Test
    public void testAliasSetModification() throws Exception
    {
        Resource aliasProvider = new Resource();
        aliasProvider.setName("aliasProvider");
        aliasProvider.setAllocatable(true);
        aliasProvider.addCapability(new AliasProviderCapability("{number:0:100}", AliasType.ROOM_NAME));
        getResourceService().createResource(SECURITY_TOKEN, aliasProvider);

        // Allocate alias set
        ReservationRequest reservationRequest = new ReservationRequest();
        reservationRequest.setSlot("2014-01-01T00:00", "2014-06-01T00:00");
        reservationRequest.setPurpose(ReservationRequestPurpose.SCIENCE);
        AliasSetSpecification aliasSetSpecification = new AliasSetSpecification();
        aliasSetSpecification.addAlias(new AliasSpecification(AliasType.ROOM_NAME).withValue("1"));
        aliasSetSpecification.addAlias(new AliasSpecification(AliasType.ROOM_NAME).withValue("2"));
        reservationRequest.setSpecification(aliasSetSpecification);
        String reservationRequestId = allocate(reservationRequest);
        Reservation reservation1 = checkAllocated(reservationRequestId);

        List<String> childReservationIds1 = reservation1.getChildReservationIds();
        Assert.assertEquals("Two child alias reservations should be allocated.", 2, childReservationIds1.size());
        AliasReservation aliasReservation1_1 =
                (AliasReservation) getReservationService().getReservation(SECURITY_TOKEN, childReservationIds1.get(0));
        AliasReservation aliasReservation1_2 =
                (AliasReservation) getReservationService().getReservation(SECURITY_TOKEN, childReservationIds1.get(1));
        Assert.assertEquals("First alias should be allocated.", "1", aliasReservation1_1.getValue());
        Assert.assertEquals("Second alias should be allocated.", "2", aliasReservation1_2.getValue());

        // Modify allocated alias set
        reservationRequest = (ReservationRequest) getReservationService().getReservationRequest(
                SECURITY_TOKEN, reservationRequestId);
        aliasSetSpecification = (AliasSetSpecification) reservationRequest.getSpecification();
        aliasSetSpecification.getAliases().get(1).setValue("22");
        aliasSetSpecification.addAlias(new AliasSpecification(AliasType.ROOM_NAME).withValue("2"));
        Reservation reservation2 = allocateAndCheck(reservationRequest);

        Assert.assertEquals("The same (only modified) reservation should be allocated.",
                reservation1.getId(), reservation2.getId());
        List<String> childReservationIds2 = reservation2.getChildReservationIds();
        Assert.assertEquals("Three child alias reservations should be allocated.", 3, childReservationIds2.size());
        AliasReservation aliasReservation2_1 =
                (AliasReservation) getReservationService().getReservation(SECURITY_TOKEN, childReservationIds2.get(0));
        AliasReservation aliasReservation2_2 =
                (AliasReservation) getReservationService().getReservation(SECURITY_TOKEN, childReservationIds2.get(1));
        AliasReservation aliasReservation2_3 =
                (AliasReservation) getReservationService().getReservation(SECURITY_TOKEN, childReservationIds2.get(2));
        Assert.assertEquals("First alias should be allocated.", "1", aliasReservation2_1.getValue());
        Assert.assertEquals("The same (only modified) child reservation should be allocated.",
                aliasReservation1_1.getId(), aliasReservation2_1.getId());
        Assert.assertEquals("Third alias should be allocated.", "2", aliasReservation2_2.getValue());
        Assert.assertEquals("Existing child reservation should be allocated.",
                aliasReservation1_2.getId(), aliasReservation2_2.getId());
        Assert.assertEquals("Modified alias should be allocated.", "22", aliasReservation2_3.getValue());
        Assert.assertFalse("New child reservation should be allocated.",
                aliasReservation1_2.getId().equals(aliasReservation2_3.getId()));
    }

    @Test
    public void testAliasSetExtension() throws Exception
    {
        Resource aliasProvider = new Resource();
        aliasProvider.setName("aliasProvider");
        aliasProvider.setAllocatable(true);
        aliasProvider.setMaximumFuture(Period.parse("P1Y"));
        aliasProvider.addCapability(new AliasProviderCapability("{number:0:100}", AliasType.ROOM_NAME));
        getResourceService().createResource(SECURITY_TOKEN, aliasProvider);

        // In 2013
        setWorkingInterval(Interval.parse("2013-01-01T00:00/2013-02-01T00:00"));
        // Allocate a new alias reservation
        ReservationRequest reservationRequest = new ReservationRequest();
        reservationRequest.setSlot("2013-01-01T00:00", "P1Y");
        reservationRequest.setPurpose(ReservationRequestPurpose.SCIENCE);
        AliasSetSpecification aliasSetSpecification = new AliasSetSpecification();
        aliasSetSpecification.addAlias(new AliasSpecification(AliasType.ROOM_NAME).withValue("1"));
        aliasSetSpecification.addAlias(new AliasSpecification(AliasType.ROOM_NAME).withValue("2"));
        reservationRequest.setSpecification(aliasSetSpecification);
        String reservationRequestId = allocate(reservationRequest);
        Reservation reservation1 = checkAllocated(reservationRequestId);

        List<String> childReservationIds1 = reservation1.getChildReservationIds();
        Assert.assertEquals("Two child alias reservations should be allocated.", 2, childReservationIds1.size());
        AliasReservation aliasReservation1_1 =
                (AliasReservation) getReservationService().getReservation(SECURITY_TOKEN, childReservationIds1.get(0));
        AliasReservation aliasReservation1_2 =
                (AliasReservation) getReservationService().getReservation(SECURITY_TOKEN, childReservationIds1.get(1));

        // In 2014
        setWorkingInterval(Interval.parse("2014-01-01T00:00/2014-02-01T00:00"));
        // Extend the validity of the alias reservation
        reservationRequest = (ReservationRequest) getReservationService().getReservationRequest(
                SECURITY_TOKEN, reservationRequestId);
        reservationRequest.setSlot("2013-01-01T00:00", "P2Y");
        Reservation reservation2 = allocateAndCheck(reservationRequest);

        Assert.assertEquals("Reservation identifiers should be same (the reservation should be only extended)",
                reservation1.getId(), reservation2.getId());
        List<String> childReservationIds2 = reservation2.getChildReservationIds();
        Assert.assertEquals("Two child alias reservations should be allocated.", 2, childReservationIds2.size());
        AliasReservation aliasReservation2_1 =
                (AliasReservation) getReservationService().getReservation(SECURITY_TOKEN, childReservationIds2.get(0));
        AliasReservation aliasReservation2_2 =
                (AliasReservation) getReservationService().getReservation(SECURITY_TOKEN, childReservationIds2.get(1));
        Assert.assertEquals("Reservation identifiers should be same (the reservation should be only extended)",
                aliasReservation1_1.getId(), aliasReservation2_1.getId());
        Assert.assertEquals("Reservation identifiers should be same (the reservation should be only extended)",
                aliasReservation1_2.getId(), aliasReservation2_2.getId());
    }

    @Test
    public void testRoomModification() throws Exception
    {
        DeviceResource multipoint = new DeviceResource();
        multipoint.setName("multipoint");
        multipoint.setAllocatable(true);
        multipoint.addTechnology(Technology.H323);
        multipoint.addCapability(new RoomProviderCapability(5));
        getResourceService().createResource(SECURITY_TOKEN, multipoint);

        // Allocate a new room reservation
        ReservationRequest reservationRequest = new ReservationRequest();
        reservationRequest.setSlot("2013-01-01T12:00", "PT1H");
        reservationRequest.setPurpose(ReservationRequestPurpose.SCIENCE);
        reservationRequest.setSpecification(new RoomSpecification(3, Technology.H323));
        String reservationRequestId = allocate(reservationRequest);
        RoomReservation roomReservation1 = (RoomReservation) checkAllocated(reservationRequestId);

        // Increase the room capacity
        reservationRequest = (ReservationRequest) getReservationService().getReservationRequest(
                SECURITY_TOKEN, reservationRequestId);
        ((RoomSpecification) reservationRequest.getSpecification()).setParticipantCount(5);
        RoomReservation roomReservation2 = (RoomReservation) allocateAndCheck(reservationRequest);

        Assert.assertEquals("Reservation identifiers should be same (only the room capacity should be increased)",
                roomReservation1.getId(), roomReservation2.getId());
    }

    @Test
    public void testRoomExtension() throws Exception
    {
        // TODO:
    }

    @Test
    public void testAliasRoomCapacityExtension() throws Exception
    {
        // TODO:
    }

    @Test
    public void testMaintenanceForcesReallocation() throws Exception
    {
        DeviceResource multipoint1 = new DeviceResource();
        multipoint1.setName("multipoint1");
        multipoint1.setAllocatable(true);
        multipoint1.addTechnology(Technology.H323);
        multipoint1.addCapability(new RoomProviderCapability(10));
        String multipoint1Id = getResourceService().createResource(SECURITY_TOKEN, multipoint1);

        DeviceResource multipoint2 = new DeviceResource();
        multipoint2.setName("multipoint2");
        multipoint2.setAllocatable(true);
        multipoint2.addTechnology(Technology.H323);
        multipoint2.addCapability(new RoomProviderCapability(5));
        String multipoint2Id = getResourceService().createResource(SECURITY_TOKEN, multipoint2);

        // Allocate a new room reservation
        ReservationRequest roomReservationRequest = new ReservationRequest();
        roomReservationRequest.setSlot("2013-01-01T12:00", "PT1H");
        roomReservationRequest.setPurpose(ReservationRequestPurpose.SCIENCE);
        roomReservationRequest.setSpecification(new RoomSpecification(3, Technology.H323));
        String roomReservationRequestId = allocate(roomReservationRequest);
        RoomReservation roomReservation = (RoomReservation) checkAllocated(roomReservationRequestId);

        Assert.assertEquals(multipoint1Id, roomReservation.getResourceId());

        ReservationRequest maintenanceReservationRequest = new ReservationRequest();
        maintenanceReservationRequest.setSlot("2013-01-01T00:00", "P1D");
        maintenanceReservationRequest.setPurpose(ReservationRequestPurpose.MAINTENANCE);
        maintenanceReservationRequest.setSpecification(new ResourceSpecification(multipoint1Id));
        ResourceReservation maintenanceReservation =
                (ResourceReservation) allocateAndCheck(maintenanceReservationRequest);
        roomReservation = (RoomReservation) getReservationService().getReservation(
                SECURITY_TOKEN, roomReservationRequestId);

        Assert.assertEquals("Maintenance reservation should be allocated.",
                multipoint1Id, maintenanceReservation.getResourceId());
        Assert.assertEquals("Room should be migrated to another device.",
                multipoint2Id, roomReservation.getResourceId());
    }
}