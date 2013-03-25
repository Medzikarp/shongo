package cz.cesnet.shongo.controller.api.rpc;

import cz.cesnet.shongo.api.*;
import cz.cesnet.shongo.connector.api.jade.ConnectorCommand;
import cz.cesnet.shongo.connector.api.jade.common.GetDeviceLoadInfo;
import cz.cesnet.shongo.connector.api.jade.common.GetSupportedMethods;
import cz.cesnet.shongo.connector.api.jade.endpoint.*;
import cz.cesnet.shongo.connector.api.jade.multipoint.io.*;
import cz.cesnet.shongo.connector.api.jade.multipoint.rooms.*;
import cz.cesnet.shongo.connector.api.jade.multipoint.users.*;
import cz.cesnet.shongo.controller.*;
import cz.cesnet.shongo.controller.api.SecurityToken;
import cz.cesnet.shongo.controller.authorization.Authorization;
import cz.cesnet.shongo.controller.common.EntityIdentifier;
import cz.cesnet.shongo.controller.resource.DeviceResource;
import cz.cesnet.shongo.controller.resource.ManagedMode;
import cz.cesnet.shongo.controller.resource.Mode;
import cz.cesnet.shongo.controller.resource.ResourceManager;
import cz.cesnet.shongo.fault.FaultException;
import cz.cesnet.shongo.jade.SendLocalCommand;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Resource service implementation.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
public class ResourceControlServiceImpl extends Component
        implements ResourceControlService, Component.ControllerAgentAware,
                   Component.EntityManagerFactoryAware, Component.AuthorizationAware
{
    /**
     * @see ControllerAgent
     */
    private ControllerAgent controllerAgent;

    /**
     * @see javax.persistence.EntityManagerFactory
     */
    private EntityManagerFactory entityManagerFactory;

    /**
     * @see cz.cesnet.shongo.controller.authorization.Authorization
     */
    private Authorization authorization;

    @Override
    public void setControllerAgent(ControllerAgent controllerAgent)
    {
        this.controllerAgent = controllerAgent;
    }

    @Override
    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory)
    {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void setAuthorization(Authorization authorization)
    {
        this.authorization = authorization;
    }

    @Override
    public void init(Configuration configuration)
    {
        checkDependency(controllerAgent, ControllerAgent.class);
        checkDependency(authorization, Authorization.class);
        super.init(configuration);
    }

    @Override
    public String getServiceName()
    {
        return "ResourceControl";
    }

    @Override
    public Collection<String> getSupportedMethods(SecurityToken token, String deviceResourceId)
            throws FaultException
    {
        String agentName = validate(token, deviceResourceId);
        return (List<String>) performDeviceAction(deviceResourceId, agentName, new GetSupportedMethods());
    }

    @Override
    public DeviceLoadInfo getDeviceLoadInfo(SecurityToken token, String deviceResourceId) throws FaultException
    {
        String agentName = validate(token, deviceResourceId);
        return (DeviceLoadInfo) performDeviceAction(deviceResourceId, agentName, new GetDeviceLoadInfo());
    }

    @Override
    public String dial(SecurityToken token, String deviceResourceId, Alias alias) throws FaultException
    {
        String agentName = validate(token, deviceResourceId);
        return (String) performDeviceAction(deviceResourceId, agentName, new Dial(alias));
    }

    @Override
    public void standBy(SecurityToken token, String deviceResourceId) throws FaultException
    {
        String agentName = validate(token, deviceResourceId);
        performDeviceAction(deviceResourceId, agentName, new StandBy());
    }

    @Override
    public void hangUp(SecurityToken token, String deviceResourceId, String callId) throws FaultException
    {
        String agentName = validate(token, deviceResourceId);
        performDeviceAction(deviceResourceId, agentName, new HangUp(callId));
    }

    @Override
    public void hangUpAll(SecurityToken token, String deviceResourceId) throws FaultException
    {
        String agentName = validate(token, deviceResourceId);
        performDeviceAction(deviceResourceId, agentName, new HangUpAll());
    }

    @Override
    public void rebootDevice(SecurityToken token, String deviceResourceId) throws FaultException
    {
        String agentName = validate(token, deviceResourceId);
        performDeviceAction(deviceResourceId, agentName, new RebootDevice());
    }

    @Override
    public void mute(SecurityToken token, String deviceResourceId) throws FaultException
    {
        String agentName = validate(token, deviceResourceId);
        performDeviceAction(deviceResourceId, agentName, new Mute());
    }

    @Override
    public void unmute(SecurityToken token, String deviceResourceId) throws FaultException
    {
        String agentName = validate(token, deviceResourceId);
        performDeviceAction(deviceResourceId, agentName, new Unmute());
    }

    @Override
    public void setMicrophoneLevel(SecurityToken token, String deviceResourceId, int level)
            throws FaultException
    {
        String agentName = validate(token, deviceResourceId);
        performDeviceAction(deviceResourceId, agentName, new SetMicrophoneLevel(level));
    }

    @Override
    public void setPlaybackLevel(SecurityToken token, String deviceResourceId, int level) throws FaultException
    {
        String agentName = validate(token, deviceResourceId);
        performDeviceAction(deviceResourceId, agentName, new SetPlaybackLevel(level));
    }

    @Override
    public void enableVideo(SecurityToken token, String deviceResourceId) throws FaultException
    {
        String agentName = validate(token, deviceResourceId);
        performDeviceAction(deviceResourceId, agentName, new EnableVideo());
    }

    @Override
    public void disableVideo(SecurityToken token, String deviceResourceId) throws FaultException
    {
        String agentName = validate(token, deviceResourceId);
        performDeviceAction(deviceResourceId, agentName, new DisableVideo());
    }

    @Override
    public void startPresentation(SecurityToken token, String deviceResourceId) throws FaultException
    {
        String agentName = validate(token, deviceResourceId);
        performDeviceAction(deviceResourceId, agentName, new StartPresentation());
    }

    @Override
    public void stopPresentation(SecurityToken token, String deviceResourceId) throws FaultException
    {
        String agentName = validate(token, deviceResourceId);
        performDeviceAction(deviceResourceId, agentName, new StopPresentation());
    }

    @Override
    public String dialParticipant(SecurityToken token, String deviceResourceId, String roomId, Alias alias)
            throws FaultException
    {
        String agentName = validate(token, deviceResourceId);
        return (String) performDeviceAction(deviceResourceId, agentName, new DialParticipant(roomId, alias));
    }

    @Override
    public void disconnectParticipant(SecurityToken token, String deviceResourceId, String roomId,
            String roomUserId) throws FaultException
    {
        String agentName = validate(token, deviceResourceId);
        performDeviceAction(deviceResourceId, agentName, new DisconnectParticipant(roomId, roomUserId));
    }

    @Override
    public Collection<RoomSummary> listRooms(SecurityToken token, String deviceResourceId) throws FaultException
    {
        String agentName = validate(token, deviceResourceId);
        return (Collection<RoomSummary>) performDeviceAction(deviceResourceId, agentName, new ListRooms());
    }

    @Override
    public Room getRoom(SecurityToken token, String deviceResourceId, String roomId) throws FaultException
    {
        String agentName = validate(token, deviceResourceId);
        return (Room) performDeviceAction(deviceResourceId, agentName, new GetRoom(roomId));
    }

    @Override
    public String createRoom(SecurityToken token, String deviceResourceId, Room room) throws FaultException
    {
        String agentName = validate(token, deviceResourceId);
        return (String) performDeviceAction(deviceResourceId, agentName, new CreateRoom(room));
    }

    @Override
    public String modifyRoom(SecurityToken token, String deviceResourceId, Room room) throws FaultException
    {
        String agentName = validate(token, deviceResourceId);
        return (String) performDeviceAction(deviceResourceId, agentName, new ModifyRoom(room));
    }

    @Override
    public void deleteRoom(SecurityToken token, String deviceResourceId, String roomId) throws FaultException
    {
        String agentName = validate(token, deviceResourceId);
        performDeviceAction(deviceResourceId, agentName, new DeleteRoom(roomId));
    }

    @Override
    public Collection<RoomUser> listParticipants(SecurityToken token, String deviceResourceId, String roomId)
            throws FaultException
    {
        String agentName = validate(token, deviceResourceId);
        return (List<RoomUser>) performDeviceAction(deviceResourceId, agentName, new ListParticipants(roomId));
    }

    @Override
    public RoomUser getParticipant(SecurityToken token, String deviceResourceId, String roomId,
            String roomUserId) throws FaultException
    {
        String agentName = validate(token, deviceResourceId);
        return (RoomUser) performDeviceAction(deviceResourceId, agentName, new GetParticipant(roomId, roomUserId));
    }

    @Override
    public void modifyParticipant(SecurityToken token, String deviceResourceId, String roomId,
            String roomUserId, Map<String, Object> attributes) throws FaultException
    {
        String agentName = validate(token, deviceResourceId);
        performDeviceAction(deviceResourceId, agentName, new ModifyParticipant(roomId, roomUserId, attributes));
    }

    @Override
    public void muteParticipant(SecurityToken token, String deviceResourceId, String roomId, String roomUserId)
            throws FaultException
    {
        String agentName = validate(token, deviceResourceId);
        performDeviceAction(deviceResourceId, agentName, new MuteParticipant(roomId, roomUserId));
    }

    @Override
    public void unmuteParticipant(SecurityToken token, String deviceResourceId, String roomId,
            String roomUserId) throws FaultException
    {
        String agentName = validate(token, deviceResourceId);
        performDeviceAction(deviceResourceId, agentName, new UnmuteParticipant(roomId, roomUserId));
    }

    @Override
    public void enableParticipantVideo(SecurityToken token, String deviceResourceId, String roomId,
            String roomUserId) throws FaultException
    {
        String agentName = validate(token, deviceResourceId);
        performDeviceAction(deviceResourceId, agentName, new EnableParticipantVideo(roomId, roomUserId));
    }

    @Override
    public void disableParticipantVideo(SecurityToken token, String deviceResourceId, String roomId,
            String roomUserId) throws FaultException
    {
        String agentName = validate(token, deviceResourceId);
        performDeviceAction(deviceResourceId, agentName, new DisableParticipantVideo(roomId, roomUserId));
    }

    @Override
    public void setParticipantMicrophoneLevel(SecurityToken token, String deviceResourceId, String roomId,
            String roomUserId, int level) throws FaultException
    {
        String agentName = validate(token, deviceResourceId);
        performDeviceAction(deviceResourceId, agentName, new SetParticipantMicrophoneLevel(roomId, roomUserId, level));
    }

    @Override
    public void setParticipantPlaybackLevel(SecurityToken token, String deviceResourceId, String roomId,
            String roomUserId, int level) throws FaultException
    {
        String agentName = validate(token, deviceResourceId);
        performDeviceAction(deviceResourceId, agentName, new SetParticipantPlaybackLevel(roomId, roomUserId, level));
    }

    @Override
    public void showMessage(SecurityToken token, String deviceResourceId, int duration, String text)
            throws FaultException
    {
        String agentName = validate(token, deviceResourceId);
        performDeviceAction(deviceResourceId, agentName, new ShowMessage(duration, text));
    }

    /**
     * Asks the local controller agent to send a command to be performed by a device.
     *
     *
     * @param agentName on which the command should be performed
     * @param action           command to be performed by the device
     * @throws FaultException
     */
    private Object performDeviceAction(String deviceResourceId, String agentName, ConnectorCommand action) throws FaultException
    {
        SendLocalCommand sendLocalCommand = controllerAgent.sendCommand(agentName, action);
        if (sendLocalCommand.getState() == SendLocalCommand.State.SUCCESSFUL) {
            return sendLocalCommand.getResult();
        }
        return ControllerFaultSet.throwDeviceCommandFailedFault(
                deviceResourceId, action.toString(), sendLocalCommand.getFailure());
    }

    /**
     * @param token to be validated against given {@code deviceResourceId}
     * @param deviceResourceId
     * @return agent name
     * @throws FaultException
     */
    private String validate(SecurityToken token, String deviceResourceId) throws FaultException
    {
        String userId = authorization.validate(token);
        EntityIdentifier entityId = EntityIdentifier.parse(deviceResourceId, EntityType.RESOURCE);
            String agentName = getAgentName(entityId);

        if (!authorization.hasPermission(userId, entityId, Permission.CONTROL_RESOURCE)) {
            ControllerFaultSet.throwSecurityNotAuthorizedFault("control device %s", entityId);
        }

        return agentName;
    }

    /**
     * Gets name of agent managing a given device.
     *
     *
     * @param entityId shongo-id of device agent of which to get
     * @return agent name of managed resource with given {@code deviceResourceId}
     * @throws FaultException when resource doesn't exist or when is not managed
     */
    protected String getAgentName(EntityIdentifier entityId) throws FaultException
    {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        ResourceManager resourceManager = new ResourceManager(entityManager);
        DeviceResource deviceResource = resourceManager.getDevice(entityId.getPersistenceId());
        entityManager.close();
        Mode mode = deviceResource.getMode();
        if (mode instanceof ManagedMode) {
            ManagedMode managedMode = (ManagedMode) mode;
            return managedMode.getConnectorAgentName();
        }
        throw new FaultException("Resource '%s' is not managed!", entityId.toId());
    }
}
