package com.example.spring_project_mid.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

// This DTO captures the response from the Pinata API
@Data
@JsonIgnoreProperties(ignoreUnknown = true) // Ignore other fields like PinSize, Timestamp
public class PinataResponse {

    @JsonProperty("IpfsHash")
    private String ipfsHash;

    // We only need the IpfsHash to build our gateway URL
}