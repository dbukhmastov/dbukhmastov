/*
 *
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
 *
 */

package sqe.antext;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import java.io.*;
import java.util.ArrayList;

public class WriteJarSize extends Task {

    private File jadFile;
    private File jarFile;

    public void setJarFile(String jar) {
        jarFile = new File(jar);
    }

    public void setJadFile(String jad) {
        jadFile = new File(jad);
    }

    public void execute() throws BuildException {
        try {
            ArrayList<String> lines = new ArrayList<String>();
            if (jadFile.exists()) {
                FileReader fr = new FileReader(jadFile);
                BufferedReader br = new BufferedReader(fr);
                String nextLine = null;
                while ((nextLine = br.readLine()) != null) {
                    nextLine = nextLine.trim();
                    if (!nextLine.startsWith("MIDlet-Jar-Size") && nextLine.length() > 0) {
                        lines.add(nextLine);
                    }
                }
                br.close();
            } else {
                jadFile.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(jadFile, false);
            while (!lines.isEmpty()) {
                fos.write(lines.remove(0).getBytes());
                fos.write("\n".getBytes());
            }
            fos.write("MIDlet-Jar-Size: ".getBytes());
            fos.write(Long.toString(jarFile.length()).getBytes());
            fos.write("\n".getBytes());
            fos.flush();
            fos.close();
        } catch (Throwable e) {
            throw new BuildException(e);
        }
    }
}
