package com.example.spring_project_mid.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PinataResponse {

    @JsonProperty("IpfsHash")
    private String ipfsHash;

}