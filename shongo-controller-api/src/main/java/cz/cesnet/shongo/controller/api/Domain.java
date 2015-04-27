package cz.cesnet.shongo.controller.api;

import cz.cesnet.shongo.api.AbstractComplexType;
import cz.cesnet.shongo.api.DataMap;
import cz.cesnet.shongo.api.IdentifiedComplexType;
import cz.cesnet.shongo.api.util.DeviceAddress;
import org.codehaus.jackson.annotate.JsonValue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Information about controlled or foreign domain.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
public class Domain extends IdentifiedComplexType
{
    /**
     * Represents an unique domain name (e.g., "cz.cesnet")
     */
    private String name;

    /**
     * Represents a user-visible domain organization (e.g., "CESNET, z.s.p.o.").
     */
    private String organization;

    /**
     * Status of the domain.
     */
    private Status status;

    /**
     * Represents shorten version of {@link #name} (e.g., used in description of virtual rooms)
     */
    private String code;

    /**
     * Path to certificate of domain.
     */
    private String certificatePath;

    /**
     * Address of foreign domain
     */
    private DeviceAddress domainAddress;

    /**
     * Use foreign domain for allocation
     */
    private boolean allocatable;

    /**
     * @return {@link #name}
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name sets the {@link #name}
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return {@link #organization}
     */
    public String getOrganization()
    {
        return organization;
    }

    /**
     * @param organization sets the {@link #organization}
     */
    public void setOrganization(String organization)
    {
        this.organization = organization;
    }

    /**
     * @return {@link #status}
     */
    public Status getStatus()
    {
        return status;
    }

    /**
     * @param status sets the {@link #status}
     */
    public void setStatus(Status status)
    {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public DeviceAddress getDomainAddress() {
        return domainAddress;
    }

    public void setDomainAddress(DeviceAddress domainAddress) {
        this.domainAddress = domainAddress;
    }

    public String getCertificatePath() {
        return certificatePath;
    }

    public void setCertificatePath(String certificatePath) {
        this.certificatePath = certificatePath;
    }

    public boolean isAllocatable() {
        return allocatable;
    }

    public void setAllocatable(boolean allocatable) {
        this.allocatable = allocatable;
    }

    private static final Pattern GLOBAL_ID_PATTERN = Pattern.compile("shongo:.*:(\\d+)");

    /**
     * @param globalId from which the local id should be returned
     * @return local id from given {@code globalId}
     */
    public static String getLocalId(String globalId)
    {
        Matcher matcher = GLOBAL_ID_PATTERN.matcher(globalId);

        if (matcher.matches() && matcher.groupCount() == 1) {
            String id = matcher.group(1);
            return id;
        }
        throw new IllegalArgumentException(String.format("The identifier '%s' isn't valid global identifier!",
                globalId));
    }

    private static final String NAME = "name";
    private static final String ORGANIZATION = "organization";
    private static final String STATUS = "status";
    private static final String CODE = "code";
    private static final String URL = "url";
    private static final String PORT = "port";
    private static final String CERTIFICATE_PATH = "certificatePath";
    private static final String ALLOCATABLE = "allocatable";

    @Override
    public DataMap toData()
    {
        DataMap dataMap = super.toData();
        dataMap.set(NAME, name);
        dataMap.set(ORGANIZATION, organization);
        dataMap.set(STATUS, status);
        dataMap.set(CODE, code);
        if (domainAddress != null) {
            dataMap.set(URL, domainAddress.getUrl());
            dataMap.set(PORT, domainAddress.getPort());
        }
        dataMap.set(CERTIFICATE_PATH, certificatePath);
        dataMap.set(ALLOCATABLE, allocatable);
        return dataMap;
    }

    @Override
    public void fromData(DataMap dataMap)
    {
        super.fromData(dataMap);
        name = dataMap.getStringRequired(NAME);
        organization = dataMap.getStringRequired(ORGANIZATION);
        status = dataMap.getEnum(STATUS, Status.class);
        code = dataMap.getString(CODE);
        domainAddress = new DeviceAddress(dataMap.getStringRequired(URL), dataMap.getIntegerRequired(PORT));
        certificatePath = dataMap.getString(CERTIFICATE_PATH);
        allocatable = dataMap.getBool(ALLOCATABLE);
    }

    /**
     * Status of a domain.
     *
     */
    @jade.content.onto.annotations.Element(name = "DomainStatus")
    public enum Status
    {
        /**
         * Means that domain is currently available to the controller.
         */
        AVAILABLE("available"),

        /**
         * Means that domain is currently not available to the controller.
         */
        NOT_AVAILABLE("not-available");

        private String status;

        Status(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
