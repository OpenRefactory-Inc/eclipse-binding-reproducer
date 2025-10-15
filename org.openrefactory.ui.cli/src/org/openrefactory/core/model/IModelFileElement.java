/*******************************************************************************
 * Copyright (c) 2012 Auburn University and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jeff Overbey (Auburn) - Initial API and implementation
 *******************************************************************************/
package org.openrefactory.core.model;

import java.io.IOException;
import java.io.Reader;

public interface IModelFileElement extends IModelElement, Comparable<IModelFileElement> {
    /**
     * 
     * @return (possibly <code>null</code>)
     */
    String getFilenameExtension();
    long getModificationTime();
    Reader getContents() throws IOException;
    boolean matchesLanguage(String language);
}
