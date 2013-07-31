package cz.cesnet.shongo.client.web.auth;

import cz.cesnet.shongo.client.web.Cache;
import cz.cesnet.shongo.client.web.models.ReservationRequestModel;
import cz.cesnet.shongo.controller.Permission;
import cz.cesnet.shongo.controller.api.AbstractReservationRequest;
import cz.cesnet.shongo.controller.api.Executable;
import cz.cesnet.shongo.controller.api.SecurityToken;
import org.springframework.security.core.Authentication;

import javax.annotation.Resource;
import java.io.Serializable;

/**
 * Evaluator of user permissions.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
public class PermissionEvaluator implements org.springframework.security.access.PermissionEvaluator
{
    @Resource
    private Cache cache;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permissionValue)
    {
        SecurityToken securityToken = ((OpenIDConnectAuthenticationToken) authentication).getSecurityToken();
        String entityId;
        if (targetDomainObject instanceof ReservationRequestModel) {
            entityId = ((ReservationRequestModel) targetDomainObject).getId();
        }
        else if (targetDomainObject instanceof Executable) {
            entityId = ((Executable) targetDomainObject).getId();
        }
        else if (targetDomainObject instanceof AbstractReservationRequest) {
            entityId = ((AbstractReservationRequest) targetDomainObject).getId();
        }
        else if (targetDomainObject instanceof ReservationRequestModel) {
            entityId = ((ReservationRequestModel) targetDomainObject).getId();
        }
        else {
            throw new IllegalArgumentException("Illegal target " + targetDomainObject + ".");
        }
        Permission permission;
        if (permissionValue instanceof Permission) {
            permission = (Permission) permissionValue;
        }
        else {
            permission = Permission.valueOf(permissionValue.toString());
        }
        return cache.getPermissions(securityToken, entityId).contains(permission);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
            Object permission)
    {
        throw new UnsupportedOperationException();
    }
}
