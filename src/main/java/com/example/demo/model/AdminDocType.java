package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "admin_doc_types")
public class AdminDocType {

    @Id
    private String id;

    private String code;           // TYPE_A | TYPE_B | TYPE_C
    private String name;
    private String documentCode;   // E DSI 3813 etc.
    private String version;
    private String description;
    private List<String> expectedFields;
    private String aiPromptHint;
}