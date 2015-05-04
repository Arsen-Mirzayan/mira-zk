package com.mira.zk.lists;

import com.mira.utils.ClassUtils;
import com.mira.utils.comparators.MultiPropertyComparator;
import com.mira.zk.ZkComponents;
import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.SortDefinition;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.*;

import java.util.*;
import java.util.logging.Logger;

public class ZkListFormBuilder<T> implements ListFormView<T> {

    private final Logger log = Logger.getLogger(getClass().getName());

    /**
     * Main component. Holder for all other created controls.
     */
    protected HtmlBasedComponent parent;

    /**
     * Presenter for the list form
     */
    protected ListFormPresenter<T> presenter;
    /**
     * Main list of editing objects.
     */
    protected Listbox objectsListbox;

    /**
     * Comparator for sorting objects in main list
     */
    protected Comparator<T> objectComparator;

    /**
     * List of columns for main list.
     */
    protected List<ColumnInfo> columns;

    /**
     * Component holder of controls for editing concrete object.
     */
    protected Component detailHolder;
    /**
     * Selected object
     */
    protected T selected;
    /**
     * Map of fields and controls for editing this fields.
     */
    protected Map<String, Component> propertyEditors = new HashMap<String, Component>();
    /**
     * Add new object button
     */
    protected Button addButton;
    /**
     * Edit selected object button
     */
    protected Button editButton;
    /**
     * Delete selected object button
     */
    protected Button deleteButton;

    /**
     * Fields metadata
     */
    private List<FieldInfo> fields;

    /**
     * Регистрирует свойство редактируемого объекта с привязанным к нему редактором.
     * Все зарегистрированные свойства и редакторы используются в процедурах
     * {@link #detailsToObject(Object)} }
     * и {@link #objectToDetails(Object)} }
     *
     * @param path   путь к свойству
     * @param editor редактор
     * @return прошлый редактор свойства, если таковой есть, иначе {@code null}.
     */
    protected Component registerObjectPropertyEditor(String path, Component editor) {
        return propertyEditors.put(path, editor);
    }

    /**
     * @return Comparator for sorting objects in main list
     */
    public Comparator<T> getObjectComparator() {
        return objectComparator;
    }

    /**
     * Setts comparator for sorting objects in main list
     *
     * @param objectComparator new comparator
     */
    public void setObjectComparator(Comparator<T> objectComparator) {
        this.objectComparator = objectComparator;
    }

    /**
     * @return fields' metadata
     */
    public List<FieldInfo> getFields() {
        return fields;
    }

    /**
     * Setts fields' metadata
     *
     * @param fields new fields' metadata
     */
    public void setFields(List<FieldInfo> fields) {
        this.fields = fields;
    }

    /**
     * @return list form presenter
     */
    public ListFormPresenter<T> getPresenter() {
        return presenter;
    }

    /**
     * Setts list for presenter
     *
     * @param presenter
     */
    public void setPresenter(ListFormPresenter<T> presenter) {
        this.presenter = presenter;
    }

    /**
     * @return main component. Holder for all others.
     */
    public HtmlBasedComponent getParent() {
        return parent;
    }

    /**
     * Setts main component
     *
     * @param parent main component
     */
    public void setParent(HtmlBasedComponent parent) {
        this.parent = parent;
    }

    /**
     * @return main list's header metadata
     */
    public List<ColumnInfo> getColumns() {
        return columns;
    }

    /**
     * Setts main list header metadata.
     *
     * @param columns new metadata
     */
    public void setColumns(List<ColumnInfo> columns) {
        this.columns = columns;
    }

    /**
     * Creates builder
     *
     * @param parent    main component
     * @param presenter list form presenter
     * @param columns   metadata for main list
     * @param fields    metadata for editing properties
     */
    public ZkListFormBuilder(HtmlBasedComponent parent, ListFormPresenter<T> presenter,
                             List<ColumnInfo> columns, List<FieldInfo> fields) {
        this.parent = parent;
        this.presenter = presenter;
        this.fields = fields;
        this.columns = columns;
    }

    /**
     * Initialization of all components on holder
     */
    protected void initGUI() {
        parent.setHeight("100%");
        parent.setWidth("100%");

        Borderlayout mainLayout = new Borderlayout();
        mainLayout.setHeight("100%");
        mainLayout.setWidth("100%");
        parent.appendChild(mainLayout);

        West listHolder = new West();
        listHolder.setSize("30%");
        listHolder.setTitle("Список");
        listHolder.setCollapsible(true);
        listHolder.setSplittable(true);
        mainLayout.appendChild(listHolder);
        initObjectsListbox();
        listHolder.appendChild(objectsListbox);

        Center center = new Center();
        mainLayout.appendChild(center);
        center.appendChild(detailHolder = new Div());
        initDetailsGUI();
        initControlPanel();
    }

    /**
     * Builds list form.
     */
    public void build() {
        if (parent.isVisible()) {
            presenter.setView(this);
            initComparator();
            initGUI();
            refresh();
        }
    }

    /**
     * Initializes comparator if it's not set
     */
    private void initComparator() {
        if (objectComparator == null) {
            SortDefinition[] sortDefinitions = new SortDefinition[columns.size()];
            for (int i = 0; i < columns.size(); i++) {
                sortDefinitions[i] = new MutableSortDefinition(columns.get(i).getPath(), true, true);
            }
            objectComparator = new MultiPropertyComparator<>(sortDefinitions);
        }
    }

    /**
     * Initializes control panel with add, remove, edit buttons.
     */
    protected void initControlPanel() {
        Div div = new Div();
        div.setSclass("buttonGroup");
        addButton = new Button("Добавить", "/images/add 16.png");
        addButton.addEventListener(Events.ON_CLICK, event -> saveNew());
        div.appendChild(addButton);

        editButton = new Button("Изменить", "/images/edit1 16.png");
        editButton.addEventListener(Events.ON_CLICK, event -> save());
        div.appendChild(editButton);

        deleteButton = new Button("Удалить", "/images/delete 16.png");
        deleteButton.addEventListener(Events.ON_CLICK, event -> delete());
        div.appendChild(deleteButton);

        detailHolder.appendChild(div);
    }

    /**
     * Возвращает список метаданных по колонкам, которые будут показываться
     * в основном списке редактируемых объектов.
     * Должен быть определён потоками.
     *
     * @return не может быть {@code null} или пустым.
     */


    /**
     * Инициализация списка редактируемых объектов. Здесь происходит только
     * создание заголовков списка и установка рендерера. Загрузка модели происходит
     * в методе {@link #refresh() } после того, как все компоненты уже инициализированы.
     */
    protected void initObjectsListbox() {
        objectsListbox = new Listbox();
        objectsListbox.setVflex(true);
        objectsListbox.setFixedLayout(true);
        List<ColumnInfo> columns = getColumns();
        Listhead head = new Listhead();
        for (ColumnInfo column : columns) {
            head.appendChild(new Listheader(column.getCaption(), null, column.getWidth()));
        }
        objectsListbox.appendChild(head);

        objectsListbox.setItemRenderer(new ObjectListRenderer(columns));
        objectsListbox.addEventListener(Events.ON_SELECT, event -> {
            Set items = ((SelectEvent) event).getSelectedItems();
            if (items.isEmpty()) {
                setSelectedObject(null);
            } else {
                Listitem item = (Listitem) new ArrayList(items).get(0);
                setSelectedObject((T) item.getValue());
            }
        });
    }

    /**
     * Initialization of editors
     */
    protected void initDetailsGUI() {
        Grid grid = new Grid();
        Rows rows = new Rows();
        for (FieldInfo field : fields) {
            rows.appendChild(createPropertyRow(field));
        }
        grid.appendChild(rows);
        detailHolder.appendChild(grid);
    }

    /**
     * Creates field based on field metadata
     *
     * @param fieldInfo field metadata
     * @return row component
     */
    private Row createPropertyRow(final FieldInfo fieldInfo) {
        if (fieldInfo.getValues() == null) {
            Row row = new Row();
            row.appendChild(new Label(fieldInfo.getCaption()));
            Class cl = ClassUtils.getType(presenter.getObjectClass(), fieldInfo.getPath());
            Component editor = ZkComponents.createInlineEditor(row, cl, null);
            fieldInfo.processEditor(editor);
            registerObjectPropertyEditor(fieldInfo.getPath(), editor);
            return row;
        } else {
            ListModelList model = new ListModelList();
            model.addAll(fieldInfo.getValues().keySet());
            ListitemRenderer renderer = new ListitemRenderer() {
                @Override
                public void render(Listitem item, Object data, int index) throws Exception {
                    item.appendChild(new Listcell(fieldInfo.getValues().get(data)));
                    item.setValue(data);
                }
            };
            return createPropertyRow(fieldInfo.getPath(), fieldInfo.getCaption(), model, renderer);
        }
    }

    /**
     * Создаёт редактор-селектор для указанного свойства объекта в строке грида. Регистрирует
     * свойство и редактор.
     *
     * @param path     путь к редактируемому свойству
     * @param caption  название свойства
     * @param model    модель списка выбора
     * @param renderer рендерер для списка выбора. Может быть {@code null}.
     * @return строку с меткой и редактором.
     */
    protected Row createPropertyRow(String path, String caption, ListModel model,
                                    ListitemRenderer renderer) {
        Row row = new Row();
        row.appendChild(new Label(caption));
        Component editor = ZkComponents.createInlineListbox(row, model, renderer, null);
        registerObjectPropertyEditor(path, editor);
        return row;
    }

    /**
     * Прокручивает список на выбранный элемент
     */
    protected void scrollToSelected() {
        Listitem item = objectsListbox.getSelectedItem();
        if (item != null) {
            Clients.scrollIntoView(item);
        }
    }

    /**
     * Сохраняет выбранный на форме объект как новый.
     */
    protected void saveNew() {
        selected = null;
        presenter.add();
        Messagebox.show(createSaveNewSuccessMessage(getSelectedObject()), "Операция завершена.", Messagebox.OK, Messagebox.INFORMATION);
    }

    /**
     * Сохраняет выбранный на форме объект.
     */
    protected void save() {
        presenter.edit();
        Messagebox.show(createSaveSuccessMessage(getSelectedObject()), "Операция завершена.", Messagebox.OK, Messagebox.INFORMATION);
        scrollToSelected();
    }

    /**
     * Удаляет выбранный на форме объект.
     */
    protected void delete() {
        if (Messagebox.show(createDeleteMessage(getSelectedObject()), "Подтверждение удаления", Messagebox.YES + Messagebox.NO, Messagebox.QUESTION) == Messagebox.YES) {
            presenter.delete();
        }
    }

    @Override
    public int setSelectedObject(T selected) {
        this.selected = selected;
        int index = selected != null ? indexOf(selected) : -1;
        objectsListbox.setSelectedIndex(index);
        editButton.setDisabled(selected == null);
        deleteButton.setDisabled(selected == null);
        objectToDetails(this.selected);
        return index;
    }

    /**
     * Обновляет GUI часть секции детализации на основе указанного объекта.
     *
     * @param source может быть {@code null}
     */
    protected void objectToDetails(T source) {
        for (Map.Entry<String, Component> entry : propertyEditors.entrySet()) {
            Component editor = entry.getValue();
            Object value = source != null ? ClassUtils.getValue(source, entry.getKey()) : null;
            if (editor instanceof Listbox) {
                ZkComponents.setValueToListbox((Listbox) editor, value, null);
            } else {
                ZkComponents.setValueToEditor(editor, value);
            }
        }
    }

    /**
     * Переносит значения выбранные в секции редактирования в сам объект (
     * либо его клон). Если переданный объект {@code null}, то будет возвращён
     * новый объект.
     *
     * @param object шаблон объекта.
     * @return заполненный объект. Не {@code null}.
     */
    protected T detailsToObject(T object) {
        T result = selected != null ? ClassUtils.clone(object) : ClassUtils.newInstance(presenter.getObjectClass());

        //Пройдёмся по всем свойствам и проставим значения.
        for (Map.Entry<String, Component> entry : propertyEditors.entrySet()) {
            String path = entry.getKey();
            Object value = ZkComponents.getValueFromEditor(entry.getValue());
            ClassUtils.setValue(result, path, value);
        }
        return result;
    }

    @Override
    public void refresh() {
        presenter.loadObjects();
    }

    /**
     * @return модель основного списка объектов
     */
    protected ListModelList getObjectListModel() {
        return (ListModelList) objectsListbox.getModel();
    }

    /**
     * Находит индект указанного элемента в основной модели.
     *
     * @param object искомый элемент
     * @return индекс найденного элемента или -1, если ничего не найдено.
     */
    protected int indexOf(T object) {
        ListModelList model = getObjectListModel();
        Comparator comparator = getObjectComparator();
        if (comparator == null) {
            return model.indexOf(object);
        } else {
            int i = 0;
            for (Object el : model) {
                if (comparator.compare(el, object) == 0) {
                    return i;
                }
                i++;
            }
            return -1;
        }
    }

    /**
     * Made some actions with object, defined in descendants.
     *
     * @param object source object
     * @return processed object
     */
    protected T processObject(T object) {
        return object;
    }

    @Override
    public void setObjects(Collection<T> objects) {
        List<T> processedObjects = new LinkedList<T>();
        for (T object : objects) {
            processedObjects.add(processObject(object));
        }
        Collections.sort(processedObjects, objectComparator);
        objectsListbox.setModel(new ListModelList(processedObjects));
        setSelectedObject((T) (processedObjects.size() > 0 ? getObjectListModel().get(0) : null));
    }

    @Override
    public void addObject(T object) {
        object = processObject(object);
        ListModelList model = getObjectListModel();
        model.add(object);
        setSelectedObject(object);
    }

    @Override
    public void refreshObject(T object) {
        object = processObject(object);
        ListModelList model = getObjectListModel();
        int index = indexOf(object);
        if (index >= 0) { //if element is found then
            model.set(index, object);
            setSelectedObject(object);
        } else {//иначе список устарел и надо его обновить полностью.
            refresh();
        }
    }

    @Override
    public void removeObject(T object) {
        object = processObject(object);
        ListModelList model = getObjectListModel();
        int index = indexOf(object);
        if (index >= 0) { //Если нашли объект, который надо удалить, то удалим его.
            model.remove(index);
            if (model.isEmpty()) {
                setSelectedObject(null);
            } else {
                index -= index > 0 ? 1 : 0;
                setSelectedObject((T) model.get(index));
            }
        } else {//иначе список устарел и надо его обновить полностью.
            refresh();
        }
    }

    @Override
    public T getSelectedObject() {
        return detailsToObject(selected);
    }

    /**
     * Создаёт сообщение при успешном сохранении в базу.
     *
     * @param object сохранённый объект.
     * @return сообщение
     */
    protected String createSaveNewSuccessMessage(T object) {
        return "Объект успешно сохранён в базу.";
    }

    /**
     * Создаёт сообщение при успешном сохранении в базу.
     *
     * @param object сохранённый объект.
     * @return сообщение
     */
    protected String createSaveSuccessMessage(T object) {
        return "Объект успешно сохранён в базу.";
    }

    /**
     * Создаёт сообщение для подтверждения удаления объекта.
     *
     * @param object удаляемый объект
     * @return вопрос на подтверждение удаления
     */
    protected String createDeleteMessage(T object) {
        return "Вы действительно хотите удалить объект?";
    }

    /**
     * Рендерер для основного списка объектов.
     */
    private class ObjectListRenderer implements ListitemRenderer {

        private List<ColumnInfo> columns;

        /**
         * Создаёт рендерер с указанным списком колонок.
         *
         * @param columns список колонок. Не может быть {@code null}
         */
        public ObjectListRenderer(List<ColumnInfo> columns) {
            this.columns = columns;
        }

        /**
         * Получает строковое представление переданного значения, основываясь
         * на метаданных колонки.
         *
         * @param columnInfo метаданные колонки
         * @param value      значение
         * @return строковое представление значения.
         */
        private String formatValue(ColumnInfo columnInfo, Object value) {
            return value != null ? value.toString() : "";
        }

        @Override
        public void render(Listitem item, Object data, int index) throws Exception {
            for (ColumnInfo columnInfo : columns) {
                String value = formatValue(columnInfo, ClassUtils.getValue(data, columnInfo.getPath()));
                item.appendChild(new Listcell(value));
            }
            item.setValue(data);
        }
    }
}
