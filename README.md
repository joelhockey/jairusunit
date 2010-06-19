JSUnit
======
JSUnit is a JavaScript unit testing framework that runs only
in Rhino and extends the JUnit java classes.

ant and maven integration
-------------------------
JSUnit is designed to be used to test either JavaScript or
Java code.  It integrates with ant and maven and should quite
easily replace or work along side any existing JUnit testing.

JSUnit produces the same plain and xml reports as JUnit and
maven-surefire-plugin.

ant
---
JSUnit defines the jsunit ant taskdef.

    <!-- example of using jsunit -->
    <target name="test" depends="compile" description="test">
      <copy todir="target/test-classes">
        <fileset dir="src/test/javascript"/>
      </copy>
      <taskdef name="jsunit" classname="com.joelhockey.jsunit.JSUnitTask">
        <classpath> <!-- path to include jsunit.jar -->
          <pathelement location="target/classes" />
        </classpath>
      </taskdef>
    
      <jsunit> <!-- optional fork (yes by default) -->
        <classpath> <!-- path to include jsunit.jar, junit.jar, js.jar, any other java libs for testing -->
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
      </jsunit>
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
JSUnit can be integrated with maven by using the maven-antrun-plugin

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
                    <taskdef name="jsunit" classname="com.joelhockey.jsunit.JSUnitTask"
                      classpathref="maven.test.classpath" />
                    <jsunit failonerror="no" classpathref="maven.test.classpath">
                      <batchtest>
                        <fileset dir="src/test/javascript">
                          <include name="**/*test*.js" />
                        </fileset>
                      </batchtest>
                    </jsunit>
                  </tasks>
                </configuration>
              </execution>
            </executions>
          </plugin>
        
        </plugins>
      </build>
    </project>

Writing Tests with JSUnit
-------------------------
JSUnit 

Tests can be defined in a number of different ways:
* defining javascript objects (using function constructor) that start or end with 'Test'
and naming test methods to start with 'test'
* global methods starting with 'test'
* methods within literal objects starting with 'test'

Examples
--------

calc.js (to be tested)
    function add(a, b) { return a + b; }
    
calctest.js
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

In the example tests, all functions starting with 'test' will be run as tests.
There will be a single failure.

    $ ant test
    Buildfile: build.xml

    init:

    compile:

    test:
       [jsunit] Running jsunit.calctest
       [jsunit] Tests run: 7, Failures: 0, Errors: 1, Time elapsed: 0.015
       [jsunit] Test testAddFail(C:\java\joelhockey\jsunit\target\test-classes/calctest.js.global.CalcTest)
       [jsunit]     ERROR: junit.framework.AssertionFailedError: 3==2 (jsunit.js#41)

    BUILD FAILED
    
It will produce a text and xml formatted report in the target/surefire-reports directory.
The text output is:
    $ cat target/surefire-reports/TEST-jsunit.calctest.txt
    Testsuite: jsunit.calctest
    Tests run: 7, Failures: 0, Errors: 1, Time elapsed: 0.015

    Testcase: testObjLitAddFail(C:\java\joelhockey\jsunit\target\test-classes/calctest.js.global.TestCalcLitObj) took 0.000 sec
    Testcase: testObjLitAddFail(C:\java\joelhockey\jsunit\target\test-classes/calctest.js.global.TestCalcLitObj) took 0.000 sec
    Testcase: testAddSuccess(C:\java\joelhockey\jsunit\target\test-classes/calctest.js.global.CalcTest) took 0.000 sec
    Testcase: testAddFail(C:\java\joelhockey\jsunit\target\test-classes/calctest.js.global.CalcTest) took 0.000 sec
            Caused an ERROR
    org.mozilla.javascript.JavaScriptException: junit.framework.AssertionFailedError: 3==2 (jsunit.js#41)
            at org.mozilla.javascript.gen.c2._c8(C:\java\joelhockey\jsunit\target\test-classes/calctest.js:23)
            at org.mozilla.javascript.gen.c2.call(C:\java\joelhockey\jsunit\target\test-classes/calctest.js)
            at adapter4.testAddFail(<adapter>)
            at adapter8.run(<adapter>)

    Testcase: testgl(C:\java\joelhockey\jsunit\target\test-classes/calctest.js.global) took 0.000 sec
    Testcase: testWithPAddSuccess(C:\java\joelhockey\jsunit\target\test-classes/calctest.js.global.CalcWithPTest) took 0.000 sec
    Testcase: testWithPAddFail(C:\java\joelhockey\jsunit\target\test-classes/calctest.js.global.CalcWithPTest) took 0.000 sec

    
load method
-----------
JSUnit provides a global 'load' method that loads the specified filename.

assert methods
--------------
JSUnit has the same assert methods as JUnit.