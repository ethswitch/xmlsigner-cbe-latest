package org.ips.xml.signer.xmlsigner.service;

import org.ips.xml.signer.xmlsigner.configuration.RetryProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

@Service
public class RetryExecutorService {

    private final RetryProperties retryProperties;

    @Autowired
    public RetryExecutorService(RetryProperties retryProperties) {
        this.retryProperties = retryProperties;
    }

    public <T> T executeWithRetry(RetryCallback<T, Exception> callback) throws Exception {
        RetryTemplate retryTemplate = new RetryTemplate();

        // Configure retry policy
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(retryProperties.getMaxAttempts());

        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(retryProperties.getDelay());

        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate.execute(callback);
    }
}
