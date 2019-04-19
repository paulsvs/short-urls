package com.example.shorturls.url;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.net.URL;

public class UrlRequest {

    @NotBlank
    @Size(max = 255)
    public String shortPath;

    public URL url;

}
