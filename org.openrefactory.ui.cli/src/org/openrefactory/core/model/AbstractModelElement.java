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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;


public abstract class AbstractModelElement implements IModelElement {

    @Override
    public void printOn(PrintStream out) {
        out.print(getDescription());
        try {
            Iterable< ? extends IModelElement> children = getChildren();
            if (children != null) {
                for (IModelElement child : children) {
                    if (child != null) {
                        out.print(System.lineSeparator());
                        out.print(indent(child.toString()));
                    }
                }
            }
        } catch (IOException e) {
            out.print(System.lineSeparator());
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            e.printStackTrace(new PrintWriter(bs));
            out.print(bs.toString());
        }
        out.flush();
    }

    @Override
    public String toString() {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        printOn(new PrintStream(bs));
        return bs.toString();
    }

    private final String indent(String string) {
        return "    " + string.replace(System.lineSeparator(), System.lineSeparator() + "    ");
    }

    protected abstract String getDescription();

    @Override
    public int hashCode() {
        return getFullPath().hashCode() * 7 + getClass().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        else if (!this.getClass().equals(o.getClass()))
            return false;
        else
            return this.getFullPath().equals(((AbstractModelElement)o).getFullPath());
    }
}