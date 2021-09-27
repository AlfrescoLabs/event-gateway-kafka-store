/*
 * Copyright 2021-2021 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.alfresco.event.gateway.subscription.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import org.alfresco.event.gateway.subscription.exception.FilterConfigurationException;
import org.alfresco.event.gateway.subscription.exception.SubscriptionConfigurationException;
import org.alfresco.event.gateway.subscription.exception.UnsupportedFilterTypeException;
import org.alfresco.event.gateway.subscription.exception.UnsupportedSubscriptionTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler. It constitutes a centralized place for application-wide REST error handling.
 */
@ControllerAdvice
public class GlobalRestExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalRestExceptionHandler.class);

    @ExceptionHandler(EntityNotFoundException.class)
    public void entityNotFoundException(Exception e, HttpServletResponse response) throws IOException {
        LOGGER.error("Entity not found", e);
        response.sendError(HttpStatus.NOT_FOUND.value(), e.getMessage());
    }

    @ExceptionHandler({UnsupportedSubscriptionTypeException.class,
        UnsupportedFilterTypeException.class})
    public void unsupportedSubscriptionTypeException(Exception e, HttpServletResponse response) throws IOException {
        LOGGER.error("Unsupported subscription type", e);
        response.sendError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }

    @ExceptionHandler({SubscriptionConfigurationException.class, FilterConfigurationException.class})
    public void unsupportedConfigurationException(Exception e, HttpServletResponse response) throws IOException {
        LOGGER.error("Unsupported subscription configuration", e);
        response.sendError(HttpStatus.UNPROCESSABLE_ENTITY.value(), e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public void handleValidationExceptions(MethodArgumentNotValidException e, HttpServletResponse response) throws IOException {
        LOGGER.error("Method argument not valid", e);
        List<String> errors = new ArrayList<>();
        e.getBindingResult().getAllErrors().forEach(error -> errors.add(error.getDefaultMessage()));
        response.sendError(HttpStatus.UNPROCESSABLE_ENTITY.value(), String.valueOf(errors));
    }
}
