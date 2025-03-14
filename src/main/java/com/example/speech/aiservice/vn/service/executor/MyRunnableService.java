package com.example.speech.aiservice.vn.service.executor;

import com.example.speech.aiservice.vn.model.entity.Chapter;
import com.example.speech.aiservice.vn.model.entity.Novel;
import com.example.speech.aiservice.vn.service.workflow.FullWorkFlow;

public class MyRunnableService implements Runnable {
    private final FullWorkFlow fullWorkFlow;
    private final String port;
    private final String seleniumFileName;
    private final Novel novel;
    private final Chapter chapter;


    public MyRunnableService(FullWorkFlow fullWorkFlow, String port, String seleniumFileName, Novel novel, Chapter chapter) {
        this.fullWorkFlow = fullWorkFlow;
        this.port = port;
        this.seleniumFileName = seleniumFileName;
        this.novel = novel;
        this.chapter = chapter;
    }

    @Override
    public void run() {
        System.out.println("run : " + Thread.currentThread().getId());
        fullWorkFlow.runProcess(port, seleniumFileName, novel, chapter);

    }
}

