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
package org.alfresco.event.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Objects;
import javax.persistence.AttributeConverter;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Json converter of configuration data of {@link org.alfresco.event.gateway.entity.Subscription}
 */
public class ConfigJsonConverter implements AttributeConverter<Map<String,String>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String,String> configAsMap) {
        try {
            if (Objects.nonNull(configAsMap)) {
                return objectMapper.writeValueAsString(configAsMap);
            } else {
                return null;
            }
        } catch (JsonProcessingException exception) {
            throw new DataIntegrityViolationException(
                "Cannot convert config map of subscription entity to json data: " + configAsMap.toString(),
                exception);
        }
    }

    @Override
    public Map<String,String> convertToEntityAttribute(String configAsString) {
        try {
            if(Objects.nonNull(configAsString)){
                return objectMapper.readValue(configAsString, Map.class);
            } else {
                return null;
            }
        } catch (JsonProcessingException exception) {
            throw new DataIntegrityViolationException(
                "Cannot convert json data to config map of subscription entity: " + configAsString,
                exception);
        }
    }
}
