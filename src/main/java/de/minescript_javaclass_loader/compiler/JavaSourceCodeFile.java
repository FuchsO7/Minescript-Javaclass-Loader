package de.minescript_javaclass_loader.compiler;

import javax.tools.SimpleJavaFileObject;
import java.net.URI;

public class JavaSourceCodeFile extends SimpleJavaFileObject {
    private final String sourceCode;

    public JavaSourceCodeFile(String name, String sourceCode) {
        super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.sourceCode = sourceCode;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return sourceCode;
    }
}