/*******************************************************************************
 * Copyright (c) 2015, 2016 James Livingston
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    James Livingston - initial implementation
 *******************************************************************************/
package org.eclipse.mat.snapshot.extension;

import java.util.EnumSet;

import org.eclipse.mat.inspections.collectionextract.ICollectionExtractor;

/**
 * @since 1.6
 */
public class CollectionExtractionInfo
{
    final public String className;
    final public EnumSet<JdkVersion> version;
    final public ICollectionExtractor extractor;

    public CollectionExtractionInfo(String className, ICollectionExtractor extractor)
    {
        this(className, JdkVersion.ALL, extractor);
    }

    public CollectionExtractionInfo(String className, JdkVersion version, ICollectionExtractor extractor)
    {
        this(className, JdkVersion.of(version), extractor);
    }

    public CollectionExtractionInfo(String className, EnumSet<JdkVersion> version, ICollectionExtractor extractor)
    {
        if (className == null)
            throw new IllegalArgumentException();
        if (version == null)
            throw new IllegalArgumentException();
        if (extractor == null)
            throw new IllegalArgumentException();
        this.className = className;
        this.version = version;
        this.extractor = extractor;
    }

    // for debugging purposes
    public String toString() {
        return "CollectionExtractionInfo for " + className + " on versions:" + version.toString();
    }
}
