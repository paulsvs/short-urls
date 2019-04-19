package com.example.shorturls.tracker;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class TrackerController {

    private TrackerRepository trackerRepository;

    public TrackerController(TrackerRepository trackerRepository) {
        this.trackerRepository = trackerRepository;
    }

    @GetMapping("/tracker/data")
    public List<Tracker> getTrackedData() {
        List<Tracker> all = new ArrayList<>();
        trackerRepository.findAll().forEach(all::add);
        return all;
    }

}
