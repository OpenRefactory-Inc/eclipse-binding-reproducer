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
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.openrefactory.core.model.AbstractModelElement;
import org.openrefactory.core.model.IModelProjectElement;
import org.openrefactory.core.model.IModelRootElement;

/**
 * Issue 37
 * Element to capture the workspace root of the eclipse model
 *
 * @author Munawar Hafiz
 */
public class EclipseModelRootElement extends AbstractModelElement implements IModelRootElement {

    private final IWorkspaceRoot root;
    private String canonicalPath;

    EclipseModelRootElement(IWorkspaceRoot root) throws IOException {
        if (root == null) {
            throw new IllegalArgumentException("the workspace root cannot be null");
        }
        this.root = root;
        this.canonicalPath = root.getFullPath().makeAbsolute().toOSString();
    }

    @Override
    public String getName() {
        if (root == null) {
            return "EMPTY_ROOT_NAME";
        } else {
            return root.getName();
        }
    }

    @Override
    public String getFullPath() {
        if (canonicalPath == null) {
            return "EMPTY_ROOT_PATH";
        } else {
            return canonicalPath;
        }
    }

    @Override
    protected String getDescription() {
        return getName();
    }

    @Override
    public Iterable< ? extends IModelProjectElement> getChildren() throws IOException {
        // Return an empty iterator if the project is empty
        if (root == null || root.getProjects() == null) {
            return new Iterable<IModelProjectElement>() {

                @Override
                public Iterator<IModelProjectElement> iterator() {
                    return new Iterator<IModelProjectElement>() {
                        @Override
                        public boolean hasNext() {
                            // TODO Auto-generated method stub
                            return false;
                        }
                        @Override
                        public IModelProjectElement next() {
                            // TODO Auto-generated method stub
                            return null;
                        }
                    };
                }
            };
        }
        IJavaProject[] resources = new IJavaProject[root.getProjects().length];
        int index = 0;
        for (IProject project: root.getProjects()) {
            IJavaProject javaProject = JavaCore.create(project);
            resources[index++] = javaProject;
        }

        return new Iterable<IModelProjectElement>() {

            @Override
            public Iterator<IModelProjectElement> iterator() {
                return new Iterator<IModelProjectElement>() {
                    int nextIndex = 0;

                    @Override
                    public boolean hasNext() {
                        return nextIndex < resources.length;
                    }

                    @Override
                    public IModelProjectElement next() {
                        return adapt(resources[nextIndex++]);
                    }


                    private IModelProjectElement adapt(IJavaProject resource) {
                        try {
                            if (resource.exists()) {
                                return new EclipseModelProjectElement(resource);
                            }
                        } catch (IOException e) {
                            throw new IllegalStateException("INTERNAL ERROR: resource is not an file or folder");
                        }
                        throw new IllegalStateException("INTERNAL ERROR: resource is not an file or folder");
                    };
                };
            }
        };
    }
}
