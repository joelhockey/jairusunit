// Example of how to use JairusUnit
// There are 4 styles of test cases that can be used.

// load file to test
load("calc.js");

// 1. any global methods that start with 'test'
function testAddSuccess() {
    assertEquals(2, add(1,1));
}

function testAddFail() {
    assertEquals(-2, add(1, 1));
}

function testAddError() {
    throw new Error("I didn't expect this error");
}

// 2. Constructor functions that are defined by their prototype
function FunctionWithPrototypeTest() {}
FunctionWithPrototypeTest.prototype = {
	testAddSuccess: function() {
        assertEquals(2, add(1, 1));
    },
    testAddFail : function() {
        assertEquals(11, add(1, 1));
    },
    testAddError : function() {
        throw new java.lang.Exception("throwing a java exception");
    }
}

// 3. Constructor functions that define their methods at construction time
function FunctionWithMethodsTest() {
    // note that JairuSUnit supports setUp and tearDown methods
    this.setUp = function() {
        this.addresult = add(1,1);
    };
    this.testAddSuccess = function() {
        assertEquals(2, this.addresult);
    };
    this.testAddFail = function() {
    	assertTrue("3==2", 3 == add(1,1))
    };
    this.testAddError = function() {
    	throw new Error("unexpected error");
    };
}

// 4. Object Literal objects - object/function name can have 'Test' at start or end
var TestObjectLiteral = {
	testAddSuccess : function() { assertEquals(2, add(1, 1)) },
	testAddFail : function() {
        assertEquals(1, add(1, 1));
    },
	testAddError : function() {
        throw new java.lang.Exception("test exception");
    }
}