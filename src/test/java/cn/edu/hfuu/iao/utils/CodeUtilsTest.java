package cn.edu.hfuu.iao.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

/** Test the some utilities */
@SuppressWarnings("static-method")
public final class CodeUtilsTest {

  /** test the importing and inlining of classes */
  @Test(timeout = 3600000)
  public void importAndInlineClassesTest_1() {
    final ArrayList<String> imports = new ArrayList<>();
    final ArrayList<String> inlines = new ArrayList<>();

    CodeUtils.importAndInlineClasses(//
        Arrays.asList(HashMap.class), //
        Arrays.asList(ConsoleIO.class), //
        (string) -> imports.add(string), //
        (string) -> inlines.add(string));

    Assert.assertFalse(imports.isEmpty());
    Assert.assertEquals(5, imports.size());
    Assert.assertFalse(inlines.isEmpty());
  }

  /** test the importing and inlining of classes */
  @Test(timeout = 3600000)
  public void importAndInlineClassesTest_2() {
    final ArrayList<String> imports = new ArrayList<>();
    final ArrayList<String> inlines = new ArrayList<>();

    CodeUtils.importAndInlineClasses(//
        Arrays.asList(), //
        Arrays.asList(Compressor.class), //
        (string) -> imports.add(string), //
        (string) -> inlines.add(string));

    Assert.assertFalse(imports.isEmpty());
    Assert.assertFalse(inlines.isEmpty());
  }
}