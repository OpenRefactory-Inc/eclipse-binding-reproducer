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

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.openrefactory.core.model.AbstractModelElement;
import org.openrefactory.core.model.IModelElement;
import org.openrefactory.core.model.IModelFolderElement;

/**
 * Issue 37
 * Element to capture a folder inside an eclipse project
 * A folder can be denoted by a package fragment root
 * or package fragment
 *
 * @author Munawar Hafiz
 */
public class EclipseModelFolderElement extends AbstractModelElement implements IModelFolderElement {

    private final IPackageFragmentRoot pkgRoot;
    private final IPackageFragment pkg;
    private String canonicalPath;

    EclipseModelFolderElement(IPackageFragmentRoot root) throws IOException {
        if (root == null) {
            throw new IllegalArgumentException("folder cannot be null");
        }
        this.pkgRoot = root;
        this.pkg = null;
        try {
            IResource file = pkgRoot.getCorrespondingResource();
            if(file != null) this.canonicalPath = file.getRawLocation().toOSString();
        } catch (JavaModelException e) {
            this.canonicalPath = "";
        }
    }

    EclipseModelFolderElement(IPackageFragment pkg) throws IOException {
        if (pkg == null) {
            throw new IllegalArgumentException("folder cannot be null");
        }
        this.pkgRoot = null;
        this.pkg = pkg;
        try {
            IResource file = pkg.getCorrespondingResource();
            this.canonicalPath = file.getRawLocation().toOSString();
        } catch (JavaModelException e) {
            this.canonicalPath = "";
        }
    }

    @Override
    public String getName() {
        if (pkgRoot != null) {
            return pkgRoot.getElementName();
        } else if (pkg != null ) {
            return pkg.getElementName();
        } else {
            return "EMPTY_FOLDER_NAME";
        }
    }

    @Override
    public String getFullPath() {
        if (canonicalPath == null) {
            return "EMPTY_FOLDER_PATH";
        } else {
            return canonicalPath;
        }
    }

    public IJavaElement getContainedElement() {
        if (pkgRoot != null) {
            return pkgRoot;
        } else if (pkg != null) {
            return pkg;
        } else {
            return null;
        }
    }

    @Override
    public Iterable< ? extends IModelElement> getChildren() throws IOException {
        if (pkgRoot != null) {
            return new EclipseFolderChildrenAdapter(pkgRoot);
        } else {
            return new EclipseFolderChildrenAdapter(pkg);
        }
    }

    @Override
    protected String getDescription() {
        return getName() + File.separator;
    }
}
