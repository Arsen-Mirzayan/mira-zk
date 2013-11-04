package com.mira.zk.components;

/**
 * Конвертор, который преобразует объект в строковое представление
 *
 * @author Мирзаян Арсен Валерьевич <arsen.mirzayan@gmail.com>>
 */
public interface ObjectToStringConverter<T> {

    /**
     * Возвращает строковое представление переданного объекта.
     *
     * @param source
     * @return строковое представление объекта
     */
    public String toString(T source);
}
