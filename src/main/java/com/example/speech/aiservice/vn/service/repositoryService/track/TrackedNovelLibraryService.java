package com.example.speech.aiservice.vn.service.repositoryService.track;

import com.example.speech.aiservice.vn.model.entity.novel.Novel;
import com.example.speech.aiservice.vn.model.entity.track.TrackedLibary;
import com.example.speech.aiservice.vn.model.entity.track.TrackedNovelSingle;
import com.example.speech.aiservice.vn.model.repository.track.TrackedLibraryRepository;
import com.example.speech.aiservice.vn.service.repositoryService.novel.NovelService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrackedNovelLibraryService {

    private final TrackedLibraryRepository trackedLibraryRepository;
    private final NovelService novelService;

    @Autowired
    public TrackedNovelLibraryService(TrackedLibraryRepository trackedLibraryRepository, NovelService novelService) {
        this.trackedLibraryRepository = trackedLibraryRepository;
        this.novelService = novelService;
    }

    public void trackNovel(Novel novel) {
        trackedLibraryRepository.deleteAll();
        trackedLibraryRepository.save(new TrackedLibary(novel));
    }

    public boolean isTrackNovellExists(String title) {
        Novel novel = novelService.findByTitle(title);
        if (novel != null) {
            return trackedLibraryRepository.existsByNovelId(novel.getId());
        }
        return false;
    }

    public Optional<TrackedLibary> getTrackedNovel() {
        List<TrackedLibary> trackedNovels = trackedLibraryRepository.findAll();
        if (!trackedNovels.isEmpty()) {
            return Optional.of(trackedNovels.get(0));
        }
        return Optional.empty();
    }


    public void clearTracking() {
        trackedLibraryRepository.deleteAll();
    }

    @PreDestroy
    public void clearTrackingOnShutdown() {
        System.out.println("Application is shutting down. Clearing tracked novel table...");
        trackedLibraryRepository.deleteAll();
    }

    @PostConstruct
    public void clearTrackingOnStartup() {
        System.out.println("Application started. Clearing tracked novel table...");
        trackedLibraryRepository.deleteAll();
    }
}


