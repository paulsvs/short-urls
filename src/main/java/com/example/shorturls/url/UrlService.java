package com.example.shorturls.url;

import com.example.shorturls.tracker.Tracker;
import com.example.shorturls.tracker.TrackerRepository;
import com.example.shorturls.user.User;
import com.example.shorturls.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UrlService {

    private UrlRepository urlRepository;
    private TrackerRepository trackerRepository;
    private UserRepository userRepository;

    public UrlService(UrlRepository urlRepository, TrackerRepository trackerRepository, UserRepository userRepository) {
        this.urlRepository = urlRepository;
        this.trackerRepository = trackerRepository;
        this.userRepository = userRepository;
    }


    @Transactional
    public void create(Url url, String cookie) {
        Url savedUrl = urlRepository.save(url);
        User savedUser = userRepository.findById(cookie).orElseGet(() -> userRepository.save(new User(cookie)));
        trackerRepository.save(new Tracker(savedUrl, savedUser, Tracker.Action.CREATED));
    }

    @Transactional
    public Optional<String> getUrl(String path, String cookie) {
        Optional<Url> urlOpt = urlRepository.findById(path);
        User user = userRepository.findById(cookie).orElseGet(() -> userRepository.save(new User(cookie)));

        if (urlOpt.isPresent()) {
            Url url = urlOpt.get();

            trackerRepository.save(new Tracker(url, user, Tracker.Action.ACCESSED));

            return Optional.of(url.getUrl());
        } else {
            trackerRepository.save(new Tracker(user, path, Tracker.Action.ACCESSED));
        }

        return Optional.empty();
    }

}
