package com.orizon.coreapi.adapter.out.storage;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.PublicAccessType;
import com.orizon.coreapi.domain.port.out.AvatarStoragePort;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.UUID;

@ApplicationScoped
public class AzureBlobStorageAdapter implements AvatarStoragePort {

    @ConfigProperty(name = "azure.storage.connection-string")
    String connectionString;

    @ConfigProperty(name = "azure.storage.container-name", defaultValue = "avatars")
    String containerName;

    @ConfigProperty(name = "azure.storage.public-base-url", defaultValue = "")
    String publicBaseUrl;

    private BlobContainerClient containerClient;

    @PostConstruct
    void init() {
        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
        containerClient = serviceClient.getBlobContainerClient(containerName);
        if (!containerClient.exists()) {
            containerClient.createWithResponse(null, PublicAccessType.BLOB, null, null);
        }
    }

    @Override
    public String upload(String userId, String fileName, byte[] data, String contentType) {
        String ext = extractExtension(fileName);
        String blobName = userId + "/" + UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);

        BlobClient blobClient = containerClient.getBlobClient(blobName);
        blobClient.upload(BinaryData.fromBytes(data), true);
        blobClient.setHttpHeaders(new BlobHttpHeaders().setContentType(contentType));

        return toPublicUrl(blobClient.getBlobUrl(), blobName);
    }

    @Override
    public void delete(String blobUrl) {
        try {
            String blobName = extractBlobName(blobUrl);
            containerClient.getBlobClient(blobName).deleteIfExists();
        } catch (Exception ignored) {
            // deletion failure must not block a new upload
        }
    }

    private String toPublicUrl(String internalUrl, String blobName) {
        if (publicBaseUrl == null || publicBaseUrl.isBlank()) {
            return internalUrl;
        }
        return publicBaseUrl.stripTrailing() + "/" + containerName + "/" + blobName;
    }

    private String extractExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot + 1).toLowerCase() : "";
    }

    private String extractBlobName(String blobUrl) {
        String marker = "/" + containerName + "/";
        int idx = blobUrl.indexOf(marker);
        if (idx < 0) throw new IllegalArgumentException("Unrecognised blob URL: " + blobUrl);
        return blobUrl.substring(idx + marker.length());
    }
}
