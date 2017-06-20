/*
 *
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
 *
 */

package sqe.antext;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestsProperties  {

    private Map<String,  List<String>> testCases = new HashMap<String,  List<String>>();

    public Collection<String> propertyNames() {
        return   testCases.keySet();
    }

    public List<String> getProperty(String className) {
        return testCases.get(className);
    }

    public void add(String className, List<String> ids) {
        testCases.put(className, ids);         
    }

}
