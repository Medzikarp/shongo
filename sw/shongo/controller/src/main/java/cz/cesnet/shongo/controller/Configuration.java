package cz.cesnet.shongo.controller;

import org.apache.commons.configuration.CompositeConfiguration;
import org.joda.time.Duration;
import org.joda.time.Period;

/**
 * Configuration for the {@link Controller}.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
public class Configuration extends CompositeConfiguration
{
    /**
     * Configuration parameters names.
     */
    public static final String LOG_RPC = "log-rpc";
    public static final String DOMAIN_NAME = "domain.name";
    public static final String DOMAIN_ORGANIZATION = "domain.organization";
    public static final String RPC_HOST = "rpc.host";
    public static final String RPC_PORT = "rpc.port";
    public static final String JADE_HOST = "jade.host";
    public static final String JADE_PORT = "jade.port";
    public static final String JADE_PLATFORM_ID = "jade.platform-id";
    public static final String WORKER_PERIOD = "worker.period";
    public static final String WORKER_INTERVAL = "worker.interval";
    public static final String RESERVATION_RESOURCE_MAX_DURATION = "reservation.resource.max-duration";
    public static final String RESERVATION_ALIAS_MAX_DURATION = "reservation.alias.max-duration";

    /**
     * SMTP configuration.
     */
    public static final String SMTP_HOST = "smtp.host";
    public static final String SMTP_USER = "smtp.user";
    public static final String SMTP_PASSWORD = "smtp.password";

    /**
     * Period in which the executor works.
     */
    public static final String EXECUTOR_PERIOD = "executor.period";

    /**
     * Duration for which the {@link Executor} looks ahead for compartments to be executed.
     */
    public static final String EXECUTOR_LOOKUP_AHEAD = "executor.lookup-ahead";

    /**
     * Duration to modify compartment starting date/time.
     */
    public static final String EXECUTOR_COMPARTMENT_START = "executor.compartment.start";

    /**
     * Duration to modify compartment ending date/time.
     */
    public static final String EXECUTOR_COMPARTMENT_END = "executor.compartment.end";

    /**
     * Duration which {@link cz.cesnet.shongo.controller.executor.ExecutorThread} waits for virtual rooms to be created.
     */
    public static final String EXECUTOR_COMPARTMENT_WAITING_VIRTUAL_ROOM = "executor.compartment.waiting-virtual-room";

    /**
     * Period in which {@link cz.cesnet.shongo.controller.executor.ExecutorThread} checks whether the compartment should be started.
     */
    public static final String EXECUTOR_COMPARTMENT_WAITING_START = "executor.compartment.waiting-start";

    /**
     * Period in which {@link cz.cesnet.shongo.controller.executor.ExecutorThread} checks whether the compartment should be stopped.
     */
    public static final String EXECUTOR_COMPARTMENT_WAITING_END = "executor.compartment.waiting-end";

    /**
     * Authorization server.
     */
    public static final String SECURITY_AUTHORIZATION_SERVER = "security.authorization-server";

    /**
     * Specifies access token which won't be verified and can be used for testing purposes.
     */
    public static final String SECURITY_TESTING_ACCESS_TOKEN = "security.testing-access-token";

    /**
     * @see {@link #getString(String)}
     */
    public Duration getDuration(String key)
    {
        String value = getString(key);
        if (value == null) {
            return null;
        }
        return Period.parse(value).toStandardDuration();
    }

    /**
     * @see {@link #getString(String)}
     */
    public Period getPeriod(String key)
    {
        String value = getString(key);
        if (value == null) {
            return null;
        }
        return Period.parse(value);
    }
}
