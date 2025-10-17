package com.openrefactory.ui.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.openrefactory.core.model.IModelFileElement;
import org.openrefactory.core.model.Model;
import org.openrefactory.internal.core.model.eclipse.EclipseModelFileElement;

/**
 * Various utility methods for JDT ASTNode class
 * 
 * @author Munawar Hafiz
 */
public class ASTNodeUtilities {
    
    /**
     * Parse a Java file
     * @param file The target
     * @return A compilation unit representing the root of the AST content
     */
    public static CompilationUnit parse(String filename) {
        IModelFileElement fileElement = Model.getInstance().getFile(filename);
        if (fileElement == null) return null;
        ASTParser parser = ASTParser.newParser(AST.JLS21);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        // Must set unit name for files that are read directly from file system
        // and not through a compilation unit
        // If we do not set unit name explicitly, type resolution will not 
        // work even if we set resolve bindings to true
        parser.setUnitName(fileElement.getName());
        parser.setEnvironment(new String[] {}, new String[] {}, new String[] {}, true);
        if (fileElement instanceof EclipseModelFileElement) {
            // If we are using eclipse model, then use the ICompilationUnit
            try {
                parser.setSource(((EclipseModelFileElement)fileElement).getCompilationUnit().getSource().toCharArray());
            } catch (JavaModelException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            // Get the source from the source file
            File file = new File(fileElement.getFullPath());
            try {
                parser.setSource(readFileToString(file).toCharArray());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);
        // Issue 998 
        // Protection against potential failure in parsing
        try {
            ASTNode node = parser.createAST(null);
            if (node instanceof CompilationUnit) {
                return (CompilationUnit)node;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    
    /**
     * Find node that is the nearest ancestor to have the matching type 
     * @param <T>         the generic type
     * @param node        the node to start searching from 
     * @param targetClass the type of the ASTNode to find amongst the ancestors
     * @return the first node amongst the ancestors that matches the specified type
     */
    @SuppressWarnings("unchecked")
    public static <T extends ASTNode> T findNearestAncestor(ASTNode node, Class<T> targetClass)
    {
        // Issue 246
        // If we are looking for type declaration or compilation unit
        // then allow calculation to go all the way, otherwise
        // stop at the nearest type declaration 
        boolean stopAtTypeDeclaration = true;
        if (targetClass.isAssignableFrom(CompilationUnit.class)
            || targetClass.isAssignableFrom(TypeDeclaration.class)) {
            stopAtTypeDeclaration = false;
        }
        for (ASTNode parent = node.getParent(); parent != null; parent = parent.getParent()) {
            if (targetClass.isAssignableFrom(parent.getClass())) {
                return (T)parent;
            }
            if (stopAtTypeDeclaration && parent.getClass().isAssignableFrom(TypeDeclaration.class)) {
                break;
            }
        }
        return null;
    }
    
    
    /**
     * Find an iterable of nodes ordered in terms of traversal that match a class type 
     * @param <T>    the generic type
     * @param node   the node to start searching from 
     * @param clazz  the type of the ASTNode to find amongst the children
     * @return an Iterable (not null but can be empty) that contains all children
     *     that match the type of the class
     */
    @SuppressWarnings("unchecked")
    public static <T extends ASTNode> Iterable<T> findAll(final ASTNode node, final Class<T> clazz)
    {
        return new Iterable<T>()
        {
            public Iterator<T> iterator()
            {
                return new FilteringIterator<T>(new ASTIterator(node))
                {
                    @Override protected boolean shouldProcess(Object item)
                    {
                        return clazz.isAssignableFrom(item.getClass());
                    }
                    @Override protected T process(Object item)
                    {
                        return (T)item;
                    }
                };
            };
        };
    }
    
    public static abstract class FilteringIterator<T> implements Iterator<T>
    {
        private boolean done;
        private Iterator<?> wrappedIterator;
        private Object next;

        public FilteringIterator(Iterator<?> wrappedIterator)
        {
            this.done = false;
            this.wrappedIterator = wrappedIterator;
            findNext();
        }

        private void findNext()
        {
            do
            {
                if (!this.wrappedIterator.hasNext())
                {
                    this.done = true;
                    return;
                }

                this.next = this.wrappedIterator.next();
            }
            while (!shouldProcess(this.next));
        }

        public boolean hasNext()
        {
            return !this.done;
        }

        public T next()
        {
            T result = process(this.next);
            findNext();
            return result;
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        /**
         * Determines whether or not the given item should be returned by this iterator.
         * <p>
         * The item will be passed to {@link #process(Object)} iff this method returns <code>true</code>.
         *
         * @return true iff the given item should be returned by this iterator
         */
        protected abstract boolean shouldProcess(Object item);

        /**
         * Translates the given item into an object of type <code>T</code>.
         * <p>
         * This method will be invoked iff {@link #shouldProcess(Object)} returned <code>true</code>.
         *
         * @return an object of type <code>T</code> corresponding to the given item
         */
        protected abstract T process(Object item);
    }

    public static final class ASTIterator implements Iterator<ASTNode>
    {
        protected final Stack<Iterator<? extends ASTNode>> stack;

        public ASTIterator(ASTNode root)
        {
            this.stack = new Stack<Iterator<? extends ASTNode>>();
            stack.push(Collections.singleton(root).iterator());
        }

        public boolean hasNext()
        {
            return !stack.isEmpty() && stack.peek().hasNext();
        }

        public ASTNode next()
        {
            if (!hasNext()) return null;

            ASTNode nextNode = stack.peek().next();

            Iterator<? extends ASTNode> children = getChildren(nextNode).iterator();
            if (children.hasNext())
                stack.push(children);

            while (!stack.isEmpty() && !stack.peek().hasNext())
                stack.pop();

            return nextNode;
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
    
    /**
     * Get the children of an ASTNode
     * @param node the node to start from
     * @return A list of AST nodes that are children of the starting node
     */
    @SuppressWarnings("rawtypes")
    public static List<ASTNode> getChildren(ASTNode node) {
        List<ASTNode> children = new ArrayList<>();
        List list = node.structuralPropertiesForType();
        for (int i = 0; i < list.size(); i++) {
            Object child = node.getStructuralProperty((StructuralPropertyDescriptor)list.get(i));
            // in the case of 
            // for (;;i++) something;
            // when node.getStructuralProperty((StructuralPropertyDescriptor)list.get(i)) is called on the for increment
            // part it returns null to child
            if (child == null) continue;
            
            if (child instanceof ASTNode) {
                children.add((ASTNode) child);
            } else if (child instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<ASTNode> nodes = (List<ASTNode>) child;
    
                if (!nodes.isEmpty()) {
                    children.addAll(nodes);
                }
            }
        }
        return children;
    }
    
    /**
     * Read file content into a string
     * @param file the file to read from
     * @return     the content of the file in string format 
     * @throws IOException 
     */
    private static String readFileToString(File file) throws IOException {
        StringBuilder fileData = new StringBuilder(1000);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            char[] buf = new char[10];
            int numRead = 0;
            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
                buf = new char[1024];
            }

            return fileData.toString();
        } catch (IOException e) {
            throw e;
        }
    }
}
