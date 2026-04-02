package com.orizon.coreapi.config;

import com.orizon.coreapi.domain.exception.DomainException;
import com.orizon.coreapi.domain.exception.DuplicateResourceException;
import com.orizon.coreapi.domain.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.time.LocalDateTime;
import java.util.Map;

public class GlobalExceptionHandler {

    @Provider
    public static class ResourceNotFoundExceptionMapper implements ExceptionMapper<ResourceNotFoundException> {
        @Override
        public Response toResponse(ResourceNotFoundException ex) {
            return buildResponse(Response.Status.NOT_FOUND, ex.getMessage());
        }
    }

    @Provider
    public static class DuplicateResourceExceptionMapper implements ExceptionMapper<DuplicateResourceException> {
        @Override
        public Response toResponse(DuplicateResourceException ex) {
            return buildResponse(Response.Status.CONFLICT, ex.getMessage());
        }
    }

    @Provider
    public static class DomainExceptionMapper implements ExceptionMapper<DomainException> {
        @Override
        public Response toResponse(DomainException ex) {
            return buildResponse(Response.Status.BAD_REQUEST, ex.getMessage());
        }
    }

    @Provider
    public static class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
        @Override
        public Response toResponse(ConstraintViolationException ex) {
            var errors = ex.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .toList();
            return buildResponse(Response.Status.BAD_REQUEST, errors.toString());
        }
    }

    private static Response buildResponse(Response.Status status, String message) {
        return Response.status(status).entity(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", status.getStatusCode(),
                "error", status.getReasonPhrase(),
                "message", message
        )).build();
    }
}
