package com.example.speech.aiservice.vn.service.queue;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component // Integrated into Spring ecosystem
public class ScanQueue {
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    public void addToQueue(String url) {
        try {
            queue.put(url);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Failed to add to queue: " + e.getMessage());
        }
    }

    public String takeFromQueue() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Failed to take from queue: " + e.getMessage());
            return null;
        }
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public void printQueue() {
        System.out.println("Current Queue: " + Arrays.toString(queue.toArray()));
    }
}
