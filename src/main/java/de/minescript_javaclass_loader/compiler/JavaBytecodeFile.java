package de.minescript_javaclass_loader.compiler;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;

public class JavaBytecodeFile extends SimpleJavaFileObject {
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    public JavaBytecodeFile(String name) {
        super(URI.create("string:///" + name.replace('.', '/') + Kind.CLASS.extension), Kind.CLASS);
    }

    @Override
    public OutputStream openOutputStream() {
        return out;
    }

    public byte[] getBytes() {
        return out.toByteArray();
    }
}
