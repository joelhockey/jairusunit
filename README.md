JairusUnit
======
JairusUnit is a JavaScript unit testing framework that uses Rhino and JUnit.

JairusUnit uses Rhino's ability to make JavaScript objects
extend Java classes.  It coerces JavaScript test objects to
extend the JUnit TestCase class and then uses JUnit to run
the tests.

ant and maven integration
-------------------------
JairusUnit is designed to be used to test either JavaScript or
Java code.  It integrates with ant and maven and should quite
easily replace or work along side any existing JUnit testing.

JairusUnit produces the same plain and xml reports as JUnit and
maven-surefire-plugin.

ant
---
JairusUnit defines the jairusunit ant taskdef which has many of the same options
as the junit task.

    <!-- example of using jairusunit -->
    <target name="test" depends="compile" description="test">
      <copy todir="target/test-classes">
        <fileset dir="src/test/javascript"/>
      </copy>
      <taskdef name="jairusunit" classname="com.joelhockey.jairusunit.JairusUnitTask">
        <classpath> <!-- path to include jairusunit.jar -->
          <pathelement location="target/classes" />
        </classpath>
      </taskdef>
    
      <jairusunit> <!-- optional fork (yes by default) -->
        <classpath> <!-- path to include jairusunit.jar, junit.jar, js.jar, any other java libs for testing -->
          <pathelement location="target/test-classes" />
          <pathelement location="target/classes" />
          <fileset dir="lib" />
        </classpath>
        <!-- no need for formatters, plain and xml always done -->
        <batchtest> <!-- optional 'todir' (target/surefire-reports) by default -->
          <fileset dir="target/test-classes">
            <include name="**/*test*.js"/>
          </fileset>
        </batchtest>
      </jairusunit>
      <!-- can create junit report from xml output -->
      <mkdir dir="target/report/html" />
      <junitreport todir="target/report">
        <fileset dir="target/surefire-reports">
          <include name="TEST-*.xml"/>
        </fileset>
        <report format="frames" todir="target/report/html"/>
      </junitreport>
    </target>

maven
-----
JairusUnit can be integrated with maven by using the maven-antrun-plugin

    <project>
      <build>
        <plugins>
        
          <plugin>
            <artifactId>maven-antrun-plugin</artifactId>
            <version>1.1</version>
            <executions>
              <execution>
                <id>test</id>
                <phase>test</phase>
                <goals>
                  <goal>run</goal>
                </goals>
                <configuration>
                  <tasks>
                    <taskdef name="jairusunit" classname="com.joelhockey.jairusunit.JairusUnitTask"
                      classpathref="maven.test.classpath" />
                    <jairusunit failonerror="no" classpathref="maven.test.classpath">
                      <batchtest>
                        <fileset dir="src/test/javascript">
                          <include name="**/*test*.js" />
                        </fileset>
                      </batchtest>
                    </jairusunit>
                  </tasks>
                </configuration>
              </execution>
            </executions>
          </plugin>
        
        </plugins>
      </build>
      
      <dependencies>
        <dependency>
          <groupId>rhino</groupId>
          <artifactId>js</artifactId>
          <version>1.7R2</version>
        </dependency>
        <dependency>
          <groupId>com.joelhockey</groupId>
          <artifactId>jairusunit</artifactId>
          <version>1.0</version>
          <scope>test</scope>
        </dependency>
        <dependency>
          <groupId>junit</groupId>
          <artifactId>junit</artifactId>
          <version>3.8.1</version>
          <scope>test</scope>
        </dependency>
      </dependencies>      
    </project>

Writing Tests with JairusUnit
-------------------------
Tests can be defined in a number of different ways:

* defining javascript objects (using function constructor) that start or end with 'Test'
and naming test methods to start with 'test'
* global methods starting with 'test'
* methods within literal objects starting with 'test'

Examples
--------

/src/main/javascript/calc.js
    // example code to be tested

    function add(a, b) {
        return a + b;
    }
    
/src/test/javascript/calctest.js
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
        // note that JairusUnit supports setUp and tearDown methods
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

In the example tests, all functions starting with 'test' will be run as tests.
There will be 12 tests run in total.  4 will pass, 4 will have failures,
and 4 will have errors.

    $ ant test
    Buildfile: build.xml

    init:

    compile:

    test:
    [jairusunit] Running jairusunit.calctest
    [jairusunit] Tests run: 12, Failures: 4, Errors: 4, Time elapsed: 0.000
    [jairusunit] Test testAddFail(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js.TestObjectLiteral)
    [jairusunit]    FAILED: expected:<1> but was:<2>
    [jairusunit] Test testAddError(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js.TestObjectLiteral)
    [jairusunit]    ERROR:
    [jairusunit] java.lang.Exception: test exception
    [jairusunit] Test testAddFail(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js.FunctionWithMethodsTest)
    [jairusunit]    FAILED: 3==2
    [jairusunit] Test testAddError(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js.FunctionWithMethodsTest)
    [jairusunit]    ERROR:
    [jairusunit] Test testAddFail(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js.FunctionWithPrototypeTest)
    [jairusunit]    FAILED: expected:<11> but was:<2>
    [jairusunit] Test testAddError(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js.FunctionWithPrototypeTest)
    [jairusunit]    ERROR:
    [jairusunit] java.lang.Exception: throwing a java exception
    [jairusunit] Test testAddError(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js.global)
    [jairusunit]    ERROR:
    [jairusunit] "C:\java\joelhockey\jairusunit\target\test-classes/calctest.js", line 17: [object Error]
    [jairusunit] Test testAddFail(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js.global)
    [jairusunit]    FAILED: expected:<-2> but was:<2>

    
It will produce a text and xml formatted report in the target/surefire-reports directory.
The text output is:
    $ cat target/surefire-reports/TEST-jairusunit.calctest.txt
    Testsuite: jairusunit.calctest
    Tests run: 12, Failures: 4, Errors: 4, Time elapsed: 0.000

    Testcase: testAddSuccess(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js.TestObjectLiteral) took 0.000 sec
    Testcase: testAddFail(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js.TestObjectLiteral) took 0.000 sec
        FAILED
    junit.framework.AssertionFailedError: expected:<1> but was:<2>
        at java.lang.reflect.Constructor.newInstance(Constructor.java:513)
        at org.mozilla.javascript.NativeJavaClass.constructSpecific(NativeJavaClass.java:281)
        at org.mozilla.javascript.NativeJavaClass.construct(NativeJavaClass.java:200)
        at org.mozilla.javascript.gen.c2._c14(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js:55)
        at org.mozilla.javascript.gen.c2.call(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js)
        at org.mozilla.javascript.gen.c2.call(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js)
        at adapter2.testAddFail(<adapter>)

    Testcase: testAddError(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js.TestObjectLiteral) took 0.000 sec
        Caused an ERROR

    java.lang.Exception: test exception
        at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
        at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:39)
        at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:27)
        at java.lang.reflect.Constructor.newInstance(Constructor.java:513)
        at org.mozilla.javascript.MemberBox.newInstance(MemberBox.java:194)
        at org.mozilla.javascript.NativeJavaClass.constructSpecific(NativeJavaClass.java:281)
        at org.mozilla.javascript.NativeJavaClass.construct(NativeJavaClass.java:200)
        at org.mozilla.javascript.ScriptRuntime.newObject(ScriptRuntime.java:2327)
        at org.mozilla.javascript.gen.c2._c15(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js:58)
        at org.mozilla.javascript.gen.c2.call(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js)
        at org.mozilla.javascript.ContextFactory.doTopCall(ContextFactory.java:398)
        at org.mozilla.javascript.ScriptRuntime.doTopCall(ScriptRuntime.java:3065)
        at org.mozilla.javascript.gen.c2.call(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js)
        at org.mozilla.javascript.Context$1.run(Context.java:504)
        at org.mozilla.javascript.Context.call(Context.java:515)
        at org.mozilla.javascript.Context.call(Context.java:502)
        at org.mozilla.javascript.JavaAdapter.callMethod(JavaAdapter.java:548)
        at adapter3.testAddError(<adapter>)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
        at java.lang.reflect.Method.invoke(Method.java:597)
        at junit.framework.TestCase.runTest(TestCase.java:164)
        at junit.framework.TestCase.runBare(TestCase.java:130)
        at junit.framework.TestResult$1.protect(TestResult.java:106)
        at junit.framework.TestResult.runProtected(TestResult.java:124)
        at junit.framework.TestResult.run(TestResult.java:109)
        at junit.framework.TestCase.run(TestCase.java:120)
        at junit.framework.TestSuite.runTest(TestSuite.java:230)
        at junit.framework.TestSuite.run(TestSuite.java:225)
        at junit.framework.TestSuite.runTest(TestSuite.java:230)
        at junit.framework.TestSuite.run(TestSuite.java:225)
        at junit.framework.TestSuite.runTest(TestSuite.java:230)
        at junit.framework.TestSuite.run(TestSuite.java:225)
        at com.joelhockey.jairusunit.JairusUnit.main(JairusUnit.java:215)

    Testcase: testAddSuccess(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js.FunctionWithMethodsTest) took 0.000 sec
    Testcase: testAddFail(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js.FunctionWithMethodsTest) took 0.000 sec
        FAILED
    junit.framework.AssertionFailedError: 3==2
        at java.lang.reflect.Constructor.newInstance(Constructor.java:513)
        at org.mozilla.javascript.NativeJavaClass.constructSpecific(NativeJavaClass.java:281)
        at org.mozilla.javascript.NativeJavaClass.construct(NativeJavaClass.java:200)
        at org.mozilla.javascript.gen.c2._c11(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js:44)
        at org.mozilla.javascript.gen.c2.call(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js)
        at org.mozilla.javascript.gen.c2.call(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js)
        at adapter5.testAddFail(<adapter>)

    Testcase: testAddError(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js.FunctionWithMethodsTest) took 0.000 sec
        Caused an ERROR

    "C:\java\joelhockey\jairusunit\target\test-classes/calctest.js", line 47: [object Error]
    org.mozilla.javascript.JavaScriptException: [object Error] (C:\java\joelhockey\jairusunit\target\test-classes/calctest.js#47)
        at org.mozilla.javascript.gen.c2._c12(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js:47)
        at org.mozilla.javascript.gen.c2.call(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js)
        at org.mozilla.javascript.ContextFactory.doTopCall(ContextFactory.java:398)
        at org.mozilla.javascript.ScriptRuntime.doTopCall(ScriptRuntime.java:3065)
        at org.mozilla.javascript.gen.c2.call(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js)
        at org.mozilla.javascript.Context$1.run(Context.java:504)
        at org.mozilla.javascript.Context.call(Context.java:515)
        at org.mozilla.javascript.Context.call(Context.java:502)
        at org.mozilla.javascript.JavaAdapter.callMethod(JavaAdapter.java:548)
        at adapter6.testAddError(<adapter>)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
        at java.lang.reflect.Method.invoke(Method.java:597)
        at junit.framework.TestCase.runTest(TestCase.java:164)
        at junit.framework.TestCase.runBare(TestCase.java:130)
        at junit.framework.TestResult$1.protect(TestResult.java:106)
        at junit.framework.TestResult.runProtected(TestResult.java:124)
        at junit.framework.TestResult.run(TestResult.java:109)
        at junit.framework.TestCase.run(TestCase.java:120)
        at junit.framework.TestSuite.runTest(TestSuite.java:230)
        at junit.framework.TestSuite.run(TestSuite.java:225)
        at junit.framework.TestSuite.runTest(TestSuite.java:230)
        at junit.framework.TestSuite.run(TestSuite.java:225)
        at junit.framework.TestSuite.runTest(TestSuite.java:230)
        at junit.framework.TestSuite.run(TestSuite.java:225)
        at com.joelhockey.jairusunit.JairusUnit.main(JairusUnit.java:215)

    Testcase: testAddSuccess(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js.FunctionWithPrototypeTest) took 0.000 sec
    Testcase: testAddFail(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js.FunctionWithPrototypeTest) took 0.000 sec
        FAILED
    junit.framework.AssertionFailedError: expected:<11> but was:<2>
        at java.lang.reflect.Constructor.newInstance(Constructor.java:513)
        at org.mozilla.javascript.NativeJavaClass.constructSpecific(NativeJavaClass.java:281)
        at org.mozilla.javascript.NativeJavaClass.construct(NativeJavaClass.java:200)
        at org.mozilla.javascript.gen.c2._c6(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js:27)
        at org.mozilla.javascript.gen.c2.call(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js)
        at org.mozilla.javascript.gen.c2.call(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js)
        at adapter8.testAddFail(<adapter>)

    Testcase: testAddError(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js.FunctionWithPrototypeTest) took 0.000 sec
        Caused an ERROR

    java.lang.Exception: throwing a java exception
        at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
        at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:39)
        at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:27)
        at java.lang.reflect.Constructor.newInstance(Constructor.java:513)
        at org.mozilla.javascript.MemberBox.newInstance(MemberBox.java:194)
        at org.mozilla.javascript.NativeJavaClass.constructSpecific(NativeJavaClass.java:281)
        at org.mozilla.javascript.NativeJavaClass.construct(NativeJavaClass.java:200)
        at org.mozilla.javascript.ScriptRuntime.newObject(ScriptRuntime.java:2327)
        at org.mozilla.javascript.gen.c2._c7(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js:30)
        at org.mozilla.javascript.gen.c2.call(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js)
        at org.mozilla.javascript.ContextFactory.doTopCall(ContextFactory.java:398)
        at org.mozilla.javascript.ScriptRuntime.doTopCall(ScriptRuntime.java:3065)
        at org.mozilla.javascript.gen.c2.call(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js)
        at org.mozilla.javascript.Context$1.run(Context.java:504)
        at org.mozilla.javascript.Context.call(Context.java:515)
        at org.mozilla.javascript.Context.call(Context.java:502)
        at org.mozilla.javascript.JavaAdapter.callMethod(JavaAdapter.java:548)
        at adapter9.testAddError(<adapter>)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
        at java.lang.reflect.Method.invoke(Method.java:597)
        at junit.framework.TestCase.runTest(TestCase.java:164)
        at junit.framework.TestCase.runBare(TestCase.java:130)
        at junit.framework.TestResult$1.protect(TestResult.java:106)
        at junit.framework.TestResult.runProtected(TestResult.java:124)
        at junit.framework.TestResult.run(TestResult.java:109)
        at junit.framework.TestCase.run(TestCase.java:120)
        at junit.framework.TestSuite.runTest(TestSuite.java:230)
        at junit.framework.TestSuite.run(TestSuite.java:225)
        at junit.framework.TestSuite.runTest(TestSuite.java:230)
        at junit.framework.TestSuite.run(TestSuite.java:225)
        at junit.framework.TestSuite.runTest(TestSuite.java:230)
        at junit.framework.TestSuite.run(TestSuite.java:225)
        at com.joelhockey.jairusunit.JairusUnit.main(JairusUnit.java:215)

    Testcase: testAddError(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js.global) took 0.000 sec
        Caused an ERROR

    "C:\java\joelhockey\jairusunit\target\test-classes/calctest.js", line 17: [object Error]
    org.mozilla.javascript.JavaScriptException: [object Error] (C:\java\joelhockey\jairusunit\target\test-classes/calctest.js#17)
        at org.mozilla.javascript.gen.c2._c3(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js:17)
        at org.mozilla.javascript.gen.c2.call(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js)
        at org.mozilla.javascript.ContextFactory.doTopCall(ContextFactory.java:398)
        at org.mozilla.javascript.ScriptRuntime.doTopCall(ScriptRuntime.java:3065)
        at org.mozilla.javascript.gen.c2.call(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js)
        at org.mozilla.javascript.Context$1.run(Context.java:504)
        at org.mozilla.javascript.Context.call(Context.java:515)
        at org.mozilla.javascript.Context.call(Context.java:502)
        at org.mozilla.javascript.JavaAdapter.callMethod(JavaAdapter.java:548)
        at adapter10.testAddError(<adapter>)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
        at java.lang.reflect.Method.invoke(Method.java:597)
        at junit.framework.TestCase.runTest(TestCase.java:164)
        at junit.framework.TestCase.runBare(TestCase.java:130)
        at junit.framework.TestResult$1.protect(TestResult.java:106)
        at junit.framework.TestResult.runProtected(TestResult.java:124)
        at junit.framework.TestResult.run(TestResult.java:109)
        at junit.framework.TestCase.run(TestCase.java:120)
        at junit.framework.TestSuite.runTest(TestSuite.java:230)
        at junit.framework.TestSuite.run(TestSuite.java:225)
        at junit.framework.TestSuite.runTest(TestSuite.java:230)
        at junit.framework.TestSuite.run(TestSuite.java:225)
        at com.joelhockey.jairusunit.JairusUnit.main(JairusUnit.java:215)

    Testcase: testAddFail(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js.global) took 0.000 sec
        FAILED
    junit.framework.AssertionFailedError: expected:<-2> but was:<2>
        at java.lang.reflect.Constructor.newInstance(Constructor.java:513)
        at org.mozilla.javascript.NativeJavaClass.constructSpecific(NativeJavaClass.java:281)
        at org.mozilla.javascript.NativeJavaClass.construct(NativeJavaClass.java:200)
        at org.mozilla.javascript.gen.c2._c2(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js:13)
        at org.mozilla.javascript.gen.c2.call(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js)
        at org.mozilla.javascript.gen.c2.call(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js)
        at adapter11.testAddFail(<adapter>)

    Testcase: testAddSuccess(C:\java\joelhockey\jairusunit\target\test-classes/calctest.js.global) took 0.000 sec

    
load method
-----------
JairusUnit provides a global 'load' method that loads the specified filename.
The JairusUnit Rhino scope class also puts methods 'print', 'printf' and 'readFile' into
the global scope.

assert methods
--------------
JairusUnit has the same assert methods as JUnit.
