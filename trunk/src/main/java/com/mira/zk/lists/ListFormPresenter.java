package com.mira.zk.lists;

/**
 * Interface of presenter for list form
 */
public interface ListFormPresenter<T> {
    /**
     * Setts view for the presenter. This method should be called previous to all other methods.
     * @param view view
     */
    void setView(ListFormView<T> view);

    /**
     * @return class of editing object
     */
    Class<? extends T> getObjectClass();

    /**
     * Loads list of objects to the view.
     */
    void loadObjects();

    /**
     * Adds new object from the view.
     */
    void add();

    /**
     * Deletes object selected on the view.
     */
    void delete();

    /**
     * Saves changes made to selected on the view object.
     */
    void edit();
}
