package com.orizon.coreapi.domain.exception;

public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " not found with id: " + id);
    }
}
