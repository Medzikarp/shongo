package cz.cesnet.shongo.controller.booking.reservation;

import cz.cesnet.shongo.controller.booking.reservation.AbstractForeignReservation;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Transient;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a {@link cz.cesnet.shongo.controller.booking.reservation.Reservation} for a foreign {@link cz.cesnet.shongo.api.Room}.
 *
 * @author Ondrej Pavelka <pavelka@cesnet.cz>
 */
public class ForeignRoomReservation extends AbstractForeignReservation
{
    private Set<String> foreignReservationRequestsIds = new HashSet<>();

    @ElementCollection
    @Column
    public Set<String> getForeignReservationRequestsIds()
    {
        return foreignReservationRequestsIds;
    }

    protected void setForeignReservationRequestsIds(Set<String> foreignReservationRequestsIds)
    {
        this.foreignReservationRequestsIds = foreignReservationRequestsIds;
    }

    public void addForeignReservationRequestId(String reservationRequestId)
    {
        this.foreignReservationRequestsIds.add(reservationRequestId);
    }

    public void removeForeignReservationRequestId(String reservationRequestId)
    {
        this.foreignReservationRequestsIds.remove(reservationRequestId);
    }

    @Override
    @Transient
    public Long getTargetId()
    {
        return null;
    }
}
