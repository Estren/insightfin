package com.orizon.coreapi.adapter.in.web;

import com.orizon.coreapi.adapter.in.web.dto.CreateTransactionRequest;
import com.orizon.coreapi.adapter.in.web.dto.TransactionResponse;
import com.orizon.coreapi.adapter.in.web.dto.UpdateTransactionRequest;
import com.orizon.coreapi.adapter.in.web.mapper.WebMapper;
import com.orizon.coreapi.config.security.AuthenticatedUser;
import com.orizon.coreapi.domain.port.in.CreateTransactionUseCase;
import com.orizon.coreapi.domain.port.in.DeleteTransactionUseCase;
import com.orizon.coreapi.domain.port.in.ListTransactionsUseCase;
import com.orizon.coreapi.domain.port.in.UpdateTransactionUseCase;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Path("/api/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionController {

    @Inject
    CreateTransactionUseCase createTransactionUseCase;

    @Inject
    ListTransactionsUseCase listTransactionsUseCase;

    @Inject
    UpdateTransactionUseCase updateTransactionUseCase;

    @Inject
    DeleteTransactionUseCase deleteTransactionUseCase;

    @Inject
    AuthenticatedUser authenticatedUser;

    @POST
    public Response create(@Valid CreateTransactionRequest request) {
        var transaction = createTransactionUseCase.execute(
                authenticatedUser.getUserId(), request.categoryId(), request.type(),
                request.amount(), request.description(), request.date());
        return Response.status(Response.Status.CREATED).entity(WebMapper.toResponse(transaction)).build();
    }

    @GET
    public List<TransactionResponse> list(@QueryParam("startDate") LocalDate startDate,
                                          @QueryParam("endDate") LocalDate endDate) {
        return listTransactionsUseCase.execute(authenticatedUser.getUserId(), startDate, endDate)
                .stream()
                .map(WebMapper::toResponse)
                .toList();
    }

    @PUT
    @Path("/{id}")
    public TransactionResponse update(@PathParam("id") UUID id, @Valid UpdateTransactionRequest request) {
        var transaction = updateTransactionUseCase.execute(
                authenticatedUser.getUserId(), id, request.categoryId(), request.type(),
                request.amount(), request.description(), request.date());
        return WebMapper.toResponse(transaction);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") UUID id) {
        deleteTransactionUseCase.execute(authenticatedUser.getUserId(), id);
        return Response.noContent().build();
    }
}
