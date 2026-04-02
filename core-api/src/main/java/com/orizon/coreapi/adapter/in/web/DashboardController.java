package com.orizon.coreapi.adapter.in.web;

import com.orizon.coreapi.adapter.in.web.dto.DashboardResponse;
import com.orizon.coreapi.adapter.in.web.mapper.WebMapper;
import com.orizon.coreapi.config.security.AuthenticatedUser;
import com.orizon.coreapi.domain.port.in.GetDashboardUseCase;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/api/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DashboardController {

    @Inject
    GetDashboardUseCase getDashboardUseCase;

    @Inject
    AuthenticatedUser authenticatedUser;

    @GET
    public DashboardResponse get(@QueryParam("month") String month) {
        var summary = getDashboardUseCase.execute(authenticatedUser.getUserId(), month);
        return WebMapper.toResponse(summary);
    }
}
