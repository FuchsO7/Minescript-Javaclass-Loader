package de.minescript_javaclass_loader.compiler;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class InMemoryCompiler {
    private final String className;
    private final String sourceCode;
    private final DiagnosticCollector<JavaFileObject> diagnostics;
    private final JavaCompiler compiler;
    private final InMemoryFileManager fileManager;
    private Map<String, JavaBytecodeFile> bytecodeFiles;

    public InMemoryCompiler(String className, String sourceCode) throws IOException {
        this.className = className;
        this.sourceCode = sourceCode;
        this.compiler = ToolProvider.getSystemJavaCompiler();
        this.diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(diagnostics, null, null);
        standardFileManager.setLocation(StandardLocation.CLASS_PATH, getClassPaths());
        this.fileManager = new InMemoryFileManager(standardFileManager);
    }

    private List<File> getClassPaths() {
        List<File> classPaths = new ArrayList<>();
        Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator)).forEach(path -> {
            if (!path.isBlank())
                classPaths.add(new File(path));
        });
        Path modsDirectory = Path.of(System.getProperty("user.dir"), "mods");
        File[] mods = modsDirectory.toFile().listFiles();
        if (mods == null)
            throw new IllegalArgumentException("Mods folder was not found, expected it at %s".formatted(modsDirectory));
        classPaths.addAll(Arrays.asList(mods));
        return classPaths;
    }

    public Class<?> compileAndLoadClasses() throws IllegalStateException, ClassNotFoundException {
        compileClasses();
        return loadClasses();
    }

    public Map<String, byte[]> compileClasses() throws IllegalStateException {
        if (bytecodeFiles != null)
            throw new IllegalStateException("Classes were already compiled!");
        bytecodeFiles = compileToBytecodeFiles();
        return getBytecode();
    }

    public Class<?> loadClasses() throws ClassNotFoundException {
        if (bytecodeFiles == null)
            throw new IllegalStateException("Compile classes before loading them!");
        ClassLoader classLoader = fileManager.getClassLoader(null);
        return classLoader.loadClass(this.className);
    }

    private Map<String, JavaBytecodeFile> compileToBytecodeFiles() {
        JavaFileObject sourceFile = new JavaSourceCodeFile(className, sourceCode);
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, List.of(sourceFile));

        if (!task.call()) {
            StringBuilder errors = new StringBuilder("Compilation failed:");
            diagnostics.getDiagnostics().forEach(error -> errors.append("\n").append(error.toString()));
            throw new IllegalArgumentException(errors.toString());
        }
        return fileManager.getCompiledClasses();
    }

    public Map<String, byte[]> getBytecode() {
        Map<String, byte[]> bytecode = new LinkedHashMap<>();
        bytecodeFiles.forEach((name, bytecodeFile) -> bytecode.put(name, bytecodeFile.getBytes()));
        return bytecode;
    }
}
