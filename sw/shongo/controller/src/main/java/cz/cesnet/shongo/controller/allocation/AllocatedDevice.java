package cz.cesnet.shongo.controller.allocation;

import cz.cesnet.shongo.controller.common.Person;
import cz.cesnet.shongo.controller.resource.Alias;
import cz.cesnet.shongo.controller.resource.DeviceResource;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a special type of {@link AllocatedResource} an allocated {@link DeviceResource}.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
public class AllocatedDevice extends AllocatedResource
{
    /**
     * List of persons which use the device in specified date/time slot.
     */
    private List<Person> persons = new ArrayList<Person>();

    /**
     * Aliases that are additionally assigned to the device.
     */
    private List<Alias> aliases = new ArrayList<Alias>();

    /**
     * @return {@link #persons}
     */
    @OneToMany
    @Access(AccessType.FIELD)
    public List<Person> getPersons()
    {
        return persons;
    }

    /**
     * @param person person to be added to the {@link #persons}
     */
    public void addPerson(Person person)
    {
        persons.add(person);
    }

    /**
     * @param person person to be removed from the {@link #persons}
     */
    public void removePerson(Person person)
    {
        persons.remove(person);
    }

    /**
     * @return {@link #aliases}
     */
    @OneToMany
    @Access(AccessType.FIELD)
    public List<Alias> getAliases()
    {
        return aliases;
    }

    /**
     * @param alias alias to be added to the {@link #aliases}
     */
    public void addAlias(Alias alias)
    {
        aliases.add(alias);
    }

    /**
     * @param alias alias to be removed from the {@link #aliases}
     */
    public void removeAlias(Alias alias)
    {
        aliases.remove(alias);
    }
}
