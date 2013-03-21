package cz.cesnet.shongo.controller.scheduler.report;

import cz.cesnet.shongo.controller.executor.Endpoint;
import cz.cesnet.shongo.controller.executor.Executable;
import cz.cesnet.shongo.controller.report.Report;

import javax.persistence.*;

/**
 * Represents a {@link Report} for connection between two {@link Endpoint}s.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
@Entity
public abstract class AbstractConnectionReport extends Report
{
    /**
     * Identification of source endpoint.
     */
    private Endpoint endpointFrom;

    /**
     * Identification of target endpoint.
     */
    private Endpoint endpointTo;

    /**
     * Constructor.
     */
    public AbstractConnectionReport()
    {
    }

    /**
     * Constructor.
     *
     * @param endpointFrom
     * @param endpointTo
     */
    public AbstractConnectionReport(Endpoint endpointFrom, Endpoint endpointTo)
    {
        this.endpointFrom = endpointFrom;
        this.endpointTo = endpointTo;
    }

    /**
     * @return {@link #endpointFrom}
     */
    @OneToOne(cascade = CascadeType.PERSIST)
    @Access(AccessType.FIELD)
    @JoinColumn(name = "endpoint_from_id")
    public Endpoint getEndpointFrom()
    {
        return endpointFrom;
    }

    /**
     * @return {@link #endpointFrom} as string
     */
    @Transient
    public String getEndpointFromAsString()
    {
        return endpointFrom.getDescription();
    }

    /**
     * @return {@link #endpointTo}
     */
    @OneToOne(cascade = CascadeType.PERSIST)
    @Access(AccessType.FIELD)
    @JoinColumn(name = "endpoint_to_id")
    public Endpoint getEndpointTo()
    {
        return endpointTo;
    }

    /**
     * @return {@link #endpointFrom} as string
     */
    @Transient
    public String getEndpointToAsString()
    {
        return endpointTo.getDescription();
    }

    @PreRemove
    public void preRemove()
    {
        if (endpointFrom.getState() == Executable.State.NOT_ALLOCATED) {
            endpointFrom.setState(Executable.State.TO_DELETE);
        }
        if (endpointTo.getState() == Executable.State.NOT_ALLOCATED) {
            endpointTo.setState(Executable.State.TO_DELETE);
        }
    }
}
