package cz.cesnet.shongo.controller.api.domains.request;

import cz.cesnet.shongo.connector.api.jade.ConnectorCommand;
import cz.cesnet.shongo.controller.api.domains.response.AbstractResponse;
import cz.cesnet.shongo.controller.api.domains.response.RoomParticipant;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Modify room's participant.
 *
 * @author Ondrej Pavelka <pavelka@cesnet.cz>
 */
public class ModifyRoomParticipant extends AbstractDomainRoomAction
{
    @JsonProperty("roomParticipant")
    private RoomParticipant roomParticipant;

    @JsonCreator
    public ModifyRoomParticipant(@JsonProperty("roomParticipant") RoomParticipant roomParticipant)
    {
        this.roomParticipant = roomParticipant;
    }

    @Override
    public ConnectorCommand toApi()
    {
        cz.cesnet.shongo.connector.api.jade.multipoint.ModifyRoomParticipant modifyRoomParticipant;
        modifyRoomParticipant = new cz.cesnet.shongo.connector.api.jade.multipoint.ModifyRoomParticipant();
        modifyRoomParticipant.setRoomParticipant(roomParticipant.toApi());

        return modifyRoomParticipant;
    }

    @Override
    public Class<AbstractResponse> getReturnClass()
    {
        return AbstractResponse.class;
    }
}
