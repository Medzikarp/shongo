package cz.cesnet.shongo.controller.api.domains.request;

import cz.cesnet.shongo.ParticipantRole;
import cz.cesnet.shongo.api.UserInformation;
import cz.cesnet.shongo.controller.api.Domain;
import cz.cesnet.shongo.controller.api.ForeignPerson;
import cz.cesnet.shongo.controller.api.PersonParticipant;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a room participant for room reservation.
 *
 * @author Ondrej Pavelka <pavelka@cesnet.cz>
 */
public class RoomParticipantRole
{
    @JsonProperty("userId")
    String userId;

    @JsonProperty("role")
    ParticipantRole role;

    @JsonProperty("values")
    List<RoomParticipantValue> values = new ArrayList<>();

    @JsonCreator
    public RoomParticipantRole(@JsonProperty("userId") String userId,
                           @JsonProperty("role") ParticipantRole role,
                           @JsonProperty("values") List<RoomParticipantValue> values)
    {
        this.userId = userId;
        this.role = role;
        this.values = values;
    }

    public RoomParticipantRole(String userId, ParticipantRole role)
    {
        this.userId = userId;
        this.role = role;
    }

    public RoomParticipantRole(UserInformation userInformation, ParticipantRole role)
    {
        this.userId = userInformation.getUserId();
        this.role = role;
        this.addValue(RoomParticipantValue.Type.NAME, userInformation.getFullName());
        this.addValue(RoomParticipantValue.Type.EMAIL, userInformation.getEmail());
        for (String principalName : userInformation.getPrincipalNames()) {
            addValue(RoomParticipantValue.Type.EPPN, principalName);
        }
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public ParticipantRole getRole()
    {
        return role;
    }

    public void setRole(ParticipantRole role)
    {
        this.role = role;
    }

    public List<RoomParticipantValue> getValues()
    {
        return values;
    }

    public void setValues(List<RoomParticipantValue> values)
    {
        this.values = values;
    }

    public void addValue(RoomParticipantValue.Type type, String value)
    {
        this.values.add(new RoomParticipantValue(type, value));
    }

    public PersonParticipant toApi(Long domainId) {
        PersonParticipant participant = new PersonParticipant();
        participant.setRole(role);
        ForeignPerson person = new ForeignPerson();
        person.setUserId(UserInformation.formatForeignUserId(userId, domainId));
        for (RoomParticipantValue value : values) {
            switch (value.getType()) {
                case NAME:
                    person.setName(value.getValue());
                    break;
                case EMAIL:
                    person.setEmail(value.getValue());
                    break;
                case EPPN:
                    System.out.println("TODO: eppn: " + value.getType());
                    break;
            }
        }
        participant.setPerson(person);
        return participant;
    }



}
