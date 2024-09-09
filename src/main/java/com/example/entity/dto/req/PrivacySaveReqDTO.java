package com.example.entity.dto.req;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Nruonan
 * @description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrivacySaveReqDTO {
    @Pattern(regexp = "(phone|email|qq|wx|gender)")
    String type;
    boolean status;
;}
