package com.example.speech.aiservice.vn.controller.http.restful;

import com.example.speech.aiservice.vn.dto.request.CreateVideoRequestDTO;
import com.example.speech.aiservice.vn.dto.response.CreateVideoResponseDTO;
import com.example.speech.aiservice.vn.service.video.VideoCreationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/video")
public class CreateVideoController {

    private final VideoCreationService videoCreationService;

    @Autowired
    public CreateVideoController(VideoCreationService videoCreationService) {
        this.videoCreationService = videoCreationService;

    }

    @PostMapping("/create")
    public ResponseEntity<CreateVideoResponseDTO> createVideo(@RequestBody CreateVideoRequestDTO createVideoRequestDTO) {
        try {
            CreateVideoResponseDTO response = videoCreationService.createVideoResponseDTO(createVideoRequestDTO.getAudioPath(), createVideoRequestDTO.getImagePath());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new CreateVideoResponseDTO("Error", null, null, null));
        }
    }
}
