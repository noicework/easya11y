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

import info.magnolia.dam.api.Asset;
import info.magnolia.dam.api.AssetDecorator;
import info.magnolia.dam.api.ItemKey;
import info.magnolia.dam.api.metadata.AssetMetadata;
import info.magnolia.form.utils.ImageResolverUtil;
import info.magnolia.rest.reference.dam.AssetWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import lombok.extern.slf4j.Slf4j;

/**
 * Rules serializer.
 */
@Slf4j
public class AssetSerializer extends StdSerializer<String> {

    public AssetSerializer() {
        super(String.class);
    }

    @Override
    public void serialize(String assetLink, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if(assetLink!=null) {
            AssetWriter assetWriter = new AssetWriter();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Asset asset = ImageResolverUtil.getAsset(assetLink);
            if(asset!=null) {
                assetWriter.writeTo(asset, null, null, null, null, null, baos);

                ObjectMapper objectMapper = new ObjectMapper();
                jsonGenerator.writeObject(objectMapper.readTree(baos.toString()));
            } else {
                jsonGenerator.writeString(assetLink);
                log.warn("Asset could not be resolved as there is no asset with this id : {{}}", assetLink);
            }
        } else {
            jsonGenerator.writeStringField(jsonGenerator.getOutputContext().getCurrentName(), null);
        }
    }

    public static void main(String[] args) {
        AssetWriter assetWriter = new AssetWriter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Asset asset = new AssetDecorator(null) {
            @Override
            public String getLink() {
                return "http://test.test";
            }

            @Override
            public String getTitle() {
                return "title";
            }

            @Override
            public String getSubject() {
                return "subject";
            }

            @Override
            public String getName() {
                return "name";
            }

            @Override
            public String getPath() {
                return "path";
            }

            @Override
            public ItemKey getItemKey() {
                return ItemKey.from("jcr:35454-549-8545");
            }

            @Override
            public String getFileName() {
                return "filename";
            }

            @Override
            public String getMimeType() {
                return "mimetype";
            }

            @Override
            public String getCaption() {
                return "caption";
            }

            @Override
            public String getComment() {
                return "comment";
            }

            @Override
            public long getFileSize() {
                return 666;
            }

            @Override
            public <M extends AssetMetadata> boolean supports(Class<M> metaDataType) {
                return false;
            }
        };


        try {
            assetWriter.writeTo(asset, null, null, null, null, null, baos);
            String assetStringified = baos.toString();
            System.out.println(assetStringified);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
