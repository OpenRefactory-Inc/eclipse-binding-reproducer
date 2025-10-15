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
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public abstract class AbstractModel implements IModel {

    private final List<IModelListener> listeners = new LinkedList<IModelListener>();

    @Override
    public void addListener(IModelListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(IModelListener listener) {
        listeners.remove(listener);
    }

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

    /**
     * @throws Error if an IOException occurs
     */
    @Override
    public Iterable<IModelFileElement> getAllFiles() {
        return getAllSourceFiles(null);
    }

    /**
     * @throws Error if an IOException occurs
     */
    @Override
    public Iterable<IModelFileElement> getAllSourceFiles(final String language) {
        return new Iterable<IModelFileElement>() {
            @Override
            public Iterator<IModelFileElement> iterator() {
                return new FileIterator(getRoot(), language);
            }
        };
    }

    /**
     * @throws IOException if {@link #writeFile(String, String)} is not supported by this model
     */
    @Override
    public void writeFile(String path, String contents) throws IOException {
        throw new IOException("writeFile not supported");
    }

    protected static final class FileIterator implements Iterator<IModelFileElement> {
        private final Queue<IModelElement> worklist;
        private final HashSet<String> seenBefore;
        private final String language;

        public FileIterator(IModelElement root, String language) {
            this.worklist = new ArrayDeque<IModelElement>();
            this.seenBefore = new HashSet<String>();
            this.language = language;
            worklist.add(root);
            ensureHeadOfQueueIsFile();
        }

        private void ensureHeadOfQueueIsFile() {
            while (!worklist.isEmpty() && !(worklist.peek() instanceof IModelFileElement)) {
                IModelElement nextElt = worklist.remove();
                try {
                    for (IModelElement child : nextElt.getChildren()) {
                        if ((language == null || matchesLanguageOrIsContainer(child)) && !seenBefore.contains(child.getFullPath())) {
                            worklist.add(child);
                            seenBefore.add(child.getFullPath());
                        }
                    }
                } catch (IOException e) {
                    throw new Error(e);
                }
            }
        }

        private boolean matchesLanguageOrIsContainer(IModelElement child) {
            if (child instanceof IModelFileElement)
                return ((IModelFileElement)child).matchesLanguage(language);
            else
                return true;
        }

        @Override
        public boolean hasNext() {
            return !worklist.isEmpty();
        }

        @Override
        public IModelFileElement next() {
            assert !worklist.isEmpty() && worklist.peek() instanceof IModelFileElement;
            final IModelFileElement nextElt = (IModelFileElement)worklist.remove();
            ensureHeadOfQueueIsFile();
            return nextElt;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void printOn(PrintStream out) {
        getRoot().printOn(out);
    }

    @Override
    public String toString() {
        try {
            return getRoot().toString();
        } catch (Exception e) {
            return "";
        }
    }
}
