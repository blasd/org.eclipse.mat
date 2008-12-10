/*******************************************************************************
 * Copyright (c) 2008 SAP AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 *    Chris Grindstaff
 *******************************************************************************/
package org.eclipse.mat.inspections.eclipse;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.extension.IClassSpecificNameResolver;
import org.eclipse.mat.snapshot.extension.Subject;
import org.eclipse.mat.snapshot.model.IObject;

public class EclipseNameResolver
{
    @Subject("org.eclipse.core.runtime.adaptor.EclipseClassLoader")
    public static class EclipseClassLoaderResolver implements IClassSpecificNameResolver
    {

        public String resolve(IObject obj) throws SnapshotException
        {
            IObject s = (IObject) obj.resolveValue("hostdata.symbolicName");
            return s != null ? s.getClassSpecificName() : null;
        }

    }

    @Subject("org.eclipse.osgi.internal.baseadaptor.DefaultClassLoader")
    public static class EclipseDefaultClassLoaderResolver implements IClassSpecificNameResolver
    {

        public String resolve(IObject obj) throws SnapshotException
        {
            IObject s = (IObject) obj.resolveValue("manager.data.symbolicName");
            return s != null ? s.getClassSpecificName() : null;
        }

    }

    @Subject("org.eclipse.equinox.launcher.Main$StartupClassLoader")
    public static class StartupClassLoaderResolver implements IClassSpecificNameResolver
    {

        public String resolve(IObject obj) throws SnapshotException
        {
            return "Equinox Startup Class Loader";
        }

    }

    @Subject("org.eclipse.swt.graphics.RGB")
    public static class RGBResolver implements IClassSpecificNameResolver
    {
        public String resolve(IObject obj) throws SnapshotException
        {
            Integer red = (Integer) obj.resolveValue("red");
            Integer blue = (Integer) obj.resolveValue("blue");
            Integer green = (Integer) obj.resolveValue("green");

            if (red == null || blue == null || green == null)
                return null;

            return String.format("(%03d,%03d,%03d)", red, blue, green);
        }
    }
}
