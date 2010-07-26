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

import static java.lang.String.format;

import java.io.PrintStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestListener;

import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeJavaObject;

/**
 * JUnit {@link TestListener} writes ant-junit-style
 * 'summary', 'plain' and 'xml' reports.
 * @author Joel Hockey
 */
public class JSUnitResultWriter implements TestListener {
    private PrintStream summary;
    private PrintStream plain;
    private PrintStream xml;

    private List<String> tests = new ArrayList<String>();
    private Map<String, Long> testTimes = new HashMap<String, Long>();
    private Map<String, Throwable> failures = new HashMap<String, Throwable>();
    private Map<String, Throwable> errors = new HashMap<String, Throwable>();
    private long startSuite;
    private long endSuite;
    private long startTest;
    private long endTest;

    public JSUnitResultWriter() {
        this(System.out, null, null);
    }
    public JSUnitResultWriter(PrintStream summary, PrintStream plain, PrintStream xml) {
        this.summary = summary;
        this.plain = plain;
        this.xml = xml;
    }

    // xml escape
    private static String esc(String s) {
        if (s == null) { return ""; }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "apos;");
    }

    public void startTestSuite(String name) {
        if (summary != null) { summary.println(format("Running %s", name)); }
        if (plain != null) { plain.println("Testsuite: " + name); }
        startSuite = System.currentTimeMillis();
    }

    public void endTestSuite(String name) {
        endSuite = System.currentTimeMillis();
        // summary
        if (summary != null) {
            summary.println(format("Tests run: %d, Failures: %d, Errors: %d, Time elapsed: %.3f",
                    tests.size(), failures.size(), errors.size(), (endSuite - startSuite) / 1000.0));
            for (String test : tests) {
                if (failures.containsKey(test)) {
                    summary.println(format("Test %s\n\tFAILED: %s", test, failures.get(test).getMessage()));
                }
                if (errors.containsKey(test)) {
                    summary.println(format("Test %s\n\tERROR: %s", test, JSUnit.dumpError(null, errors.get(test))));
                }
            }
        }

        // plain
        if (plain != null) {
            plain.println(format("Tests run: %d, Failures: %d, Errors: %d, Time elapsed: %.3f\n",
                    tests.size(), failures.size(), errors.size(), (endSuite - startSuite) / 1000.0));
            for (String test : tests) {
                plain.println(format("Testcase: %s took %.3f sec", test, testTimes.get(test) / 1000.0));
                if (failures.containsKey(test)) {
                    plain.println("\tFAILED");
                    plain.println(JSUnit.filterStackTrace(failures.get(test)));
                }
                if (errors.containsKey(test)) {
                    plain.println("\tCaused an ERROR");
                    plain.println(JSUnit.dumpError(null, errors.get(test)));
                }
            }
        }

        // xml
        if (xml != null) {
            xml.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
            xml.println(format("<testsuite name=\"%s\" skipped=\"0\" tests=\"%d\" errors=\"%d\" time=\"%.3f\" failures=\"%d\">",
                    esc(name), tests.size(), errors.size(), (endSuite - startSuite) / 1000.0, failures.size()));
            xml.println(format("  <properties>"));
            for (Entry<Object, Object> prop : System.getProperties().entrySet()) {
                xml.println(format("    <property value=\"%s\" name=\"%s\"/>",
                        esc(prop.getValue().toString()), esc(prop.getKey().toString())));
            }
            xml.println(format("  </properties>"));
            for (String test : tests) {
                xml.print(format("  <testcase classname=\"%s\" name=\"%s\" time=\"%.3f\"",
                        test, test, testTimes.get(test) / 1000.0));
                Throwable t = failures.get(test);
                String failureOrError = "failure";
                String stack = JSUnit.filterStackTrace(t); // filter failures
                if (t == null) {
                    t = errors.get(test);
                    failureOrError = "error";
                    stack = JSUnit.dumpError(null, t);  // full dump for errors
                }

                if (t == null) {
                    xml.println("/>");
                } else {
                    xml.println(">");
                    xml.println(format("    <%s type=\"%s\" message=\"%s\">%s</%s>",
                            failureOrError, esc(t.getClass().getName()), esc(t.getMessage()),
                            esc(stack), failureOrError));
                    xml.println(format("  </testcase>"));
                }
            }
            xml.print(format("</testsuite>"));
        }
    }

    public void startTest(Test test) {
        tests.add(test.toString());
        startTest = System.currentTimeMillis();
    }
    public void endTest(Test test) {
        endTest = System.currentTimeMillis();
        testTimes.put(test.toString(), (endTest - startTest));
    }
    public void addError(Test test, Throwable t) {
        StringWriter sw = new StringWriter();
        sw.write(test.toString());

        // need to unwrap Throwable if it is JavaScriptException
        // if unwrapped is JUnit AssertionFailedErrors,
        // then register as failure rather than error
        if (t instanceof JavaScriptException) {
            JavaScriptException jse = (JavaScriptException) t;
            if (jse.getValue() instanceof NativeJavaObject) {
                NativeJavaObject njo = (NativeJavaObject) jse.getValue();
                Object o = njo.unwrap();
                if (o instanceof AssertionFailedError) {
                    addFailure(test, (AssertionFailedError) o);
                    return;
                } else if (o instanceof Throwable) { // root cause
                    t = (Throwable) o;
                }
            }
        }
        errors.put(test.toString(), t);
    }
    public void addFailure(Test test, AssertionFailedError t) {
        failures.put(test.toString(), t);
    }
}
