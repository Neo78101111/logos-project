package com.logos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DreamRequestDTO {
    @NotBlank
    @Size(max = 200)
    private String title;

    private String description;

    // imageUrl будет устанавливаться после загрузки файла
    private String dreamImageUrl;
}