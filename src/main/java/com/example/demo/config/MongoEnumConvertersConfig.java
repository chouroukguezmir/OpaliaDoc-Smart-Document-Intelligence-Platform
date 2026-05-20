package com.example.demo.config;

import com.example.demo.model.Classification;
import com.example.demo.model.KindOfUpdate;
import com.example.demo.model.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.List;

/**
 * Convertit les chaînes MongoDB vers les enums du domaine.
 * Les enums utilisent {@code fromLabel} qui accepte indifféremment le label
 * ("Confidentiel") ou le nom ("CONFIDENTIEL"), pour rester compatibles avec
 * les anciennes données stockées sous forme de chaîne libre.
 */
@Configuration
public class MongoEnumConvertersConfig {

    @Bean
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(List.of(
                new StringToClassificationConverter(),
                new StringToRoleConverter(),
                new StringToKindOfUpdateConverter()
        ));
    }

    @ReadingConverter
    static class StringToClassificationConverter implements Converter<String, Classification> {
        @Override public Classification convert(String source) { return Classification.fromLabel(source); }
    }

    @ReadingConverter
    static class StringToRoleConverter implements Converter<String, Role> {
        @Override public Role convert(String source) { return Role.fromLabel(source); }
    }

    @ReadingConverter
    static class StringToKindOfUpdateConverter implements Converter<String, KindOfUpdate> {
        @Override public KindOfUpdate convert(String source) { return KindOfUpdate.fromLabel(source); }
    }
}