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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;


public final class NullModel implements IModel {

    private static final String ERROR_MESSAGE = "(OpenRefactory is currently using a null model.  Use Model#useFileModel to configure a model.)";

    private static IModel instance = null;

    public static IModel getInstance() {
        if (instance == null)
            instance = new NullModel();
        return instance;
    }

    private NullModel() { }

    @Override
    public boolean isVirtualFileModel() {
        return false;
    }

    @Override
    public void initialize() {
    }

    @Override
    public void deinitialize() {
    }

    @Override
    public void addListener(IModelListener listener) {
    }

    @Override
    public void removeListener(IModelListener listener) {
    }

    @Override
    public IModelRootElement getRoot() {
        return new IModelRootElement() {
            @Override
            public String getName() {
                return "";
            }
            @Override
            public String getFullPath() {
                return File.separator;
            }
            @Override
            public Iterable<? extends IModelProjectElement> getChildren() {
                return Collections.emptyList();
            }
            @Override
            public void printOn(PrintStream out) {
                out.println(ERROR_MESSAGE);
            }
        };
    }

    @Override
    public void printOn(PrintStream out) {
        out.println(ERROR_MESSAGE);
    }

    @Override
    public String toString() {
        return ERROR_MESSAGE;
    }

    @Override
    public IModelFileElement getFile(String path) {
        return null;
    }

    @Override
    public IModelProjectElement getProjectForPath(String path) {
        return null;
    }

    @Override
    public IModelProjectElement getProjectForElement(IModelElement element) {
        return null;
    }

    @Override
    public Iterable<IModelFileElement> getAllFiles() {
        return Collections.emptyList();
    }

    @Override
    public Iterable<IModelFileElement> getAllSourceFiles(String language) {
        return Collections.emptyList();
    }

    @Override
    public void writeFile(String path, String contents) throws IOException {
        throw new IOException("writeFile not supported");
    }
}
