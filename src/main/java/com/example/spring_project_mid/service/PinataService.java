package com.example.spring_project_mid.service;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${pinata.api.key}")
    private String pinataApiKey;

    @Value("${pinata.secret.api.key}")
    private String pinataSecretApiKey;

    @Value("${pinata.api.url}")
    private String pinataApiUrl; // Should be https://api.pinata.cloud/pinning/pinFileToIPFS

    @Value("${ipfs.gateway.url}")
    private String ipfsGatewayUrl; // e.g., https://gateway.pinata.cloud/ipfs/ or https://ipfs.io/ipfs/

    private final RestTemplate restTemplate;

    public PinataService() {
        this.restTemplate = new RestTemplate();
    }

    public String uploadFile(MultipartFile file) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("pinata_api_key", pinataApiKey);
        headers.set("pinata_secret_api_key", pinataSecretApiKey);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });

        // Optional: Add metadata like filename
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