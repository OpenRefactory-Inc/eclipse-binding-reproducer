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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.openrefactory.core.model.IModelElement;

/**
 * Issue 37
 * Adapter to create an iterator for model elements
 *
 * @author Munawar Hafiz
 */
public class EclipseFolderChildrenAdapter implements Iterable<IModelElement> {
    private final List<ICompilationUnit> resources = new ArrayList<>();

    public EclipseFolderChildrenAdapter(final IPackageFragmentRoot container) throws IOException {
        collectAccessibleResources(container);
    }

    public EclipseFolderChildrenAdapter(final IPackageFragment container) throws IOException {
        collectAccessibleResources(container);
    }

    private void collectAccessibleResources(final IJavaElement container) throws IOException {
        List<IPackageFragment> pkgs = new ArrayList<>();
        try {
            if (container instanceof IPackageFragmentRoot) {
                if (((IPackageFragmentRoot)container).getChildren() != null) {
                    for (IJavaElement temp: ((IPackageFragmentRoot)container).getChildren()) {
                        if (temp instanceof IPackageFragment) {
                            pkgs.add((IPackageFragment)temp);
                        } else if (temp instanceof ICompilationUnit) {
                            if (temp.exists()) {
                                resources.add((ICompilationUnit)temp);
                            }
                        }
                    }
                }
            } else if (container instanceof IPackageFragment) {
                pkgs.add((IPackageFragment)container);
            }

            for (IPackageFragment pkg: pkgs) {
                if (pkg.getCompilationUnits() != null) {
                    for (ICompilationUnit icu: pkg.getCompilationUnits()) {
                        if (icu.exists()) {
                            resources.add(icu);
                        }
                    }
                }
            }
        } catch (JavaModelException e) {
            // Do nothing
        }
    }

    @Override
    public Iterator<IModelElement> iterator() {
        return new Iterator<IModelElement>() {
            int nextIndex = 0;

            @Override
            public boolean hasNext() {
                return nextIndex < resources.size();
            }

            @Override
            public IModelElement next() {
                return adapt(resources.get(nextIndex++));
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

        };
    }

    private IModelElement adapt(ICompilationUnit resource) {
        IModelElement result = null;
        try {
            if (resource.exists()) {
                result = new EclipseModelFileElement(resource);
            }
        } catch (IOException e) {
        }
        if (result == null) {
            throw new IllegalStateException("INTERNAL ERROR: resource is not an file or folder");
        } else {
            return result;
        }
    }
}
