package com.example.speech.aiservice.vn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
//@AllArgsConstructor
public class FullProcessResponseDTO {

    public FullProcessResponseDTO(WebCrawlResponseDTO webCrawlResponseDTO, TextToSpeechResponseDTO textToSpeechResponseDTO) {
        this.webCrawlResponseDTO = webCrawlResponseDTO;
        this.textToSpeechResponseDTO = textToSpeechResponseDTO;
    }

    @JsonProperty("web_crawl_response")
    private WebCrawlResponseDTO webCrawlResponseDTO;

    @JsonProperty("text_to_speech_response")
    private TextToSpeechResponseDTO textToSpeechResponseDTO;


}

