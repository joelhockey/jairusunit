load("calc.js");

function CalcWithPTest() {}
CalcWithPTest.prototype = {
	testWithPAddSuccess: function() {
        assertEquals(2, add(1, 1));
    },
    testWithPAddFail : function() {
        assertEquals(2, add(1, 1));
    }
}

function testgl() {
    assertEquals(2, add(1,1));
}

function CalcTest() {
    this.setUp = function() { this.addresult = add(1,1); };
    this.testAddSuccess = function() {
        assertEquals(2, this.addresult);
    }
    this.testAddFail = function() {
    	assertTrue("3==2", 3 == add(1,1))
    }
}

var TestCalcLitObj = {
	testObjLitAddSuccess : function() { assertEquals(2, add(1, 1)) },
	testObjLitAddFail : function() { assertEquals(2, add(1, 1)) }
}