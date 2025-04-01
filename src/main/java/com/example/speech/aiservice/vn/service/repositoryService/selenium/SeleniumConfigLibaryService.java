package com.example.speech.aiservice.vn.service.repositoryService.selenium;

import com.example.speech.aiservice.vn.model.entity.selenium.SeleniumConfigLibary;
import com.example.speech.aiservice.vn.model.entity.selenium.SeleniumConfigSingle;
import com.example.speech.aiservice.vn.model.repository.selenium.SeleniumConfigLibaryRepository;
import com.example.speech.aiservice.vn.model.repository.selenium.SeleniumConfigSingleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeleniumConfigLibaryService {
    private final SeleniumConfigLibaryRepository seleniumConfigLibaryRepository;

    @Autowired
    public SeleniumConfigLibaryService(SeleniumConfigLibaryRepository seleniumConfigLibaryRepository) {
        this.seleniumConfigLibaryRepository = seleniumConfigLibaryRepository;
    }

    public List<SeleniumConfigLibary> getAllConfigs() {
        return seleniumConfigLibaryRepository.findAll();
    }

    public SeleniumConfigLibary saveConfig(SeleniumConfigLibary config) {
        return seleniumConfigLibaryRepository.save(config);
    }

    public SeleniumConfigLibary getConfigByPort(String port) {
        return seleniumConfigLibaryRepository.findByPort(port);
    }
}
