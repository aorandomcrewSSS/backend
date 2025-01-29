package com.vectoredu.backend.dto.courseDto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain=true)
@AllArgsConstructor
public class CreateCourseDto {
    @NotBlank()
    private String title;

    @NotBlank()
    private String description;
}
