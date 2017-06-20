/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
 */
package sqe.antext;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.MacroDef;
import org.apache.tools.ant.taskdefs.MacroInstance;

public class For extends Task {
    private int begin = -1, end = -1, step = -1;
    private String list, param;
    private MacroDef macroDef;

    public void setList(String list) {
        this.list = list;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public void setBegin(String begin) {
        this.begin = Integer.parseInt(begin);
    }

    public void setEnd(String end) {
        this.end = Integer.parseInt(end);
    }

    public void setStep(String step) {
        this.step = Integer.parseInt(step);
    }

    public Object createSequential() {
        macroDef = new MacroDef();
        macroDef.setProject(getProject());
        return macroDef.createSequential();
    }

    public void execute() {
        if (list != null || (step > 0 ? (end > begin && begin > 0) : (begin > end && end > 0))) {
            if (macroDef.getAttributes().isEmpty()) {
                MacroDef.Attribute attribute = new MacroDef.Attribute();
                attribute.setName(param);
                macroDef.addConfiguredAttribute(attribute);
            }
            if (list != null) {
                for (String next : list.split("\\p{Space}*,\\p{Space}*")) {
                    if (next.length() > 0) {
                        execute(next);
                    }
                }
            }
            if (begin > 0 && end > 0) {
                while (step > 0 ? begin <= end : begin > end) {
                    execute(String.valueOf(begin));
                    begin += step;
                }
            }
        } else {
            throw new BuildException("Wrong <for> params: list=\"" + list + "\", begin=" + begin + ", end=" + end + ", step=" + step);
        }
    }

    private void execute(String str) {
        MacroInstance instance = new MacroInstance();
        instance.setProject(getProject());
        instance.setOwningTarget(getOwningTarget());
        instance.setMacroDef(macroDef);
        instance.setDynamicAttribute(param.toLowerCase(), str);
        instance.execute();
    }
}
