package com.mira.zk.binding;

import com.mira.zk.ZkComponents;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.*;

/**
 * Класс для настройки биндинга между объектом строки некоторого списка {@link Listbox}
 * и компонентом для редактирования свойства этого элемента, находящегося в одной
 * из ячеек этой строки.
 * При создании биндер добавляет слушателя на события {@code Events.ON_CHANGE} переданного
 * контрола. При изменении значения в контроле, слушатель проходит по родителям
 * этого контрола до тех пока не найдёт компонент {@link Listitem}, который отвечает
 * за строку списка, и получает объект через метод {@code getValue}. Именно у этого
 * объекта и будет меняться связанное свойство. <br>
 * Таким образом для корректной работы биндера должны быть выполнены следующие
 * требования:
 * <ul>
 * <li> Контрол редактирования должен лежать внутри строки списка.
 * <li> Объект, свойство которого надо будет менять, должен быть привязан к компоненту
 * {@link Listitem} через метод {@code setValue}.
 * <li> Контрол редактирования должен иметь соответствующий связанному свойству тип.
 * </ul>
 *
 * @author Мирзаян Арсен Валерьевич
 */
public class InlineEditorBinder extends AbstractBinder {

    protected Component component;

    private AbstactEditorListener getEventListener(Class<? extends Component> cl) {
        if (cl.isAssignableFrom(Datebox.class)) {
            return new DateboxListener();
        } else if (cl.isAssignableFrom(Textbox.class)) {
            return new TextboxListener();
        } else if (cl.isAssignableFrom(Doublebox.class)) {
            return new DoubleboxListener();
        } else if (cl.isAssignableFrom(Decimalbox.class)) {
            return new DecimalboxListener();
        } else if (cl.isAssignableFrom(Intbox.class)) {
            return new IntboxListener();
        } else if (cl.isAssignableFrom(Checkbox.class)) {
            return new CheckboxListener();
        } else if (cl.isAssignableFrom(Listbox.class)) {
            return new ListboxListener();
        }

        return null;
    }

    protected String getEvent(Class<? extends Component> cl) {
        if (cl.isAssignableFrom(Checkbox.class)) {
            return Events.ON_CHECK;
        } else if (cl.isAssignableFrom(Listbox.class)) {
            return Events.ON_SELECT;
        }
        return Events.ON_CHANGE;
    }

    /**
     * Создаёт экземпляр биндера и насранивает компонент ввода.
     *
     * @param component    компонент ввода, отвечающий за свойство
     * @param propertyName пусть к свойству объекта.
     */
    public InlineEditorBinder(Component component, String propertyName) {
        super(propertyName);
        this.component = component;
        AbstactEditorListener listener = getEventListener(component.getClass());
        if (listener == null) {
            throw new BindingException(String.format("Component %s is not supported by binder %s", component.getClass().getName(), getClass().getName()));
        }
        String event = getEvent(component.getClass());
        listener.setBinder(this);
        component.addEventListener(event, listener);
    }

    @Override
    protected Object getDestinationObject() {
        Listitem item = ZkComponents.findAncestor(component, Listitem.class);
        return item != null ? item.getValue() : null;
    }

    /**
     * Абстрактный слушатель. Получает событие и передаёт его абстрактному методу
     * {@link #getValue(org.zkoss.zk.ui.event.Event) }, чтобы получить из события
     * объект. Далее полученный объект передаётся биндеру, чтобы тот проставил
     * его свойству целевого объекта.
     */
    private abstract class AbstactEditorListener implements EventListener {

        private InlineEditorBinder binder;

        public AbstactEditorListener() {
        }

        public void setBinder(InlineEditorBinder binder) {
            this.binder = binder;
        }

        protected abstract Object getValue(Event event);

        public void onEvent(Event event) throws java.lang.Exception {
            binder.setValue(getValue(event));
        }
    }

    private class IntboxListener extends AbstactEditorListener {

        public IntboxListener() {
        }

        @Override
        protected Object getValue(Event event) {
            return ((Intbox) event.getTarget()).getValue();
        }
    }

    private class TextboxListener extends AbstactEditorListener {

        public TextboxListener() {
        }

        @Override
        protected Object getValue(Event event) {
            return ((Textbox) event.getTarget()).getValue();
        }
    }

    private class DoubleboxListener extends AbstactEditorListener {

        public DoubleboxListener() {
        }

        @Override
        protected Object getValue(Event event) {
            return ((Doublebox) event.getTarget()).getValue();
        }
    }

    private class DecimalboxListener extends AbstactEditorListener {

        public DecimalboxListener() {
        }

        @Override
        protected Object getValue(Event event) {
            return ((Decimalbox) event.getTarget()).getValue();
        }
    }

    private class DateboxListener extends AbstactEditorListener {

        public DateboxListener() {
        }

        @Override
        protected Object getValue(Event event) {
            return ((Datebox) event.getTarget()).getValue();
        }
    }

    private class CheckboxListener extends AbstactEditorListener {

        public CheckboxListener() {
        }

        @Override
        protected Object getValue(Event event) {
            return ((CheckEvent) event).isChecked();
        }
    }

    private class ListboxListener extends AbstactEditorListener {

        public ListboxListener() {
        }

        @Override
        protected Object getValue(Event event) {
            Listitem item = ((Listbox) event.getTarget()).getSelectedItem();
            return item == null ? null : item.getValue();
        }
    }
}
