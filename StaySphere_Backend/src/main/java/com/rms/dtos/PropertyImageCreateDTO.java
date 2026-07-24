package com.rms.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertyImageCreateDTO {

    @NotBlank(message = "Image URL is required")
    @Size(max = 500)
    private String imageUrl;

    private boolean primary;
}