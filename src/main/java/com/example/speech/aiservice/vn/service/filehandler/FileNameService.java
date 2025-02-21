package com.example.speech.aiservice.vn.service.filehandler;

import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class FileNameService {
    public String getAvailableFileName(String directoryPath, String baseFileName, String extension) {
        int fileNumber = 1;
        String fileName = baseFileName + "(" + fileNumber + ")" + extension;

        while (Files.exists(Paths.get(directoryPath + fileName))) {
            fileNumber++;
            fileName = baseFileName + "(" + fileNumber + ")" + extension;
            System.out.println(directoryPath + fileName);
        }
        return directoryPath + fileName;
    }
}
