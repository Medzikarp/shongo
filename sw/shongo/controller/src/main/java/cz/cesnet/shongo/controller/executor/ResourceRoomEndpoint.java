package cz.cesnet.shongo.controller.executor;

import cz.cesnet.shongo.Technology;
import cz.cesnet.shongo.connector.api.ontology.actions.multipoint.rooms.CreateRoom;
import cz.cesnet.shongo.connector.api.ontology.actions.multipoint.rooms.DeleteRoom;
import cz.cesnet.shongo.connector.api.ontology.actions.multipoint.rooms.ModifyRoom;
import cz.cesnet.shongo.controller.ControllerAgent;
import cz.cesnet.shongo.controller.Domain;
import cz.cesnet.shongo.controller.api.Executable;
import cz.cesnet.shongo.controller.common.RoomConfiguration;
import cz.cesnet.shongo.controller.common.RoomSetting;
import cz.cesnet.shongo.controller.report.ReportException;
import cz.cesnet.shongo.controller.reservation.RoomReservation;
import cz.cesnet.shongo.controller.resource.*;
import cz.cesnet.shongo.controller.scheduler.report.AbstractResourceReport;
import cz.cesnet.shongo.jade.command.AgentActionCommand;
import cz.cesnet.shongo.jade.command.Command;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Represents a {@link DeviceResource} which acts as {@link RoomEndpoint} in a {@link Compartment}.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
@Entity
public class ResourceRoomEndpoint extends RoomEndpoint implements ManagedEndpoint
{
    /**
     * {@link DeviceResource}.
     */
    private DeviceResource deviceResource;

    /**
     * {@link cz.cesnet.shongo.Technology} specific id of the {@link cz.cesnet.shongo.controller.common.RoomConfiguration}.
     */
    private String roomId;

    /**
     * Constructor.
     */
    public ResourceRoomEndpoint()
    {
    }

    /**
     * @return {@link #deviceResource}
     */
    @OneToOne
    public DeviceResource getDeviceResource()
    {
        return deviceResource;
    }

    /**
     * @param deviceResource sets the {@link #deviceResource}
     */
    public void setDeviceResource(DeviceResource deviceResource)
    {
        this.deviceResource = deviceResource;
    }

    /**
     * @return {@link #roomId}
     */
    @Override
    @Column
    public String getRoomId()
    {
        return roomId;
    }

    /**
     * @param roomId sets the {@link #roomId}
     */
    public void setRoomId(String roomId)
    {
        this.roomId = roomId;
    }

    @Override
    protected cz.cesnet.shongo.controller.api.Executable createApi()
    {
        return new Executable.ResourceRoom();
    }

    @Override
    public Executable.ResourceRoom toApi(Domain domain)
    {
        return (Executable.ResourceRoom) super.toApi(domain);
    }

    @Override
    public void toApi(cz.cesnet.shongo.controller.api.Executable executableApi, Domain domain)
    {
        super.toApi(executableApi, domain);

        Executable.ResourceRoom resourceRoomEndpointApi = (Executable.ResourceRoom) executableApi;
        resourceRoomEndpointApi.setId(domain.formatId(getId()));
        resourceRoomEndpointApi.setSlot(getSlot());
        resourceRoomEndpointApi.setState(getState().toApi());
        resourceRoomEndpointApi.setLicenseCount(getLicenseCount());
        resourceRoomEndpointApi.setResourceId(domain.formatId(getDeviceResource().getId()));
        for (Technology technology : getTechnologies()) {
            resourceRoomEndpointApi.addTechnology(technology);
        }
        for (Alias alias : getAssignedAliases()) {
            resourceRoomEndpointApi.addAlias(alias.toApi());
        }
        for (RoomSetting roomSetting : getRoomSettings()) {
            resourceRoomEndpointApi.addRoomSetting(roomSetting.toApi());
        }
    }

    @Override
    @Transient
    public Set<Technology> getTechnologies()
    {
        RoomConfiguration roomConfiguration = getRoomConfiguration();
        if (roomConfiguration.getTechnologies().size() > 0) {
            return roomConfiguration.getTechnologies();
        }
        else {
            return deviceResource.getTechnologies();
        }
    }

    /**
     * @return {@link RoomConfiguration#licenseCount} or 0 if {@link #roomConfiguration} is null
     */
    @Transient
    public int getLicenseCount()
    {
        return getRoomConfiguration().getLicenseCount();
    }

    /**
     * @return {@link RoomConfiguration#roomSettings} or empty collection if {@link #roomConfiguration} is null
     */
    @Transient
    private Collection<RoomSetting> getRoomSettings()
    {
        return getRoomConfiguration().getRoomSettings();
    }

    @Override
    @Transient
    public boolean isStandalone()
    {
        return getDeviceResource().isStandaloneTerminal();
    }

    @Override
    @Transient
    public List<Alias> getAliases()
    {
        List<Alias> aliases = new ArrayList<Alias>();
        TerminalCapability terminalCapability = getDeviceResource().getCapability(TerminalCapability.class);
        if (terminalCapability != null) {
            aliases.addAll(terminalCapability.getAliases());
        }
        aliases.addAll(super.getAssignedAliases());
        return aliases;
    }

    @Override
    public void addAssignedAlias(Alias assignedAlias) throws ReportException
    {
        deviceResource.evaluateAlias(assignedAlias);
        super.addAssignedAlias(assignedAlias);
    }

    @Override
    @Transient
    public Address getAddress()
    {
        return getDeviceResource().getAddress();
    }

    @Override
    @Transient
    public String getReportDescription()
    {
        return String.format("virtual room in %s",
                AbstractResourceReport.formatResource(getDeviceResource()));
    }

    @Override
    @Transient
    public String getConnectorAgentName()
    {
        Mode mode = getDeviceResource().getMode();
        if (mode instanceof ManagedMode) {
            ManagedMode managedMode = (ManagedMode) mode;
            return managedMode.getConnectorAgentName();
        }
        else {
            throw new IllegalStateException("Resource " + getReportDescription() + " is not managed!");
        }
    }

    @Override
    protected State onStart(ExecutorThread executorThread, EntityManager entityManager)
    {
        DeviceResource deviceResource = getDeviceResource();
        StringBuilder message = new StringBuilder();
        message.append(String.format("Starting %s for %d licenses.", getReportDescription(), getLicenseCount()));
        if (deviceResource.hasIpAddress()) {
            message.append(String.format(" Device has address '%s'.", deviceResource.getAddress().getValue()));
        }
        executorThread.getLogger().debug(message.toString());
        List<Alias> aliases = getAliases();
        for (Alias alias : aliases) {
            StringBuilder aliasMessage = new StringBuilder();
            aliasMessage.append(String.format("%s has allocated alias '%s'.",
                    getReportDescription(), alias.getValue()));
            executorThread.getLogger().debug(aliasMessage.toString());
        }

        if (getDeviceResource().isManaged()) {
            ManagedMode managedMode = (ManagedMode) getDeviceResource().getMode();
            String agentName = managedMode.getConnectorAgentName();
            ControllerAgent controllerAgent = executorThread.getControllerAgent();

            cz.cesnet.shongo.api.Room room = new cz.cesnet.shongo.api.Room();
            room.setCode(String.format("shongo-%d", getId()));
            room.setName(getRoomName());
            room.setTechnologies(getTechnologies());
            room.setLicenseCount(getLicenseCount());
            for (RoomSetting roomSetting : getRoomSettings()) {
                room.fillOptions(roomSetting.toApi());
            }
            for (Alias alias : getAliases()) {
                room.addAlias(alias.toApi());
            }
            Command command = controllerAgent.performCommandAndWait(new AgentActionCommand(agentName,
                    new CreateRoom(room)));
            if (command.getState() != Command.State.SUCCESSFUL) {
                return State.STARTING_FAILED;
            }
            setRoomId((String) command.getResult());
        }
        return super.onStart(executorThread, entityManager);
    }

    @Override
    public boolean modifyRoom(RoomConfiguration roomConfiguration, ExecutorThread executorThread,
            EntityManager entityManager)
    {
        DeviceResource deviceResource = getDeviceResource();
        StringBuilder message = new StringBuilder();
        message.append(String.format("Modifying %s for %d licenses.", getReportDescription(),
                roomConfiguration.getLicenseCount()));
        executorThread.getLogger().debug(message.toString());

        if (getDeviceResource().isManaged()) {
            ManagedMode managedMode = (ManagedMode) getDeviceResource().getMode();
            String agentName = managedMode.getConnectorAgentName();
            ControllerAgent controllerAgent = executorThread.getControllerAgent();

            cz.cesnet.shongo.api.Room room = new cz.cesnet.shongo.api.Room();
            room.setId(roomId);
            // TODO: modify name
            room.setName(getRoomName());
            room.setTechnologies(roomConfiguration.getTechnologies());
            room.setLicenseCount(roomConfiguration.getLicenseCount());
            for (RoomSetting roomSetting : roomConfiguration.getRoomSettings()) {
                room.fillOptions(roomSetting.toApi());
            }
            // TODO: assign more aliases
            for (Alias alias : getAliases()) {
                room.addAlias(alias.toApi());
            }
            Command command = controllerAgent.performCommandAndWait(new AgentActionCommand(agentName,
                    new ModifyRoom(room)));
            if (command.getState() != Command.State.SUCCESSFUL) {
                return false;
            }
        }
        return super.modifyRoom(roomConfiguration, executorThread, entityManager);
    }

    @Override
    protected State onStop(ExecutorThread executorThread, EntityManager entityManager)
    {
        StringBuilder message = new StringBuilder();
        message.append(String.format("Stopping %s for %d licenses.", getReportDescription(), getLicenseCount()));
        executorThread.getLogger().debug(message.toString());

        if (getDeviceResource().isManaged()) {
            ManagedMode managedMode = (ManagedMode) getDeviceResource().getMode();
            String agentName = managedMode.getConnectorAgentName();
            ControllerAgent controllerAgent = executorThread.getControllerAgent();
            String roomId = getRoomId();
            if (roomId == null) {
                throw new IllegalStateException("Cannot delete virtual room because it's identifier is null.");
            }
            Command command = controllerAgent
                    .performCommandAndWait(new AgentActionCommand(agentName, new DeleteRoom(roomId)));
            if (command.getState() != Command.State.SUCCESSFUL) {
                return State.STARTED;
            }
        }
        return super.onStop(executorThread, entityManager);
    }
}
