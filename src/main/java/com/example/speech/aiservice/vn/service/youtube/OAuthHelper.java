package com.example.speech.aiservice.vn.service.youtube;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class OAuthHelper {
    private  String CLIENT_SECRET_FILE = "E:\\CongViecHocTap\\OAuth JSON\\client_secret_267380930072-coupu92dabi2djrnpv3a2rdcn319grmv.apps.googleusercontent.com.json"; // ⚠️ Thay bằng file OAuth JSON của bạn
    private  String TOKENS_DIRECTORY_PATH = "E:\\CongViecHocTap\\Token"; // Save token after first login

    private JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private Credential authorize() throws IOException, GeneralSecurityException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        // Load client_secret.json
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                JSON_FACTORY, new FileReader(CLIENT_SECRET_FILE));

        // Create authentication flow
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets,
                Collections.singletonList(YouTubeScopes.YOUTUBE_UPLOAD))
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        // Open a browser to authenticate
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver.Builder().setPort(8888).build()).authorize("user");
    }

    private YouTube getService() throws IOException, GeneralSecurityException {
        return new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, authorize())
                .setApplicationName("YouTubeUploader")
                .build();
    }
}
