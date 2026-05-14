package com.example.demo.model.embedded;

import lombok.Data;

@Data
public class ProfileEntry {
    private String profile;
    private String assignedDate;
    private String modifiedDate;
    private String removedDate;
}