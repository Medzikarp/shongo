package cz.cesnet.shongo.client.web.controllers;

import cz.cesnet.shongo.client.web.*;
import cz.cesnet.shongo.client.web.models.ReservationRequestModel;
import cz.cesnet.shongo.controller.api.AbstractReservationRequest;
import cz.cesnet.shongo.controller.api.ReservationRequestSummary;
import cz.cesnet.shongo.controller.api.SecurityToken;
import cz.cesnet.shongo.controller.api.request.ListResponse;
import cz.cesnet.shongo.controller.api.request.ReservationRequestListRequest;
import cz.cesnet.shongo.controller.api.rpc.ReservationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.List;

/**
 * Controller for managing reservation requests.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
@Controller
public class ReservationRequestDeleteController implements BreadcrumbProvider
{
    @Resource
    private ReservationService reservationService;

    /**
     * {@link Breadcrumb} for the {@link #handleDeleteView}
     */
    private Breadcrumb breadcrumb;

    @Override
    public Breadcrumb createBreadcrumb(NavigationPage navigationPage, String requestURI)
    {
        if (navigationPage == null) {
            return null;
        }
        if (ClientWebNavigation.RESERVATION_REQUEST_DELETE.isNavigationPage(navigationPage)) {
            breadcrumb = new Breadcrumb(navigationPage, requestURI);
            return breadcrumb;
        }
        return new Breadcrumb(navigationPage, requestURI);
    }

    /**
     * Handle deletion of reservation request view.
     */
    @RequestMapping(value = ClientWebUrl.RESERVATION_REQUEST_DELETE, method = RequestMethod.GET)
    public String handleDeleteView(
            SecurityToken securityToken,
            @PathVariable(value = "reservationRequestId") String reservationRequestId,
            Model model)
    {
        AbstractReservationRequest reservationRequest =
                reservationService.getReservationRequest(securityToken, reservationRequestId);
        List<ReservationRequestSummary> dependencies =
                ReservationRequestModel.getDeleteDependencies(reservationRequestId, reservationService, securityToken);
        model.addAttribute("reservationRequest", reservationRequest);
        model.addAttribute("dependencies", dependencies);

        // Initialize breadcrumb
        ReservationRequestModel reservationRequestModel = new ReservationRequestModel(reservationRequest, null);
        if (breadcrumb != null) {
            breadcrumb.addItems(breadcrumb.getItemsCount() - 1,
                    reservationRequestModel.getBreadcrumbItems(ClientWebUrl.RESERVATION_REQUEST_DETAIL));

            // Set back url
            model.addAttribute("backUrl", breadcrumb.getBackUrl());
        }

        return "reservationRequestDelete";
    }

    /**
     * Handle confirmation for deletion of reservation request.
     */
    @RequestMapping(value = ClientWebUrl.RESERVATION_REQUEST_DELETE_CONFIRM, method = RequestMethod.GET)
    public String handleDeleteConfirm(
            SecurityToken securityToken,
            @RequestParam(value = "dependencies", required = false, defaultValue = "false") boolean dependencies,
            @PathVariable(value = "reservationRequestId") String reservationRequestId)
    {
        if (dependencies) {
            List<ReservationRequestSummary> reservationRequestDependencies =
                    ReservationRequestModel.getDeleteDependencies(
                            reservationRequestId, reservationService, securityToken);
            for (ReservationRequestSummary reservationRequestSummary : reservationRequestDependencies) {
                reservationService.deleteReservationRequest(securityToken, reservationRequestSummary.getId());
            }
        }
        reservationService.deleteReservationRequest(securityToken, reservationRequestId);
        return "redirect:" + ClientWebUrl.RESERVATION_REQUEST_LIST;
    }
}
