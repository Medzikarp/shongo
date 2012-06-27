package cz.cesnet.shongo.controller.impl;

import cz.cesnet.shongo.common.api.SecurityToken;
import cz.cesnet.shongo.common.xmlrpc.TypeFactory;
import cz.cesnet.shongo.controller.AbstractDatabaseTest;
import cz.cesnet.shongo.controller.Controller;
import cz.cesnet.shongo.controller.Domain;
import cz.cesnet.shongo.controller.api.ReservationService;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.junit.Test;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

/**
 * Tests for using the implementation of {@link ReservationService} through XML-RPC.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
public class ReservationServiceImplTest extends AbstractDatabaseTest
{
    Controller controller;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        controller = new Controller();
        controller.setEntityManagerFactory(getEntityManagerFactory());
        controller.addService(new ReservationServiceImpl(new Domain("cz.cesnet")));
        controller.start();
        controller.startRpc();
    }

    @Override
    public void tearDown()
    {
        super.tearDown();

        controller.stop();
    }

    @Test
    public void testCreateReservationRequest() throws Exception
    {
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL(String.format("http://%s:%d", controller.getRpcHost(), controller.getRpcPort())));

        XmlRpcClient client = new XmlRpcClient();
        client.setConfig(config);
        client.setTypeFactory(new TypeFactory(client));

        Map attributes = new HashMap();
        attributes.put("purpose", "SCIENCE");
        attributes.put("slots", new ArrayList()
        {{
                add(new HashMap()
                {{
                        put("dateTime", "2012-06-01T15:00");
                        put("duration", "PT2H");
                    }});
                add(new HashMap()
                {{
                        put("dateTime", new HashMap()
                        {{
                                put("start", "2012-07-01T14:00");
                                put("period", "P1W");
                            }});
                        put("duration", "PT2H");
                    }});
            }});
        attributes.put("compartments", new ArrayList()
        {{
                add(new HashMap()
                {{
                        put("persons", new ArrayList()
                        {{
                                add(new HashMap()
                                {{
                                        put("name", "Martin Srom");
                                        put("email", "srom@cesnet.cz");
                                    }});
                            }});
                        put("resources", new ArrayList()
                        {{
                                add(new HashMap()
                                {{
                                        put("technology", "H323");
                                        put("count", 2);
                                        put("persons", new ArrayList()
                                        {{
                                                add(new HashMap()
                                                {{
                                                        put("name", "Ondrej Bouda");
                                                        put("email", "bouda@cesnet.cz");
                                                    }});
                                                add(new HashMap()
                                                {{
                                                        put("name", "Petr Holub");
                                                        put("email", "hopet@cesnet.cz");
                                                    }});
                                            }});
                                    }});

                            }});
                    }});
            }});

        List params = new ArrayList();
        params.add(new HashMap(){{
            put("test", "Test value");
        }});
        params.add("NORMAL");
        params.add(attributes);

        String identifier = (String) client.execute("Reservation.createReservationRequest", params);
        assertEquals("shongo:cz.cesnet:1", identifier);
    }
}
