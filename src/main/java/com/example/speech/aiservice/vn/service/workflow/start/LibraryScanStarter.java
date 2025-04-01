package com.example.speech.aiservice.vn.service.workflow.start;

import com.example.speech.aiservice.vn.service.schedule.TimeDelay;
import com.example.speech.aiservice.vn.service.wait.WaitService;
import com.example.speech.aiservice.vn.service.workflow.previous.LibraryPreProcessorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("library-scan") // Set profile as "library-scan"
public class LibraryScanStarter {
    private final LibraryPreProcessorService libraryPreProcessorService;
    private final WaitService waitService;
    private final TimeDelay timeDelay;

    @Autowired
    public LibraryScanStarter(LibraryPreProcessorService libraryPreProcessorService, WaitService waitService, TimeDelay timeDelay) {
        this.libraryPreProcessorService = libraryPreProcessorService;
        this.waitService = waitService;
        this.timeDelay = timeDelay;
        startMonitoring();
    }

    public void startMonitoring() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    int second = timeDelay.getSecond();
                    waitService.waitForSeconds(5);
                    if (second != 0) {
                        libraryPreProcessorService.startWorkflow(second);
                    }
                }
            }
        }).start();
    }
}
