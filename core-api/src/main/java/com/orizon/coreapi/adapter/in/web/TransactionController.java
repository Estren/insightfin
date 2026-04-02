package com.orizon.coreapi.adapter.in.web;

import com.orizon.coreapi.adapter.in.web.dto.CreateTransactionRequest;
import com.orizon.coreapi.adapter.in.web.dto.TransactionResponse;
import com.orizon.coreapi.adapter.in.web.mapper.WebMapper;
import com.orizon.coreapi.config.security.AuthenticatedUser;
import com.orizon.coreapi.domain.port.in.CreateTransactionUseCase;
import com.orizon.coreapi.domain.port.in.ListTransactionsUseCase;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.List;

@Path("/api/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionController {

    @Inject
    CreateTransactionUseCase createTransactionUseCase;

    @Inject
    ListTransactionsUseCase listTransactionsUseCase;

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
}
