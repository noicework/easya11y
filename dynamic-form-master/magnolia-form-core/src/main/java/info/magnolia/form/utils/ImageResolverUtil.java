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
package info.magnolia.form.utils;

import info.magnolia.dam.api.Asset;
import info.magnolia.dam.api.AssetProviderRegistry;
import info.magnolia.objectfactory.Components;
import info.magnolia.rest.reference.dam.AssetReferenceResolver;
import info.magnolia.rest.reference.dam.ConfiguredAssetReferenceResolverDefinition;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
/**
 * Image resolver utility class.
 */
public class ImageResolverUtil {

    private ImageResolverUtil(){
        //Private constructor only
    }

    public static Asset getAsset(String assetId){
        if(StringUtils.isNotBlank(assetId)) {
            AssetProviderRegistry assetProviderRegistry = Components.getComponent(AssetProviderRegistry.class);
            ConfiguredAssetReferenceResolverDefinition referenceResolverDefinition = new ConfiguredAssetReferenceResolverDefinition();
            AssetReferenceResolver resolver = new AssetReferenceResolver(assetProviderRegistry, referenceResolverDefinition);

            Optional<Asset> resolvedAsset = resolver.resolve(assetId);
            if(resolvedAsset.isPresent()){
                return resolvedAsset.get();
            }
        }
        return null;
    }
}
