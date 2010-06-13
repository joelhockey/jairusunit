/*
 * The MIT Licence
 *
 * Copyright 2010 Joel Hockey (joel.hockey@gmail.com).  All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.joelhockey.jsunit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.mozilla.javascript.ClassCache;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.RhinoException;

/**
 * Main class to execute JSUnit.  Takes names of js files as args.
 * @author Joel Hockey
 */
public class JSUnit {
    public static final int SUCCESS_EXIT = 0;
    public static final int FAILURE_EXIT = 1;
    public static final int EXCEPTION_EXIT = 2;

    public static final Pattern STACK_TRACE_FILTER = Pattern.compile(
            "^\tat (" +
                "junit|java.lang.reflect.Method.invoke\\(" +
                "|org.mozilla.javascript.(Context|JavaAdapter|MemberBox|NativeJavaMethod|[Oo]ptimizer|ScriptRuntime|.*jsunit.js)" +
                "|sun.reflect" +
                "|org.apache.tools.ant" +
                "|com.joelhockey.jsunit" +
            ")"
    );

    /**
     * Return stacktrace string with uninteresting lines removed.
     * @param t throwable
     * @return stacktrace string with uninteresting lines removed
     */
    public static String filterStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return filterStackTrace(sw.toString());
    }
    /**
     * Return stacktrace string with uninteresting lines removed.
     * @param stack stacktrace dump
     * @return stacktrace string with uninteresting lines removed
     */
    public static String filterStackTrace(String stack) {
        StringBuilder result = new StringBuilder();
        for (String line : stack.split("\n")) {
            if (!STACK_TRACE_FILTER.matcher(line).find()) {
                result.append(line).append("\n");
            }
        }
        return result.toString();
    }

    /**
     * Return JUnit {@link TestSuite} containing all
     * tests from given js file.
     * @param file javascript file containing tests
     * @return test suite
     */
    public static Test getTests(String file) {
        TestSuite result = new TestSuite("jsunit");
        Context cx = Context.enter();
        try {
            Function getTests;
            JSUnitScope scope = new JSUnitScope(cx);
            ClassCache.get(scope).setCachingEnabled(false); // need this
            try {
                scope.load("jsunit.js");
                getTests = (Function) scope.get("getTests", scope);
                NativeJavaObject obj = (NativeJavaObject) getTests.call(
                        cx, scope, getTests, new Object[] {file});
                TestSuite suite = (TestSuite) obj.unwrap();
                result.addTest(suite);
            } catch (RhinoException re) {
                String msg = String.format("\n\"jsunit.js\", line %d: %s", re.lineNumber(), re.details());
                if (re.lineSource() != null) {
                    byte[] dots = new byte[re.columnNumber() - 1];
                    Arrays.fill(dots, (byte) '.');
                    msg += "\n" + re.lineSource() + "\n" + new String(dots) + "^";
                }
                result.addTest(TestSuite.warning(msg));
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                result.addTest(TestSuite.warning("Error loading jsunit.js\n" + sw));
            }
        } finally {
            Context.exit();
        }
        return result;
    }

    /**
     * Main.  Args include filenames with optional '-todir &;t'dir>' or '-basedir &lt;dir> included
     * at any position within files.  todir option gives directory to write
     * junit-style 'plain' and 'xml' reports, default todir is 'target/surefire-reports'.
     * basedir gives base directory for given filenames, default basedir is '/'.
     * @param args js files
     */
    public static void main(String[] args) {
        boolean failure = false;
        String userDir = System.getProperty("user.dir", "");
        try {
            String todir = "target/surefire-reports";
            String basedir = "";
            int i = 0;
            while (i < args.length) {
                String arg = args[i++];
                if ("-todir".equals(arg)) {
                    todir = args[i++];
                    File f = new File(todir);
                    if (!f.exists()) {
                        f.mkdir();
                    }
                    continue;
                } else if ("-basedir".equals(arg)) {
                    basedir = args[i++];
                    continue;
                }
                // get suite using full filepath
                Test suite = getTests(basedir + "/" + arg);

                // we need to mimic java-style pkgname.classname style to make reports look nice
                // strip '.js' suffix, exclude basedir, prefix with 'jsunit.' and change slashes to dots
                String testName = "jsunit." + arg.replaceAll("\\.js$", "").replaceAll("/|\\\\", ".");

                // always write plain and xml reports - don't bother making people choose
                PrintStream plain = new PrintStream(new FileOutputStream(todir + "/TEST-" + testName + ".txt"), true, "UTF-8");
                PrintStream xml = new PrintStream(new FileOutputStream(todir + "/TEST-" + testName + ".xml"), true, "UTF-8");;
                JSUnitResultWriter printer = new JSUnitResultWriter(System.out, plain, xml);
                TestResult result = new TestResult();
                result.addListener(printer);
                printer.startTestSuite(testName);
                suite.run(result);
                printer.endTestSuite(testName);
                plain.close();
                xml.close();
                if (!result.wasSuccessful()) {
                    failure = true;
                }
            }
            System.exit(failure ? FAILURE_EXIT : SUCCESS_EXIT);
        } catch (Exception e) {
            System.exit(EXCEPTION_EXIT);
        }
    }
}