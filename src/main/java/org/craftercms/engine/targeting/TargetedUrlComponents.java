package org.craftercms.engine.targeting;

/**
 * Created by alfonsovasquez on 13/8/15.
 */
public class TargetedUrlComponents {

    private String prefix;
    private String targetId;
    private String suffix;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

}
