package cz.cesnet.shongo.controller.api.request;

import cz.cesnet.shongo.Technology;
import cz.cesnet.shongo.controller.api.Specification;

import java.util.HashSet;
import java.util.Set;

/**
 * Request for list of reservation requests.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
public class ReservationRequestListRequest extends ListRequest
{
    private Set<Technology> technologies = new HashSet<Technology>();

    private Set<Class<? extends Specification>> specificationClasses = new HashSet<Class<? extends Specification>>();

    public Set<Technology> getTechnologies()
    {
        return technologies;
    }

    public void setTechnologies(Set<Technology> technologies)
    {
        this.technologies = technologies;
    }

    public void addTechnology(Technology technology)
    {
        technologies.add(technology);
    }

    public Set<Class<? extends Specification>> getSpecificationClasses()
    {
        return specificationClasses;
    }

    public void setSpecificationClasses(Set<Class<? extends Specification>> specificationClasses)
    {
        this.specificationClasses = specificationClasses;
    }

    public void addSpecificationClass(Class<? extends Specification> specificationClass)
    {
        specificationClasses.add(specificationClass);
    }
}
