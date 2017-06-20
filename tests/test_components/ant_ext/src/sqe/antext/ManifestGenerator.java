/*
 *
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
 *
 */

package sqe.antext;

import org.apache.tools.ant.BuildException;

public class ManifestGenerator extends JadGenerator {
    
    public void execute() throws BuildException {
        if(propFile == null) {
            throw new BuildException("Need to set property file location");
        }
        
        if(globalPropFile != null) {
            loadGloabalCustomProperties(globalPropFile);
        }
        generateFile(propFile, MANIFEST_FILE);
    }
}
