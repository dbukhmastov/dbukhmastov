/*
 *
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
 *
 */
package sqe.antext.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sqe.antext.TestsProperties;

public class FeatureTestFinder {

    private TestsProperties props = new TestsProperties();
    private String baseDir;

    public void scan(File file) {
        baseDir = file.getAbsolutePath();
        log("start scan : " + file.getAbsolutePath());
        if (file.isDirectory()) {
            readDirectory(file);
        } else {
            foundFile(file);
        }
    }

    private void readDirectory(File dir) {
        File[] childs = dir.listFiles();
        for (File file : childs) {
            if (file.isDirectory()) {
                readDirectory(file);
            } else if (isFeatureTest(file)) {
                foundFile(file);
            }
        }
    }

    private boolean isFeatureTest(File file) {
        // file must contains @test and @id
        if (getIDTagValues(file) != null)
            return true;
        else
            return false;
    }

    private List<String> getIDTagValues(File file) {
        ArrayList<String> tagValues = new ArrayList<String>();
        Pattern p = Pattern.compile("\\@id");

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                Matcher m = p.matcher(line);
                if (m.find()) {
                    log("found tag");
                    tagValues.add(line.substring(m.end() + 1).trim());
                    log("found tagValue=@" + tagValues);
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tagValues;
    }

    private void foundFile(File file) {
        log("foundFile:" + file.getAbsolutePath());
        String className = file.getAbsolutePath().substring(baseDir.length() + 1).replace("\\", "/");
        if(className.endsWith(".java")){
            className = className.substring(0, className.lastIndexOf(".java"));        
            List<String> ids = getIDTagValues(file);
            for (String idValue : ids) {
                log("ADD " + className + ", value=" + idValue);
            }
            props.add(className, ids);
        }
    }

    public TestsProperties getFileProperties() {
        return props;
    }

    private void log(String msg) {
        System.out.println(msg);
    }
}
