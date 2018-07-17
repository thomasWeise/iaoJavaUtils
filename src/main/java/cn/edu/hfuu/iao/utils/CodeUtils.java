package cn.edu.hfuu.iao.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.Consumer;

/** Utilities for Processing and Printing Java Code */
public final class CodeUtils {

  /** the static string */
  private static final String STATIC = "static "; //$NON-NLS-1$

  /**
   * the string for marking stuff commented out to be turned to java in
   * {@link #importAndInlineClasses(Iterable, Iterable, Consumer, Consumer)}
   */
  public static final String COMMENT_TO_JAVA = "// $"; //$NON-NLS-1$
  /**
   * the string for marking stuff to be removed in
   * {@link #importAndInlineClasses(Iterable, Iterable, Consumer, Consumer)}
   */
  public static final String JAVA_TO_BE_REMOVED = "// #"; //$NON-NLS-1$

  /**
   * convert a class name to a string
   *
   * @param o
   *          the object whose name should be converted
   * @return the class name
   */
  public static final String className(final Object o) {
    final String s;
    s = o.getClass().getSimpleName();
    return ((Character.toLowerCase(s.charAt(0))) + s.substring(1));
  }

  /**
   * get the resource name of a class
   *
   * @param c
   *          the class
   * @return the resource name
   */
  private static final String __resourceName(final Class<?> c) {
    return (c.getSimpleName() + ".java"); //$NON-NLS-1$
  }

  /**
   * Try to find a given class
   *
   * @param clazzName
   *          the class name
   * @return the class, or {@code null} if not found
   */
  private static final Class<?> __find(final String clazzName) {
    String useName = clazzName;
    for (;;) {
      try {
        // try to load the class
        return (Class.forName(useName));
      } catch (@SuppressWarnings("unused") final Throwable error) {
        // ignore;
      }
      final int i = useName.lastIndexOf('.');
      if (i <= 0) {
        return null;
      }
      // check for inner class
      useName = useName.substring(0, i) + '$' + useName.substring(i + 1);
    }
  }

  /**
   * Load all the source codes of the specified classes. This method tries
   * to load the source codes and corresponding import statements of a set
   * of classes whose implementation is supposed to available in the
   * current {@code jar} archive. This lines of code of the classes are
   * passed to an {@code inlineConsumer}, while the import declarations are
   * passed to an {@code importConsumer}.
   *
   * @param imports
   *          the classes to import
   * @param inlines
   *          the classes to inline
   * @param importConsumer
   *          the consumer to receive all imported classes
   * @param inlineConsumer
   *          the consumer for all strings to inline
   */
  public static final void importAndInlineClasses(
      final Iterable<Class<?>> imports, final Iterable<Class<?>> inlines,
      final Consumer<String> importConsumer,
      final Consumer<String> inlineConsumer) {

    // first check all the classes that need to be imported
    final HashSet<Class<?>> needToImport = new HashSet<>();
    for (final Class<?> clazz : imports) {
      needToImport.add(Objects.requireNonNull(clazz));
    }

    // the list of inlined and imported classes
    final HashSet<Class<?>> inlined = new HashSet<>();

    // the classes nexd to inline
    Iterable<Class<?>> currentInline = inlines;

    for (;;) { // repeat until done
      HashSet<Class<?>> nextInline = null;

      for (final Class<?> inlineClass : currentInline) {
        // for a class inlineClass that should be inlined
        if (inlined.add(Objects.requireNonNull(inlineClass))) {

          inlineConsumer.accept("");//$NON-NLS-1$
          inlineConsumer.accept(//
              "// begin of inlined version of class "//$NON-NLS-1$
                  + inlineClass.getCanonicalName());
          inlineConsumer.accept("");//$NON-NLS-1$

          try {
            // we read the class line-by-line
            try (
                final InputStream inputStream = inlineClass
                    .getResourceAsStream(
                        CodeUtils.__resourceName(inlineClass));
                final InputStreamReader reader = new InputStreamReader(
                    inputStream);
                final BufferedReader bufferedReader = new BufferedReader(
                    reader)) {

              String line;
              boolean checkClass = true;
              boolean checkPackage = true;

              while ((line = bufferedReader.readLine()) != null) {

                // remove unnecessary spaces and empty lines
                String trimmedLine = line.trim();
                if (trimmedLine.length() <= 0) {
                  continue;
                }

                if (trimmedLine.endsWith(CodeUtils.JAVA_TO_BE_REMOVED)) {
                  continue; // should the line be deleted
                }

                if (trimmedLine.startsWith(CodeUtils.COMMENT_TO_JAVA)) {
                  // is the line a comment that should become java code
                  line = line
                      .substring(line.indexOf(CodeUtils.COMMENT_TO_JAVA)
                          + CodeUtils.COMMENT_TO_JAVA.length());
                  trimmedLine = trimmedLine.substring(//
                      CodeUtils.COMMENT_TO_JAVA.length()).trim();
                }

                if (checkPackage && // check for package
                    trimmedLine.startsWith("package ")) { //$NON-NLS-1$
                  checkPackage = false;
                  continue;
                }

                if (checkClass && // imports come before classes
                    trimmedLine.startsWith("import ")) {//$NON-NLS-1$
                  final String clazzName = trimmedLine.substring(7, //
                      trimmedLine.lastIndexOf(';')).trim();

                  // try to find the class
                  Class<?> neededClass = CodeUtils.__find(clazzName);
                  if (neededClass == null) {
                    neededClass = CodeUtils
                        .__find(inlineClass.getPackage().getName() + '.'
                            + clazzName);
                    if (neededClass == null) {
                      throw new IllegalArgumentException(
                          "Could not find class " //$NON-NLS-1$
                              + clazzName);
                    }
                  }

                  // if the source code of the class exist, mark it for
                  // inlining, otherwise for importing
                  if (neededClass.getResource(//
                      CodeUtils.__resourceName(neededClass)) != null) {
                    // class source code exists, try to load
                    if (nextInline == null) {
                      nextInline = new HashSet<>();
                    }
                    nextInline.add(neededClass);
                  } else {
                    // class does not exist, try to import
                    needToImport.add(neededClass);
                  }
                  continue;
                }

                doCheck: {
                  // fix some lines
                  if (checkClass) {
                    if (trimmedLine.startsWith("public class ")) {//$NON-NLS-1$
                      line = CodeUtils.STATIC + trimmedLine.substring(7);
                      checkClass = false;
                      break doCheck;
                    }
                    if (trimmedLine.startsWith("public final class ")) {//$NON-NLS-1$
                      line = CodeUtils.STATIC + trimmedLine.substring(7);
                      checkClass = false;
                      break doCheck;
                    }
                    if (trimmedLine.startsWith("class ")) {//$NON-NLS-1$
                      line = CodeUtils.STATIC + trimmedLine;
                      checkClass = false;
                      break doCheck;
                    }
                    if (trimmedLine.startsWith("final class ")) {//$NON-NLS-1$
                      line = CodeUtils.STATIC + trimmedLine;
                      checkClass = false;
                      break doCheck;
                    }
                  }
                }

                // print line
                if (line.length() > 0) {
                  inlineConsumer.accept(line);
                }
              }
            }
          } catch (final Throwable error) {
            throw new RuntimeException(error);
          }

          inlineConsumer.accept("");//$NON-NLS-1$
          inlineConsumer.accept(//
              "// end of inlined version of class "//$NON-NLS-1$
                  + inlineClass.getCanonicalName());
          inlineConsumer.accept("");//$NON-NLS-1$
        }
      }

      if ((nextInline == null) || (nextInline.isEmpty())) {
        break;
      }
      currentInline = nextInline;
    }

    // store the imports
    needToImport.stream().filter((a) -> (!(inlined.contains(a))))//
        .map((clazz) -> clazz.getCanonicalName())//
        .sorted().forEach(importConsumer);
  }
}