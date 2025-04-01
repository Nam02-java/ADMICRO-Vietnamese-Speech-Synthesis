package com.example.speech.aiservice.vn.model.entity.track;

import com.example.speech.aiservice.vn.model.entity.novel.Novel;
import jakarta.persistence.*;

@Entity
@Table(name = "tracked_novel_library")
public class TrackedLibary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "novel_id", referencedColumnName = "id", nullable = false)
    private Novel novel;

    public TrackedLibary() {
    }

    public TrackedLibary(Novel novel) {
        this.novel = novel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Novel getNovel() {
        return novel;
    }

    public void setNovel(Novel novel) {
        this.novel = novel;
    }
}
