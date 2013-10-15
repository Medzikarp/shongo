<%--
  -- Page for displaying details about a single reservation request.
  --%>
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8"%>
<%@ page import="cz.cesnet.shongo.client.web.ClientWebUrl" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="tag" uri="/WEB-INF/client-web.tld" %>

<security:accesscontrollist hasPermission="WRITE" domainObject="${room}" var="isWritable"/>
<c:if test="${room.state == 'STOPPED'}">
    <c:set var="isWritable" value="false"/>
</c:if>

<tag:url var="reservationRequestDetailUrl" value="<%= ClientWebUrl.RESERVATION_REQUEST_DETAIL %>">
    <tag:param name="reservationRequestId" value="${reservationRequestId}"/>
    <tag:param name="back-url" value="${requestScope.requestUrl}"/>
</tag:url>

<script type="text/javascript">
    angular.module('jsp:room', ['ngTooltip']);

    function MoreDetailController($scope) {
        $scope.show = false;
    }
</script>

<h1>
<c:choose>
    <c:when test="${room.type == 'PERMANENT_ROOM'}">
        <spring:message code="views.room.heading" arguments="${room.name}"/>
    </c:when>
    <c:otherwise>
        <spring:message code="views.room.headingAdhoc"/>
    </c:otherwise>
</c:choose>
</h1>

<div ng-app="jsp:room">

    <%-- Detail of room --%>
    <dl class="dl-horizontal">

        <dt><spring:message code="views.room.technology"/>:</dt>
        <dd>${room.technology.title}</dd>

        <dt><spring:message code="views.room.name"/>:</dt>
        <dd>${room.name}</dd>

        <dt><spring:message code="views.room.slot"/>:</dt>
        <dd>
            <tag:format value="${room.slot}" multiline="true"/>
        </dd>

        <dt><spring:message code="views.room.state"/>:</dt>
        <dd class="room-state">
            <spring:message code="views.executable.roomState.${room.type}.${room.state}" var="roomStateLabel"/>
            <spring:message code="views.executable.roomStateHelp.${room.type}.${room.state}" var="roomStateHelp"/>
            <tag:help label="${roomStateLabel}" labelClass="${room.state}">
                <span>${roomStateHelp}</span>
                <c:if test="${not empty room.stateReport}">
                    <pre>${room.stateReport}</pre>
                </c:if>
            </tag:help>
        </dd>

        <c:if test="${room.state.available}">
            <dt><spring:message code="views.room.licenseCount"/>:</dt>
            <dd>
                ${room.licenseCount}
                <c:if test="${room.licenseCountUntil != null}">
                    (<spring:message code="views.room.licenseCountUntil"/>
                    <tag:format value="${room.licenseCountUntil}"/>)
                </c:if>
            </dd>
        </c:if>

        <dt><spring:message code="views.room.aliases"/>:</dt>
        <dd>
            <tag:help label="${room.aliases}">
                <c:set value="${room.aliasesDescription}" var="roomAliasesDescription"/>
                <c:if test="${not empty roomAliasesDescription}">
                    ${roomAliasesDescription}
                </c:if>
            </tag:help>
        </dd>

        <div ng-controller="MoreDetailController">

            <div ng-show="show">

                <hr/>

                <dt><spring:message code="views.room.identifier"/>:</dt>
                <dd>${room.id}</dd>

                <dt><spring:message code="views.reservationRequest"/>:</dt>
                <dd><a href="${reservationRequestDetailUrl}">${reservationRequestId}</a></dd>
            </div>

            <dt></dt>
            <dd>
                <a href="" ng-click="show = true" ng-show="!show"><spring:message code="views.button.showMoreDetail"/></a>
                <a href="" ng-click="show = false" ng-show="show"><spring:message code="views.button.hideMoreDetail"/></a>
            </dd>

        </div>

    </dl>

    <%-- Allowed Participants --%>
    <c:if test="${room.technology == 'ADOBE_CONNECT'}">
        <h2><spring:message code="views.room.participants"/></h2>
        <p><spring:message code="views.room.participants.help"/></p>
        <tag:url var="participantModifyUrl" value="<%= ClientWebUrl.ROOM_PARTICIPANT_MODIFY %>">
            <tag:param name="back-url" value="${requestUrl}"/>
        </tag:url>
        <tag:url var="participantDeleteUrl" value="<%= ClientWebUrl.ROOM_PARTICIPANT_DELETE %>">
            <tag:param name="back-url" value="${requestUrl}"/>
        </tag:url>
        <tag:participantList isWritable="${isWritable}" data="${room.participants}" description="${not empty room.usageId}"
                             modifyUrl="${participantModifyUrl}" deleteUrl="${participantDeleteUrl}"
                             urlParam="roomId" urlValue="roomId"/>
        <c:if test="${isWritable}">
            <tag:url var="participantCreateUrl" value="<%= ClientWebUrl.ROOM_PARTICIPANT_CREATE %>">
                <tag:param name="roomId" value="${room.id}"/>
                <tag:param name="back-url" value="${requestUrl}"/>
            </tag:url>
            <a class="btn btn-primary" href="${participantCreateUrl}">
                <spring:message code="views.button.add"/>
                <c:if test="${not empty room.usageId}">
                    (<spring:message code="views.room.participants.addRoom"/>)
                </c:if>
            </a>
            <c:if test="${not empty room.usageId}">
                <tag:url var="participantCreateUrl" value="<%= ClientWebUrl.ROOM_PARTICIPANT_CREATE %>">
                    <tag:param name="roomId" value="${room.usageId}"/>
                    <tag:param name="back-url" value="${requestUrl}"/>
                </tag:url>
                <a class="btn btn-primary" href="${participantCreateUrl}">
                    <spring:message code="views.button.add"/>
                    (<spring:message code="views.room.participants.addUsage"/>)
                </a>
            </c:if>
        </c:if>
    </c:if>

    <%-- Runtime management - Not-Available --%>
    <c:if test="${roomNotAvailable}">
        <tag:url value="<%= ClientWebUrl.REPORT %>" var="reportUrl">
            <tag:param name="back-url" value="${requestScope.requestUrl}"/>
        </tag:url>

        <div class="not-available">
            <h2><spring:message code="views.room.notAvailable.heading"/></h2>
            <p><spring:message code="views.room.notAvailable.text" arguments="${reportUrl}"/></p>
        </div>
    </c:if>

    <%-- Runtime management - Current Participants --%>
    <c:if test="${roomParticipants != null}">
        <h2><spring:message code="views.room.currentParticipants"/></h2>
        <p><spring:message code="views.room.currentParticipants.help"/></p>
        <table class="table table-striped table-hover">
            <thead>
            <tr>
                <th><spring:message code="views.room.currentParticipant.name"/></th>
                <th><spring:message code="views.room.currentParticipant.email"/></th>
                <th style="min-width: 85px; width: 85px;"><spring:message code="views.list.action"/></th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${roomParticipants}" var="participant" varStatus="status">
                <c:set var="user" value="${participant.userId != null ? cacheProvider.getUserInformation(participant.userId): null}"/>
                <tr>
                    <td>
                        ${user != null ? user.fullName : participant.displayName}
                    </td>
                    <td>
                        ${user.primaryEmail}
                    </td>
                    <td>
                        <c:if test="${participant.audioMuted != null}">
                            <tag:url var="toggleParticipantAudioMutedUrl" value="<%= ClientWebUrl.ROOM_MANAGEMENT_PARTICIPANT_TOGGLE_AUDIO_MUTED %>">
                                <tag:param name="roomId" value="${room.id}"/>
                                <tag:param name="participantId" value="${participant.id}"/>
                            </tag:url>
                            <spring:message var="toggleParticipantAudioMutedTitle" code="views.room.currentParticipant.audioMuted.${participant.audioMuted ? 'enable' : 'disable'}"/>
                            <a href="${toggleParticipantAudioMutedUrl}" title="${toggleParticipantAudioMutedTitle}"><i class="icon-volume-${participant.audioMuted ? "off" : "up"}"></i></a>&nbsp;
                        </c:if>
                        <c:if test="${participant.videoMuted != null}">
                            <tag:url var="toggleParticipantVideoMutedUrl" value="<%= ClientWebUrl.ROOM_MANAGEMENT_PARTICIPANT_TOGGLE_VIDEO_MUTED %>">
                                <tag:param name="roomId" value="${room.id}"/>
                                <tag:param name="participantId" value="${participant.id}"/>
                            </tag:url>
                            <spring:message var="toggleParticipantVideoMutedTitle" code="views.room.currentParticipant.videoMuted.${participant.videoMuted ? 'enable' : 'disable'}"/>
                            <a href="${toggleParticipantVideoMutedUrl}" title="${toggleParticipantVideoMutedTitle}"><i class="icon-eye-${participant.videoMuted ? "close" : "open"}"></i></a>&nbsp;
                        </c:if>
                        <tag:url var="disconnectParticipantUrl" value="<%= ClientWebUrl.ROOM_MANAGEMENT_PARTICIPANT_DISCONNECT %>">
                            <tag:param name="roomId" value="${room.id}"/>
                            <tag:param name="participantId" value="${participant.id}"/>
                        </tag:url>
                        <tag:listAction code="disconnect" url="${disconnectParticipantUrl}"/>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${roomParticipants.isEmpty()}">
                <tr>
                    <td colspan="3" class="empty"><spring:message code="views.list.none"/></td>
                </tr>
            </c:if>
            </tbody>
        </table>
    </c:if>

    <%-- Runtime management - Recordings --%>
    <c:if test="${roomRecordings != null}">
        <h2><spring:message code="views.room.recordings"/></h2>
        <table class="table table-striped table-hover">
            <thead>
            <tr>
                <th><spring:message code="views.room.recording.name"/></th>
                <th><spring:message code="views.room.recording.uploaded"/></th>
                <th><spring:message code="views.room.recording.duration"/></th>
                <th>
                    <c:choose>
                        <c:when test="${isWritable}">
                            <spring:message code="views.room.recording.editableUrl"/>
                        </c:when>
                        <c:otherwise>
                            <spring:message code="views.room.recording.url"/>
                        </c:otherwise>
                    </c:choose>
                </th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${roomRecordings}" var="recording" varStatus="status">
                <tr>
                    <td>
                        <c:choose>
                            <c:when test="${not empty recording.description}">
                                <tag:help label="${recording.name}">
                                    <strong><spring:message code="views.room.recording.description"/>:</strong>
                                    ${recording.description}
                                </tag:help>
                            </c:when>
                            <c:otherwise>
                                ${recording.name}
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <tag:format value="${recording.beginDate}"/>
                    </td>
                    <td>
                        <tag:format value="${recording.duration}" style="time"/>
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${isWritable}">
                                <a href="${recording.editableUrl}" target="_blank">${recording.editableUrl}</a>
                            </c:when>
                            <c:otherwise>
                                <a href="${recording.url}" target="_blank">${recording.url}</a>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${roomRecordings.isEmpty()}">
                <tr>
                    <td colspan="4" class="empty"><spring:message code="views.list.none"/></td>
                </tr>
            </c:if>
            </tbody>
        </table>
    </c:if>
</div>

<div class="pull-right">
    <c:if test="${room.state.started && room.licenseCount == 0}">
        <tag:url var="createPermanentRoomCapacityUrl" value="<%= ClientWebUrl.RESERVATION_REQUEST_CREATE %>">
            <tag:param name="specificationType" value="PERMANENT_ROOM_CAPACITY"/>
            <tag:param name="permanentRoom" value="${room.id}"/>
            <tag:param name="back-url" value="${requestScope.requestUrl}"/>
        </tag:url>
        <a class="btn btn-primary" href="${createPermanentRoomCapacityUrl}">
            <spring:message code="views.room.requestCapacity"/>
        </a>
    </c:if>
    <a class="btn" href="javascript: location.reload();">
        <spring:message code="views.button.refresh"/>
    </a>
</div>