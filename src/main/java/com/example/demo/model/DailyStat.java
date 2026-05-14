package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Data
@Document(collection = "daily_stats")
public class DailyStat {

    @Id
    private String id;

    @Indexed(unique = true)
    private LocalDate statDate;

    private Long scannedTotal          = 0L;
    private Long scannedEmployee       = 0L;
    private Long scannedAdministrative = 0L;
    private Long scannedUnknown        = 0L;

    private Long analyzedTotal          = 0L;
    private Long analyzedEmployee       = 0L;
    private Long analyzedAdministrative = 0L;
    private Long analyzedFailed         = 0L;

    private Map<String, Long> bySubType = new HashMap<>() {{
        put("TYPE_A", 0L);
        put("TYPE_B", 0L);
        put("TYPE_C", 0L);
    }};

    private Long handwritten    = 0L;
    private Long pdfsGenerated  = 0L;
}