package com.example.demo.controller;

import com.example.demo.model.AdminDocument;
import com.example.demo.service.AdminDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class AdminDocumentController {

    @Autowired
    private AdminDocumentService service;

    // CREATE
    @PostMapping
    public AdminDocument create(@RequestBody AdminDocument document) {
        return service.create(document);
    }

    // READ ALL
    @GetMapping
    public List<AdminDocument> getAll() {
        return service.getAll();
    }

    // READ BY ID
    @GetMapping("/{id}")
    public AdminDocument getById(@PathVariable String id) {
        return service.getById(id);
    }

    // FILTER BY STATUS
    @GetMapping("/status/{status}")
    public List<AdminDocument> getByStatus(@PathVariable String status) {
        return service.getByStatus(status);
    }

    // UPDATE
    @PutMapping("/{id}")
    public AdminDocument update(@PathVariable String id, @RequestBody AdminDocument document) {
        return service.update(id, document);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public String delete(@PathVariable String id) {
        service.delete(id);
        return "Deleted successfully";
    }
}