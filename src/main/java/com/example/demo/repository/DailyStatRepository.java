package com.example.demo.repository;

import com.example.demo.model.DailyStat;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyStatRepository
        extends MongoRepository<DailyStat, String> {

    Optional<DailyStat> findByStatDate(LocalDate statDate);
    List<DailyStat> findByStatDateBetween(LocalDate from, LocalDate to);
}