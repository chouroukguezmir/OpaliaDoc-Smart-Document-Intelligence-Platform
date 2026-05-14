package com.example.demo.model;

import com.example.demo.model.embedded.ProfileEntry;
import com.example.demo.model.embedded.VpnEntry;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "employees")
public class Employee {

    @Id
    private String id;
    @Indexed
    private String employeeId;
    @TextIndexed
    private String fullName;
    private String position;
    @Indexed
    private String department;
    private String costCenter;
    private String company;
    private String site;
    private String mobile;
    private String officePhone;

    private String requester;
    private String requesterJobRole;
    private String kindOfUpdate;
    private List<String> systemsAccessGranted;
    private List<ProfileEntry> profiles;
    private List<VpnEntry> vpnEntries;

    private String systemOwnerSignature;
    private String systemOwnerDate;
    private String itSecuritySignature;
    private String itSecurityDate;
    private String itManagerSignature;
    private String itManagerDate;

    private String sourceDocumentId;
    private Map<String, Object> additionalInfo;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}