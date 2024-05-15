package org.sanadlite.courseenrollmentservice.service;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.sanadlite.courseenrollmentservice.dto.EnrollmentRequestDto;
import org.sanadlite.courseenrollmentservice.feign.courseservice.CourseInterface;
import org.sanadlite.courseenrollmentservice.feign.courseservice.CourseResponseDto;
import org.sanadlite.courseenrollmentservice.model.Enrollment;
import org.sanadlite.courseenrollmentservice.repository.EnrollmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@AllArgsConstructor
public class EnrollmentService {
    private EnrollmentRepository enrollmentRepository;
    private CourseInterface courseInterface;
    private ModelMapper modelMapper;

    public Page<Enrollment> getByStudentId(String studentUUID, Pageable pageable) {
        return enrollmentRepository.findByStudentUUID(studentUUID, pageable);
    }

    public Enrollment addEnrollment(EnrollmentRequestDto enrollmentRequestDto) {
        ResponseEntity<CourseResponseDto> courseResponse = courseInterface.getCourseById(enrollmentRequestDto.getCourseId());
        // if course is not present don't create enrollment
        if (courseResponse.getStatusCode() == HttpStatusCode.valueOf(404))
            return null;

        // if capacity exceeded don't create enrollment
        if(courseResponse.getBody().getEnrollmentsNumber() >= courseResponse.getBody().getMaxCapacity()
                && courseResponse.getBody().getMaxCapacity() != -1)
            return null;

        // if exact enrollment exists don't create enrollment
        if(enrollmentRepository.findByCourseIdAndAndStudentUUID(enrollmentRequestDto.getCourseId(),
                enrollmentRequestDto.getStudentUUID()).isPresent())
            return null;

        Enrollment enrollment = new Enrollment();
        modelMapper.map(enrollmentRequestDto, enrollment);
        return enrollmentRepository.save(enrollment);
    }

    public Integer cancelEnrollment(EnrollmentRequestDto enrollmentRequestDto) {
        Optional<Enrollment> enrollment =
                enrollmentRepository.findByCourseIdAndAndStudentUUID
                        (enrollmentRequestDto.getCourseId(), enrollmentRequestDto.getStudentUUID());
        if (enrollment.isEmpty())
            return -1;

        // if enroll accepted, don't delete
        if (enrollment.get().getStatus())
            return 0;

        enrollmentRepository.deleteById(enrollment.get().getId());
        return 1;
    }
}