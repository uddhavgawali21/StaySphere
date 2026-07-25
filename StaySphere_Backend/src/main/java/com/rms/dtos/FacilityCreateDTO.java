package com.rms.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FacilityCreateDTO {

    @NotBlank(message = "Facility name is required")
    @Size(max = 100)
    private String facilityName;
}