package com.example.shorturls;

import com.example.shorturls.tracker.Tracker;
import com.example.shorturls.url.UrlRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static com.example.shorturls.tracker.Tracker.Action.ACCESSED;
import static com.example.shorturls.tracker.Tracker.Action.CREATED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"logging.level.o.a.coyote.http11.Http11InputBuffer=debug", "logging.level.org.apache.http.wire=debug"})
public class ShortUrlsApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DirtiesContext
    public void createLink_openLink_checkTracking() throws JsonProcessingException, MalformedURLException {
        UrlRequest request = new UrlRequest();
        request.shortPath = "test";
        request.url = new URL("http://google.com");

        // create URL
        ResponseEntity<Void> postResp = restTemplate.postForEntity("/url/create", request, Void.class);
        List<String> cookies = postResp.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertThat(postResp.getStatusCode()).isEqualTo(OK);
        assertThat(cookies).isNotEmpty();
        assertThat(cookies.size()).isEqualTo(1);

        // creator accesses/tests URL
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, cookies.get(0));
        ResponseEntity<Void> getResp = restTemplate.exchange("/" + request.shortPath, HttpMethod.GET, new HttpEntity<>(headers), Void.class);
        assertThat(getResp.getHeaders().getLocation().toString()).isEqualTo(request.url.toString());
        assertThat(getResp.getStatusCode()).isEqualTo(MOVED_PERMANENTLY);

        // access created URL with no cookie
        getResp = restTemplate.getForEntity("/" + request.shortPath, Void.class);
        assertThat(getResp.getHeaders().getLocation().toString()).isEqualTo(request.url.toString());
        assertThat(getResp.getStatusCode()).isEqualTo(MOVED_PERMANENTLY);

        // check tracking
        ResponseEntity<Tracker[]> trackerResp = restTemplate.getForEntity("/tracker/data", Tracker[].class);
        assertThat(trackerResp.getStatusCode()).isEqualTo(OK);
        Tracker[] trackerBody = trackerResp.getBody();
        assertThat(trackerBody.length).isEqualTo(3);
        System.out.println("Tracker pretty print: " + prettyPrint(trackerBody));

        Tracker trackerEvent1 = trackerBody[0];
        assertThat(trackerEvent1.getAction()).isEqualTo(CREATED);
        assertThat(trackerEvent1.getUrl()).isNotNull();
        assertThat(trackerEvent1.getUser()).isNotNull();

        Tracker trackerEvent2 = trackerBody[1];
        assertThat(trackerEvent2.getAction()).isEqualTo(ACCESSED);
        assertThat(trackerEvent2.getTimestamp()).isGreaterThan(trackerEvent1.getTimestamp());

        assertThat(trackerEvent1.getUser().getCookie()).isEqualTo(trackerEvent2.getUser().getCookie());
        assertThat(trackerEvent1.getUser().getCreatedTimestamp()).isEqualTo(trackerEvent2.getUser().getCreatedTimestamp());
        assertThat(trackerEvent1.getUrl().getShortUrl()).isEqualTo(trackerEvent2.getUrl().getShortUrl());
        assertThat(trackerEvent1.getUrl().getUrl()).isEqualTo(trackerEvent2.getUrl().getUrl());
        assertThat(trackerEvent1.getUrl().getCreatedTimestamp()).isEqualTo(trackerEvent2.getUrl().getCreatedTimestamp());

        Tracker trackerEvent3 = trackerBody[2];
        assertThat(trackerEvent3.getAction()).isEqualTo(ACCESSED);
        assertThat(trackerEvent3.getTimestamp()).isGreaterThan(trackerEvent2.getTimestamp());
        assertThat(trackerEvent3.getUser().getCookie()).isNotEqualTo(trackerEvent2.getUser().getCookie());
        assertThat(trackerEvent3.getUser().getCreatedTimestamp()).isNotEqualTo(trackerEvent2.getUser().getCreatedTimestamp());
    }

    @Test
    @DirtiesContext
    public void openNonExistingLink_checkTracking() throws JsonProcessingException, MalformedURLException {
        UrlRequest request = new UrlRequest();
        request.shortPath = "test";
        request.url = new URL("http://google.com");

        ResponseEntity<Void> getResp = restTemplate.getForEntity("/" + request.shortPath, Void.class);
        assertThat(getResp.getStatusCode()).isEqualTo(NOT_FOUND);

        ResponseEntity<Tracker[]> trackerResp = restTemplate.getForEntity("/tracker/data", Tracker[].class);
        assertThat(trackerResp.getStatusCode()).isEqualTo(OK);
        Tracker[] trackerBody = trackerResp.getBody();
        assertThat(trackerBody.length).isEqualTo(1);
        System.out.println("Tracker pretty print: " + prettyPrint(trackerBody));

        Tracker trackerEvent = trackerBody[0];
        assertThat(trackerEvent.getAction()).isEqualTo(ACCESSED);
        assertThat(trackerEvent.getNonExistingPath()).isEqualTo(request.shortPath);
        assertThat(trackerEvent.getUrl()).isNull();
        assertThat(trackerEvent.getUser()).isNotNull();
    }

    private String prettyPrint(Object obj) throws JsonProcessingException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    }

}
