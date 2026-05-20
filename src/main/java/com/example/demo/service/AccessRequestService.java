package com.example.demo.service;

import com.example.demo.model.AccessRequest;

import java.util.List;

public interface AccessRequestService {

    List<AccessRequest> getAll();

    AccessRequest getById(String id);
}