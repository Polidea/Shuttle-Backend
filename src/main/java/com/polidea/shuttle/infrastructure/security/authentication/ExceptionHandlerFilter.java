package com.polidea.shuttle.infrastructure.security.authentication;

import com.polidea.shuttle.error_codes.ErrorResponseBody;
import com.polidea.shuttle.error_codes.ShuttleException;
import com.polidea.shuttle.infrastructure.Logging;
import com.polidea.shuttle.infrastructure.json.JsonUtils;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Catches all exceptions thrown during authentication
 * Should be explicitly put in the {@link org.springframework.security.web.SecurityFilterChain}.
 *
 * @see com.polidea.shuttle.configuration.SecurityConfiguration
 */
public class ExceptionHandlerFilter extends OncePerRequestFilter {

    private final static Logger LOGGER = getLogger(ExceptionHandlerFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        try {
            chain.doFilter(request, response);
        } catch (Exception exception) {
            handleException(exception, request, response);
        }
    }

    private void handleException(Exception exception,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws IOException {
        Logging.logException(LOGGER, exception);
        if (exception instanceof ShuttleException) {
            handleException((ShuttleException) exception, request, response);
        } else {
            handleException(exception, request, response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void handleException(ShuttleException shuttleException,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws IOException {
        response.setStatus(shuttleException.getHttpStatus().value());
        response.setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE);
        response.getWriter().write(JsonUtils.toJsonString(ErrorResponseBody.prepareErrorResponseBody(
            shuttleException,
            request
        )));
    }

    private void handleException(Exception exception,
                                 HttpServletRequest request,
                                 HttpServletResponse response,
                                 HttpStatus httpStatus) throws IOException {
        response.setStatus(httpStatus.value());
        response.setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE);
        response.getWriter().write(JsonUtils.toJsonString(ErrorResponseBody.prepareErrorResponseBody(
            httpStatus.getReasonPhrase(),
            exception,
            httpStatus,
            request
        )));
    }
}
