package com.example.speech.aiservice.vn.model.repository.track;

import com.example.speech.aiservice.vn.model.entity.track.TrackedNovelSingle;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface TrackedNovelSingleRepository extends JpaRepository<TrackedNovelSingle, Long> {
    boolean existsByNovelId(Long novelId);
}
