package de.minescript_javaclass_loader.compiler;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import java.util.Hashtable;
import java.util.Map;

public class InMemoryFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
    private final Map<String, JavaBytecodeFile> compiledClasses = new Hashtable<>();
    private final ClassLoader loader;

    public InMemoryFileManager(StandardJavaFileManager fileManager) {
        super(fileManager);
        this.loader = new InMemoryClassLoader(Thread.currentThread().getContextClassLoader(), this);
    }

    @Override
    public ClassLoader getClassLoader(Location location) {
        return loader;
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String name, Kind kind, FileObject sibling) {
        JavaBytecodeFile classFile = new JavaBytecodeFile(name);
        compiledClasses.put(name, classFile);
        return classFile;
    }

    public Map<String, JavaBytecodeFile> getCompiledClasses() {
        return compiledClasses;
    }
}
