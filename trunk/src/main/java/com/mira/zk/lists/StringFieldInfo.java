package com.mira.zk.lists;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Textbox;

import java.util.Map;

/**
 * Metadata for string field
 */
public class StringFieldInfo extends DefaultFieldInfo {
    protected int rowCount;

    public StringFieldInfo(String path, String caption, int rowCount) {
        super(path, caption);
        this.rowCount = rowCount;
    }

    @Override
    public void processEditor(Component editor) {
        super.processEditor(editor);
        if (editor instanceof Textbox){
            ((Textbox)editor).setRows(rowCount);
        }
    }
}
