package com.example.speech.aiservice.vn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
//@AllArgsConstructor
public class TextToSpeechResponseDTO {

    public TextToSpeechResponseDTO(String message, String urlWebsite, String urlDownload, String filePath) {
        this.message = message;
        this.urlDownload = urlDownload;
        this.urlWebsite = urlWebsite;
        this.filePath = filePath;
    }

    @JsonProperty("message")
    private String message;

    @JsonProperty("url_website")
    private String urlWebsite;

    @JsonProperty("url_download")
    private String urlDownload;

    @JsonProperty("file_path")
    private String filePath;
}
