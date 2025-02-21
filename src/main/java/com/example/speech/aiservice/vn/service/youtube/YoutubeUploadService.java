package com.example.speech.aiservice.vn.service.youtube;


import com.example.speech.aiservice.vn.dto.response.YoutubeUploadResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;

@Service
public class YoutubeUploadService {

    private final OAuthHelper oAuthHelper;
    private final YouTubeUploader youTubeUploader;

    @Autowired
    public YoutubeUploadService(OAuthHelper oAuthHelper, YouTubeUploader youTubeUploader) {
        this.oAuthHelper = oAuthHelper;
        this.youTubeUploader = youTubeUploader;
    }


    public YoutubeUploadResponseDTO upload(String videoFilePath) {
        try {
            String title = Paths.get(videoFilePath).getFileName().toString();
            String description = "https://speech.aiservice.vn/tts/tools/demo";
            String tags = "api, java, upload";
            String privacyStatus = "public"; // "public", "private", "unlisted"

            String uploadVideoURL = youTubeUploader.uploadVideo(videoFilePath, title, description, tags, privacyStatus);

            return new YoutubeUploadResponseDTO("Video uploaded successfully", uploadVideoURL, videoFilePath);
        } catch (Exception e) {
            throw new RuntimeException("Upload failed: " + e.getMessage(), e);
        }
    }
}

