quickfix
========

A java agent that allows for runtime replacement of methods.

When to use
========

Whenever you need to quickly replace a method of a 3rd-party library with your implementation

How to use
========

1. Annotate your replacement method with @ReplaceMethod and specify the target class and method to be replaced
2. Add method-replacements.txt file to the root of your classpath, containing a list of classes that contain @ReplaceMethod
3. Run your jvm with -javaagent:/path/to/quickfix.jar