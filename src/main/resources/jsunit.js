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

function fail() {
    throw new Packages.junit.framework.AssertionFailedError(arguments.length > 0 ? arguments[0] : null);
}

function assertEquals() {
    if (arguments.length <= 2 && arguments[0] != arguments[1]) {
        throw new Packages.junit.framework.AssertionFailedError("expected:<" + arguments[0] + "> but was:<" + arguments[1] +">");
    } else if (arguments.length > 2 && arguments[1] != arguments[2]) {
        throw new Packages.junit.framework.AssertionFailedError(arguments[0] + " expected:<" + arguments[1] + "> but was:<" + arguments[2] +">");
    }
}

function assertTrue() {
    if (arguments.length == 1 && !arguments[0]) {
        throw new Packages.junit.framework.AssertionFailedError();
    } else if (arguments.length > 1 && !arguments[1]){
        throw new Packages.junit.framework.AssertionFailedError(arguments[0]);
    }
}

function assertNotNull() {
    if (arguments.length == 1 && arguments[0] === null) {
        throw new Packages.junit.framework.AssertionFailedError();
    } else if (arguments.length > 1 && arguments[1] === null) {
        throw new Packages.junit.framework.AssertionFailedError(arguments[0]);
    }
}

function assertNull() {
    if (arguments.length == 1 && arguments[0] !== null) {
        throw new Packages.junit.framework.AssertionFailedError();
    } else if (arguments.length > 1 && arguments[1] !== null) {
        throw new Packages.junit.framework.AssertionFailedError(arguments[0]);
    }
}

function assertSame() {
    if (arguments.length <= 2 && arguments[0] !== arguments[1]) {
        throw new Packages.junit.framework.AssertionFailedError("expected same:<" + arguments[0] + "> but was:<" + arguments[1] +">");
    } else if (arguments.length > 2 && arguments[1] !== arguments[2]) {
        throw new Packages.junit.framework.AssertionFailedError(arguments[0] + " expected same:<" + arguments[1] + "> but was:<" + arguments[2] +">");
    }
}

function assertNotSame() {
    if (arguments.length <= 2 && arguments[0] !== arguments[1]) {
        throw new Packages.junit.framework.AssertionFailedError("expected not same");
    } else if (arguments.length > 2 && arguments[1] !== arguments[2]) {
        throw new Packages.junit.framework.AssertionFailedError(arguments[0] + " expected not same");
    }
}

function assertMatches() {
    if (arguments.length <= 2 && !arguments[0].test(arguments[1])) {
        throw new Packages.junit.framework.AssertionFailedError("expected match:<" + arguments[0] + "> but was:<" + arguments[1] +">");
    } else if (arguments.length > 2 && !arguments[1].test(arguments[2])) {
        throw new Packages.junit.framework.AssertionFailedError(arguments[0] + " expected match:<" + arguments[1] + "> but was:<" + arguments[2] +">");
    }
}

function getTests(file) {
    var result = new Packages.junit.framework.TestSuite(file);
    try {
        load(file);
    } catch (e) {
        if (e.rhinoException) {
            var re = e.rhinoException;
            var msg = "\n\"" + file + "\", line " + re.lineNumber() + ": " + re.details();
            var ls = re.lineSource();
            if (ls) {
                msg += "\n" + ls + "\n" + new String(ls).substring(0, re.columnNumber() - 1).replace(/./g, ".") + "^";
            }
        } else {
            var sw = new java.io.StringWriter("\n");
            if (e.javaException) {
                e.javaException.printStackTrace(new java.io.PrintWriter(sw));
            }
            msg = "Error loading javascript file: " + file + sw;
        }
        result.addTest(Packages.junit.framework.TestSuite.warning(msg));
    }
    result.addTest(new Packages.junit.framework.Test(new JSUnitTestSuite(file + ".global", global(), true)));
    if (result.countTestCases() == 0) {
        result.addTest(Packages.junit.framework.TestSuite.warning("No tests found in " + file));
    }
    return result;
}

function JSUnitTestSuite(name, theClass, allowNoTests) {
    function createTestCase(theClass, className, methodName) {
        var instance = theClass;
        if (typeof theClass == "function") {
            instance = new theClass();
        } else if (typeof theClass != "object") {
            return Packages.junit.framework.TestSuite.warning("Invalid JS object for TestCase class: " + theClass);
        }
        instance.toString = function() { return methodName + "(" + className + ")"; };
        var testCase = new Packages.junit.framework.TestCase(instance);
        testCase.setName(methodName);
        return testCase;
    }

    this.name = name;
    this.tests = [];
    var instance = theClass;
    if (typeof theClass == "function") {
        instance = new theClass();
    }

    for (var p in instance) {
        if (/^Test|Test$/.test(p)) {
            this.tests.push(new JSUnitTestSuite(name + "." + p, instance[p]))
        } else if (/^test/.test(p) && typeof instance[p] == "function") {
            var testCase = createTestCase(theClass, name, p)
            this.tests.push(testCase);
        }
    }

    if (!allowNoTests && this.tests.length == 0) {
        this.tests.push(Packages.junit.framework.TestSuite.warning("No tests found in " + name));
    }
    
    this.countTestCases = function() {
        var count = 0;
        for (var i = 0; i < this.tests.length; i++) {
            count += this.tests[i].countTestCases();
        }
        return count;
    };
    
    this.run = function(result) {
        for (var i = 0; i < this.tests.length; i++) {
            if (result.shouldStop()) {
                break;
            }
            this.tests[i].run(result);
        }
    }
}