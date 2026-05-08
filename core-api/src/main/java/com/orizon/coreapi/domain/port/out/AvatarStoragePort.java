package com.orizon.coreapi.domain.port.out;

public interface AvatarStoragePort {
    String upload(String userId, String fileName, byte[] data, String contentType);
    void delete(String blobUrl);
}
