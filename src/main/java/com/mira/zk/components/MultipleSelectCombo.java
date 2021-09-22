package com.mira.zk.components;

import com.mira.utils.StringUtils;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * Аналогичен обычному combobox, но позволяет выбирать несколько значений.
 */
public class MultipleSelectCombo<T> extends Bandbox {
  private Listbox listbox;
  private Function<T, String> converter;
  private boolean selectAllIfEmpty;
  private Button selectAllButton;
  private Button removeAllButton;

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
   */
  public void setEmptyTitle(String emptyTitle) {
    this.emptyTitle = emptyTitle;
  }

  /**
   * Строит компонент
   *
   * @param source    модель с данными для выбора
   * @param converter конвертер объекта модели в строку
   */
  public MultipleSelectCombo(List<T> source, Function<T, String> converter) {
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
    ListModelList<T> model = new ListModelList<T>(source);
    model.setMultiple(true);
    listbox.setModel(model);
    listbox.addEventListener(Events.ON_SELECT, event -> refreshTitle());
    bandpopup.appendChild(listbox);

    refreshTitle();
  }

  /**
   * Возвращает список доступных элементов. Список редактируемый и его изменения отражаются на компоненте.
   *
   * @return редактируемый список
   */
  public List<T> getSource() {
    return (List<T>) listbox.<T>getModel();
  }

  /**
   * Выбрать все значения, либо снять выбор со всех.
   *
   * @param select {@code true} - выбрать все, иначе - снять
   */
  public void selectAll(boolean select) {
    for (Object item : listbox.getItems()) {
      ((Listitem) item).setSelected(select);
    }
    refreshTitle();
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

  @SuppressWarnings("unchecked")
  private List<T> getInnerSelectedObjects() {
    List<T> list = new LinkedList<T>();
    for (Listitem item : listbox.getItems()) {
      if (item.isSelected()) {
        list.add(getSource().get(item.getIndex()));
      }
    }
    return list;
  }

  /**
   * Ставит галочки напротив выбранных объектов. Выбор со всех остальных объектов будет снят
   *
   * @param selected множество объектов, которые нужно выбрать
   */
  @SuppressWarnings("unchecked")
  public void select(Collection<T> selected) {
    List<T> model = (ListModelList) listbox.getModel();
    List items = listbox.getItems();
    for (int i = 0; i < items.size(); i++) {
      ((Listitem) items.get(i)).setSelected(selected.contains(model.get(i)));
    }
    refreshTitle();
  }

  /**
   * @return возвращает список всех выбранных объектов модели
   */
  @SuppressWarnings("unchecked")
  public List<T> getSelectedObjects() {
    List<T> list = getInnerSelectedObjects();
    if (list.isEmpty() && selectAllIfEmpty) {
      list.addAll((ListModelList) listbox.getModel());
    }
    return list;
  }

  /**
   * Обновляет заголовок
   */
  private void refreshTitle() {
    List<String> list = new LinkedList<String>();
    for (T o : getInnerSelectedObjects()) {
      list.add(converter.apply(o));
    }
    setValue(StringUtils.join(list, ","));
    if (list.isEmpty()) {
      setValue(StringUtils.isNotEmpty(emptyTitle) ? emptyTitle : (selectAllIfEmpty ? "Все" : ""));
    }
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

}
