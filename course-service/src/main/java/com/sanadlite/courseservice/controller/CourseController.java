package com.sanadlite.courseservice.controller;

import com.sanadlite.courseservice.dto.request.CourseReqDto;
import com.sanadlite.courseservice.dto.request.CourseUpdateReqDto;
import com.sanadlite.courseservice.dto.response.CourseResDto;
import com.sanadlite.courseservice.model.Course;
import com.sanadlite.courseservice.service.CourseService;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Log
@RestController
@AllArgsConstructor
@Validated
@RequestMapping("/api/v1/courses")
public class CourseController {
    private final CourseService courseService;

    @GetMapping()
    public ResponseEntity<Page<Course>> getAllCourses(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(required = false) Long instructorId
    ) {
        Sort sort = Sort.by(sortField);
        if (sortOrder.equalsIgnoreCase("desc")) {
            sort = sort.descending();
        }else sort = sort.ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return new ResponseEntity<>(courseService.getAll(search, instructorId, pageable),HttpStatus.OK);
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<Course> getCourseById(@PathVariable(name = "courseId") Long id) {
        Optional<Course> course = courseService.getById(id);
        return course.map(value -> new ResponseEntity<>(value, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping()
    public ResponseEntity<CourseResDto> createCourse(@RequestBody CourseReqDto courseReqDto) {
       return new ResponseEntity<>(courseService.addCourse(courseReqDto), HttpStatus.CREATED) ;
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<Object>  deleteCourseById(@PathVariable(name = "courseId") Long id) {
        if(courseService.deleteCourseById(id)) return ResponseEntity.noContent().build();
        else return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{courseId}")
    public ResponseEntity<Object>  updateCourseById(@PathVariable(name = "courseId") Long id, @RequestBody CourseUpdateReqDto courseUpdateReqDto) {
        Course course = courseService.updateCourse(id,courseUpdateReqDto);
        if(course != null) return ResponseEntity.ok().body(course);
        else return ResponseEntity.notFound().build();
    }

}
