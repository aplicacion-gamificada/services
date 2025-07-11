package com.gamified.application.clasroom.controller;

import com.gamified.application.auth.service.auth.TokenService;
import com.gamified.application.clasroom.service.ClassroomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
@Slf4j
public class ClassroomStudentController {
    private final ClassroomService classroomService;
    private final TokenService tokenService;

    @GetMapping("/classrooms/class-data/{userId}")
    public ResponseEntity<Map<String, Object>> getClassroomDataByUserId(@PathVariable int userId){
        try{
            Map<String, Object> data = classroomService.getClassroomDataByUserId(userId);
            return ResponseEntity.ok(data);
        }catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/classrooms/classmates/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getClassmatesByUserId(@PathVariable int userId){
        try{
            List<Map<String, Object>> data = classroomService.getClassmatesByUserId(userId);
            return ResponseEntity.ok(data);
        }catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }
}
