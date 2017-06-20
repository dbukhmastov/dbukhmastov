/*
 *
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
 *
 */
package sqe.antext.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.Properties;

public class CustomProperties extends Properties {

    private static final long serialVersionUID = 1L;

    @Override
    public void store(OutputStream out, String comments) throws IOException {
        customStore0(new BufferedWriter(new OutputStreamWriter(out, "8859_1")),
                comments, true);
    }

    private void customStore0(BufferedWriter bw, String comments, boolean escUnicode)
            throws IOException {
        synchronized (this) {
            for (Enumeration<Object> e = keys(); e.hasMoreElements();) {
                String key = (String) e.nextElement();
                String val = (String) get(key);
                bw.write(key + ": " + val);
                bw.newLine();
            }
        }
        bw.flush();
    }
}
