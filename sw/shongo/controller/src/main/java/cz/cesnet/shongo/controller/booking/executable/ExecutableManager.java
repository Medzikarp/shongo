package cz.cesnet.shongo.controller.booking.executable;

import cz.cesnet.shongo.AbstractManager;
import cz.cesnet.shongo.CommonReportSet;
import cz.cesnet.shongo.SimplePersistentObject;
import cz.cesnet.shongo.controller.ControllerReportSetHelper;
import cz.cesnet.shongo.controller.authorization.AuthorizationManager;
import cz.cesnet.shongo.controller.booking.reservation.Reservation;
import cz.cesnet.shongo.controller.booking.room.ResourceRoomEndpoint;
import cz.cesnet.shongo.controller.booking.room.RoomEndpoint;
import cz.cesnet.shongo.controller.booking.room.UsedRoomEndpoint;
import cz.cesnet.shongo.controller.util.QueryFilter;
import org.joda.time.DateTime;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.*;

/**
 * Manager for {@link Executable}.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
public class ExecutableManager extends AbstractManager
{
    /**
     * List of {@link cz.cesnet.shongo.controller.executor.ExecutionReport}s which have been created.
     */
    private List<cz.cesnet.shongo.controller.executor.ExecutionReport> executionReports = new LinkedList<cz.cesnet.shongo.controller.executor.ExecutionReport>();

    /**
     * @param entityManager sets the {@link #entityManager}
     */
    public ExecutableManager(EntityManager entityManager)
    {
        super(entityManager);
    }

    /**
     * @param executable to be created in the database
     */
    public void create(Executable executable)
    {
        super.create(executable);
    }

    /**
     * @param executable to be updated in the database
     */
    public void update(Executable executable)
    {
        super.update(executable);
    }

    /**
     * @param executable to be deleted in the database
     */
    public void delete(Executable executable, AuthorizationManager authorizationManager)
    {
        authorizationManager.deleteAclRecordsForEntity(executable);
        super.delete(executable);
    }

    /**
     * @param executableId of the {@link Executable}
     * @return {@link Executable} with given id
     * @throws CommonReportSet.EntityNotFoundException
     *          when the {@link Executable} doesn't exist
     */
    public Executable get(Long executableId) throws CommonReportSet.EntityNotFoundException
    {
        try {
            return entityManager.createQuery(
                    "SELECT executable FROM Executable executable"
                            + " WHERE executable.id = :id AND executable.state != :notAllocated",
                    Executable.class)
                    .setParameter("id", executableId)
                    .setParameter("notAllocated", Executable.State.NOT_ALLOCATED)
                    .getSingleResult();
        }
        catch (NoResultException exception) {
            return ControllerReportSetHelper.throwEntityNotFoundFault(Executable.class, executableId);
        }
    }

    /**
     * @param executableServiceId of the {@link ExecutableService}
     * @return {@link ExecutableService} with given id
     * @throws CommonReportSet.EntityNotFoundException
     *          when the {@link ExecutableService} doesn't exist
     */
    public ExecutableService getService(Long executableServiceId)
    {
        try {
            return entityManager.createQuery("SELECT service FROM ExecutableService service WHERE service.id = :id",
                    ExecutableService.class)
                    .setParameter("id", executableServiceId)
                    .getSingleResult();
        }
        catch (NoResultException exception) {
            return ControllerReportSetHelper.throwEntityNotFoundFault(ExecutableService.class, executableServiceId);
        }
    }

    /**
     * @param ids requested identifiers
     * @return list of all allocated {@link Executable}s
     */
    public List<Executable> list(Set<Long> ids)
    {
        QueryFilter filter = new QueryFilter("executable");
        filter.addFilterIn("id", ids);
        TypedQuery<Executable> query = entityManager.createQuery("SELECT executable FROM Executable executable"
                + " WHERE executable.state != :notAllocated"
                + " AND executable NOT IN("
                + "    SELECT childExecutable FROM Executable executable "
                + "   INNER JOIN executable.childExecutables childExecutable"
                + " )"
                + " AND " + filter.toQueryWhere(),
                Executable.class);
        query.setParameter("notAllocated", Executable.State.NOT_ALLOCATED);
        filter.fillQueryParameters(query);
        List<Executable> executables = query.getResultList();
        return executables;
    }

    /**
     * @param states in which the {@link Executable}s must be
     * @return list of {@link Executable}s which are in one of given {@code states}
     */
    public List<Executable> list(Collection<Executable.State> states)
    {
        List<Executable> executables = entityManager.createQuery(
                "SELECT executable FROM Executable executable"
                        + " WHERE executable NOT IN("
                        + "   SELECT childExecutable FROM Executable executable "
                        + "   INNER JOIN executable.childExecutables childExecutable"
                        + " ) AND executable.state IN(:states)",
                Executable.class)
                .setParameter("states", states)
                .getResultList();
        return executables;
    }

    /**
     * @param referenceDateTime which represents now
     * @param maxAttemptCount
     * @return list of {@link Executable}s which should be started for given {@code referenceDateTime}
     */
    public List<Executable> listExecutablesForStart(DateTime referenceDateTime, int maxAttemptCount)
    {
        return entityManager.createQuery(
                "SELECT executable FROM Executable executable"
                        + " WHERE (executable.state IN(:notStartedStates)"
                        + "        OR (executable.nextAttempt != NULL AND executable.state = :startingFailedState))"
                        + " AND (executable.slotStart <= :dateTime AND executable.slotEnd >= :dateTime)"
                        + " AND ((executable.nextAttempt IS NULL AND executable.attemptCount = 0) OR executable.nextAttempt <= :dateTime)"
                        + " AND (executable.attemptCount < :maxAttemptCount)",
                Executable.class)
                .setParameter("dateTime", referenceDateTime)
                .setParameter("notStartedStates", EnumSet.of(Executable.State.NOT_STARTED))
                .setParameter("startingFailedState", Executable.State.STARTING_FAILED)
                .setParameter("maxAttemptCount", maxAttemptCount)
                .getResultList();
    }

    /**
     * @param referenceDateTime in which the {@link Executable}s must take place
     * @param maxAttemptCount
     * @return list of {@link Executable}s which are in one of given {@code states}
     *         and take place at given {@code dateTime}
     */
    public List<Executable> listExecutablesForUpdate(DateTime referenceDateTime, int maxAttemptCount)
    {
        return entityManager.createQuery(
                "SELECT executable FROM Executable executable"
                        + " WHERE executable.state IN(:states)"
                        + " AND (executable.slotStart <= :dateTime AND executable.slotEnd >= :dateTime)"
                        + " AND ((executable.nextAttempt IS NULL AND executable.attemptCount = 0) OR executable.nextAttempt <= :dateTime)"
                        + " AND (executable.attemptCount < :maxAttemptCount)",
                Executable.class)
                .setParameter("dateTime", referenceDateTime)
                .setParameter("states", EnumSet.of(Executable.State.MODIFIED))
                .setParameter("maxAttemptCount", maxAttemptCount)
                .getResultList();
    }

    /**
     * @param referenceDateTime which represents now
     * @param maxAttemptCount
     * @return list of {@link Executable}s which should be stopped for given {@code referenceDateTime}
     */
    public List<Executable> listExecutablesForStop(DateTime referenceDateTime, int maxAttemptCount)
    {
        return entityManager.createQuery(
                "SELECT executable FROM Executable executable"
                        + " WHERE (executable.state IN(:startedStates)"
                        + "        OR (executable.nextAttempt != NULL AND executable.state = :stoppingFailedState))"
                        + " AND (executable.slotStart > :dateTime OR executable.slotEnd <= :dateTime)"
                        + " AND ((executable.nextAttempt IS NULL AND executable.attemptCount = 0) OR executable.nextAttempt <= :dateTime)"
                        + " AND (executable.attemptCount < :maxAttemptCount)",
                Executable.class)
                .setParameter("dateTime", referenceDateTime)
                .setParameter("startedStates", EnumSet.of(Executable.State.STARTED, Executable.State.PARTIALLY_STARTED,
                        Executable.State.MODIFIED))
                .setParameter("stoppingFailedState", Executable.State.STOPPING_FAILED)
                .setParameter("maxAttemptCount", maxAttemptCount)
                .getResultList();
    }

    /**
     * @param referenceDateTime which represents now
     * @param maxAttemptCount
     * @return list of {@link ExecutableService}s which should be activated for given {@code referenceDateTime}
     */
    public List<ExecutableService> listServicesForActivation(DateTime referenceDateTime, int maxAttemptCount)
    {
        return entityManager.createQuery(
                "SELECT service FROM ExecutableService service"
                        + " WHERE (service.state IN(:notActiveStates)"
                        + "        OR (service.nextAttempt != NULL AND service.state = :activationFailedState))"
                        + " AND (service.slotStart <= :dateTime AND service.slotEnd >= :dateTime)"
                        + " AND ((service.nextAttempt IS NULL AND service.attemptCount = 0) OR service.nextAttempt <= :dateTime)"
                        + " AND (service.attemptCount < :maxAttemptCount)",
                ExecutableService.class)
                .setParameter("dateTime", referenceDateTime)
                .setParameter("notActiveStates", EnumSet.of(ExecutableService.State.PREPARED))
                .setParameter("activationFailedState", ExecutableService.State.ACTIVATION_FAILED)
                .setParameter("maxAttemptCount", maxAttemptCount)
                .getResultList();
    }

    /**
     * @param referenceDateTime which represents now
     * @param maxAttemptCount
     * @return list of {@link ExecutableService}s which should be deactivated for given {@code referenceDateTime}
     */
    public List<ExecutableService> listServicesForDeactivation(DateTime referenceDateTime, int maxAttemptCount)
    {
        return entityManager.createQuery(
                "SELECT service FROM ExecutableService service"
                        + " WHERE (service.state IN(:activeStates)"
                        + "        OR (service.nextAttempt != NULL AND service.state = :deactivationFailedState))"
                        + " AND (service.slotStart > :dateTime OR service.slotEnd <= :dateTime)"
                        + " AND ((service.nextAttempt IS NULL AND service.attemptCount = 0) OR service.nextAttempt <= :dateTime)"
                        + " AND (service.attemptCount < :maxAttemptCount)",
                ExecutableService.class)
                .setParameter("dateTime", referenceDateTime)
                .setParameter("activeStates", EnumSet.of(ExecutableService.State.ACTIVE))
                .setParameter("deactivationFailedState", ExecutableService.State.DEACTIVATION_FAILED)
                .setParameter("maxAttemptCount", maxAttemptCount)
                .getResultList();
    }

    /**
     * Delete all {@link Executable}s which are not placed inside another {@link Executable} and not referenced by
     * any {@link Reservation} and which should be automatically
     * deleted ({@link Executable.State#NOT_ALLOCATED} or {@link Executable.State#NOT_STARTED}).
     *
     * @param authorizationManager
     * @return true whether some {@link Executable} has been deleted, false otherwise
     */
    public boolean deleteAllNotReferenced(AuthorizationManager authorizationManager)
    {
        List<Executable> executablesForDeletion = entityManager
                .createQuery("SELECT executable FROM Executable executable"
                        + " WHERE executable NOT IN("
                        + "   SELECT childExecutable FROM Executable executable "
                        + "   INNER JOIN executable.childExecutables childExecutable "
                        + " ) AND ("
                        + "       executable.state = :toDelete "
                        + "   OR ("
                        + "       executable.state = :notStarted "
                        + "       AND executable NOT IN (SELECT reservation.executable FROM Reservation reservation))"
                        + " )",
                        Executable.class)
                .setParameter("notStarted", Executable.State.NOT_STARTED)
                .setParameter("toDelete", Executable.State.TO_DELETE)
                .getResultList();

        List<Executable> referencedExecutables = new LinkedList<Executable>();
        for (Executable executableForDeletion : executablesForDeletion) {
            getReferencedExecutables(SimplePersistentObject.getLazyImplementation(executableForDeletion),
                    referencedExecutables);
        }
        // Move all reused reservations to the end
        for (Executable referencedExecutable : referencedExecutables) {
            Executable topReferencedReservation = referencedExecutable;
            if (executablesForDeletion.contains(topReferencedReservation)) {
                executablesForDeletion.remove(topReferencedReservation);
                executablesForDeletion.add(topReferencedReservation);
            }
        }
        for (Executable executable : executablesForDeletion) {
            delete(executable, authorizationManager);
        }
        return executablesForDeletion.size() > 0;
    }

    /**
     * @param deviceResourceId
     * @param roomId
     * @return {@link cz.cesnet.shongo.controller.booking.room.RoomEndpoint} in given {@code deviceResourceId} with given {@code roomId}
     *         and currently taking place
     */
    public RoomEndpoint getRoomEndpoint(Long deviceResourceId, String roomId)
    {
        ResourceRoomEndpoint resourceRoomEndpoint;
        try {
            resourceRoomEndpoint = entityManager.createQuery(
                    "SELECT room FROM ResourceRoomEndpoint room"
                            + " WHERE room.roomProviderCapability.resource.id = :resourceId"
                            + " AND room.roomId = :roomId"
                            + " AND room.state IN(:startedStates)", ResourceRoomEndpoint.class)
                    .setParameter("resourceId", deviceResourceId)
                    .setParameter("roomId", roomId)
                    .setParameter("startedStates", Executable.STARTED_STATES)
                    .getSingleResult();
        }
        catch (NoResultException exception) {
            return null;
        }
        List<UsedRoomEndpoint> usedRoomEndpoints = entityManager.createQuery(
                "SELECT room FROM UsedRoomEndpoint room"
                        + " WHERE room.roomEndpoint = :room"
                        + " AND room.state IN(:startedStates)", UsedRoomEndpoint.class)
                .setParameter("room", resourceRoomEndpoint)
                .setParameter("startedStates", Executable.STARTED_STATES)
                .getResultList();
        if (usedRoomEndpoints.size() == 0) {
            return resourceRoomEndpoint;
        }
        if (usedRoomEndpoints.size() == 1) {
            return usedRoomEndpoints.get(0);
        }
        throw new RuntimeException("Found multiple " + UsedRoomEndpoint.class.getSimpleName()
                + "s taking place at " + DateTime.now() + ".");
    }

    /**
     * @return started {@link UsedRoomEndpoint} for given {@code ResourceRoomEndpoint} or null if none exists
     */
    public UsedRoomEndpoint getStartedUsedRoomEndpoint(ResourceRoomEndpoint resourceRoomEndpoint)
    {
        List<UsedRoomEndpoint> usedRoomEndpoints = entityManager.createQuery(
                "SELECT room FROM UsedRoomEndpoint room"
                        + " WHERE room.roomEndpoint = :roomEndpoint"
                        + " AND room.state IN(:stateStarted)",
                UsedRoomEndpoint.class)
                .setParameter("roomEndpoint", resourceRoomEndpoint)
                .setParameter("stateStarted", EnumSet.of(Executable.State.STARTED, Executable.State.MODIFIED))
                .getResultList();
        if (usedRoomEndpoints.size() == 0) {
            return null;
        }
        if (usedRoomEndpoints.size() == 1) {
            return usedRoomEndpoints.get(0);
        }
        throw new RuntimeException("Found multiple started " + UsedRoomEndpoint.class.getSimpleName()
                + "s for " + ResourceRoomEndpoint.class + " with id " + resourceRoomEndpoint.getId() + ".");
    }

    /**
     * @param executable
     * @return {@link Reservation} for which is given {@code executable} allocated
     */
    public Reservation getReservation(Executable executable)
    {
        // Go to top parent executable
        Set<Executable> executables = new HashSet<Executable>();
        while (!executables.contains(executable)) {
            executables.add(executable);
            List<Executable> parentExecutables = entityManager.createQuery(
                    "SELECT executable FROM Executable executable"
                            + " LEFT JOIN executable.childExecutables AS childExecutable"
                            + " WHERE childExecutable = :executable", Executable.class)
                    .setParameter("executable", executable)
                    .getResultList();
            if (parentExecutables.size() > 0) {
                executable = parentExecutables.get(0);
            }
        }

        List<Reservation> reservations = entityManager.createQuery(
                "SELECT reservation FROM Reservation reservation"
                        + " WHERE reservation.executable = :executable", Reservation.class)
                .setParameter("executable", executable)
                .getResultList();
        Set<Reservation> topReservations = new HashSet<Reservation>();
        for (Reservation reservation : reservations) {
            topReservations.add(reservation.getTopReservation());
        }
        if (topReservations.size() > 0) {
            return topReservations.iterator().next();
        }
        return null;
    }

    /**
     * @param executionTarget to which the {@code executionReport} will be added
     * @param executionReport to be added to the {@code executable}
     */
    public void createExecutionReport(ExecutionTarget executionTarget,
            cz.cesnet.shongo.controller.executor.ExecutionReport executionReport)
    {
        executionReport.setDateTime(DateTime.now());
        executionTarget.addReport(executionReport);

        executionReports.add(executionReport);
    }

    /**
     * @return {@link #executionReports}
     */
    public List<cz.cesnet.shongo.controller.executor.ExecutionReport> getExecutionReports()
    {
        return executionReports;
    }

    /**
     * Fill {@link Executable}s which are referenced (e.g., by {@link UsedRoomEndpoint})
     * from given {@code executable} to given {@code referencedExecutables}.
     *
     * @param executable
     * @param referencedExecutables
     */
    private void getReferencedExecutables(Executable executable, List<Executable> referencedExecutables)
    {
        if (executable instanceof UsedRoomEndpoint) {
            UsedRoomEndpoint usedRoomEndpoint = (UsedRoomEndpoint) executable;
            Executable referencedExecutable = usedRoomEndpoint.getRoomEndpoint();
            referencedExecutables.add(referencedExecutable);
        }
        for (Executable childExecutable : executable.getChildExecutables()) {
            getReferencedExecutables(childExecutable, referencedExecutables);
        }
    }
}