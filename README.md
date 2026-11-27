# Minescript Javaclass Loader

## Introduction

This mod is an addon for [Minescript](https://github.com/maxuser0/minescript/). It allows to compile and load any class from strings in scripts. This is still *very* experimental.

### Usage

The compiler and classloader can be accessed with `JavaClass("de.minescript_javaclass_loader.compiler.InMemoryCompiler")`.
The method `compileClass(String className, String sourceCode)` compiles the provided source into bytecode, which can then be loaded with the method `loadClass(String className)`.
`loadClass` returns a class object which can then be used in scripts. The method `compileAndLoadClass(String className, String sourceCode)` calls the other two methods in order and returns the class object as well.

### Example

```python
import minescript
from java import JavaClass

InMemoryCompiler = JavaClass("de.minescript_javaclass_loader.compiler.InMemoryCompiler")
compiler = InMemoryCompiler()

name = "net.minescript_custom_javaclasses.Test"

source = """
package net.minescript_custom_javaclasses;

public class Test {
    public String bar = "Hello World!";

    public String foo() {
        return bar;
    }
}
"""

class_ = compiler.compileAndLoadClass(name, source)
minescript.echo(class_.newInstance().foo())
```
Running this script will print "Hello World!" to the ingame chat.

## License

This project is available under the MIT license.
