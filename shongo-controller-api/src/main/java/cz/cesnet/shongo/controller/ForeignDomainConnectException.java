package cz.cesnet.shongo.controller;

import cz.cesnet.shongo.controller.api.Domain;

import java.net.URL;

/**
 * Cannot connect to foreign domain.
 *
 * @author: Ondřej Pavelka <pavelka@cesnet.cz>
 */
public class ForeignDomainConnectException extends RuntimeException
{
    /**
     * Domain
     */
    private Domain domain;

    /**
     * Action URL.
     */
    private String url;

    public ForeignDomainConnectException(String url, Throwable cause)
    {
        super(cause);
        this.url = url;
    }

    public ForeignDomainConnectException(Domain domain, String url, Throwable cause)
    {
        super(cause);
        this.domain = domain;
        this.url = url;
    }

    public ForeignDomainConnectException(Domain domain, String url, String message)
    {
        super(message);
        this.domain = domain;
        this.url = url;
    }

    @Override
    public String getMessage()
    {
        return String.format("Cannot connect to foreign domain '%s' (URL: %s): %s", domain.getName(), url, super.getMessage());
    }
}

