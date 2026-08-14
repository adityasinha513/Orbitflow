package com.orbitflow.worker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

/**
 * v1 step execution: a plain webhook callout to the URL registered at
 * workflow-definition time. A later phase can add pluggable executors
 * (Lambda invoke, SQS publish) behind the same StepExecutionOutcome contract.
 */
@Service
@Slf4j
public class WebhookStepExecutor {

    private final RestClient restClient;

    public WebhookStepExecutor(RestClient.Builder restClientBuilder,
                                @Value("${orbitflow.worker.webhook-timeout-ms:5000}") int timeoutMs) {
        this.restClient = restClientBuilder
            .requestFactory(clientRequestFactory(timeoutMs))
            .build();
    }

    public StepExecutionOutcome execute(String callbackUrl, String inputPayload) {
        try {
            restClient.post()
                .uri(callbackUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(inputPayload == null ? "{}" : inputPayload)
                .retrieve()
                .toBodilessEntity();
            return StepExecutionOutcome.success();
        } catch (RestClientException e) {
            log.warn("webhook callout to {} failed: {}", callbackUrl, e.getMessage());
            return StepExecutionOutcome.failure(e.getMessage());
        }
    }

    private static org.springframework.http.client.ClientHttpRequestFactory clientRequestFactory(int timeoutMs) {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(timeoutMs));
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));
        return factory;
    }
}
