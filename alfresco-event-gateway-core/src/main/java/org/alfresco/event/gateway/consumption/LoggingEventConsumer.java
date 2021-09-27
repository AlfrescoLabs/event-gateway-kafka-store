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
package org.alfresco.event.gateway.consumption;

import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.repo.event.v1.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link EventConsumer} implementation for logging purposes. It simply show a <code>logLevel</code> (defaulted to <code>INFO</code>) message in the log with
 * the detail of the event consumed.
 * <p>
 * It automatically registers itself in the {@link EventConsumerRegistry} if the property <code>alfresco.event.gateway.loggingEventConsumer.autoRegister</code>
 * is set to <code>true</code>.
 */
public class LoggingEventConsumer extends AbstractEventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingEventConsumer.class);

    private final String logLevel;
    private final String messageTemplate;

    /**
     * Constructor.
     *
     * @param eventConsumerRegistry given {@link EventConsumerRegistry}
     */
    public LoggingEventConsumer(final EventConsumerRegistry eventConsumerRegistry,
                                final Boolean autoRegisterEnabled,
                                final String logLevel,
                                final String messageTemplate) {
        super(eventConsumerRegistry, autoRegisterEnabled);
        this.logLevel = logLevel;
        this.messageTemplate = messageTemplate;
    }


    @Override
    public void consumeEvent(RepoEvent<DataAttributes<Resource>> event) {
        switch (logLevel) {
            case "TRACE":
                LOGGER.trace(messageTemplate, event);
            case "DEBUG":
                LOGGER.debug(messageTemplate, event);
            case "WARN":
                LOGGER.warn(messageTemplate, event);
            case "ERROR":
                LOGGER.error(messageTemplate, event);
            default:
                LOGGER.info(messageTemplate, event);
        }
    }
}
