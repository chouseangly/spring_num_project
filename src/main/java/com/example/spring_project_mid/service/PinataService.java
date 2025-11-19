package com.example.spring_project_mid.service;

import com.example.spring_project_mid.dto.PinataResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
// Note: StringEntity is not used, but we'll leave the import
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Slf4j
public class PinataService {

    @Value("${pinata.jwt.token}")
    private String pinataJwtToken;

    @Value("${pinata.api.url}")
    private String pinataApiUrl;

    @Value("${ipfs.gateway.url}")
    private String ipfsGatewayUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Uploads a file to Pinata and returns the public gateway URL.
     */
    public String uploadFileToPinata(MultipartFile file) throws IOException, InterruptedException {
        log.info("Starting Pinata upload for file: {}", file.getOriginalFilename());

        // 1. Create the HTTP Client and POST Request
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(pinataApiUrl);

            // 2. Set the Authorization Header with the JWT Token
            httpPost.setHeader("Authorization", "Bearer " + pinataJwtToken);

            // 3. Build the Multipart/Form-Data request
            HttpEntity mimeEntity;
            mimeEntity = MultipartEntityBuilder.create()
                    .addBinaryBody(
                            "file", // This "file" name must match what Pinata expects
                            file.getInputStream(),
                            ContentType.DEFAULT_BINARY,
                            file.getOriginalFilename()
                    )
                    .build();

            httpPost.setEntity(mimeEntity);

            log.info("Executing POST request to Pinata API...");
            String responseString = httpClient.execute(httpPost, response -> {
                log.info("Received response from Pinata. Status: {}", response.getCode());
                String responseBody = EntityUtils.toString(response.getEntity());
                if (response.getCode() != 200) {
                    log.error("Pinata API Error. Status: {}, Body: {}", response.getCode(), responseBody);
                    throw new IOException("Pinata API returned status " + response.getCode() + ": " + responseBody);
                }
                return responseBody;
            });

            log.info("Pinata response body: {}", responseString);
            PinataResponse pinataResponse = objectMapper.readValue(responseString, PinataResponse.class);
            String ipfsHash = pinataResponse.getIpfsHash();

            if (ipfsHash == null || ipfsHash.isEmpty()) {
                throw new IOException("Failed to parse IpfsHash from Pinata response.");
            }

            String gatewayUrl = ipfsGatewayUrl + ipfsHash;
            log.info("File uploaded successfully. Gateway URL: {}", gatewayUrl);
            return gatewayUrl;

        } catch (Exception e) {
            log.error("Error during Pinata upload", e);
            throw new RuntimeException("Error uploading file to Pinata: " + e.getMessage(), e);
        }
    }
}
