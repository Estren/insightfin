package com.orizon.coreapi.adapter.in.web;

import com.orizon.coreapi.adapter.in.web.dto.NotificationResponse;
import com.orizon.coreapi.adapter.in.web.dto.UnreadCountResponse;
import com.orizon.coreapi.adapter.in.web.mapper.WebMapper;
import com.orizon.coreapi.config.security.AuthenticatedUser;
import com.orizon.coreapi.domain.port.in.GetUnreadCountsUseCase;
import com.orizon.coreapi.domain.port.in.ListNotificationsUseCase;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/api/notifications")
@Produces(MediaType.APPLICATION_JSON)
public class NotificationController {

    @Inject
    ListNotificationsUseCase listNotificationsUseCase;

    @Inject
    GetUnreadCountsUseCase getUnreadCountsUseCase;

    @Inject
    AuthenticatedUser authenticatedUser;

    @GET
    public List<NotificationResponse> list() {
        return listNotificationsUseCase.list(authenticatedUser.getUserId())
                .stream()
                .map(WebMapper::toResponse)
                .toList();
    }

    @GET
    @Path("/unread-count")
    public UnreadCountResponse unreadCount() {
        return WebMapper.toResponse(getUnreadCountsUseCase.count(authenticatedUser.getUserId()));
    }
}
