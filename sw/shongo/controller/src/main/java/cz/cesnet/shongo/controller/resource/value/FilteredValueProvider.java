package cz.cesnet.shongo.controller.resource.value;

import cz.cesnet.shongo.controller.Domain;
import cz.cesnet.shongo.controller.FilterType;
import cz.cesnet.shongo.controller.resource.Capability;
import cz.cesnet.shongo.controller.resource.Resource;
import cz.cesnet.shongo.controller.resource.ResourceManager;
import cz.cesnet.shongo.controller.resource.ValueProviderCapability;
import cz.cesnet.shongo.fault.FaultException;

import javax.persistence.*;
import java.util.Set;

/**
 * Object which can allocate unique values based on the specified patterns.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
@Entity
public class FilteredValueProvider extends ValueProvider
{
    /**
     * {@link ValueProvider} which is used for generating values.
     */
    private ValueProvider valueProvider;

    /**
     * Type of filter which is used on requested values.
     */
    private FilterType type;

    /**
     * Constructor.
     */
    public FilteredValueProvider()
    {
    }

    /**
     * Constructor.
     *
     * @param capability sets the {@link #capability}
     */
    public FilteredValueProvider(Capability capability)
    {
        super(capability);
    }

    /**
     * @return {@link #valueProvider}
     */
    @ManyToOne(cascade = CascadeType.ALL)
    public ValueProvider getValueProvider()
    {
        return valueProvider;
    }

    /**
     * @param valueProvider sets the {@link #valueProvider}
     */
    public void setValueProvider(ValueProvider valueProvider)
    {
        this.valueProvider = valueProvider;
    }

    /**
     * @return {@link #type}
     */
    @Column
    @Enumerated(EnumType.STRING)
    public FilterType getType()
    {
        return type;
    }

    /**
     * @param type sets the {@link #type}
     */
    public void setType(FilterType type)
    {
        this.type = type;
    }

    @Override
    public void loadLazyCollections()
    {
        super.loadLazyCollections();

        valueProvider.loadLazyCollections();
    }

    @Override
    protected cz.cesnet.shongo.controller.api.ValueProvider createApi()
    {
        return new cz.cesnet.shongo.controller.api.ValueProvider.Filtered();
    }

    @Override
    protected void toApi(cz.cesnet.shongo.controller.api.ValueProvider valueProviderApi)
    {
        super.toApi(valueProviderApi);

        cz.cesnet.shongo.controller.api.ValueProvider.Filtered filteredValueProviderApi =
                (cz.cesnet.shongo.controller.api.ValueProvider.Filtered) valueProviderApi;

        filteredValueProviderApi.setType(getType());

        Resource valueProviderResource = valueProvider.getCapabilityResource();
        if (valueProviderResource != getCapability().getResource()) {
            filteredValueProviderApi.setValueProvider(Domain.getLocalDomain().formatId(valueProviderResource));
        }
        else {
            filteredValueProviderApi.setValueProvider(valueProvider.toApi());
        }
    }

    @Override
    public void fromApi(cz.cesnet.shongo.controller.api.ValueProvider valueProviderApi, EntityManager entityManager)
            throws FaultException
    {
        super.fromApi(valueProviderApi, entityManager);

        cz.cesnet.shongo.controller.api.ValueProvider.Filtered filteredValueProviderApi =
                (cz.cesnet.shongo.controller.api.ValueProvider.Filtered) valueProviderApi;

        if (filteredValueProviderApi.isPropertyFilled(filteredValueProviderApi.TYPE)) {
            setType(filteredValueProviderApi.getType());
        }
        if (valueProviderApi.isPropertyFilled(filteredValueProviderApi.VALUE_PROVIDER)) {
            Object valueProvider = filteredValueProviderApi.getValueProvider();
            setValueProvider(ValueProvider.modifyFromApi(valueProvider, this.valueProvider, getCapability(), entityManager));
        }
    }

    @Override
    public String generateValue(Set<String> usedValues)
    {
        String value = valueProvider.generateValue(usedValues);
        if (value != null) {
            value = FilterType.applyFilter(value, type);
        }
        return value;
    }

    @Override
    @Transient
    public String generateValue(Set<String> usedValues, String requestedValue)
    {
        return valueProvider.generateValue(usedValues, FilterType.applyFilter(requestedValue, type));
    }

    @Override
    @Transient
    public ValueProvider getTargetValueProvider()
    {
        return valueProvider;
    }
}
