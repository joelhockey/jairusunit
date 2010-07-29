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

package com.joelhockey.jairusunit;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * Rhino global scope for JairusUnit.
 * @author Joel Hockey
 */
public class JairusUnitScope extends ImporterTopLevel {
    private static final long serialVersionUID = 0x37A5FBB0114CDFECL;

    public JairusUnitScope(Context cx) {
        super(cx);
        String[] names = {
            "load",
            "print",
            "printf",
            "readFile",
        };
        defineFunctionProperties(names, JairusUnitScope.class, ScriptableObject.DONTENUM);
     }

    /**
     * Load file.  Tries to find file using absolute path, then
     * using classloader as relative file, then adds '/' to start of path
     * and tries classloader again.
     * @param path path to file
     * @throws IOException if error finding, reading file
     */
    public void load(String path) throws IOException {
        InputStream ins;

        // if file exists, load
        File f = new File(path);
        if (f.exists()) {
            ins = new FileInputStream(f);
        // else, try a resource
        } else {
            ins = JairusUnitScope.class.getResourceAsStream(path);
            if (ins == null && !path.startsWith("/")) {
                ins = JairusUnitScope.class.getResourceAsStream("/" + path);
            }
            if (ins == null) {
                throw new IOException("Could not find file: " + path);
            }
        }

        Reader reader = new InputStreamReader(ins);
        Context cx = Context.enter();
        try {
            cx.evaluateReader(this, reader, path, 1, null);
        } finally {
            Context.exit();
            reader.close();
        }
    }

    public static void print(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (int i=0; i < args.length; i++) {
            sb.append(sep);
            sep = " ";
            sb.append(Context.toString(args[i]));
        }
        System.out.println(sb);
    }

    public static String printf(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        String fstr = (String) args[0];
        fstr = fstr.replace("%d", "%.0f"); // all numbers will be doubles, so convert here
        Object[] fargs = new Object[args.length - 1];
        System.arraycopy(args, 1, fargs, 0, fargs.length);
        return String.format(fstr, fargs);
    }

    /**
     * Returns file as string.  Tries to find file using absolute path, then
     * using classloader as relative file, then adds '/' to start of path
     * and tries classloader again.
     * @param path path to file
     * @throws IOException if error finding, reading file
     */
    public String readFile(String path) throws IOException {
        InputStream ins;

        // if file exists, load
        File f = new File(path);
        if (f.exists()) {
            ins = new FileInputStream(f);
        // else, try a resource
        } else {
            ins = JairusUnitScope.class.getResourceAsStream(path);
            if (ins == null && !path.startsWith("/")) {
                ins = JairusUnitScope.class.getResourceAsStream("/" + path);
            }
            if (ins == null) {
                throw new IOException("Could not find file: " + path);
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        for (int l = 0; (l = ins.read(buf)) != -1; ) {
            baos.write(buf, 0, l);
        }
        ins.close();
        return new String(baos.toByteArray());
    }
}
