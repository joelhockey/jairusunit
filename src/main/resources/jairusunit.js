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

// Return TestSuite containing the following:
// 1. TestCase for any global functions starting with 'test'
// 2. TestSuite for any objects or (constructor) functions starting or ending with 'Test'
// 2a) If TestSuite is not function or object add warning 'Invalid object for TestSuite <name>:<type>'
// 2b) If TestSuite is constructor function, create new instance and add all 'test*' methods
// 2c) If TestSuite is object, extend object and add all 'test*' functions
// 2d) If TestSuite contains no TestCases, add warning 'No tests found in <TestSuite>'
function jairusunitTestSuite(file) {
    var result = new Packages.junit.framework.TestSuite(file);
    try {
        load(file);
    } catch (e) {
        var msg = com.joelhockey.jairusunit.JairusUnit.dumpError(
                "Error loading javascript file: " + file,
                e.rhinoException || e.javaException || null);
        result.addTest(com.joelhockey.jairusunit.JairusUnit.warning(msg));
    }
    
    function createTestCase(instance, className, methodName) {
        instance.toString = function() { return methodName + "(" + className + ")"; };
        var testCase = new Packages.junit.framework.TestCase(instance);
        testCase.setName(methodName);
        return testCase;
    }
    
    var glbl = global();
    for (var prop in glbl) {
        // 1. TestCase for any global functions starting with 'test'
        if (/^test/.test(prop) && typeof glbl[prop] === "function") {
            var testInstance = {};
            testInstance[prop] = glbl[prop];
            result.addTest(createTestCase(testInstance, file + ".global", prop))

        // 2. TestSuite for any objects or (constructor) functions starting or ending with 'Test'
        } else if (/^Test|Test$/.test(prop)) {
            var suiteName = file + "." + prop;
            var testSuite = new Packages.junit.framework.TestSuite(suiteName);
            var testObj = glbl[prop];

            // 2a) If TestSuite is not function or object add warning 'Invalid object for TestSuite <name>:<type>'
            if (typeof testObj !== "function" && typeof testObj !== "object") {
                result.addTest(com.joelhockey.jairusunit.JairusUnit.warning("Invalid object for TestSuite " + suiteName + ":" + typeof testObj));
                continue;
            }

            // instantiate TestClass if function to ensure prototype and methods are defined
            var allMethods = typeof testObj === "function" ? new testObj() : testObj;

            for (var testMethod in allMethods) {
                // only want 'test*' methods
                if (typeof allMethods[testMethod] === "function" && /^test/.test(testMethod)) {
                    var testInstance = null;
                    // 2b) If TestSuite is constructor function, jairusunit will 'new' a new instance and add all 'test*' methods
                    if (typeof testObj === "function") {
                        testInstance = new testObj();

                    // 2c) If TestSuite is object, extend object and add all 'test*' functions
                    } else {
                        var F = function() {};
                        F.prototype = testObj;
                        testInstance = new F();
                    }
                    testSuite.addTest(createTestCase(testInstance, suiteName, testMethod))
                }
            }
            
            //   2d) If TestSuite contains no TestCases, add warning 'No tests found in <TestSuite>'
            if (testSuite.countTestCases() === 0) {
                testSuite.addTest(com.joelhockey.jairusunit.JairusUnit.warning("No tests found in " + suiteName));
            }
            result.addTest(testSuite);
        }
    }
    return result;
}
