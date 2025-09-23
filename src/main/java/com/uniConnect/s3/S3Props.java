package com.uniConnect.s3;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@ConfigurationProperties(prefix = "app.s3")
public record S3Props(
        String bucket,
        String keyPrefix,
        Long maxPresignSeconds,
        List<String> allowedContentTypes
) { }
