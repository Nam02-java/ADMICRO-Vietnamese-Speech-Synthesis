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
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class YouTubeUploader {

    private String APPLICATION_NAME = "YouTubeUploader";
    private JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();


    // Load OAuth credentials from client_secret.json file
    private Credential authorize() throws IOException, GeneralSecurityException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                JSON_FACTORY, new FileReader("E:\\CongViecHocTap\\OAuth JSON\\client_secret_234892455390-7277cm147vbvbngcnoigbckcsvjp99d3.apps.googleusercontent.com.json"));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets,
                Collections.singletonList("https://www.googleapis.com/auth/youtube.upload"))
                .setDataStoreFactory(new FileDataStoreFactory(new File("tokens"))) // Lưu token vào local
                .setAccessType("offline")
                .build();

        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }


    // Create YouTube API Service
    private YouTube getService() throws GeneralSecurityException, IOException {
        Credential credential = authorize();
        return new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    // Upload video to YouTube
    public String uploadVideo(String videoFilePath, String title, String description, String tags, String privacyStatus) throws Exception {
        YouTube youtubeService = getService();

        // Configure video metadata
        Video video = new Video();
        VideoStatus status = new VideoStatus();
        status.setPrivacyStatus(privacyStatus); // "public", "private", "unlisted"
        video.setStatus(status);

        VideoSnippet snippet = new VideoSnippet();
        snippet.setTitle(title);
        snippet.setDescription(description);
        snippet.setTags(Collections.singletonList(tags));
        video.setSnippet(snippet);

        File mediaFile = new File(videoFilePath);
        FileContent mediaContent = new FileContent("video/*", mediaFile);

        // Send upload request
        YouTube.Videos.Insert request = youtubeService.videos()
                .insert("snippet,status", video, mediaContent);

        String uploadedVideoURL = null;
        try {
            Video response = request.execute();
            String videoId = response.getId();
            uploadedVideoURL = "https://www.youtube.com/watch?v=" + videoId;
            System.out.println("Video uploaded at the link : " + uploadedVideoURL);
            return uploadedVideoURL;
        } catch (IOException e) {
            throw new RuntimeException("YouTube API Upload Error: " + e.getMessage(), e);
        }
    }
}