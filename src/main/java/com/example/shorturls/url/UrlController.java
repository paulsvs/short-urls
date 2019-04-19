package com.example.shorturls.url;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.Duration;
import java.util.UUID;

import static org.springframework.http.HttpStatus.MOVED_PERMANENTLY;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.status;

@RestController
public class UrlController {

    @Value("${tracker.cookie.max.age.days:365}")
    private int cookieMaxAgeDays;
    @Value("${tracker.cookie.name}")
    private String cookieName;

    private UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }


    @PostMapping("/url/create")
    public ResponseEntity create(@RequestBody @Valid UrlRequest urlRequest,
                                 @CookieValue(name = "${tracker.cookie.name}", required = false) String cookie,
                                 HttpServletResponse response) {

        if (cookie == null) {
            cookie = createTrackingCookie(response);
        } else {
            if (!isCookieValid(cookie)) {
                cookie = createTrackingCookie(response);
            }
        }

        urlService.create(new Url(urlRequest.shortPath, urlRequest.url.toString()), cookie);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{path}")
    public ResponseEntity redirect(
            @PathVariable String path,
            @CookieValue(name = "${tracker.cookie.name}", required = false) String cookie,
            HttpServletResponse response) {

        if (cookie == null) {
            cookie = createTrackingCookie(response);
        } else {
            if (!isCookieValid(cookie)) {
                cookie = createTrackingCookie(response);
            }
        }

        return urlService.getUrl(path, cookie)
                .map(url -> status(MOVED_PERMANENTLY)
                        .header(HttpHeaders.LOCATION, url)
                        .build())
                .orElse(notFound().build());
    }

    private String createTrackingCookie(HttpServletResponse response) {
        long seconds = Duration.ofDays(cookieMaxAgeDays).toSeconds();
        if (seconds > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Max cookie age converted to seconds exceeds integer range");
        }

        Cookie cookie = new Cookie(cookieName, UUID.randomUUID().toString());
        cookie.setMaxAge((int) seconds);
        response.addCookie(cookie);
        return cookie.getValue();
    }

    private boolean isCookieValid(String cookie) {
        return cookie != null && cookie.length() == 36;
    }

}
