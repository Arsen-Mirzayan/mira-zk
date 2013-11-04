package com.mira.zk.lists;

/**
 * Default column metadata
 */
public class DefaultColumnInfo implements ColumnInfo {
    private String caption;
    private String width;
    private String path;

    /**
     * Creates default column metadata
     * @param path path to field
     * @param caption caption
     * @param width with in px or %
     */
    public DefaultColumnInfo(String path, String caption, String width) {
        this.caption = caption;
        this.width = width;
        this.path = path;
    }

    @Override
    public String getCaption() {
        return caption;
    }

    @Override
    public String getWidth() {
        return width;
    }

    @Override
    public String getPath() {
        return path;
    }
}
