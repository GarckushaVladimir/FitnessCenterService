package com.fitness.config;

import com.fitness.model.Trainer;
import com.fitness.service.TrainerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final TrainerService trainerService;

    @Autowired
    public WebMvcConfig(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToTrainerConverter());
    }

    private class StringToTrainerConverter implements Converter<String, Trainer> {
        @Override
        public Trainer convert(String source) {
            if (source == null || source.isEmpty()) {
                return null; // Если тренер не выбран
            }
            try {
                Long id = Long.parseLong(source);
                return trainerService.getTrainerById(id);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Некорректный ID тренера: " + source);
            }
        }
    }
}