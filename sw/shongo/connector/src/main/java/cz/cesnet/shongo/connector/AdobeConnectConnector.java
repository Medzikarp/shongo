package cz.cesnet.shongo.connector;

import cz.cesnet.shongo.api.*;
import cz.cesnet.shongo.api.util.Address;
import cz.cesnet.shongo.connector.api.*;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;


/**
 * Created with IntelliJ IDEA.
 * User: opicak
 * Date: 10/9/12
 * Time: 1:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class AdobeConnectConnector extends AbstractConnector implements MultipointService
{
    /**
     * The URL of the server.
     */
    protected String serverUrl;

    /**
     * This is the user log in name, typically the user email address.
     */
    protected String login;

    /**
     * The password of the user.
     */
    protected String password;

    /**
     * The Java session ID that is generated upon successful login.  All calls
     * except login must provide this ID for authentication.
     */
    protected String breezesession;

    /**
     * @param serverUrl     the base URL of the Breeze server, including the
     *                      trailing slash http://www.breeze.example/ is a typical
     *                      example.  Most Breeze installations will not need any
     *                      path except that of the host.
     * @param login         the login of the user as whom the adapter will act on
     *                      the Breeze system.  This is often an administrator but
     *                      Breeze will properly apply permissions for any user.
     * @param password      The password of the user who's logging in.
     * @param breezesession The Java session ID created by the Breeze server upon
     *                      successful login.
     */
    public AdobeConnectConnector(String serverUrl, String login, String password, String breezesession)
    {
        this.serverUrl = serverUrl;
        this.login = login;
        this.password = password;
        this.breezesession = breezesession;
    }

    @java.lang.Override
    public void connect(Address address, String username, String password) throws CommandException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @java.lang.Override
    public void disconnect() throws CommandException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @java.lang.Override
    public ConnectorInfo getConnectorInfo()
    {
        ConnectorInfo connectorInfo = new ConnectorInfo("Adobe Connect");

        DeviceInfo deviceInfo = new DeviceInfo();

        connectorInfo.setConnectionState(ConnectorInfo.ConnectionState.LOOSELY_CONNECTED);
        connectorInfo.setDeviceInfo(deviceInfo);
        connectorInfo.setName("Adobe Connect Connector");

        // TODO: Finish after DeviceState specified

        return connectorInfo;
    }

    @java.lang.Override
    public List<String> getSupportedMethods()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @java.lang.Override
    public void muteUser(String roomId, String roomUserId) throws CommandException, CommandUnsupportedException
    {
        // TODO: Check function in AC setting
    }

    @java.lang.Override
    public void unmuteUser(String roomId, String roomUserId) throws CommandException, CommandUnsupportedException
    {
        // TODO: Check function in AC setting
    }

    @java.lang.Override
    public void setUserMicrophoneLevel(String roomId, String roomUserId, int level)
            throws CommandException, CommandUnsupportedException
    {
        throw new CommandUnsupportedException(
                "Adobe Connect does not support changing microphone level. This setting is accessible in Adobe Connect virtual room.");
    }

    @java.lang.Override
    public void setUserPlaybackLevel(String roomId, String roomUserId, int level)
            throws CommandException, CommandUnsupportedException
    {
        throw new CommandUnsupportedException("Adobe Connect does not support changing playback level.");
    }

    @java.lang.Override
    public void enableUserVideo(String roomId, String roomUserId) throws CommandException, CommandUnsupportedException
    {
        throw new CommandUnsupportedException(
                "Adobe Connect does not support this function. This setting is accessible in Adobe Connect virtual room.");
    }

    @java.lang.Override
    public void disableUserVideo(String roomId, String roomUserId) throws CommandException, CommandUnsupportedException
    {
        throw new CommandUnsupportedException(
                "Adobe Connect does not support this function. This setting is accessible in Adobe Connect virtual room.");
    }

    @java.lang.Override
    public DeviceLoadInfo getDeviceLoadInfo() throws CommandException, CommandUnsupportedException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @java.lang.Override
    public UsageStats getUsageStats() throws CommandException, CommandUnsupportedException
    {
        //report-bulk-consolidated-transactions
        //report-meeting....
        return null;  //TODO
    }

    @java.lang.Override
    public Collection<RoomSummary> getRoomList() throws CommandException, CommandUnsupportedException
    {
        HashMap<String, String> attributes = new HashMap<String, String>();
        attributes.put("filter-type", "meeting");

        Element response = request("report-bulk-objects", attributes);

        //TODO: array vs collection

        for (Element room : response.getChild("report-bulk-objects").getChildren("row")) {

        }

        return null;
    }

    @java.lang.Override
    public MediaData getReceivedVideoSnapshot(String roomId, String roomUserId)
            throws CommandException, CommandUnsupportedException
    {
        throw new CommandUnsupportedException("Adobe Connect does not support this function.");
    }

    @java.lang.Override
    public MediaData getSentVideoSnapshot(String roomId, String roomUserId)
            throws CommandException, CommandUnsupportedException
    {
        throw new CommandUnsupportedException("Adobe Connect does not support this function.");
    }

    @java.lang.Override
    public int startRecording(String roomId, ContentType format, RoomLayout layout)
            throws CommandException, CommandUnsupportedException
    {
        //TODO: AC maybe does not support
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @java.lang.Override
    public void stopRecording(int recordingId) throws CommandException, CommandUnsupportedException
    {
        //TODO: AC maybe does not support
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @java.lang.Override
    public String getRecordingDownloadURL(int recordingId) throws CommandException, CommandUnsupportedException
    {
        //TODO: is even possible?
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @java.lang.Override
    public Collection<String> notifyParticipants(int recordingId) throws CommandException, CommandUnsupportedException
    {
        //TODO: ???
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @java.lang.Override
    public void downloadRecording(String downloadURL, String targetPath)
            throws CommandException, CommandUnsupportedException
    {
        //TODO: ???
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @java.lang.Override
    public void deleteRecording(int recordingId) throws CommandException, CommandUnsupportedException
    {
        deleteSCO(Integer.toString(recordingId));
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @java.lang.Override
    public MediaData getRoomContent(String roomId) throws CommandException, CommandUnsupportedException
    {
        HashMap<String, String> attributes = new HashMap<String, String>();
        attributes.put("sco-id", roomId);

        Element response = request("sco-contents", attributes);

        for (Element child : response.getChild("scos").getChildren("sco")) {
            //TODO: archive all
        }
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @java.lang.Override
    public void addRoomContent(String roomId, String name, MediaData data)
            throws CommandException, CommandUnsupportedException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @java.lang.Override
    public void removeRoomContentFile(String roomId, String name) throws CommandException, CommandUnsupportedException
    {
        HashMap<String, String> attributes = new HashMap<String, String>();
        attributes.put("sco-id", roomId);
        attributes.put("filter-name", name);

        Element response = request("sco-contents", attributes);

        if (response.getChild("scos").getChild("sco") != null) {
            deleteSCO(response.getChild("scos").getChild("sco").getAttributeValue("sco-id"));
        }
    }

    @java.lang.Override
    public void clearRoomContent(String roomId) throws CommandException, CommandUnsupportedException
    {
        // TODO: erase and re-create room?
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @java.lang.Override
    public RoomSummary getRoomInfo(String roomId) throws CommandException, CommandUnsupportedException
    {
        RoomSummary roomSummary = new RoomSummary();
        HashMap<String, String> attributes = new HashMap<String, String>();
        attributes.put("sco-id", roomId);

        Element response = request("sco-info", attributes);

        // TODO: WTF roomSummary.setType(Technology.ADOBE_CONNECT);
        roomSummary.setName(response.getChild("sco").getChild("name").getText());

        // TODO: roomInfo.setOwner();
        // TODO: roomInfo.setCreation();
        // TODO: roomInfo.setReservation();

        return roomSummary;
    }

    @java.lang.Override
    public String createRoom(Room room) throws CommandException, CommandUnsupportedException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
/*
        HashMap<String,String> attributes = new HashMap<String, String>();
        // TODO: ??? name, url, etc.

        Element respose = request("sco-update", attributes);

        for (RoomUser roomUser : room.getUsers()) {
            String principalId = roomUser.getUserIdentity().getId();
            HashMap<String,String> userAttributes = new HashMap<String, String>();
            userAttributes.put("acl-id",respose.getChild("sco").getAttributeValue("sco-id"));
            userAttributes.put("principal-id",principalId);
            userAttributes.put("permission-id","view");

            request("permissions-update", attributes);
        }

        importRoomSettings(respose.getChild("sco").getAttributeValue("sco-id"),room.getConfiguration());
        */
    }

    @java.lang.Override
    public String modifyRoom(String roomId, Map<String, Object> attributes)
            throws CommandException, CommandUnsupportedException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @java.lang.Override
    public void deleteRoom(String roomId) throws CommandException, CommandUnsupportedException
    {
        deleteSCO(roomId);
    }

    @java.lang.Override
    public String exportRoomSettings(String roomId) throws CommandException, CommandUnsupportedException
    {
        HashMap<String, String> attributes = new HashMap<String, String>();
        attributes.put("sco-id", roomId);

        Element response = request("sco-info", attributes);
        Document document = response.getDocument();

        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        String xmlString = outputter.outputString(document);

        return xmlString;
    }

    @java.lang.Override
    public void importRoomSettings(String roomId, String settings) throws CommandException, CommandUnsupportedException
    {
        SAXBuilder saxBuilder = new SAXBuilder();
        Document document = null;
        try {
            document = saxBuilder.build(new StringReader(settings));
        }
        catch (Exception exception) {
            throw new CommandException(exception.getMessage(), exception);
        }

        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        String xmlString = outputter.outputString(document);


        HashMap<String, String> attributes = new HashMap<String, String>();

        attributes.put("sco-id", roomId);
//        attributes.put("date-begin", document.getRootElement().getChild("sco").getChild("date-begin").getText());
//        attributes.put("date-end", document.getRootElement().getChild("sco").getChild("date-end").getText());
        if (document.getRootElement().getChild("sco").getChild("description") != null) {
            attributes.put("description", document.getRootElement().getChild("sco").getChild("description").getText());
        }
        attributes.put("url-path", document.getRootElement().getChild("sco").getChild("url-path").getText());
        attributes.put("name", document.getRootElement().getChild("sco").getChild("name").getText());

        request("sco-update", attributes);
    }

    @java.lang.Override
    public String dialParticipant(String roomId, String address) throws CommandException, CommandUnsupportedException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @java.lang.Override
    public String dialParticipant(String roomId, Alias alias) throws CommandException, CommandUnsupportedException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @java.lang.Override
    public Collection<RoomUser> listRoomUsers(String roomId) throws CommandException, CommandUnsupportedException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @java.lang.Override
    public RoomUser getRoomUser(String roomId, String roomUserId) throws CommandException, CommandUnsupportedException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @java.lang.Override
    public void modifyRoomUser(String roomId, String roomUserId, Map attributes)
            throws CommandException, CommandUnsupportedException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @java.lang.Override
    public void disconnectParticipant(String roomId, String roomUserId)
            throws CommandException, CommandUnsupportedException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @java.lang.Override
    public void enableContentProvider(String roomId, String roomUserId)
            throws CommandException, CommandUnsupportedException
    {
        throw new CommandUnsupportedException("Adobe Connect does not support this function. Use user role instead.");
    }

    @java.lang.Override
    public void disableContentProvider(String roomId, String roomUserId)
            throws CommandException, CommandUnsupportedException
    {
        throw new CommandUnsupportedException("Adobe Connect does not support this function. Use user role instead.");
    }

    protected void deleteSCO(String scoID) throws CommandException
    {
        HashMap<String, String> attributes = new HashMap<String, String>();
        attributes.put("sco-id", scoID);

        request("sco-delete", attributes);
    }

    /**
     * Retrieves the appropriate Breeze URL.
     *
     * @param action    the action to perform
     * @param atributes the map os parameters for the action
     * @return the URL to perform the action
     */
    protected URL breezeUrl(String action, Map<String, String> atributes) throws IOException
    {
        String queryString = "";

        if (atributes != null) {
            for (Map.Entry<String, String> entry : atributes.entrySet()) {
                queryString += '&' + entry.getKey() + '=' + entry.getValue();
            }
        }

        return new URL(serverUrl + "/api/xml?" + "action=" + action
                + queryString);
    }

    protected String getBreezesession() throws Exception
    {

        if (breezesession == null) {
            login();
        }
        return breezesession;
    }

    /**
     * Performs the action to log into Adobe Connect server. Stores the breezeseession ID.
     */
    protected void login() throws CommandException
    {
        if (this.breezesession != null) {
            logout();
        }

        HashMap<String, String> loginAtributes = new HashMap<String, String>();
        loginAtributes.put("login", login);
        loginAtributes.put("password", password);

        URLConnection conn;
        try {
            URL loginUrl = breezeUrl("login", loginAtributes);
            conn = loginUrl.openConnection();
            conn.connect();
        }
        catch (Exception exception) {
            throw new CommandException(exception.getMessage(), exception);
        }

        String breezesessionString = (String) (conn.getHeaderField("Set-Cookie"));

        StringTokenizer st = new StringTokenizer(breezesessionString, "=");
        String sessionName = null;

        if (st.countTokens() > 1) {
            sessionName = st.nextToken();
        }

        if (sessionName != null &&
                (sessionName.equals("JSESSIONID") ||
                         sessionName.equals("BREEZESESSION"))) {

            String breezesessionNext = st.nextToken();
            int semiIndex = breezesessionNext.indexOf(';');
            this.breezesession = breezesessionNext.substring(0, semiIndex);
        }


        if (breezesession == null) {
            throw new RuntimeException("Could not log in to Adobe Connect server: " + serverUrl);
        }
    }

    /**
     * Logout of the server, clearing the session as well.
     */
    public void logout() throws CommandException
    {
        request("logout", null);
        this.breezesession = null;
    }

    protected Element request(String action, Map<String, String> atributes) throws CommandException
    {
        try {
            if (this.breezesession == null) {
                if (action.equals("logout")) {
                    return null;
                }
                else {
                    login();
                }
            }

            URL url = breezeUrl(action, atributes);

            URLConnection conn = url.openConnection();
            conn.setRequestProperty("Cookie", "BREEZESESSION=" + this.breezesession);
            conn.connect();

            InputStream resultStream = conn.getInputStream();

            Document doc = new SAXBuilder().build(resultStream);

            Element status = doc.getRootElement().getChild("status");
            if (status == null) {
                throw new RuntimeException("Response from server " + this.serverUrl + " is unreadable.");
            }
            else if (!status.getAttributeValue("code").equals("ok")) {
                List<Attribute> attributes = status.getAttributes();
                String errorMsg = "Error: ";
                for (Attribute attribute : attributes) {
                    errorMsg += " " + attribute.getName() + ": " + attribute.getValue();
                }

                if (status.getChild(status.getAttributeValue("code")) != null) {
                    List<Attribute> childAttributes = status.getChild(status.getAttributeValue("code")).getAttributes();
                    for (Attribute attribute : childAttributes) {
                        errorMsg += ", " + attribute.getName() + ": " + attribute.getValue() + " ";
                    }
                }

                throw new RuntimeException(errorMsg);
            }
            return doc.getRootElement();
        }
        catch (Exception exception) {
            throw new CommandException(exception.getMessage(), exception);
        }
    }


    public static void main(String[] args) throws Exception
    {
        try {
            AdobeConnectConnector acc = new AdobeConnectConnector("https://actest-w3.cesnet.cz", "admin",
                    "cip9skovi3t2", null);
            acc.login();

            acc.removeRoomContentFile("42108", "vyrocni_zprava_2011_2012.pdf");
//            String str = acc.exportRoomSettings("42108");
//            acc.importRoomSettings("42108",str);

            acc.logout();

        }
        catch (ExceptionInInitializerError ex) {
            System.out.println("Ex: " + ex.getException());
        }
    }

    public static void printInputStream(InputStream inputStream) throws IOException
    {
        System.out.println();
        System.out.println("-- SOURCE DATA --");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        String line = new String();

        while (bufferedReader.ready()) {
            line = bufferedReader.readLine();
            System.out.println(line);
        }
        System.out.println("-- SOURCE DATA --");
        System.out.println();
    }
}