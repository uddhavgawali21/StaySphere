package com.rms.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertyImageCreateDTO {

    @NotBlank(message = "Image URL is required")
    @Size(max = 500)
    // Rendered directly as <img src> to every tenant browsing this property —
    // restrict to http(s) so an owner can't slip in a javascript:/data: URI.
    @Pattern(regexp = "^https?://.+", message = "Image URL must start with http:// or https://")
    private String imageUrl;

    private boolean primary;
}