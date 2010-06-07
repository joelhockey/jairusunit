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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.resources.FileResource;

/**
 * Ant 'jsunit' task for JSUnit extends from {@link Java}.
 * Supports similar format to 'junit' task, including
 * nested 'batchtest' elements and forking (fork once is default).
 * Always does plain and xml reports.
 * @author Joel Hockey
 */
public class JSUnitTask extends Java {
    public static class BatchTest {
        public String todir = "target/surefire-reports";
        public List<FileSet> fileSets = new ArrayList<FileSet>();
        public void addFileSet(FileSet fileSet) {
            fileSets.add(fileSet);
        }
        public void setTodir(String todir) { this.todir = todir; }
    }

    private List<BatchTest> batchTests = new ArrayList<BatchTest>();
    public JSUnitTask() {
        setClassname(JSUnit.class.getName());
        setFork(true);
    }

    public void addBatchTest(BatchTest batchTest) { batchTests.add(batchTest); }

    public void execute() throws BuildException {
        String todir = "";
        String basedir = "";
        for (BatchTest batchTest : batchTests) {
            todir = setArgIfDifferent("-todir", todir, batchTest.todir);
            for (FileSet fs : batchTest.fileSets) {
                for (Iterator it = fs.iterator(); it.hasNext(); ) {
                    FileResource fr = (FileResource) it.next();
                    basedir = setArgIfDifferent("-basedir", basedir, fr.getBaseDir().toString());
                    createArg().setValue(fr.getName());
                }
            }
        }
        executeJava();
    }

    private String setArgIfDifferent(String argName, String currentValue, String newValue) {
        if (!newValue.equals(currentValue)) {
            createArg().setValue(argName);
            createArg().setValue(newValue);
        }
        return newValue;
    }
}
