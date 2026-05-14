package com.example.demo.service;

import com.example.demo.model.ScanSession;
import org.springframework.web.multipart.MultipartFile;

public interface ScanService {
    ScanSession scanDocument(MultipartFile file, String adminUsername);
    ScanSession getSessionById(String id);
}