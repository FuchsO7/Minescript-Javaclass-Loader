package de.minescript_javaclass_loader.classloader;

import java.nio.file.Files;
import java.nio.file.Path;

public class BytecodeClassLoader extends ClassLoader {

    public BytecodeClassLoader(ClassLoader parent) {
        super(parent);
    }

    public Class<?> loadBytecode(String name, byte[] bytecode) throws ClassNotFoundException {
        Class<?> class_ = defineClass(name, bytecode, 0, bytecode.length);
        loadClass(name, true);
        return class_;
    }

    public Class<?> loadClassFile(String name, String path) throws Exception {
        byte[] bytes = Files.readAllBytes(Path.of(path));
        return loadBytecode(name, bytes);
    }
}
