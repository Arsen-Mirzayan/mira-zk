package com.mira.zk.binding;

import com.mira.utils.ClassUtils;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * Абстрактный класс-родитель для всех классов биндеров. Содержит в себе функционал
 * для работы с подписчиками, а также основные методы по установки значение в свойство
 * целевого объекта.
 *
 * @author Мирзаян Арсен Валерьевич
 */
public abstract class AbstractBinder {

    protected static final String ERROR = "Исключение при связывании поля %s объекта класса %s биндером класса %s";
    protected List<BindingListener> bindingListeners = new LinkedList<BindingListener>();
    protected String propertyName;
    protected String pathToSetter;

    protected AbstractBinder(String propertyName) {
        this.propertyName = propertyName;
        int index = propertyName.lastIndexOf(".");
        pathToSetter = index < 0 ? "" : propertyName.substring(0, index);
    }

    /**
     * Возвращает объект, свойство которого надо установить.
     *
     * @return целевой объект. Может быть {@code null}.
     */
    protected abstract Object getDestinationObject();

    /**
     * Метод проставляет значение в свойство целевого объекта и оповещает слушаетелей
     * о том, что свойство целевого объекта было изменено. Если свойство составное,
     * то метод сначала через геттеры получает объект, у которого надо непосредственно
     * проставить свойство. Если какой-нибудь объект в цепочке составного свойства
     * окажется {@code null}, то исключения не возникнет, однако слушатели оповещены
     * не будут.
     *
     * @param value значение целевого свойства. Может быть {@code null}.
     */
    protected void setValue(Object value) {
        Object destinationObject = getDestinationObject();
        if (destinationObject != null) {
            try {
                Object dest = pathToSetter.equals("") ? destinationObject : ClassUtils.getValue(destinationObject, pathToSetter);
                if (dest != null) {
                    int index = propertyName.lastIndexOf(".");
                    String lastPropertyName = index < 0 ? propertyName : propertyName.substring(index + 1);
                    Method setter = ClassUtils.getSetter(dest.getClass(), lastPropertyName);
                    Class valueClass = setter.getParameterTypes()[0];
                    setter.invoke(dest, ClassUtils.convert(valueClass, value));
                    fireBinderEvent();
                }
            } catch (Exception ex) {
                throw new BindingException(String.format(ERROR, propertyName, destinationObject.getClass().getName(), getClass().getName()), ex);
            }
        }
    }

    /**
     * Adds new listener
     *
     * @param listener new listener
     */
    public void addBinderListener(BindingListener listener) {
        this.bindingListeners.add(listener);
    }

    /**
     * Fires binding event
     */
    protected void fireBinderEvent() {
        for (BindingListener listener : bindingListeners) {
            listener.onBindedValueLoad(new BindingEvent(this));
        }
    }
}
