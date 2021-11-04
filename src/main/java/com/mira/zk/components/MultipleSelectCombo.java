package com.mira.zk.components;

import com.mira.utils.StringUtils;
import org.zkoss.zk.ui.annotation.ComponentAnnotation;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Аналогичен обычному combobox, но позволяет выбирать несколько значений.
 */
public class MultipleSelectCombo<T> extends Bandbox {
  public static final String SELECT_EVENT = Events.ON_SELECT;
  private final Listbox listbox;
  private Function<T, String> converter;
  private boolean selectAllIfEmpty;
  private final Button selectAllButton;
  private final Button removeAllButton;
  private Set<T> selected;

  private String emptyTitle;

  /**
   * @return текст при пустом контроле
   */
  public String getEmptyTitle() {
    return emptyTitle;
  }

  /**
   * Устанавливает текст для контрола, когда не выбрано ни одного значения
   *
   * @param emptyTitle текст для пустого контрола
   * @return себя же последовательного вызова
   */
  public MultipleSelectCombo<T> setEmptyTitle(String emptyTitle) {
    this.emptyTitle = emptyTitle;
    return this;
  }

  /**
   * Создание компонента с пустой моделью и конвертором по умолчанию
   */
  public MultipleSelectCombo() {
    this(Collections.emptyList(), Objects::toString);
  }

  /**
   * Строит компонент
   *
   * @param availableObjects список доступных для выбора объектов
   * @param converter        конвертер объекта модели в строку
   */
  public MultipleSelectCombo(List<T> availableObjects, Function<T, String> converter) {
    this.converter = converter;
    setReadonly(true);
    Bandpopup bandpopup = new Bandpopup();
    appendChild(bandpopup);

    selectAllButton = new Button("Выбрать все");
    bandpopup.appendChild(selectAllButton);
    selectAllButton.addEventListener(Events.ON_CLICK, event -> selectAll(true));

    removeAllButton = new Button("Убрать все");
    bandpopup.appendChild(removeAllButton);
    removeAllButton.addEventListener(Events.ON_CLICK, event -> selectAll(false));

    listbox = new Listbox();
    listbox.setMultiple(true);
    listbox.setMold("paging");
    listbox.setPageSize(10);
    listbox.setItemRenderer((ListitemRenderer<T>) (item, data, index) -> {
      item.appendChild(new Listcell(MultipleSelectCombo.this.converter.apply(data)));
      item.setValue(data);
    });
    ListModelList<T> model = new ListModelList<T>(availableObjects);
    model.setMultiple(true);
    listbox.setModel(model);
    listbox.addEventListener(Events.ON_SELECT, event -> onSelect());
    bandpopup.appendChild(listbox);

    refreshTitle();
  }

  /**
   * @return модель вложенного listbox
   */
  private ListModelList<T> getModel() {
    return (ListModelList<T>) listbox.<T>getModel();
  }

  /**
   * Выбрать все значения, либо снять выбор со всех.
   *
   * @param select {@code true} - выбрать все, иначе - снять
   */
  public void selectAll(boolean select) {
    ListModelList<T> model = getModel();
    model.setSelection(select ? model.getInnerList() : Collections.emptyList());
    onSelect();
  }

  /**
   * Обновляет заголовок и отправляет сообщение об изменении списка выбранных элементов
   */
  private void onSelect() {
    refreshTitle();
    Events.postEvent(SELECT_EVENT, this, getSelectedObjects());
  }

  /**
   * Если это свойство {@code true} то в случае пустого списка (не выбрано ни одно значение) выбираются все
   * доступные значения.
   *
   * @return выбирать ли все значения для пустого списка.
   */
  public boolean isSelectAllIfEmpty() {
    return selectAllIfEmpty;
  }

  /**
   * Определяет возвращать ли все значения, если не выбрано ни одного
   *
   * @param selectAllIfEmpty возвращать ли все значения
   * @return себя же
   */
  public MultipleSelectCombo<T> setSelectAllIfEmpty(boolean selectAllIfEmpty) {
    this.selectAllIfEmpty = selectAllIfEmpty;
    refreshTitle();
    return this;
  }

  /**
   * Устанавливает список доступных для выбора объектов. При этом сбрасывается текущий выбор элементов.
   *
   * @param availableObjects список доступных объектов
   * @return себя же для последовательного вызова
   */
  public MultipleSelectCombo<T> setAvailableObjects(List<T> availableObjects) {
    ListModelList<T> model = new ListModelList<>(availableObjects);
    model.setMultiple(true);
    setSelectedObjects(selected != null ? selected : Collections.emptySet());
    listbox.setModel(model);
    return this;
  }


  /**
   * Ставит галочки напротив выбранных объектов. Выбор со всех остальных объектов будет снят. Если модель ещё не инициализирована,
   * то выбор будет вызван после инициализации модели. Если модель инициализирована, то выбраны будут только те элементы, которые
   * есть в модели.
   *
   * @param selected множество объектов, которые нужно выбрать
   * @return себя же для последовательного вызова
   */
  public MultipleSelectCombo<T> setSelectedObjects(Set<T> selected) {
    ListModelList<T> model = getModel();
    if (model.isEmpty()) {   //Если модель пока ещё не инициализирована, то сохраним выбор для последующей инициализации
      this.selected = selected;
    } else { //Иначе выберем в модели указанные элементы
      this.selected = null;
      model.setSelection(model.getInnerList().stream().filter(selected::contains).collect(Collectors.toSet()));
    }
    refreshTitle();
    return this;
  }

  /**
   * @return возвращает список всех выбранных объектов модели
   */
  @ComponentAnnotation("@ZKBIND(ACCESS=both, SAVE_EVENT=" + SELECT_EVENT + ")")
  public Set<T> getSelectedObjects() {
    ListModelList<T> model = getModel();
    Set<T> selection = model.getSelection();
    if (selection.isEmpty() && selectAllIfEmpty) {
      selection.addAll(model.getInnerList());
    }
    return selection;
  }

  /**
   * Обновляет заголовок
   */
  private void refreshTitle() {
    ListModelList<T> model = getModel();
    Set<T> selection = model.getSelection();
    //Найдём список выбранных элементов в том порядке, в котороым они указаны в модели
    String title = model.getInnerList().stream().filter(selection::contains).map(converter).collect(Collectors.joining(","));
    //Если получился пустой текст, то выведем указанный текст для пустого выбора
    if (StringUtils.isEmpty(title)) {
      title = StringUtils.isNotEmpty(emptyTitle) ? emptyTitle : (selectAllIfEmpty ? "Все" : "");
    }
    setValue(title);
  }

  /**
   * Устанавливает класс для кнопки выбора всех элементов
   *
   * @param selectAllButtonClass класс
   * @return себя же для последовательного вызова
   */
  public MultipleSelectCombo<T> setSelectAllButtonClass(String selectAllButtonClass) {
    this.selectAllButton.setSclass(selectAllButtonClass);
    return this;
  }

  /**
   * Устанавливает класс для кнопки снятия всех выделений
   *
   * @param removeAllButtonClass класс
   * @return себя же для последовательного вызова
   */
  public MultipleSelectCombo<T> setRemoveAllButtonClass(String removeAllButtonClass) {
    this.removeAllButton.setSclass(removeAllButtonClass);
    return this;
  }

  /**
   * Устанавливает ширину списка
   *
   * @param width ширина
   * @return себя же для последовательного вызова
   */
  public MultipleSelectCombo<T> setInnerListboxWidth(String width) {
    this.listbox.setWidth(width);
    return this;
  }

  /**
   * Устанавливает высоту списка
   *
   * @param height высота
   * @return себя же для последовательного вызова
   */
  public MultipleSelectCombo<T> setInnerListboxHeight(String height) {
    this.listbox.setHeight(height);
    return this;
  }

  /**
   * @return функция перевода объекта в текст для пользователя
   */
  public Function<T, String> getConverter() {
    return converter;
  }

  /**
   * Устанавливает функцию перевода объекта в строке для пользователя
   *
   * @param converter функция
   * @return себя же последовательного вызова
   */
  public MultipleSelectCombo<T> setConverter(Function<T, String> converter) {
    this.converter = converter;
    listbox.setModel(listbox.getModel());
    refreshTitle();
    return this;
  }

  /**
   * @return размер страницы внутреннего списка см {@link Listbox::getPageSize}
   */
  public int getInnerListboxPageSize() {
    return listbox.getPageSize();
  }

  /**
   * Устанавливает размер страницы для внутреннего списка. Смотри {@link Listbox::setPageSize}
   *
   * @param pageSize размер страницы
   * @return себя же для последовательного вызова
   */
  public MultipleSelectCombo<T> setInnerListboxPageSize(int pageSize) {
    listbox.setPageSize(pageSize);
    return this;
  }
}
