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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

/**
 * This components triggers the startup of the {@link GatewayEventConsumer} once the spring application context is fully loaded.
 */
@Order(1000)
public class EventConsumptionStarter {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventConsumptionStarter.class);

    private final GatewayEventConsumer gatewayEventConsumer;

    /**
     * Constructor.
     *
     * @param gatewayEventConsumer given {@link GatewayEventConsumer}
     */
    public EventConsumptionStarter(final GatewayEventConsumer gatewayEventConsumer) {
        this.gatewayEventConsumer = gatewayEventConsumer;
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        LOGGER.info("Starting the gateway event consumption process");
        long startTime = System.currentTimeMillis();
        gatewayEventConsumer.startConsumingEvents();
        LOGGER.info("Gateway event consumption started in {} milliseconds", (System.currentTimeMillis() - startTime));
    }
}
