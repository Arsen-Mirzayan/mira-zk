package com.mira.zk;

import com.mira.utils.StringUtils;
import com.mira.zk.binding.InlineEditorBinder;
import org.springframework.util.comparator.NullSafeComparator;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.*;
import org.zkoss.zul.impl.InputElement;

import java.math.BigDecimal;
import java.util.*;

import static com.mira.utils.ClassUtils.convert;

/**
 * Useful methods for working with ZK components
 *
 * @author Мирзаян Арсен Валерьевич <arsen.mirzayan@gmail.com>>
 */
public class ZkComponents {

    /**
     * Finds closes ancestor of matching class or <code>null</code>, non is found.
     *
     * @param source        source object.
     * @param ancestorClass class of ancestor
     * @return founded ancestor or {@code null}
     */
    public static <T> T findAncestor(Component source, Class<T> ancestorClass) {
        Component result = null;
        while (result == null && source != null) {
            if (ancestorClass.isInstance(source)) {
                result = source;
            } else {
                source = source.getParent();
            }
        }
        return (T) result;
    }

    private final static Map<Class, Class<? extends Component>> EDITORS;

    static {
        EDITORS = new HashMap<Class, Class<? extends Component>>();
        EDITORS.put(String.class, Textbox.class);
        EDITORS.put(Date.class, Datebox.class);
        EDITORS.put(Boolean.class, Checkbox.class);
        EDITORS.put(Boolean.TYPE, Checkbox.class);
        EDITORS.put(BigDecimal.class, Decimalbox.class);
        EDITORS.put(Double.class, Doublebox.class);
        EDITORS.put(java.lang.Integer.class, Intbox.class);
        EDITORS.put(Integer.TYPE, Intbox.class);
        EDITORS.put(Long.class, Longbox.class);
        EDITORS.put(Long.TYPE, Longbox.class);
    }

    /**
     * Reads value from editor
     *
     * @param editor editor
     * @return editor's value
     */
    public static Object getValueFromEditor(Component editor) {
        Object value;
        if (editor instanceof Combobox) {
            Combobox combobox = (Combobox) editor;
            int selectedIndex = combobox.getSelectedIndex();
            value = selectedIndex >= 0 ? combobox.getModel().getElementAt(selectedIndex) : null;
        } else if (editor instanceof InputElement) {
            value = ((InputElement) editor).getRawValue();
        } else if (editor instanceof Checkbox) {
            value = ((Checkbox) editor).isChecked();
        } else if (editor instanceof Listbox) {
            Listbox listbox = (Listbox) editor;
            if (listbox.getSelectedIndex() == -1) {
                value = null;
            } else {
                value = listbox.getModel().getElementAt(listbox.getSelectedIndex());
            }
        } else if (editor instanceof Label) {
            value = ((Label) editor).getValue();
        } else if (editor instanceof Radiogroup) {
            value = getValueFromRadiogroup((Radiogroup) editor);
        } else {
            throw new IllegalArgumentException(String.format("Unregistered type of editor %s", editor.getClass().getName()));
        }
        return value;
    }

    /**
     * Читает выбранный элемент из модели радиогруппы
     *
     * @param editor радиогруппа
     * @return выбранный элемент можели или {@code null}
     */
    public static Object getValueFromRadiogroup(Radiogroup editor) {
        int selectedIndex = editor.getSelectedIndex();
        if (selectedIndex == -1) {
            return null;
        } else {
            return editor.getModel().getElementAt(selectedIndex);
        }
    }

    /**
     * Selects specified value in listbox. If value is not found then throws {@link IllegalArgumentException}
     *
     * @param editor     listbox
     * @param value      value
     * @param comparator comparator for searching value in list model
     * @throws IllegalArgumentException if value is not found
     */
    public static void setValueToListbox(Listbox editor, Object value, Comparator comparator) throws IllegalArgumentException {
        if (!setValueToListboxSilent(editor, value, comparator)) {
            throw new IllegalArgumentException(String.format("Value %s is not found", value));
        }
    }

    /**
     * Selects specified value in listbox. If value is not found then returns false else returns true
     *
     * @param editor     listbox
     * @param value      value
     * @param comparator comparator for searching value in list model
     * @throws IllegalArgumentException if value is not found
     */
    public static boolean setValueToListboxSilent(Listbox editor, Object value, Comparator comparator) throws IllegalArgumentException {
        //finding index of selected value
        if (comparator == null) {
            comparator = new NullSafeComparator(new Comparator() {

                public int compare(Object o1, Object o2) {
                    return o1.equals(o2) ? 0 : 1;
                }
            }, true);
        } else if (!(comparator instanceof NullSafeComparator)) {
            comparator = new NullSafeComparator(comparator, true);
        }
        ListModel model = ((Listbox) editor).getListModel();
        int index = -1;
        for (int i = 0, max = model.getSize(); i < max; i++) {
            Object listValue = model.getElementAt(i);
            if (comparator.compare(value, listValue) == 0) {
                index = i;
                break;
            }
        }
        //Setting value
        if (index >= 0) {
            editor.setSelectedIndex(index);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Setts value to editor
     *
     * @param editor editor
     * @param value  value
     * @throws IllegalArgumentException unknown type of editors
     */
    public static void setValueToEditor(Component editor, Object value) throws IllegalArgumentException {
        if (editor instanceof Datebox) {
            ((Datebox) editor).setValue(convert(Date.class, value));
        } else if (editor instanceof Textbox) {
            ((Textbox) editor).setValue(convert(String.class, value));
        } else if (editor instanceof Checkbox) {
            ((Checkbox) editor).setChecked(convert(Boolean.TYPE, value));
        } else if (editor instanceof Decimalbox) {
            ((Decimalbox) editor).setValue(convert(BigDecimal.class, value));
        } else if (editor instanceof Doublebox) {
            ((Doublebox) editor).setValue(convert(Double.class, value));
        } else if (editor instanceof Intbox) {
            ((Intbox) editor).setValue(convert(Integer.class, value));
        } else if (editor instanceof Longbox) {
            ((Longbox) editor).setValue(convert(Long.class, value));
        } else if (editor instanceof Label) {
            ((Label) editor).setValue(convert(String.class, value));
        } else {
            throw new IllegalArgumentException(String.format("Unregistered class of editor %s", editor.getClass().getName()));
        }
    }


    /**
     * Creates default editor for class
     *
     * @param cl           class
     * @param initialValue initial value. Can be {@code null}
     * @return editor
     */
    public static <T> Component createEditor(Class<T> cl, T initialValue) {
        Component result;
        try {
            result = EDITORS.get(cl).newInstance();
        } catch (Exception ex) {
            throw new IllegalArgumentException(String.format("Can't create editor for class %s", cl.getName()));
        }
        result.applyProperties();
        if (result instanceof Decimalbox) {
            ((Decimalbox) result).setFormat("##0.00");
        } else if (result instanceof Doublebox) {
            ((Doublebox) result).setFormat("##0.00");
        }

        //setting initial value
        if (initialValue != null) {
            setValueToEditor(result, initialValue);
        }

        return result;
    }

    /**
     * Создаёт и настраивает компонент для редактирования объекта указанного класса.
     * Компонент настраивается так, как будто он должен лежать внутри ячейки списка
     * или таблицы.<p>
     * Настроенный компонент для редактирования помещается на родительский компонент,
     * но не непосредственно, а через промежуточный элемент, который нужен для
     * форматирования.<p>
     *
     * @param parent       родительский компонент.
     * @param cl           класс объектов для редактирования.
     * @param initialValue начальное значение редактора. Может быть {@code null}.
     * @return компонент редактор
     */
    public static <T> Component createInlineEditor(Component parent, Class<T> cl, T initialValue) {
        return createInlineEditor(parent, cl, initialValue, null);
    }

    /**
     * Создаёт и настраивает компонент для редактирования пароля.
     * Компонент настраивается так, как будто он должен лежать внутри ячейки списка
     * или таблицы.<p>
     * Настроенный компонент для редактирования помещается на родительский компонент,
     * но не непосредственно, а через промежуточный элемент, который нужен для
     * форматирования.<p>
     *
     * @param parent       родительский компонент.
     * @param initialValue начальное значение редактора. Может быть {@code null}.
     * @return компонент редактор
     */
    public static Component createInlinePasswordEditor(Component parent, String initialValue) {
        Textbox result = (Textbox) createInlineEditor(parent, String.class, initialValue, null);
        result.setType("password");
        return result;
    }


    /**
     * Создаёт и настраивает компонент для редактирования объекта указанного класса.
     * Компонент настраивается так, как будто он должен лежать внутри ячейки списка
     * или таблицы.<p>
     * Настроенный компонент для редактирования помещается на родительский компонент,
     * но не непосредственно, а через промежуточный элемент, который нужен для
     * форматирования.<p>
     * Если указан параметр {@code propertyName}, то автоматически настраивается
     * биндинг между значением компонента и указанным свойством объекта. Объектом,
     * свойство которого будет меняться в процессе биндинга является свойство
     * {@code value} родительского {@link org.zkoss.zul.Listitem}, на который будет помещён этот
     * компонент.
     *
     * @param parent       родительский компонент.
     * @param cl           класс объектов для редактирования.
     * @param initialValue начальное значение редактора. Может быть {@code null}.
     * @param propertyName свойство редактируемого объекта, которое будет связано со списком. Может быть {@code null}.
     * @return
     */
    public static <T> Component createInlineEditor(Component parent, Class<T> cl, T initialValue, String propertyName) {
        Component editor = createEditor(cl, initialValue);

        //Setting binding
        if (!StringUtils.isEmpty(propertyName)) {
            new InlineEditorBinder(editor, propertyName);
        }

        //Setting width for inline editor
        if (editor instanceof Datebox) {
            ((Datebox) editor).setWidth("90px");
        } else if (editor instanceof HtmlBasedComponent) {
            ((HtmlBasedComponent) editor).setHflex("1");
        }

        if (parent != null) {
            Div div = new Div();
            div.setSclass("inlineEditorHolder");
            div.appendChild(editor);
            parent.appendChild(div);
        }
        return editor;
    }

    /**
     * Создаёт и настраивает кнопку удаления строки в списке. Обычно эта кнопка
     * находится в последней ячейке строки. Созданая кнопка добавляется на родительский
     * компонент. <p>
     * Если не указан слушатель события нажатия на кнопку, то будет установлен слушатель
     * по умолчанию. В этом случае при нажатии на кнопку будет найден родительский компонент
     * {@link org.zkoss.zul.Listitem}. Свойство {@code value}, этого компонента и есть искомый элемент модели,
     * который надо удалить. Будет найден родительский компонент {@link Listbox} и если
     * его модель поддерживает удаление элемента, то найденный ранее элемент будет удалён
     * ихз модели. Чтобы модель поддерживала удаление, надо, чтобы она реализовывала интерфейс
     * {@link java.util.Collection}, как например {@link org.zkoss.zul.ListModelList}.<p>
     * Рекомендуемая ширина ячейки под кнопку 40px.
     *
     * @param parent        родительский компонент.
     * @param clickListener слушатель события {@code Events.ON_CLICK}
     * @return компонент редактор
     * @see org.zkoss.zul.ListModelList
     * @see java.util.Collection
     */
    public static Button createInlineDeleteButton(Component parent, EventListener clickListener) {
        Button deleteButton = new Button("", "/images/delete 16.png");
        //Слушатель
        if (clickListener != null) {
            deleteButton.addEventListener(Events.ON_CLICK, clickListener);
        } else {
            deleteButton.addEventListener(Events.ON_CLICK, new EventListener() {

                public void onEvent(Event event) throws Exception {
                    Listitem listitem = findAncestor(event.getTarget(), Listitem.class);
                    if (listitem != null && listitem.getValue() != null) {
                        Object value = listitem.getValue();
                        Listbox listbox = findAncestor(listitem, Listbox.class);
                        if (listbox != null && listbox.getModel() != null) {
                            ListModel model = listbox.getModel();
                            if (model instanceof Collection) {
                                ((Collection) model).remove(value);
                            }
                        }
                    }
                }
            });
        }

        //Родитель
        if (parent != null) {
            parent.appendChild(deleteButton);
        }
        return deleteButton;
    }

    /**
     * Создаёт и настраивает кнопку удаления строки в списке. На кнопку вешается
     * слушатель по умолчанию. Детально читайте здесь {@link #createInlineDeleteButton(org.zkoss.zk.ui.Component, org.zkoss.zk.ui.event.EventListener) }
     *
     * @param parent родительский компонент.
     * @return
     */
    public static Button createInlineDeleteButton(Component parent) {
        return createInlineDeleteButton(parent, null);
    }

    /**
     * Создаёт выпадающий список выбора значений. Компонент настраивается так,
     * чтобы он отрисовывался в ячейке списка. <p>
     * Настроенный компонент для редактирования помещается на родительский компонент,
     * но не непосредственно, а через промежуточный элемент, который нужен для
     * форматирования.<p>
     *
     * @param parent       родитель.
     * @param model        модель списка.
     * @param renderer     реденерер списка. Если указан {@code null}, то будет использоваться
     *                     рендерер по умолчанию.
     * @param initialValue начальное значение.
     * @return
     */
    public static Listbox createInlineListbox(Component parent, ListModel model, ListitemRenderer renderer,
                                              Object initialValue) {
        return createInlineListbox(parent, model, renderer, initialValue, null);
    }

    /**
     * Создаёт выпадающий список выбора значений. Компонент настраивается так,
     * чтобы он отрисовывался в ячейке списка. <p>
     * Настроенный компонент для редактирования помещается на родительский компонент,
     * но не непосредственно, а через промежуточный элемент, который нужен для
     * форматирования.<p>
     * Если указан параметр {@code propertyName}, то автоматически настраивается
     * биндинг между значением компонента и указанным свойством объекта. Объектом,
     * свойство которого будет меняться в процессе биндинга является свойство
     * {@code value} родительского {@link Listitem}, на который будет помещён этот
     * компонент.
     *
     * @param parent       родитель.
     * @param model        модель списка.
     * @param renderer     реденерер списка. Если указан {@code null}, то будет использоваться
     *                     рендерер по умолчанию.
     * @param initialValue начальное значение.
     * @param propertyName свойство редактируемого объекта, которое будет связано со списком. Может быть {@code null}.
     * @return
     */
    public static Listbox createInlineListbox(Component parent, ListModel model, ListitemRenderer renderer,
                                              Object initialValue, String propertyName) {
        Listbox listbox = new Listbox();
        listbox.setMold("select");
        listbox.setWidth("100%");
        if (renderer != null) {
            listbox.setItemRenderer(renderer);
        }
        listbox.setModel(model);

        if (!StringUtils.isEmpty(propertyName)) {
            new InlineEditorBinder(listbox, propertyName);
        }

        if (initialValue != null) {
            int selectedIndex = -1;
            for (int i = 0, max = model.getSize(); i < max; i++) {
                if (initialValue.equals(model.getElementAt(i))) {
                    selectedIndex = i;
                    break;
                }
            }
            listbox.setSelectedIndex(selectedIndex);
        }

        if (parent != null) {
            Div div = new Div();
            div.setSclass("inlineEditHolder");
            div.appendChild(listbox);
            parent.appendChild(div);
        }
        return listbox;
    }

    /**
     * Ищет в модели списка элемент, равный указанному по указанному компаратору.
     * Используется для синхронизации внешнего объекта с данными модели, потому что
     * зачастую бывает так, что внешний объект равен логически некоторому объекту
     * модели, однако метод {@code equals} возвращает {@code false}. Например, когда
     * 2 объекта, представляющие записи в БД, имеют одинаковый id.
     *
     * @param model       модель
     * @param searchValue объект равный которому будем искать
     * @param comparator  компаратор, который определит равенство объектов.
     * @return искомый элемент в модели, либо {@code null}
     */
    public static Object findInModel(ListModel model, Object searchValue, Comparator comparator) {
        for (int i = 0, max = model.getSize(); i < max; i++) {
            if (comparator.compare(searchValue, model.getElementAt(i)) == 0) {
                return model.getElementAt(i);
            }
        }
        return null;
    }

    /**
     * Создаёт таблицу для контролов с указанным числом пар колонк: для меток и для контролов.
     *
     * @param count количество пар колонок.
     * @return таблицу для контролов
     */
    public static Grid createControlGrid(int count) {
        Grid grid = new Grid();
        Columns columns = new Columns();
        for (int i = 0; i < count; i++) {
            Column column = new Column();
            column.setHflex("min");
            columns.appendChild(column);
            column = new Column();
            column.setHflex("1");
            columns.appendChild(column);
        }
        grid.appendChild(columns);
        return grid;
    }

    /**
     * Создаёт таблицу для контролов с двумя колонками: для меток и для контролов.
     *
     * @return таблицу для контролов
     */
    public static Grid createControlGrid() {
        return createControlGrid(1);
    }


}
