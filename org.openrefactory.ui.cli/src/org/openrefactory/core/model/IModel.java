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
import java.io.PrintStream;

public interface IModel {

    public static final String C_LANGUAGE = "C";

    public static final String JAVA_LANGUAGE = "JAVA";

    boolean isVirtualFileModel();

    void initialize();
    void deinitialize();

    void addListener(IModelListener listener);
    void removeListener(IModelListener listener);

    IModelRootElement getRoot();
    IModelFileElement getFile(String path);
    IModelProjectElement getProjectForPath(String path);
    IModelProjectElement getProjectForElement(IModelElement element);
    Iterable<IModelFileElement> getAllFiles();
    Iterable<IModelFileElement> getAllSourceFiles(String language);

    void writeFile(String path, String contents) throws IOException;

    void printOn(PrintStream out);
}
