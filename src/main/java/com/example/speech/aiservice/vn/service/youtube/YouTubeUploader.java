package com.example.speech.aiservice.vn.service.youtube;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class YouTubeUploader {

    private final OAuthHelper oAuthHelper;

    @Autowired
    public YouTubeUploader(OAuthHelper oAuthHelper) {
        this.oAuthHelper = oAuthHelper;
    }

    // Upload video lên YouTube
    public String uploadVideo(String videoFilePath, String title, String description, String tags, String privacyStatus) throws Exception {
        YouTube youtubeService = oAuthHelper.getService();

        // Cấu hình metadata video
        Video video = new Video();
        VideoStatus status = new VideoStatus();
        status.setPrivacyStatus(privacyStatus);
        video.setStatus(status);

        VideoSnippet snippet = new VideoSnippet();
        snippet.setTitle(title);
        snippet.setDescription(description);
        snippet.setTags(Collections.singletonList(tags));
        video.setSnippet(snippet);

        File mediaFile = new File(videoFilePath);
        FileContent mediaContent = new FileContent("video/*", mediaFile);

        // Gửi request upload
        YouTube.Videos.Insert request = youtubeService.videos().insert("snippet,status", video, mediaContent);

        try {
            Video response = request.execute();
            String videoId = response.getId();
            String uploadedVideoURL = "https://www.youtube.com/watch?v=" + videoId;
            System.out.printf("%s - uploaded at: %s%n", title, uploadedVideoURL);
            return uploadedVideoURL;
        } catch (IOException e) {
            throw new RuntimeException("YouTube API Upload Error: " + e.getMessage(), e);
        }
    }
}
