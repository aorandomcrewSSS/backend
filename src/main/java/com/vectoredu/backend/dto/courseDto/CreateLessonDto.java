package com.vectoredu.backend.dto.courseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain=true)
@AllArgsConstructor
public class CreateLessonDto {
    private String title;
    private String videoUrl;
}
