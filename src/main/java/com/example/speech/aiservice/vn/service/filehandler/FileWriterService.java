package com.example.speech.aiservice.vn.service.filehandler;

import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

@Service
public class FileWriterService {

    public void writeToFile(String directoryPath, String fileName, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(directoryPath + fileName))) {
            writer.write(content);
            System.out.println("The content has been downloaded and saved as :" + directoryPath + fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
