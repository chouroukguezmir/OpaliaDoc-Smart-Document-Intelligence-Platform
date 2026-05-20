package com.example.demo.service;

import com.example.demo.model.AccessRequest;
import com.example.demo.repository.AccessRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccessRequestServiceImpl implements AccessRequestService {

    private final AccessRequestRepository repository;

    @Override
    public List<AccessRequest> getAll() {
        return repository.findAll();
    }

    @Override
    public AccessRequest getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande d'accès introuvable : " + id));
    }
}