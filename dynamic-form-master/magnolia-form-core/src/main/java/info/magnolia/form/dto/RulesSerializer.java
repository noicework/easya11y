/**
 * This file Copyright (c) 2022 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This program and the accompanying materials are made
 * available under the terms of the Magnolia Network Agreement
 * which accompanies this distribution, and is available at
 * http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.form.dto;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
/**
 * Rules serializer.
 */
public class RulesSerializer extends StdSerializer<String> {

    public RulesSerializer() {
        super(String.class);
    }

    @Override
    public void serialize(String rules, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        jsonGenerator.writeObject(objectMapper.readTree(rules));
    }
}
