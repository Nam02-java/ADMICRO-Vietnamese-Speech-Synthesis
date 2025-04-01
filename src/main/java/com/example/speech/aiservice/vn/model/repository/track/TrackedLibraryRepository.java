package com.example.speech.aiservice.vn.model.repository.track;

import com.example.speech.aiservice.vn.model.entity.track.TrackedLibary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrackedLibraryRepository extends JpaRepository<TrackedLibary, Long> {
    boolean existsByNovelId(Long novelId);
}
