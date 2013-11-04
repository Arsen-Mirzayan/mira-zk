package com.mira.zk.lists;

import org.zkoss.zk.ui.Component;

import java.util.Map;

/**
 * Field metadata
 */
public interface FieldInfo {
    /**
     * @return caption of the field
     */
    String getCaption();

    /**
     * @return path to the field
     */
    String getPath();

    /**
     * Returns list of available values. Only for fields with comboboxes.
     * @return available values
     */
    Map<Object, String> getValues();

    /**
     * @return is field readonly.
     */
    boolean isReadOnly();

    /**
     * Processes editor before field is assigned to it
     * @param editor editor for the field
     */
    void processEditor(Component editor);
}
