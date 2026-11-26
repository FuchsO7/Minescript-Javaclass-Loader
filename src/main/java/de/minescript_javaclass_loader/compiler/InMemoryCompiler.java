package de.minescript_javaclass_loader.compiler;

import javax.tools.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class InMemoryCompiler {
    private final DiagnosticCollector<JavaFileObject> diagnostics;
    private final JavaCompiler compiler;
    private final InMemoryFileManager fileManager;
    private Map<String, JavaBytecodeFile> bytecodeFiles;

    public InMemoryCompiler(boolean includeModsInClassPath) throws IOException {
        this.compiler = ToolProvider.getSystemJavaCompiler();
        this.diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(diagnostics, null, null);
        standardFileManager.setLocation(StandardLocation.CLASS_PATH, getClassPaths(includeModsInClassPath));
        this.fileManager = new InMemoryFileManager(standardFileManager);
    }

    public InMemoryCompiler() throws IOException {
        this(true);
    }

    private List<File> getClassPaths(boolean includeModsInClassPath) throws FileNotFoundException {
        List<File> classPaths = new ArrayList<>();
        Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator)).forEach(path -> {
            if (!path.isBlank())
                classPaths.add(new File(path));
        });
        if (includeModsInClassPath){
            Path modsDirectory = Path.of(System.getProperty("user.dir"), "mods");
            File[] mods = modsDirectory.toFile().listFiles();
            if (mods == null)
                throw new FileNotFoundException("Mods folder was not found, expected it at %s".formatted(modsDirectory));
            classPaths.addAll(Arrays.asList(mods));
        }
        return classPaths;
    }

    public Class<?> compileAndLoadClasses(String className, String sourceCode) throws ClassNotFoundException {
        compileClasses(className, sourceCode);
        return loadClasses(className);
    }

    public Map<String, byte[]> compileClasses(String className, String sourceCode) {
        bytecodeFiles = compileToBytecodeFiles(className, sourceCode);
        return getBytecode();
    }

    public Class<?> loadClasses(String className) throws ClassNotFoundException {
        if (bytecodeFiles == null)
            throw new IllegalStateException("Compile classes before loading them!");
        ClassLoader classLoader = fileManager.getClassLoader(null);
        return classLoader.loadClass(className);
    }

    private Map<String, JavaBytecodeFile> compileToBytecodeFiles(String className, String sourceCode) {
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
