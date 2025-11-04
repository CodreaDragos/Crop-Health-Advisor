// JwtAuthResponse.java (Response with Token)
package com.proiect.SCD.CropHealthAdvisor.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class JwtAuthResponse {  
    private String accessToken;
    private String tokenType = "Bearer";

    public JwtAuthResponse(String accessToken)
    {
        this.accessToken=accessToken;
        this.tokenType= "Bearer";
    }
}