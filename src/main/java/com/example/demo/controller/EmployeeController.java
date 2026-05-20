package com.example.demo.controller;

import com.example.demo.model.Employee;
import com.example.demo.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<List<Employee>> getAll() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getOne(@PathVariable String id) {
        try {
            return ResponseEntity.ok(employeeService.getEmployeeById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Télécharger le document d'origine joint à l'employé
    @GetMapping("/{id}/file")
    public ResponseEntity<FileSystemResource> getFile(@PathVariable String id) {
        try {
            Employee emp = employeeService.getEmployeeById(id);
            if (emp.getAttachedFile() == null) return ResponseEntity.notFound().build();

            File file = new File(emp.getAttachedFile());
            if (!file.exists()) return ResponseEntity.notFound().build();

            String contentType = Files.probeContentType(file.toPath());
            return ResponseEntity.ok()
                    .contentType(contentType != null
                            ? MediaType.parseMediaType(contentType)
                            : MediaType.APPLICATION_OCTET_STREAM)
                    .body(new FileSystemResource(file));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}