package com.mira.zk.lists;

import org.zkoss.zk.ui.Component;

import java.util.Map;

/**
 * Editing field metadata
 */
public class DefaultFieldInfo implements FieldInfo {
    private String caption;
    private String path;
    private Map<Object, String> values;
    private boolean readOnly;

    public DefaultFieldInfo(String path, String caption) {
        this.caption = caption;
        this.path = path;
    }

    public DefaultFieldInfo(String path, String caption, Map<Object, String> values) {
        this.caption = caption;
        this.path = path;
        this.values = values;
    }

    @Override
    public String getCaption() {
        return caption;
    }

    /**
     * Setts caption for the field
     *
     * @param caption new caption
     */
    public void setCaption(String caption) {
        this.caption = caption;
    }

    @Override
    public String getPath() {
        return path;
    }

    /**
     * Setts path to the field. Path could be composite.
     *
     * @param path new path
     */
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public Map<Object, String> getValues() {
        return values;
    }

    /**
     * Setts map of available values.
     *
     * @param values available values
     */
    public void setValues(Map<Object, String> values) {
        this.values = values;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public void processEditor(Component editor) {

    }

    /**
     * If true, then field is visible, but can't be edited by user.
     *
     * @param readOnly is readonly?
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
}
