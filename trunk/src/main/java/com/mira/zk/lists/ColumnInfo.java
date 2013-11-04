package com.mira.zk.lists;

/**
 * Column metadata
 */
public interface ColumnInfo {
    /**
     * @return caption of column
     */
    String getCaption();

    /**
     * @return with of column in px or %
     */
    String getWidth();

    /**
     * @return field name
     */
    String getPath();
}
