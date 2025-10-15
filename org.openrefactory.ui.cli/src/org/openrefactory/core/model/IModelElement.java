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

public interface IModelElement {
    String getName();
    String getFullPath();
    Iterable<? extends IModelElement> getChildren() throws IOException;
    void printOn(PrintStream out);
}
