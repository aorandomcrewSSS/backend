package com.vectoredu.backend.service;

import com.vectoredu.backend.dto.courseDto.CreateBlockDto;
import com.vectoredu.backend.dto.courseDto.CreateCourseDto;
import com.vectoredu.backend.dto.courseDto.CreateLessonDto;
import com.vectoredu.backend.model.Block;
import com.vectoredu.backend.model.Course;
import com.vectoredu.backend.model.Lesson;
import com.vectoredu.backend.repository.BlockRepository;
import com.vectoredu.backend.repository.CourseRepository;
import com.vectoredu.backend.repository.LessonRepository;
import com.vectoredu.backend.util.exception.NotFoundException;
import com.vectoredu.backend.util.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final BlockRepository blockRepository;
    private final LessonRepository lessonRepository;

    // Метод для создания нового курса
    public Course createCourse(CreateCourseDto createCourseDto) {
        validateCourseInput(createCourseDto);
        Course course = buildCourse(createCourseDto);
        return saveCourse(course);
    }

    // Метод для добавления блока в курс
    public Block addBlockToCourse(Long courseId, CreateBlockDto createBlockDto) {
        validateTitle(createBlockDto.getTitle());
        Course course = findCourseById(courseId);
        Block block = buildBlock(createBlockDto, course);
        return saveBlock(block);
    }

    // Метод для добавления урока в блок
    public Lesson addLessonToBlock(Long blockId, CreateLessonDto createLessonDto) {
        validateTitle(createLessonDto.getTitle());
        Block block = findBlockById(blockId);
        Lesson lesson = buildLesson(createLessonDto, block);
        return saveLesson(lesson);
    }

    // Валидация входных данных курса
    private void validateCourseInput(CreateCourseDto createCourseDto) {
        validateTitle(createCourseDto.getTitle());
        validateDescription(createCourseDto.getDescription());
    }

    private void validateTitle(String title) {
        if (title.isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }
    }

    private void validateDescription(String description) {
        if (description.isBlank()) {
            throw new ValidationException("Описание не может быть пустым");
        }
    }

    // Создание и сохранение курса
    private Course buildCourse(CreateCourseDto createCourseDto) {
        return Course.builder()
                .title(createCourseDto.getTitle())
                .description(createCourseDto.getDescription())
                .blocks(new ArrayList<>())
                .build();
    }

    private Course saveCourse(Course course) {
        return courseRepository.save(course);
    }

    // Поиск курса по ID
    private Course findCourseById(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Курс не найден"));
    }

    // Создание и сохранение блока
    private Block buildBlock(CreateBlockDto createBlockDto, Course course) {
        return Block.builder()
                .title(createBlockDto.getTitle())
                .course(course)
                .lessons(new ArrayList<>())
                .build();
    }

    private Block saveBlock(Block block) {
        return blockRepository.save(block);
    }

    // Поиск блока по ID
    private Block findBlockById(Long blockId) {
        return blockRepository.findById(blockId)
                .orElseThrow(() -> new NotFoundException("Блок не найден"));
    }

    // Создание и сохранение урока
    private Lesson buildLesson(CreateLessonDto createLessonDto, Block block) {
        return Lesson.builder()
                .title(createLessonDto.getTitle())
                .videoUrl(createLessonDto.getVideoUrl())
                .block(block)
                .comments(new ArrayList<>())
                .build();
    }

    private Lesson saveLesson(Lesson lesson) {
        return lessonRepository.save(lesson);
    }
}
