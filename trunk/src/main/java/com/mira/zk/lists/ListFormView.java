package com.mira.zk.lists;

import java.util.Collection;

/**
 * View for list of objects
 * @param <T> object class
 */
public interface ListFormView<T> {
    /**
     * Full refresh data on form
     */
    void refresh();

    /**
     * Sets list of objects. And selects first one.
     * @param objects list of objects.
     */
    void setObjects(Collection<T> objects);

    /**
     * Adds object to the list
     * @param object new object
     */
    void addObject(T object);

    /**
     * Refresh object in list
     * @param object new object
     */
    void refreshObject(T object);

    /**
     * Removes object from the list
     * @param object object to remove
     */
    void removeObject(T object);

    /**
     * @return selected object
     */
    T getSelectedObject();

    /**
     * Selects specified object in list
     * @param object object to selected
     * @return index of selected object
     */
    int setSelectedObject(T object);
}
