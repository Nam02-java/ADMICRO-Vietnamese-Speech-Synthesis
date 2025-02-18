package com.example.speech.aiservice.vn.service.google;

import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GoogleChromeLauncherService {

    private Process process;
    private String chromePath = "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";
    private String userDataDir = "C:\\selenium_chrome";

    public void openGoogleChrome() throws IOException {
        String command = chromePath + " --remote-debugging-port=9222 --user-data-dir=" + userDataDir + " --headless";
        process = Runtime.getRuntime().exec(command);
    }

    public void shutdown() {
        process.destroy();
    }
}
