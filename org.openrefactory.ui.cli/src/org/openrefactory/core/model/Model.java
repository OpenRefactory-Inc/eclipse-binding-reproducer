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

public final class Model {

    private static IModel instance = NullModel.getInstance();

    public static IModel getInstance() {
        return instance;
    }

    public static void useModel(IModel model) {
        instance = (model == null ? NullModel.getInstance() : model);
        instance.initialize();
    }
    
    private Model() {;}
}
