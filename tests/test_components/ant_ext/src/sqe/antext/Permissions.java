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

public class Permissions extends Task {

    private File file;
    private String permissions;

    public void setFile(String jar) {
        file = new File(jar);
    }

    public void setPermissions(String perms) {
        permissions = perms;
    }

    public void execute() throws BuildException {
        try {
            ArrayList<String> lines = new ArrayList<String>();
            ArrayList<String> permLines = new ArrayList<String>();
            if (file.exists()) {
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                String nextLine = null;
                while ((nextLine = br.readLine()) != null) {
                    nextLine = nextLine.trim();
                    if (nextLine.length() > 0) {
                        if (nextLine.startsWith("MIDlet-Permission-")) {
                            permLines.add(nextLine);
                        } else {
                            lines.add(nextLine);
                        }
                    }

                }
                br.close();
            } else {
                file.createNewFile();
            }
            if (permissions != null && permissions.length() > 0) {
                permissions = permissions.substring(1, permissions.length() - 1);
                String[] perms = permissions.split("\\}\\p{Space}+\\{");
                for (String nextPermission : perms) {
                    System.out.println("--> " + nextPermission);
                    permLines.add("MIDlet-Permission-" + (permLines.size() + 1) + ": " + nextPermission);
                }
            }
            FileOutputStream fos = new FileOutputStream(file, false);
            while (!lines.isEmpty()) {
                fos.write(lines.remove(0).getBytes());
                fos.write("\n".getBytes());
            }
            while (!permLines.isEmpty()) {
                fos.write(permLines.remove(0).getBytes());
                fos.write("\n".getBytes());
            }
            fos.flush();
            fos.close();
        } catch (Throwable e) {
            throw new BuildException(e);
        }
    }
}
