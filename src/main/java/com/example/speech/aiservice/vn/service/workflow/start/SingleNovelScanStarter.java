package com.example.speech.aiservice.vn.service.workflow.start;

import com.example.speech.aiservice.vn.service.schedule.TimeDelay;
import com.example.speech.aiservice.vn.service.wait.WaitService;
import com.example.speech.aiservice.vn.service.workflow.previous.SingleNovelPreProcessorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("single-novel") // Set profile to "single-novel"
public class SingleNovelScanStarter {
    private final SingleNovelPreProcessorService singleNovelPreProcessorService;
    private final WaitService waitService;
    private final TimeDelay timeDelay;

    @Autowired
    public SingleNovelScanStarter(SingleNovelPreProcessorService singleNovelPreProcessorService, WaitService waitService, TimeDelay timeDelay) {
        this.singleNovelPreProcessorService = singleNovelPreProcessorService;
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
                        singleNovelPreProcessorService.startWorkflow(second);
                    }
                }
            }
        }).start();
    }
}
