package cz.cesnet.shongo.client.web.controllers;

import cz.cesnet.shongo.api.Alias;
import cz.cesnet.shongo.api.UserInformation;
import cz.cesnet.shongo.client.web.Cache;
import cz.cesnet.shongo.client.web.CacheProvider;
import cz.cesnet.shongo.client.web.ClientWebUrl;
import cz.cesnet.shongo.client.web.models.ReservationRequestModel;
import cz.cesnet.shongo.controller.api.*;
import cz.cesnet.shongo.controller.api.request.*;
import cz.cesnet.shongo.controller.api.rpc.ReservationService;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * Controller for displaying detail of reservation requests.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
@Controller
public class ReservationRequestDetailController
{
    @Resource
    private ReservationService reservationService;

    @Resource
    private Cache cache;

    @Resource
    private MessageSource messageSource;

    /**
     * Handle detail of reservation request view.
     */
    @RequestMapping(value = ClientWebUrl.RESERVATION_REQUEST_DETAIL, method = RequestMethod.GET)
    public String handleDetailView(
            Locale locale,
            SecurityToken securityToken,
            @PathVariable(value = "reservationRequestId") String id,
            Model model)
    {
        // Get reservation request
        AbstractReservationRequest abstractReservationRequest =
                reservationService.getReservationRequest(securityToken, id);

        // Check if it is single reservation request
        ReservationRequestModel reservationRequestModel =
                new ReservationRequestModel(abstractReservationRequest, new CacheProvider(cache, securityToken));

        // Reservation request is active (e.g., it can be modified and deleted)
        boolean isActive = true;
        // Reservation request has visible allocated reservation
        boolean hasVisibleReservation = true;

        // Get history of reservation request (only if it is not child reservation request)
        if (reservationRequestModel.getParentReservationRequestId() == null) {
            ReservationRequestListRequest request = new ReservationRequestListRequest();
            request.setSecurityToken(securityToken);
            request.setHistoryReservationRequestId(id);
            request.setSort(ReservationRequestListRequest.Sort.DATETIME);
            request.setSortDescending(true);
            String reservationRequestId = abstractReservationRequest.getId();
            Map<String, Object> currentHistoryItem = null;
            List<Map<String, Object>> historyItems = new LinkedList<Map<String, Object>>();
            for (ReservationRequestSummary historyItem : reservationService.listReservationRequests(request)) {
                Map<String, Object> item = new HashMap<String, Object>();
                String historyItemId = historyItem.getId();
                item.put("id", historyItemId);
                item.put("dateTime", historyItem.getDateTime());
                UserInformation user = cache.getUserInformation(securityToken, historyItem.getUserId());
                item.put("user", user.getFullName());
                item.put("type", historyItem.getType());

                AllocationState allocationState = historyItem.getAllocationState();
                String allocationStateMessage = messageSource.getMessage(
                        "views.reservationRequest.allocationState." + allocationState, null, locale);
                item.put("allocationState", allocationState);
                item.put("allocationStateMessage", allocationStateMessage);

                if (historyItemId.equals(reservationRequestId)) {
                    currentHistoryItem = item;
                }

                // Reservation is visible only for reservation requests until first ALLOCATED reservation request
                if (allocationState.equals(AllocationState.ALLOCATED) && currentHistoryItem == null) {
                    hasVisibleReservation = false;
                }

                historyItems.add(item);
            }
            if (currentHistoryItem == null) {
                throw new RuntimeException(
                        "Reservation request " + reservationRequestId + " should exist in it's history.");
            }
            currentHistoryItem.put("selected", true);

            model.addAttribute("history", historyItems);
            isActive = currentHistoryItem == historyItems.get(0);
        }
        else {
            // Child reservation requests don't have history
            // and thus they are automatically active and have visible reservation
        }

        // Get reservation for the reservation request
        if (hasVisibleReservation && abstractReservationRequest instanceof ReservationRequest) {
            ReservationRequest reservationRequest = (ReservationRequest) abstractReservationRequest;
            String reservationId = reservationRequest.getLastReservationId();
            if (reservationId != null) {
                Reservation reservation = reservationService.getReservation(securityToken, reservationId);
                model.addAttribute("reservation", ReservationRequestModel.getReservationModel(
                        reservation, messageSource, locale));
            }
        }

        model.addAttribute("reservationRequest", reservationRequestModel);
        model.addAttribute("isActive", isActive);
        return "reservationRequestDetail";
    }

    /**
     * Handle data request for children of reservation request.
     */
    @RequestMapping(value = ClientWebUrl.RESERVATION_REQUEST_DETAIL_CHILDREN, method = RequestMethod.GET)
    @ResponseBody
    public Map handleDetailChildren(
            Locale locale,
            SecurityToken securityToken,
            @PathVariable(value = "reservationRequestId") String reservationRequestId,
            @RequestParam(value = "start", required = false) Integer start,
            @RequestParam(value = "count", required = false) Integer count)
    {
        // List reservation requests
        ChildReservationRequestListRequest request = new ChildReservationRequestListRequest();
        request.setSecurityToken(securityToken);
        request.setStart(start);
        request.setCount(count);
        request.setReservationRequestId(reservationRequestId);
        ListResponse<ReservationRequest> response = reservationService.listChildReservationRequests(request);

        ReservationListRequest reservationListRequest = new ReservationListRequest();
        reservationListRequest.setSecurityToken(securityToken);
        for (ReservationRequest reservationRequest : response.getItems()) {
            String reservationId = reservationRequest.getLastReservationId();
            if (reservationId != null) {
                reservationListRequest.addReservationId(reservationId);
            }
        }
        Map<String, Reservation> reservationById = new HashMap<String, Reservation>();
        if (reservationListRequest.getReservationIds().size() > 0) {
            ListResponse<Reservation> reservations = reservationService.listReservations(reservationListRequest);
            for (Reservation reservation : reservations) {
                reservationById.put(reservation.getId(), reservation);
            }
        }

        // Build response
        DateTimeFormatter dateTimeFormatter = ReservationRequestModel.DATE_TIME_FORMATTER.withLocale(locale);
        List<Map<String, Object>> children = new LinkedList<Map<String, Object>>();
        for (ReservationRequest reservationRequest : response.getItems()) {
            Map<String, Object> child = new HashMap<String, Object>();
            child.put("id", reservationRequest.getId());

            Interval slot = reservationRequest.getSlot();
            child.put("slot", dateTimeFormatter.print(slot.getStart()) + " - " +
                    dateTimeFormatter.print(slot.getEnd()));

            AllocationState allocationState = reservationRequest.getAllocationState();
            if (allocationState != null) {
                String allocationStateMessage = messageSource.getMessage(
                        "views.reservationRequest.allocationState." + allocationState, null, locale);
                String allocationStateHelp = messageSource.getMessage(
                        "views.help.reservationRequest.allocationState." + allocationState, null, locale);
                child.put("allocationState", allocationState);
                child.put("allocationStateMessage", allocationStateMessage);
                child.put("allocationStateHelp", allocationStateHelp);
                child.put("allocationStateReport", getAllocationStateReport(reservationRequest));
            }

            String reservationId = reservationRequest.getLastReservationId();
            Reservation reservation = reservationById.get(reservationId);
            if (reservation != null) {
                // If reservation is not null it means that the reservation is allocated
                child.put("allocationState", AllocationState.ALLOCATED);

                // Reservation should contain allocated room
                RoomExecutable room = (RoomExecutable) reservation.getExecutable();
                if (room != null) {
                    child.put("roomId", room.getId());

                    // Set room state and report
                    Executable.State roomState = room.getState();
                    String roomStateMessage = messageSource.getMessage(
                            "views.reservationRequest.executableState." + roomState, null, locale);
                    String roomStateHelp = messageSource.getMessage(
                            "views.help.reservationRequest.executableState." + roomState, null, locale);
                    child.put("roomState", roomState);
                    child.put("roomStateMessage", roomStateMessage);
                    child.put("roomStateHelp", roomStateHelp);
                    child.put("roomStateAvailable", roomState.isAvailable());
                    switch (roomState) {
                        case STARTING_FAILED:
                        case STOPPING_FAILED:
                            child.put("roomStateReport", room.getStateReport());
                            break;
                    }

                    // Set room aliases
                    List<Alias> aliases = room.getAliases();
                    child.put("roomAliases", ReservationRequestModel.formatAliases(aliases, roomState));
                    child.put("roomAliasesDescription", ReservationRequestModel.formatAliasesDescription(
                            aliases, roomState, locale, messageSource));
                }
            }

            children.add(child);
        }
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("start", response.getStart());
        data.put("count", response.getCount());
        data.put("items", children);
        return data;
    }

    /**
     * Handle data request for usages of reservation request.
     */
    @RequestMapping(value = ClientWebUrl.RESERVATION_REQUEST_DETAIL_USAGES, method = RequestMethod.GET)
    @ResponseBody
    public Map handleDetailUsages(
            Locale locale,
            SecurityToken securityToken,
            @PathVariable(value = "reservationRequestId") String reservationRequestId,
            @RequestParam(value = "start", required = false) Integer start,
            @RequestParam(value = "count", required = false) Integer count)
    {
        // List reservation requests
        ReservationRequestListRequest request = new ReservationRequestListRequest();
        request.setSecurityToken(securityToken);
        request.setStart(start);
        request.setCount(count);
        request.setProvidedReservationRequestId(reservationRequestId);
        ListResponse<ReservationRequestSummary> response = reservationService.listReservationRequests(request);

        // Build response
        DateTimeFormatter dateTimeFormatter = ReservationRequestModel.DATE_TIME_FORMATTER.withLocale(locale);
        List<Map<String, Object>> usages = new LinkedList<Map<String, Object>>();
        for (ReservationRequestSummary reservationRequest : response.getItems()) {
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("id", reservationRequest.getId());
            item.put("description", reservationRequest.getDescription());
            item.put("purpose", reservationRequest.getPurpose());
            usages.add(item);

            AllocationState allocationState = reservationRequest.getAllocationState();
            if (allocationState != null) {
                String allocationStateMessage = messageSource.getMessage(
                        "views.reservationRequest.allocationState." + allocationState, null, locale);
                String allocationStateHelp = messageSource.getMessage(
                        "views.help.reservationRequest.allocationState." + allocationState, null, locale);
                item.put("allocationState", allocationState);
                item.put("allocationStateMessage", allocationStateMessage);
                item.put("allocationStateHelp", allocationStateHelp);
            }

            UserInformation user = cache.getUserInformation(securityToken, reservationRequest.getUserId());
            item.put("user", user.getFullName());

            Interval earliestSlot = reservationRequest.getEarliestSlot();
            if (earliestSlot != null) {
                item.put("slot", dateTimeFormatter.print(earliestSlot.getStart()) + " - " + dateTimeFormatter
                        .print(earliestSlot.getEnd()));
            }

            ReservationRequestSummary.RoomSpecification roomSpecification =
                    (ReservationRequestSummary.RoomSpecification) reservationRequest.getSpecification();
            if (roomSpecification != null) {
                item.put("roomParticipantCount", roomSpecification.getParticipantCount());
            }
        }
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("start", response.getStart());
        data.put("count", response.getCount());
        data.put("items", usages);
        return data;
    }

    /**
     * @param reservationRequest
     * @return allocation state report for given {@code reservationRequest}
     */
    private Object getAllocationStateReport(ReservationRequest reservationRequest)
    {
        switch (reservationRequest.getAllocationState()) {
            case ALLOCATION_FAILED:
                return reservationRequest.getAllocationStateReport();
        }
        return null;
    }
}