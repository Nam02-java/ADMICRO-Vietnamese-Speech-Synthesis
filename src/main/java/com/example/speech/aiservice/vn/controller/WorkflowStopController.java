package com.example.speech.aiservice.vn.controller;

import com.example.speech.aiservice.vn.service.workflow.previous.SingleNovelPreProcessorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/workflow")
public class WorkflowStopController  {

    private final SingleNovelPreProcessorService singleNovelPreProcessorService;

    @Autowired
    public WorkflowStopController(SingleNovelPreProcessorService singleNovelPreProcessorService) {
        this.singleNovelPreProcessorService = singleNovelPreProcessorService;
    }

    @PostMapping("/stop")
    public ResponseEntity<String> stopWorkflow() {
        System.out.println("Received STOP request!");
        singleNovelPreProcessorService.stopConditions();
        return ResponseEntity.ok("Workflow stopped successfully!");
    }
}
