package cz.cesnet.shongo.connector;

import cz.cesnet.shongo.jade.Container;
import cz.cesnet.shongo.jade.ContainerCommandSet;
import cz.cesnet.shongo.shell.CommandHandler;
import cz.cesnet.shongo.shell.Shell;
import cz.cesnet.shongo.util.Logging;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a device connector.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
public class Connector
{
    private static Logger logger = LoggerFactory.getLogger(Connector.class);

    /**
     * Connector parameters.
     */
    public static String jadeHost = "127.0.0.1";
    public static int jadePort = 8383;
    public static String controllerHost = "127.0.0.1";
    public static int controllerPort = 8282;
    public static int reconnectTimeout = 10000;

    /**
     * Jade container.
     */
    Container jadeContainer;

    /**
     * Jade agent names.
     */
    List<String> jadeAgents = new ArrayList<String>();

    /**
     * Init connector.
     */
    public void start()
    {
        logger.info("Starting Connector JADE container on {}:{}...", jadeHost, jadePort);
        logger.info("Connecting to the JADE main container {}:{}...", controllerHost, controllerPort);

        jadeContainer = Container.createContainer(controllerHost, controllerPort, jadeHost, jadePort);
        jadeContainer.start();
    }

    /**
     * Run connector shell.
     */
    public void run()
    {
        final Shell shell = new Shell();
        shell.setPrompt("connector");
        shell.setExitCommand("exit", "Shutdown the connector");
        shell.addCommands(ContainerCommandSet.createContainerCommandSet(jadeContainer));

        shell.addCommand("add", "Add a new connector instance", new CommandHandler()
        {
            @Override
            public void perform(CommandLine commandLine)
            {
                String[] args = commandLine.getArgs();
                if (commandLine.getArgs().length < 2) {
                    Shell.printError("You must specify the new agent name.");
                    return;
                }
                String agentName = args[1];
                jadeContainer.addAgent(agentName, ConnectorAgent.class);
                jadeAgents.add(agentName);
            }
        });
        shell.addCommand("list", "List all connector instances", new CommandHandler()
        {
            @Override
            public void perform(CommandLine commandLine)
            {
                for (String agent : jadeAgents) {
                    Shell.printInfo("Connector [%s]", agent);
                }
            }
        });
        shell.addCommand("select", "Select current connector instance", new CommandHandler()
        {
            @Override
            public void perform(CommandLine commandLine)
            {
                String[] args = commandLine.getArgs();
                if (commandLine.getArgs().length < 2) {
                    shell.setPrompt("connector");
                    shell.removeCommands(ContainerCommandSet.createContainerAgentCommandSet(jadeContainer, null));
                    return;
                }
                String agentName = args[1];
                for (String agent : jadeAgents) {
                    if (agent.equals(agentName)) {
                        shell.setPrompt(agentName + "@connector");
                        shell.addCommands(ContainerCommandSet.createContainerAgentCommandSet(jadeContainer, agentName));

                        return;
                    }
                }
                Shell.printError("Agent [%s] was not found!", agentName);
            }
        });

        // Thread that checks the connection to the main controller
        // and if it is down it tries to connect.
        final Thread connectThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                boolean startFailed = false;
                while (true) {
                    try {
                        Thread.sleep(reconnectTimeout);
                    }
                    catch (InterruptedException e) {
                    }
                    // We want to reconnect if container is not started or when the
                    // previous start failed
                    if (startFailed || jadeContainer.isStarted() == false) {
                        logger.info("Reconnecting to the JADE main container {}:{}...", controllerHost, controllerPort);
                        startFailed = false;
                        if (jadeContainer.start() == false) {
                            startFailed = true;
                        }
                    }
                }
            }
        });
        connectThread.start();

        shell.run();

        connectThread.stop();

        stop();
    }

    /**
     * De init connector
     */
    public void stop()
    {
        logger.info("Stopping Connector JADE container...");
        jadeContainer.stop();
    }

    /**
     * Main method of device connector.
     *
     * @param args
     */
    public static void main(String[] args)
    {
        Logging.installBridge();

        // Create options
        Option optionHelp = new Option(null, "help", false, "Print this usage information");
        Option optionHost = OptionBuilder.withLongOpt("host")
                .withArgName("HOST")
                .hasArg()
                .withDescription("Set the local interface address on which the connector Jade container will run")
                .create("h");
        Option optionPort = OptionBuilder.withLongOpt("port")
                .withArgName("PORT")
                .hasArg()
                .withDescription("Set the port on which the connector Jade container will run")
                .create("p");
        Option optionController = OptionBuilder.withLongOpt("controller")
                .withArgName("HOST:PORT")
                .hasArg()
                .withDescription("Set the url on which the controller is running")
                .create("c");
        Options options = new Options();
        options.addOption(optionHost);
        options.addOption(optionPort);
        options.addOption(optionController);
        options.addOption(optionHelp);

        // Parse command line
        CommandLine commandLine = null;
        try {
            CommandLineParser parser = new PosixParser();
            commandLine = parser.parse(options, args);
        }
        catch (ParseException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }

        // Print help
        if (commandLine.hasOption(optionHelp.getLongOpt())) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.setOptionComparator(new Comparator<Option>()
            {
                public int compare(Option opt1, Option opt2)
                {
                    if (opt1.getOpt() == null && opt2.getOpt() != null) {
                        return -1;
                    }
                    if (opt1.getOpt() != null && opt2.getOpt() == null) {
                        return 1;
                    }
                    if (opt1.getOpt() == null && opt2.getOpt() == null) {
                        return opt1.getLongOpt().compareTo(opt2.getLongOpt());
                    }
                    return opt1.getOpt().compareTo(opt2.getOpt());
                }
            });
            formatter.printHelp("connector", options);
            System.exit(0);
        }

        // Process parameters
        if (commandLine.hasOption(optionHost.getOpt())) {
            jadeHost = commandLine.getOptionValue(optionHost.getOpt());
        }
        if (commandLine.hasOption(optionPort.getOpt())) {
            jadePort = Integer.parseInt(commandLine.getOptionValue(optionPort.getOpt()));
        }
        if (commandLine.hasOption(optionController.getOpt())) {
            String url = commandLine.getOptionValue(optionController.getOpt());
            String[] urlParts = url.split(":");
            if (urlParts.length == 1) {
                controllerHost = urlParts[0];
            }
            else if (urlParts.length == 2) {
                controllerHost = urlParts[0];
                controllerPort = Integer.parseInt(urlParts[1]);
            }
            else {
                System.err.println("Failed to parse controller url. It should be in <HOST:URL> format.");
                System.exit(-1);
            }
        }

        final Connector connector = new Connector();
        connector.start();

        logger.info("Connector successfully started.");

        connector.run();

        logger.info("Connector exiting...");
    }
}
