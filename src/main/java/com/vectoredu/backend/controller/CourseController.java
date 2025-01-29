package com.vectoredu.backend.controller;


import com.vectoredu.backend.dto.courseDto.CreateBlockDto;
import com.vectoredu.backend.dto.courseDto.CreateCourseDto;
import com.vectoredu.backend.dto.courseDto.CreateLessonDto;
import com.vectoredu.backend.model.Block;
import com.vectoredu.backend.model.Course;
import com.vectoredu.backend.model.Lesson;
import com.vectoredu.backend.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/courses")
@Tag(name = "Управление курсами", description = "Операции, связанные с добавлением курсов, блоков и уроков")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "Создание нового курса", responses = {
            @ApiResponse(responseCode = "201", description = "Курс успешно создан"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации данных")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Course> createCourse(@RequestBody CreateCourseDto createCourseDto) {
        Course course = courseService.createCourse(createCourseDto);
        return ResponseEntity.ok(course);
    }

    @Operation(summary = "Добавление блока в курс", responses = {
            @ApiResponse(responseCode = "201", description = "Блок успешно добавлен в курс"),
            @ApiResponse(responseCode = "404", description = "Курс не найден")
    })
    @PostMapping("/{courseId}/blocks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Block> addBlockToCourse(@PathVariable Long courseId,
                                                  @RequestBody CreateBlockDto createBlockDto) {
        Block block = courseService.addBlockToCourse(courseId, createBlockDto);
        return ResponseEntity.ok(block);
    }

    @Operation(summary = "Добавление урока в блок", responses = {
            @ApiResponse(responseCode = "201", description = "Урок успешно добавлен в блок"),
            @ApiResponse(responseCode = "404", description = "Блок не найден")
    })
    @PostMapping("/blocks/{blockId}/lessons")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Lesson> addLessonToBlock(@PathVariable Long blockId,
                                                   @RequestBody CreateLessonDto createLessonDto) {
        Lesson lesson = courseService.addLessonToBlock(blockId, createLessonDto);
        return ResponseEntity.ok(lesson);
    }
}
