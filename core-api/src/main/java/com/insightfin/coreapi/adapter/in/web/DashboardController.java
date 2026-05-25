package com.insightfin.coreapi.adapter.in.web;

import com.insightfin.coreapi.adapter.in.web.dto.DashboardResponse;
import com.insightfin.coreapi.adapter.in.web.mapper.WebMapper;
import com.insightfin.coreapi.config.security.AuthenticatedUser;
import com.insightfin.coreapi.domain.model.Category;
import com.insightfin.coreapi.domain.port.in.GetDashboardUseCase;
import com.insightfin.coreapi.domain.port.out.CategoryRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/api/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DashboardController {

    @Inject
    GetDashboardUseCase getDashboardUseCase;

    @Inject
    AuthenticatedUser authenticatedUser;

    @Inject
    CategoryRepository categoryRepository;

    @GET
    public DashboardResponse get(@QueryParam("month") String month) {
        var userId = authenticatedUser.getUserId();
        var summary = getDashboardUseCase.execute(userId, month);
        Map<UUID, String> categoryNames = categoryRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        return WebMapper.toResponse(summary, categoryNames);
    }
}
