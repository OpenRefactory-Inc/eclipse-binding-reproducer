package com.openrefactory.ui.cli;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.openrefactory.core.model.IModel;
import org.openrefactory.core.model.IModelFileElement;
import org.openrefactory.core.model.Model;
import org.openrefactory.internal.core.model.eclipse.EclipseModel;

/**
 * OpenRefactory daemon.
 * 
 * @author Munawar Hafiz
 */
public class ORDaemon implements IApplication {
    
    
    public static Object lock = new Object();
    
    public ORDaemon() throws IOException {
    }
    
    /**
     * Entry method based on the extension point of
     * org.eclipse.core.runtime.applications
     * 
     * See manifest file and plugin.xml for more
     */
    @Override
    public Object start(IApplicationContext context) throws Exception {
        // Bug 4492
        // Run this by uncommenting the next few lines all the way to the line
        //     // Bug 4493
        // and commenting all the lines upto the end of the method.
        String currentProjectPath = "/Users/munawar/Desktop/orjava/source/binding-reproducrer-project-bug4492";
        File currentProjectDirectory = new File(currentProjectPath);
        try {
            Model.useModel(new EclipseModel(currentProjectDirectory));
        } catch (CoreException e) {
            e.printStackTrace();
        }
        for (IModelFileElement file : Model.getInstance().getAllSourceFiles(IModel.JAVA_LANGUAGE)) {
            final String fullPath = file.getFullPath();
            CompilationUnit cu = ASTNodeUtilities.parse(fullPath);
            ICompilationUnit icu = (ICompilationUnit)cu.getJavaElement();
            if (icu == null) {
                System.err.println("MH: KABOOM ICompilationUnit not found");
            }
            String path = ASTNodeUtilities.getFilePathFromCompilationUnit(cu);
            if (path == null) {
                System.err.println("MH: KABOOM Path not found");
            }
            if (cu != null) {
                for (ClassInstanceCreation cic : ASTNodeUtilities.findAll(cu, ClassInstanceCreation.class)) {
                    ITypeBinding typeBinding = cic.resolveTypeBinding();
                    System.err.println("MH: TypeBinding " + typeBinding);
                }
            }
        }
        return null;
        
        // Bug 4493
        // Run this by uncommenting the next few lines
        // while commenting the lines from the start all the way upto this comment.
//        String currentProjectPath = "/Users/munawar/Desktop/orjava/source/binding-reproducrer-project-bug4493";
//        File currentProjectDirectory = new File(currentProjectPath);
//        try {
//            Model.useModel(new EclipseModel(currentProjectDirectory));
//        } catch (CoreException e) {
//            e.printStackTrace();
//        }
//        for (IModelFileElement file : Model.getInstance().getAllSourceFiles(IModel.JAVA_LANGUAGE)) {
//            final String fullPath = file.getFullPath();
//            CompilationUnit cu = ASTNodeUtilities.parse(fullPath);
//            if (cu != null) {
//                for (MethodDeclaration methodDecl : ASTNodeUtilities.findAll(cu, MethodDeclaration.class)) {
//                    if (methodDecl.getName().toString().equals("anotherFoo")) {
//                        // Get to anotherFoo method
//                        // Look for binding of b.foo method invocation
//                        // There is only one, so no need to check
//                        for (MethodInvocation methodInvoc : ASTNodeUtilities.findAll(methodDecl, MethodInvocation.class)) {
//                            if (methodInvoc.getExpression() instanceof SimpleName name) {
//                             // We have to handle the array type case differently same as
//                                // in the FieldAccess.
//                                IBinding nodeBinding = name.resolveBinding();
//                                if (nodeBinding == null) {
//                                     // ...
//                                } else if (nodeBinding instanceof ITypeBinding) {
//                                    // ...
//                                } else if (nodeBinding instanceof IVariableBinding) {
//                                    ITypeBinding typeBinding = ((IVariableBinding)nodeBinding).getType();
//                                    System.err.println("MH: TypeBinding " + typeBinding);
//                                }
//                                return false;
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return null;
    }
    
    /**
     * Exit method based on the extension point of
     * org.eclipse.core.runtime.applications
     */
    @Override
    public void stop() {
        terminate(false);
    }
        
    /**
     * Show shut down message. System exit if there is an error
     * 
     * @param hasError if we have encountered an error
     */
    public void terminate(boolean hasError) {
        if (hasError) {
            System.exit(1);
        } else {
            System.exit(0);
        }
    }
}
