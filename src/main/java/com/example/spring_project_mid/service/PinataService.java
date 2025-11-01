package com.example.spring_project_mid.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class PinataService {

    // --- START: MODIFIED PROPERTIES ---
    // This now correctly matches your properties file
    @Value("${pinata.jwt.token}")
    private String pinataJwtToken;

    @Value("${pinata.api.url}")
    private String pinataApiUrl; // Should be https://api.pinata.cloud/pinning/pinFileToIPFS

    @Value("${ipfs.gateway.url}")
    private String ipfsGatewayUrl; // e.g., https://gateway.pinata.cloud/ipfs/
    // --- END: MODIFIED PROPERTIES ---

    private final RestTemplate restTemplate;

    public PinataService() {
        this.restTemplate = new RestTemplate();
    }

    public String uploadFile(MultipartFile file) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // --- THIS IS THE FIX ---
        // Pinata now uses a JWT Bearer Token for authentication
        // This will now correctly be: "Bearer eyJhbGci..."
        headers.set("Authorization", "Bearer " + pinataJwtToken);
        // --- END OF FIX ---

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new PinataService.ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                // Use a generic name or a secure random name if desired
                return file.getOriginalFilename();
            }
        });

        // Optional: Add metadata
        MultiValueMap<String, String> pinataMetadata = new LinkedMultiValueMap<>();
        pinataMetadata.add("name", file.getOriginalFilename());
        body.add("pinataMetadata", pinataMetadata);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(pinataApiUrl, requestEntity, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return (String) response.getBody().get("IpfsHash");
        } else {
            throw new RuntimeException("Pinata upload failed: " + response.getStatusCode() + " - " + response.getBody());
        }
    }

    public String getGatewayUrl(String ipfsHash) {
        return ipfsGatewayUrl + ipfsHash;
    }

    // Helper class for MultipartFile
    private static class ByteArrayResource extends org.springframework.core.io.ByteArrayResource {
        private String filename;

        public ByteArrayResource(byte[] byteArray) {
            super(byteArray);
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }
}