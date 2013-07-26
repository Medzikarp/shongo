<%--
  -- Page for displaying details about a single reservation request.
  --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@ taglib prefix="tag" uri="/WEB-INF/client-web.tld" %>

<c:set var="contextPath" value="${pageContext.request.contextPath}"/>
<spring:eval expression="T(cz.cesnet.shongo.client.web.ClientWebUrl).getReservationRequestDetail(contextPath, reservationRequestId)" var="urlDetail"/>

<script type="text/javascript">
    angular.module('jsp:room', ['ngTooltip']);
</script>

<div ng-app="jsp:room">

    <dl class="dl-horizontal">

        <dt><spring:message code="views.reservationRequest"/>:</dt>
        <dd><a href="${urlDetail}">${reservationRequestId}</a></dd>

        <dt><spring:message code="views.room.identifier"/>:</dt>
        <dd>${executable.id}</dd>

        <dt><spring:message code="views.reservationRequest.slot"/>:</dt>
        <dd>
            <joda:format value="${executable.slot.start}" style="MM"/>
            <br/>
            <joda:format value="${executable.slot.end}" style="MM"/>
        </dd>

        <dt><spring:message code="views.room.state"/>:</dt>
        <dd class="executable-state">
            <spring:message code="views.reservationRequest.executableState.${executable.state}" var="executableState"/>
            <tag:help label="${executableState}" labelClass="${executable.state}">
                <span>
                    <spring:message code="views.help.reservationRequest.executableState.${executable.state}"/>
                </span>
                <c:if test="${!executable.state.available && not empty executable.stateReport}">
                    <pre>${executable.stateReport}</pre>
                </c:if>
            </tag:help>
        </dd>

        <c:if test="${room != null}">
            <dt><spring:message code="views.room.licenseCount"/>:</dt>
            <dd>
                ${room.licenseCount}
                <c:if test="${room.licenseCount == 0}">
                    <spring:eval var="createPermanentRoomCapacityUrl"
                                 expression="T(cz.cesnet.shongo.client.web.ClientWebUrl).getReservationRequestCreatePermanentRoomCapacity(contextPath, reservationRequestId)"/>
                    (<spring:message code="views.room.licenseCount.none" arguments="${createPermanentRoomCapacityUrl}"/>)
                </c:if>
            </dd>
        </c:if>

        <dt><spring:message code="views.room.aliases"/>:</dt>
        <dd>
            <tag:help label="${roomAliases}">
                <c:if test="${not empty roomAliasesDescription}">
                    ${roomAliasesDescription}
                </c:if>
            </tag:help>
        </dd>

    </dl>

    <c:if test="${notAvailable}">
        <div class="not-available">
            <h2><spring:message code="views.room.notAvailable.heading"/></h2>
            <p><spring:message code="views.room.notAvailable.text" arguments="${configuration.contactEmail}"/></p>
        </div>
    </c:if>

    <c:if test="${participants != null}">
        <h2><spring:message code="views.room.participants"/></h2>
        <table class="table table-striped table-hover">
            <thead>
            <tr>
                <th><spring:message code="views.room.participant.name"/></th>
                <th><spring:message code="views.room.participant.email"/></th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${participants}" var="participant" varStatus="status">
                <tr>
                    <td>${participant.name}</td>
                    <td>
                        ${participant.user.primaryEmail}
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${participants.isEmpty()}">
                <tr>
                    <td colspan="2" class="empty"><spring:message code="views.list.none"/></td>
                </tr>
            </c:if>
            </tbody>
        </table>
    </c:if>

    <c:if test="${recordings != null}">
        <h2><spring:message code="views.room.recordings"/></h2>
        <table class="table table-striped table-hover">
            <thead>
            <tr>
                <th><spring:message code="views.room.recording.url"/></th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${recordings}" var="recording" varStatus="status">
                <tr>
                    <td>
                        <a href="${recording}" target="_blank">${recording}</a>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${recordings.isEmpty()}">
                <tr>
                    <td colspan="2" class="empty"><spring:message code="views.list.none"/></td>
                </tr>
            </c:if>
            </tbody>
        </table>
    </c:if>
</div>

<div class="pull-right">
    <a class="btn" href="javascript: location.reload();">
        <spring:message code="views.button.refresh"/>
    </a>
</div>