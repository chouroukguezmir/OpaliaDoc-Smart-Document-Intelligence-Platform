package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "employees")
public class Employee {

    @Id
    private String id;

    private String fullName;
    private String employeeId; // matricule
    private String department;
    private String position;   // fonction
    private String site;
    private String email;
    private String phone;
    private String company;
    private String mobile;

    private String sourceDocumentId; // référence à EmployeeDocument

    private List<VpnEntry> vpnEntries;

    private LocalDateTime createdAt = LocalDateTime.now();

    @Data
    public static class VpnEntry {
        private String application;
        private String profile;
    }
}