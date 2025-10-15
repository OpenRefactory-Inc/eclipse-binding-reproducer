/*************************************************************************
 *
 * OPENREFACTORY CONFIDENTIAL
 * __________________
 *
 * Copyright (c) 2019 OpenRefactory, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of OpenRefactory, Inc. The
 * intellectual and technical concepts contained herein are proprietary to OpenRefactory, Inc. and
 * may be covered by U.S. and Foreign Patents, patents in process, and are protected by trade secret
 * or copyright law. Dissemination of this information or reproduction of this material is strictly
 * forbidden unless prior written permission is obtained from OpenRefactory, Inc.
 *
 * Contributors: Munawar Hafiz (OpenRefactory, Inc.) - Initial API and implementation
 *******************************************************************************/
package org.openrefactory.internal.core.model.eclipse;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.openrefactory.core.model.AbstractModelElement;
import org.openrefactory.core.model.IModel;
import org.openrefactory.core.model.IModelElement;
import org.openrefactory.core.model.IModelFileElement;

/**
 * Issue 37
 * Element to capture a file inside an eclipse project
 *
 * @author Munawar Hafiz
 */
public class EclipseModelFileElement extends AbstractModelElement implements IModelFileElement {

    private final ICompilationUnit cu;
    private String canonicalPath;

    EclipseModelFileElement(ICompilationUnit cu) throws IOException {
        if (cu == null) {
            throw new IllegalArgumentException("file cannot be null");
        }
        this.cu = cu;
        try {
            IResource file = cu.getCorrespondingResource();
            this.canonicalPath = file.getRawLocation().toOSString();
        } catch (JavaModelException e) {
            this.canonicalPath = "";
        }
    }

    @Override
    public String getName() {
        if (cu == null) {
            return "EMPTY_FILE_NAME";
        } else {
            return cu.getElementName();
        }
    }

    @Override
    public String getFullPath() {
        if (canonicalPath == null) {
            return "EMPTY_FILE_PATH";
        } else {
            return canonicalPath;
        }
    }

    public ICompilationUnit getCompilationUnit() {
        return cu;
    }

    @Override
    public Iterable< ? extends IModelElement> getChildren() throws IOException {
        return Collections.emptyList();
    }

    @Override
    public int compareTo(IModelFileElement that) {
        if (that == null || !this.getClass().equals(that.getClass())) {
            throw new IllegalArgumentException();
        } else {
            return this.getFullPath().compareTo(that.getFullPath());
        }
    }

    @Override
    public String getFilenameExtension() {
        String name = getName();
        int indexOfLastPeriod = name.lastIndexOf('.');
        if (indexOfLastPeriod <= 0) {
            // Filename does not have an extension (e.g., /etc/passwd)
            // or is a Unix-style hidden file (e.g., /home/user/.bashrc)
            return null;
        } else {
            return name.substring(indexOfLastPeriod + 1);
        }
    }

    @Override
    public long getModificationTime() {
        try {
            IResource file = cu.getCorrespondingResource();
            return file.getModificationStamp();
        } catch (JavaModelException e) {
            return 0;
        }
    }

    @Override
    public Reader getContents() throws IOException {
        Reader targetReader;
        try {
            targetReader = new StringReader(cu.getSource());
        } catch (JavaModelException e) {
            targetReader = new StringReader("");        }
        return targetReader;
    }

    @Override
    public boolean matchesLanguage(String language) {
        if (IModel.C_LANGUAGE.equals(language)) {
            return getFilenameExtension() != null
                && getFilenameExtension().equals("c");
        } else if (IModel.JAVA_LANGUAGE.equals(language)) {
            return getFilenameExtension() != null
                && getFilenameExtension().equals("java");
        } else {
            return false;
        }
    }

    @Override
    protected String getDescription() {
        return getName();
    }

    @Override
    public int hashCode() {
        return getFullPath().hashCode();
    }

    @Override
    public boolean equals(Object that) {
        if (this == that)
            return true;
        else if (that == null || !this.getClass().equals(that.getClass()))
            return false;
        else
            return this.getFullPath().equals(((EclipseModelFileElement)that).getFullPath());
    }
}
