
<%--
  -- Page which is shown when the web client can't connect/send request to controller.
  --%>
<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=UTF-8"%>
<%@ page import="cz.cesnet.shongo.client.web.ClientWebUrl" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="tag" uri="/WEB-INF/client-web.tld" %>

<tag:url value="<%= ClientWebUrl.REPORT %>" var="reportUrl">
    <tag:param name="back-url" value="${requestScope.requestUrl}"/>
</tag:url>

<div class="not-available">
    <h2><spring:message code="views.controllerNotAvailable.heading"/></h2>
    <p><spring:message code="views.controllerNotAvailable.text" arguments="${reportUrl}"/></p>
</div>

