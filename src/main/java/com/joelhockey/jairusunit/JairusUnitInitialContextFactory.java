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

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

/**
 * Initial Context can be helpful in testing J2EE components.
 * @author Joel Hockey
 */
public class JairusUnitInitialContextFactory implements InitialContextFactory {
    private static Context ctx = new Context();

    public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
        return ctx;
    }

    private static class Context implements javax.naming.Context {
        public Map ctxMap = new HashMap<String, Object>();
        public Object lookup(String name) throws NamingException {
            return ctxMap.get(name);
        }
        public void bind(String name, Object obj) throws NamingException {
            ctxMap.put(name, obj);
        }
        public void close() throws NamingException {}

        // ignore below here
        public Object addToEnvironment(String propName, Object propVal) throws NamingException { throw new UnsupportedOperationException(); }
        public void bind(Name name, Object obj) throws NamingException { throw new UnsupportedOperationException(); }
        public Name composeName(Name name, Name prefix) throws NamingException { throw new UnsupportedOperationException(); }
        public String composeName(String name, String prefix) throws NamingException { throw new UnsupportedOperationException(); }
        public javax.naming.Context createSubcontext(Name name) throws NamingException { throw new UnsupportedOperationException(); }
        public javax.naming.Context createSubcontext(String name) throws NamingException { throw new UnsupportedOperationException(); }
        public void destroySubcontext(Name name) throws NamingException { throw new UnsupportedOperationException(); }
        public void destroySubcontext(String name) throws NamingException { throw new UnsupportedOperationException(); }
        public Hashtable<?, ?> getEnvironment() throws NamingException { throw new UnsupportedOperationException(); }
        public String getNameInNamespace() throws NamingException { throw new UnsupportedOperationException(); }
        public NameParser getNameParser(Name name) throws NamingException { throw new UnsupportedOperationException(); }
        public NameParser getNameParser(String name) throws NamingException { throw new UnsupportedOperationException(); }
        public NamingEnumeration<NameClassPair> list(Name name) throws NamingException { throw new UnsupportedOperationException(); }
        public NamingEnumeration<NameClassPair> list(String name) throws NamingException { throw new UnsupportedOperationException(); }
        public NamingEnumeration<Binding> listBindings(Name name) throws NamingException { throw new UnsupportedOperationException(); }
        public NamingEnumeration<Binding> listBindings(String name) throws NamingException { throw new UnsupportedOperationException(); }
        public Object lookup(Name name) throws NamingException { throw new UnsupportedOperationException(); }
        public Object lookupLink(Name name) throws NamingException { throw new UnsupportedOperationException(); }
        public Object lookupLink(String name) throws NamingException { throw new UnsupportedOperationException(); }
        public void rebind(Name name, Object obj) throws NamingException { throw new UnsupportedOperationException(); }
        public void rebind(String name, Object obj) throws NamingException { throw new UnsupportedOperationException(); }
        public Object removeFromEnvironment(String propName) throws NamingException { throw new UnsupportedOperationException(); }
        public void rename(Name oldName, Name newName) throws NamingException { throw new UnsupportedOperationException(); }
        public void rename(String oldName, String newName) throws NamingException { throw new UnsupportedOperationException(); }
        public void unbind(Name name) throws NamingException { throw new UnsupportedOperationException(); }
        public void unbind(String name) throws NamingException { throw new UnsupportedOperationException(); }
    }
}
