package cz.cesnet.shongo.controller.domains;

import cz.cesnet.shongo.Technology;
import cz.cesnet.shongo.TodoImplementException;
import cz.cesnet.shongo.controller.*;
import cz.cesnet.shongo.controller.api.*;
import cz.cesnet.shongo.controller.api.Capability;
import cz.cesnet.shongo.controller.api.Domain;
import cz.cesnet.shongo.controller.api.Resource;
import cz.cesnet.shongo.controller.api.request.*;
import cz.cesnet.shongo.controller.api.rpc.AbstractServiceImpl;
import cz.cesnet.shongo.controller.authorization.Authorization;
import cz.cesnet.shongo.controller.booking.ObjectIdentifier;
import cz.cesnet.shongo.controller.booking.resource.*;
import cz.cesnet.shongo.controller.util.NativeQuery;
import cz.cesnet.shongo.controller.util.QueryFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.*;

/**
 * Domain service implementation only for controller.
 *
 * @author Ondrej Pavelka <pavelka@cesnet.cz>
 */
public class DomainService extends AbstractServiceImpl implements Component.EntityManagerFactoryAware {
    private static Logger logger = LoggerFactory.getLogger(DomainService.class);

//    /**
//     * @see cz.cesnet.shongo.controller.cache.Cache
//     */
//    private Cache cache;

    /**
     * @see javax.persistence.EntityManagerFactory
     */
    private EntityManagerFactory entityManagerFactory;

    /**
     * @see cz.cesnet.shongo.controller.authorization.Authorization
     */
    private Authorization authorization;

//    /**
//     * Constructor.
//     *
//     * @param cache sets the {@link #cache}
//     */
//    public DomainService(Cache cache)
//    {
//        this.cache = cache;
//    }


    public DomainService(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

//    @Override
//    public void setAuthorization(Authorization authorization)
//    {
//        this.authorization = authorization;
//    }

    @Override
    public void init(ControllerConfiguration configuration) {
//        checkDependency(cache, Cache.class);
        checkDependency(entityManagerFactory, EntityManagerFactory.class);
//        checkDependency(authorization, Authorization.class);
        //TODO: overit zda tu configuration potrebuji
        super.init(configuration);
    }

//    @Override
//    public String getServiceName()
//    {
//        return "Resource";
//    }

    public List<Domain> listDomains() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        ResourceManager resourceManager = new ResourceManager(entityManager);
        try {
            List<Domain> domainList = new ArrayList<Domain>();
            domainList.add(LocalDomain.getLocalDomain().toApi());
            for (cz.cesnet.shongo.controller.booking.domain.Domain domain : resourceManager.listAllDomains()) {
                domainList.add(domain.toApi());
            }
            return Collections.unmodifiableList(domainList);
        }
        finally {
            entityManager.close();
        }
    }

    public ListResponse<ResourceSummary> listResources(ResourceListRequest request) {
        checkNotNull("request", request);
        SecurityToken securityToken = request.getSecurityToken();
        authorization.validate(securityToken);

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        ResourceManager resourceManager = new ResourceManager(entityManager);
        try {
            Set<Long> readableResourceIds = authorization.getEntitiesWithPermission(securityToken,
                    cz.cesnet.shongo.controller.booking.resource.Resource.class, ObjectPermission.READ);
            if (readableResourceIds != null) {
                // Allow to add/delete items in the set
                readableResourceIds = new HashSet<Long>(readableResourceIds);
            }

            QueryFilter queryFilter = new QueryFilter("resource_summary", true);
            queryFilter.addFilterIn("id", readableResourceIds);

            // Filter requested resource-ids
            if (request.getResourceIds().size() > 0) {
                Set<Long> requestedResourceIds = new HashSet<Long>();
                Set<Long> notReadableResourceIds = new HashSet<Long>();
                for (String resourceApiId : request.getResourceIds()) {
                    Long resourceId = ObjectIdentifier.parseId(resourceApiId, ObjectType.RESOURCE);
                    if (readableResourceIds != null && !readableResourceIds.contains(resourceId)) {
                        notReadableResourceIds.add(resourceId);
                    }
                    requestedResourceIds.add(resourceId);
                }
                queryFilter.addFilter("id IN(:resourceIds)");
                queryFilter.addFilterParameter("resourceIds", requestedResourceIds);

                // Check if user has any reservations for not readable resources for them to become readable
                if (notReadableResourceIds.size() > 0) {
                    Set<Long> readableReservationIds = authorization.getEntitiesWithPermission(securityToken,
                            cz.cesnet.shongo.controller.booking.reservation.Reservation.class, ObjectPermission.READ);
                    List readableResources = entityManager.createNativeQuery("SELECT resource_reservation.resource_id"
                            + " FROM resource_reservation"
                            + " WHERE resource_reservation.id IN (:reservationIds)"
                            + " AND resource_reservation.resource_id IN(:resourceIds)")
                            .setParameter("reservationIds", readableReservationIds)
                            .setParameter("resourceIds", notReadableResourceIds)
                            .getResultList();
                    for (Object readableResourceId : readableResources) {
                        readableReservationIds.add(((Number) readableResourceId).longValue());
                    }
                }
            }

            // Filter requested tag-id
            if (request.getTagId() != null) {
                queryFilter.addFilter("resource_summary.id IN ("
                        + " SELECT resource_id FROM resource_tag "
                        + " WHERE tag_id = :tagId)", "tagId", ObjectIdentifier.parseId(request.getTagId(), ObjectType.TAG));
            }

            // Filter requested tag-name
            if (request.getTagName() != null) {
                queryFilter.addFilter("resource_summary.id IN ("
                        + " SELECT resource_tag.resource_id FROM resource_tag "
                        + " LEFT JOIN tag ON tag.id = resource_tag.tag_id"
                        + " WHERE tag.name = :tagName)", "tagName", request.getTagName());
            }

            // Filter requested by foreign domain
            if (request.getDomainId() != null) {
                queryFilter.addFilter("resource_summary.id IN ("
                        + " SELECT resource_id FROM domain_resource "
                        + " WHERE domain_id = :domainId)", "domainId", ObjectIdentifier.parseId(request.getDomainId(), ObjectType.DOMAIN));
            }

            // Filter user-ids
            Set<String> userIds = request.getUserIds();
            if (userIds != null && !userIds.isEmpty()) {
                queryFilter.addFilterIn("resource_summary.user_id", userIds);
            }

            // Filter name
            if (request.getName() != null) {
                queryFilter.addFilter("resource_summary.name = :name", "name", request.getName());
            }

            // Capability type
            if (request.getCapabilityClasses().size() > 0) {
                StringBuilder capabilityClassFilter = new StringBuilder();
                for (Class<? extends Capability> capabilityClass : request.getCapabilityClasses()) {
                    if (capabilityClassFilter.length() > 0) {
                        capabilityClassFilter.append(" OR ");
                    }
                    if (capabilityClass.equals(cz.cesnet.shongo.controller.api.RoomProviderCapability.class)) {
                        capabilityClassFilter.append("resource_summary.id IN ("
                                + " SELECT capability.resource_id FROM room_provider_capability"
                                + " LEFT JOIN capability ON capability.id = room_provider_capability.id)");
                    } else if (capabilityClass.equals(RecordingCapability.class)) {
                        capabilityClassFilter.append("resource_summary.id IN ("
                                + " SELECT capability.resource_id FROM recording_capability"
                                + " LEFT JOIN capability ON capability.id = recording_capability.id)");
                    } else {
                        throw new TodoImplementException(capabilityClass);
                    }
                }
                queryFilter.addFilter(capabilityClassFilter.toString());
            }


            // Technologies
            Set<Technology> technologies = request.getTechnologies();
            if (technologies.size() > 0) {
                queryFilter.addFilter("resource_summary.id IN ("
                        + " SELECT device_resource.id FROM device_resource "
                        + " LEFT JOIN device_resource_technologies ON device_resource_technologies.device_resource_id = device_resource.id"
                        + " WHERE device_resource_technologies.technologies IN(:technologies))");
                queryFilter.addFilterParameter("technologies", technologies);
            }

            // Allocatable
            if (request.isAllocatable()) {
                queryFilter.addFilter("resource_summary.allocatable = TRUE");
            }

            // Query order by
            String queryOrderBy =
                    "resource_summary.allocatable DESC, resource_summary.allocation_order, resource_summary.id";

            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("filter", queryFilter.toQueryWhere());
            parameters.put("order", queryOrderBy);
            String query = NativeQuery.getNativeQuery(NativeQuery.RESOURCE_LIST, parameters);

            ListResponse<ResourceSummary> response = new ListResponse<ResourceSummary>();
            List<Object[]> records = performNativeListRequest(query, queryFilter, request, response, entityManager);
            for (Object[] record : records) {
                ResourceSummary resourceSummary = new ResourceSummary();
                resourceSummary.setId(ObjectIdentifier.formatId(ObjectType.RESOURCE, record[0].toString()));
                if (record[1] != null) {
                    resourceSummary.setParentResourceId(
                            ObjectIdentifier.formatId(ObjectType.RESOURCE, record[1].toString()));
                }
                resourceSummary.setUserId(record[2].toString());
                resourceSummary.setName(record[3].toString());
                resourceSummary.setAllocatable(record[4] != null && (Boolean) record[4]);
                resourceSummary.setAllocationOrder(record[5] != null ? (Integer) record[5] : null);
                resourceSummary.setDescription(record[7] != null ? record[7].toString() : "");
                if (record[6] != null) {
                    String recordTechnologies = record[6].toString();
                    if (!recordTechnologies.isEmpty()) {
                        for (String technology : recordTechnologies.split(",")) {
                            resourceSummary.addTechnology(Technology.valueOf(technology.trim()));
                        }
                    }
                }
                resourceSummary.setCalendarPublic((Boolean) record[8]);
                if (resourceSummary.isCalendarPublic()) {
                    resourceSummary.setCalendarUriKey(record[9].toString());
                }
                response.addItem(resourceSummary);
            }
            return response;
        } finally {
            entityManager.close();
        }
    }

    public Resource getResource(SecurityToken securityToken, String resourceId) {
        authorization.validate(securityToken);
        checkNotNull("resourceId", resourceId);

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        ResourceManager resourceManager = new ResourceManager(entityManager);
        ObjectIdentifier objectId = ObjectIdentifier.parse(resourceId, ObjectType.RESOURCE);
        try {
            cz.cesnet.shongo.controller.booking.resource.Resource resource = resourceManager.get(
                    objectId.getPersistenceId());

            if (!authorization.hasObjectPermission(securityToken, resource, ObjectPermission.READ)) {
                ControllerReportSetHelper.throwSecurityNotAuthorizedFault("read resource %s", objectId);
            }

            return resource.toApi(entityManager);
        } finally {
            entityManager.close();
        }
    }

    public List<DomainResource> listDomainResources(DomainListRequest request) {
        throw new TodoImplementException("listDomainResources");
//        checkNotNull("request", request);
//        SecurityToken securityToken = request.getSecurityToken();
//        authorization.validate(securityToken);
//
//        EntityManager entityManager = entityManagerFactory.createEntityManager();
//        ResourceManager resourceManager = new ResourceManager(entityManager);
//        try {
//            List<Tag> tagList = new ArrayList();
//            List<ResourceTag> resourceTags;
//            if (request.getResourceId() == null) {
//                for (cz.cesnet.shongo.controller.booking.resource.Tag tag : resourceManager.listAllTags()) {
//                    tagList.add(tag.toApi());
//                }
//            } else {
//                Long persistenceResourceId = ObjectIdentifier.parseId(request.getResourceId(),ObjectType.RESOURCE);
//                resourceTags = resourceManager.getResourceTags(persistenceResourceId);
//
//                for (ResourceTag resourceTag : resourceTags) {
//                    Tag tag = resourceTag.getTag().toApi();
//
//                    tagList.add(tag);
//                }
//            }
//
//            return tagList;
//        }
//        finally {
//            entityManager.close();
//        }
    }

    public cz.cesnet.shongo.controller.api.Domain getDomain(SecurityToken token, String domainId) {
        checkNotNull("domain-id", domainId);
        authorization.validate(token);

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        ResourceManager resourceManager = new ResourceManager(entityManager);
        try {
            cz.cesnet.shongo.controller.booking.domain.Domain domain = resourceManager.getDomain(
                    ObjectIdentifier.parseId(domainId, ObjectType.DOMAIN));

            if (!authorization.hasObjectPermission(token, domain, ObjectPermission.READ)) {
                ControllerReportSetHelper.throwSecurityNotAuthorizedFault("read domain %s", domainId);
            }

            return domain.toApi();
        } finally {
            entityManager.close();
        }
    }
}