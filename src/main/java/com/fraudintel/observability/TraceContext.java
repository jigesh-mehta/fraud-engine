package com.fraudintel.observability;

import com.fraudintel.domain.InvestigationRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Optional;
import java.util.UUID;

@Component
public class TraceContext {

    public static final String MDC_KEY = "traceId";
    public static final String REQUEST_ATTR = "traceId";

    /**
     * Binds a stable trace id for the current HTTP request (MDC + request scope).
     * Uses {@link InvestigationRequest#transactionId()} when provided; otherwise generates a UUID.
     * That value is echoed as {@code transactionId} on {@link com.fraudintel.domain.InvestigationResponse}.
     */
    public String bind(InvestigationRequest request) {
        String traceId = resolveTraceId(request);
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            attrs.setAttribute(REQUEST_ATTR, traceId, RequestAttributes.SCOPE_REQUEST);
        }
        MDC.put(MDC_KEY, traceId);
        return traceId;
    }

    public void clear() {
        MDC.remove(MDC_KEY);
    }

    public Optional<String> currentTraceId() {
        String fromMdc = MDC.get(MDC_KEY);
        if (StringUtils.hasText(fromMdc)) {
            return Optional.of(fromMdc);
        }
        return getRequestBoundTraceId();
    }

    private String resolveTraceId(InvestigationRequest request) {
        Optional<String> existing = getRequestBoundTraceId();
        if (existing.isPresent()) {
            return existing.get();
        }

        if (request != null && request.transactionId() != null) {
            return request.transactionId();
        }

        return UUID.randomUUID().toString();
    }

    private Optional<String> getRequestBoundTraceId() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return Optional.empty();
        }
        Object existing = attrs.getAttribute(REQUEST_ATTR, RequestAttributes.SCOPE_REQUEST);
        if (existing instanceof String traceId && StringUtils.hasText(traceId)) {
            return Optional.of(traceId);
        }
        return Optional.empty();
    }
}
