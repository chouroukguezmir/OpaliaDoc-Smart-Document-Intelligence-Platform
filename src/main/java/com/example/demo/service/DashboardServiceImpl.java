package com.example.demo.service;

import com.example.demo.model.PendingDocument;
import com.example.demo.repository.AccessRequestRepository;
import com.example.demo.repository.EmployeeRepository;
import com.example.demo.repository.ExternalMaterialRepository;
import com.example.demo.repository.MaterialRepository;
import com.example.demo.repository.PendingDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final EmployeeRepository employeeRepository;
    private final MaterialRepository materialRepository;
    private final ExternalMaterialRepository externalMaterialRepository;
    private final AccessRequestRepository accessRequestRepository;
    private final PendingDocumentRepository pendingDocumentRepository;

    @Override
    public Map<String, Object> getStats() {
        long confirmedEmployees = employeeRepository.count();
        long confirmedMaterials = materialRepository.count();
        long confirmedExtMats   = externalMaterialRepository.count();
        long confirmedAccess    = accessRequestRepository.count();

        List<PendingDocument> allPending = pendingDocumentRepository.findAll();
        long pendingTotal = allPending.size();

        Map<String, Long> pendingByType = new HashMap<>();
        for (PendingDocument p : allPending) {
            String t = p.getDocumentType() == null ? "UNKNOWN" : p.getDocumentType();
            pendingByType.merge(t, 1L, Long::sum);
        }
        long pendingEmployees = pendingByType.getOrDefault("EMPLOYEE", 0L);
        long pendingMaterials = pendingByType.getOrDefault("TYPE_B", 0L);
        long pendingExtMats   = pendingByType.getOrDefault("TYPE_C", 0L);
        long pendingAccess    = pendingByType.getOrDefault("TYPE_A", 0L);

        long confirmedTotal = confirmedEmployees + confirmedMaterials
                + confirmedExtMats + confirmedAccess;

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long todayScanned = allPending.stream()
                .filter(p -> p.getScannedAt() != null && p.getScannedAt().isAfter(startOfDay))
                .count();

        Map<String, Object> totals = new LinkedHashMap<>();
        totals.put("all", pendingTotal + confirmedTotal);
        totals.put("pending", pendingTotal);
        totals.put("confirmed", confirmedTotal);

        Map<String, Object> byType = new LinkedHashMap<>();
        byType.put("employees",         typeBucket(pendingEmployees, confirmedEmployees));
        byType.put("materials",         typeBucket(pendingMaterials, confirmedMaterials));
        byType.put("externalMaterials", typeBucket(pendingExtMats,   confirmedExtMats));
        byType.put("accessRequests",    typeBucket(pendingAccess,    confirmedAccess));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totals", totals);
        result.put("byType", byType);
        result.put("todayScanned", todayScanned);
        return result;
    }

    private Map<String, Object> typeBucket(long pending, long confirmed) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("pending", pending);
        m.put("confirmed", confirmed);
        m.put("total", pending + confirmed);
        return m;
    }
}