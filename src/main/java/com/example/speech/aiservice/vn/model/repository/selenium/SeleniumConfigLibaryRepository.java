package com.example.speech.aiservice.vn.model.repository.selenium;

import com.example.speech.aiservice.vn.model.entity.selenium.SeleniumConfigLibary;
import com.example.speech.aiservice.vn.model.entity.selenium.SeleniumConfigSingle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeleniumConfigLibaryRepository extends JpaRepository<SeleniumConfigLibary, Long> {
    SeleniumConfigLibary findByPort(String port);
}
