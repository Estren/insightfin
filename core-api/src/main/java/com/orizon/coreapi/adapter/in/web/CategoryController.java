package com.orizon.coreapi.adapter.in.web;

import com.orizon.coreapi.adapter.in.web.dto.CategoryResponse;
import com.orizon.coreapi.adapter.in.web.dto.CreateCategoryRequest;
import com.orizon.coreapi.adapter.in.web.mapper.WebMapper;
import com.orizon.coreapi.config.security.AuthenticatedUser;
import com.orizon.coreapi.domain.model.TransactionType;
import com.orizon.coreapi.domain.port.in.CreateCategoryUseCase;
import com.orizon.coreapi.domain.port.in.ListCategoriesUseCase;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoryController {

    @Inject
    CreateCategoryUseCase createCategoryUseCase;

    @Inject
    ListCategoriesUseCase listCategoriesUseCase;

    @Inject
    AuthenticatedUser authenticatedUser;

    @POST
    public Response create(@Valid CreateCategoryRequest request) {
        var category = createCategoryUseCase.execute(
                authenticatedUser.getUserId(), request.name(), request.type(), request.icon(), request.color());
        return Response.status(Response.Status.CREATED).entity(WebMapper.toResponse(category)).build();
    }

    @GET
    public List<CategoryResponse> list(@QueryParam("type") TransactionType type) {
        return listCategoriesUseCase.execute(authenticatedUser.getUserId(), type)
                .stream()
                .map(WebMapper::toResponse)
                .toList();
    }
}
