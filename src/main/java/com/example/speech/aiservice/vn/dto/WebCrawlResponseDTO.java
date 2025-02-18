package com.example.speech.aiservice.vn.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//@Getter
@Setter
@NoArgsConstructor
//@AllArgsConstructor
public class WebCrawlResponseDTO {

    public WebCrawlResponseDTO(String message, String urlWebsite, String filePath) {
        this.message = message;
        this.urlWebsite = urlWebsite;
        this.filePath = filePath;
    }

    @JsonProperty("message")
    private String message;

    @JsonProperty("url_website")
    private String urlWebsite;

    @JsonProperty("file_path")
    private String filePath;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
