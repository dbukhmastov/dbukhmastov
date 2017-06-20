/*
 *
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
 *
 */

package sqe.antext;

import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import sqe.antext.util.FeatureTestFinder;

public class LSTGenerator extends Task {
    String srcPath;
    String classPath;
    String[] dependencyPackages;

    public void execute() throws BuildException {
        // Properties contains class, id Tag value.
        // ie, sqe/test/imc/LIBletUpdatTest, LIBletUpdateTest

        TestsProperties props = getClassProperties(srcPath);

        Path path = Paths.get(classPath, "shared", "testClasses.lst");
        // If the file doesn't exist then make one.
        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
            } catch (Exception ex) {
                throw new BuildException("Failed prepare "+path+" for writing");
            }
        }

        try (OutputStream os = Files.newOutputStream(path, TRUNCATE_EXISTING)) {
            // Write copywrite header
            try {
                os.write("#\n# Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.\n#\n\n".getBytes());
            } catch (IOException ex) {
                throw new BuildException("Failed to write record to testclasses.lst");
            }

            for (String className : props.propertyNames()) { 
                for (String id : props.getProperty(className)) { //list all className's ids (test headers)
                    log("extracted : " + className + ".java#" + id);
                    try {
                        os.write((className + ".java#" + id).getBytes());
                        Set<String> dependencies = findDependencies(classPath + "/preverified", className + ".class", dependencyPackages);
                        for (String d : dependencies) {
                            os.write((" " + d).getBytes());
                        }
                        os.write(("\n").getBytes());
                    } catch (IOException ex) {
                        throw new BuildException("Failed to write record to testclasses.lst");
                    }
                }
            }
        } catch (IOException ex) {
            throw new BuildException("Unexpected exception while writing to testclasses.lst");
        }
    }

    private TestsProperties getClassProperties(String srcPath) {
        FeatureTestFinder finder = new FeatureTestFinder();
        finder.scan(new File(srcPath));
        return finder.getFileProperties();
    }

    public void setSrcPath(String srcPath) {
        this.srcPath = srcPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    public void setDependencyPackages(String dependencyPackages) {
        String[] packages = dependencyPackages.split(",");
        for (int i = 0; i < packages.length; i++) {
            packages[i] = packages[i].replace('.', '/');
        }
        this.dependencyPackages = packages;
    }

    /**
     * Finds all the dependencies within the 'sqe' package for the indicated
     * class.
     *
     * @param classesDir
     *            The directory containing the root of the sqe package binaries.
     *
     * @param className
     *            The fully qualified name of the class for which dependencies
     *            are to be found.
     *
     * @return A 'Set' of Strings containing the fully qualified class names of
     *         all dependencies.
     */
     Set<String> findDependencies(String classesDir, String className, String... packages) {
        Set<String> dependencies = new TreeSet<>();
        Set<String> toSearch = new TreeSet<>();
        toSearch.add(className);

        while (!toSearch.isEmpty()) {
            Set<String> newSearch = new TreeSet<>();
            for (String c : toSearch) {
                Path path = Paths.get(classesDir, c);
                // Verify that class has a corresponding class file, if not then skip it.
                if (!Files.exists(path)) {
                    toSearch.remove(c);
                }
                try {
                    DataInputStream is = new DataInputStream(Files.newInputStream(path, READ));
                    while (is.available() > 0) {
                        String s = nextString(is);
                        int index = -1;
                        for (String p : packages) {                           
                            index = s.indexOf(p + "/");
                            if (index >= 0) {
                                String cl = s.substring(index).trim() + ".class";
                                if (!dependencies.contains(cl) && !toSearch.contains(cl) && !newSearch.contains(cl)) {
                                    newSearch.add(cl);
                                }
                                break;
                            }
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    return null;
                }
            }
            dependencies.addAll(toSearch);
            toSearch.clear();
            toSearch.addAll(newSearch);
        }

        return dependencies;
    }

    /**
     * Gets the next String in the DataInputStream that could be a valid Java
     * identifier.
     *
     * @param is
     *            The DataInputStream in which to search for a String.
     *
     * @return The next String in the DataInputStream or an empty String if \
     *         none is found.
     *
     * @throws IOException
     *             If some error occurs while reading from the DataInputStream.
     */
    String nextString(DataInputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        char c;
        while (is.available() > 0) {
            c = (char) (is.readByte() & 0x00FF);
            if (Character.isJavaIdentifierStart(c)) {
                sb.append(c);
                break;
            }
        }
        if (sb.length() > 0) {
            while (is.available() > 0) {
                c = (char) (is.readByte() & 0x00FF);
                if ((Character.isJavaIdentifierPart(c) || c == '/') && c != ';' && c > ' ') {
                    sb.append(c);
                } else {
                    break;
                }
            }
        }
        return sb.toString();
    }
}
