package cz.cesnet.shongo.controller;

import cz.cesnet.shongo.api.UserInformation;
import cz.cesnet.shongo.controller.api.SecurityToken;
import cz.cesnet.shongo.controller.authorization.AclRecord;
import cz.cesnet.shongo.controller.authorization.Authorization;
import cz.cesnet.shongo.controller.common.EntityIdentifier;
import cz.cesnet.shongo.fault.FaultException;
import cz.cesnet.shongo.fault.TodoImplementException;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
* Testing {@link Authorization},
*
* @author Martin Srom <martin.srom@cesnet.cz>
*/
public class TestingAuthorization extends Authorization
{
    public TestingAuthorization()
    {
        super(new Configuration());
    }

    public TestingAuthorization(Configuration config)
    {
        super(config);
    }

    @Override
    protected String onValidate(SecurityToken securityToken) throws FaultException
    {
        if (AbstractControllerTest.SECURITY_TOKEN.getAccessToken().equals(securityToken.getAccessToken())) {
            return ROOT_USER_ID;
        }
        return super.onValidate(securityToken);
    }

    @Override
    protected AclRecord onCreateAclRecord(String userId, EntityIdentifier entityId, Role role) throws FaultException
    {
        return new AclRecord(userId, entityId, role);
    }

    @Override
    protected void onDeleteAclRecord(AclRecord aclRecord) throws FaultException
    {
    }

    @Override
    protected AclRecord onGetAclRecord(String aclRecordId) throws FaultException
    {
        throw new TodoImplementException();
    }

    @Override
    protected Collection<AclRecord> onListAclRecords(String userId, EntityIdentifier entityId, Role role)
            throws FaultException
    {
        List<AclRecord> aclRecords = new LinkedList<AclRecord>();
        for (AclRecord aclRecord : cache.getAclRecords()) {
            if (userId != null && !userId.equals(aclRecord.getUserId())) {
                continue;
            }
            if (entityId != null && !entityId.equals(aclRecord.getEntityId())) {
                continue;
            }
            if (role != null && !role.equals(aclRecord.getRole())) {
                continue;
            }
            aclRecords.add(aclRecord);
        }
        return aclRecords;
    }

    @Override
    protected UserInformation onGetUserInformationByAccessToken(String accessToken) throws FaultException
    {
        if (AbstractControllerTest.SECURITY_TOKEN.getAccessToken().equals(accessToken)) {
            return ROOT_USER_INFORMATION;
        }
        throw new TodoImplementException();
    }

    @Override
    protected UserInformation onGetUserInformationByUserId(String userId) throws FaultException
    {
        throw new TodoImplementException();
    }

    @Override
    protected Collection<UserInformation> onListUserInformation() throws FaultException
    {
        throw new TodoImplementException();
    }

    public static TestingAuthorization createInstance(Configuration configuration) throws IllegalStateException
    {
        TestingAuthorization authorization = new TestingAuthorization(configuration);
        Authorization.setInstance(authorization);
        return authorization;
    }
}
