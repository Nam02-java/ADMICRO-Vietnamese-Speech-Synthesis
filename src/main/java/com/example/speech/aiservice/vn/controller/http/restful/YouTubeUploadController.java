package com.example.speech.aiservice.vn.controller.http.restful;

import com.example.speech.aiservice.vn.dto.request.UploadVideoRequestDTO;
import com.example.speech.aiservice.vn.dto.response.YoutubeUploadResponseDTO;
import com.example.speech.aiservice.vn.service.youtube.YoutubeUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/youtube")
public class YouTubeUploadController {

    private final YoutubeUploadService youtubeUploadService;


    @Autowired
    public YouTubeUploadController(YoutubeUploadService youtubeUploadService) {
        this.youtubeUploadService = youtubeUploadService;
    }

    @PostMapping("/upload")
    public ResponseEntity<YoutubeUploadResponseDTO> uploadVideo(@RequestBody UploadVideoRequestDTO uploadRequestDTO) {
        try {
            //YoutubeUploadResponseDTO response = youtubeUploadService.upload(uploadRequestDTO.getVideoFilePath());
            //  return ResponseEntity.ok(response);
            return null;
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new YoutubeUploadResponseDTO("INTERNAL_SERVER_ERROR", null, uploadRequestDTO.getVideoFilePath()));
        }
    }
}
