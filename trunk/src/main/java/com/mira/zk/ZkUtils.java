package com.mira.zk;

import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.util.Clients;

import javax.servlet.http.Cookie;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

/**
 * Полезные утильные методы для работы с ZK приложениями
 */
public interface ZkUtils {
  /**
   * Переключается на указанный рабочий стол и показывает на нём сообщение загрузки приложения. Метод презназначен
   * для задач, которые выполняются в отдельном потоке и должны показывать информацию о своём прогрессе.
   * После выполенения действия отключается от рабочего стола
   *
   * @param desktop рабочий стол, на котором нужно активировать сообщение
   * @param message текст сообщения
   */
  void threadShowBusy(Desktop desktop, String message);

  /**
   * Переключается на указанный рабочий стол и скрывает на нём сообщение загрузки приложения. Метод презназначен
   * для задач, которые выполняются в отдельном потоке и должны показывать информацию о своём прогрессе.
   * После выполенения действия отключается от рабочего стола
   *
   * @param desktop рабочий стол, на котором нужно скрыть сообщение
   */
  void threadClearBusy(Desktop desktop);

  /**
   * Выполняет указанное действие при активированном рабочем столе. После окончания рабочий стол деактивируется.
   *
   * @param desktop рабочий стол, который нужно активировать
   * @param action  действие
   */
  void threadDoInActivatedDesktop(Desktop desktop, Runnable action);

  /**
   * Открывает файловый поток на чтение ресурса
   *
   * @param path относительный путь к ресурсу
   * @return поток на чтение
   */
  InputStream getResourceAsStream(String path);

  /**
   * Ищет куки с указанным именем. Имя чувствительно к регистру
   *
   * @param name имя куки
   * @return найденный куки или {@code null}
   */
  Cookie getCookie(String name);

  /**
   * Добавляет куки к ответу сервера
   *
   * @param cookie куки
   */
  void setCookie(Cookie cookie);


  /**
   * Shows message box with information message
   *
   * @param message message
   */
  void showInformation(String message);

  /**
   * Shows message box with information message
   *
   * @param caption header
   * @param message message
   */
  void showInformation(String caption, String message);

  /**
   * Shows message box with information message which disappears after timeout
   *
   * @param message message
   */
  void showNotification(String message);

  /**
   * Shows message box with information message which disappears after timeout
   *
   * @param message message
   * @param type    type of notification
   */
  void showNotification(String message, Notification.Type type);

  /**
   * Shows message box with information message which disappears after timeout
   *
   * @param message message
   * @param type    type of notification
   * @param timeout timeout in seconds
   */
  void showNotification(String message, Notification.Type type, int timeout);

  /**
   * Shows message box with information message which disappears after timeout
   *
   * @param message  message
   * @param type     type of notification
   * @param timeout  timeout in seconds
   * @param position position of notification
   */
  void showNotification(String message, Notification.Type type, int timeout, Notification.Position position);

  /**
   * Shows message box with information message
   *
   * @param message message
   */
  void showWarning(String message);

  /**
   * Shows message box with information message
   *
   * @param title   title
   * @param message message
   */
  void showWarning(String title, String message);

  /**
   * Shows question and returns {@code true} if user answered yes.
   *
   * @param title   title
   * @param message question
   * @param onYes   action after Yes button is pressed
   */
  void showQuestion(String title, String message, Runnable onYes);

  /**
   * Validates all input children of specified parent component. Any validation exception is thrown.
   *
   * @param parent   parent component
   * @param excludes components to exclude from checking
   */
  void validate(Component parent, Component... excludes);

  /**
   * Validates all input children of specified parent component. All validation exceptions are returned.
   *
   * @param parent   parent component
   * @param excludes components to exclude from checking
   * @return collection of validation exceptions
   */
  public Collection<WrongValueException> getValidationException(org.zkoss.zk.ui.Component parent, org.zkoss.zk.ui.Component... excludes);

  /**
   * Clears all validation and other messages for all children who are input elements
   *
   * @param parent   parent component
   * @param excludes components to exclude from search
   */
  public void clearAllErrorMessages(Component parent, Component... excludes);

  /**
   * Uploads single file and returns stream. If user cancelled uploading then returns {@code null}
   *
   * @return stream
   */
  public InputStream uploadFileAsStream();

  /**
   * uploads single file and returns arrays of bytes. If user cancelled uploading then returns {@code null}
   *
   * @return array of uploaded bytes
   */
  public byte[] uploadFileAsBytes();

  /**
   * Вызывает окно диалога по выбору файла
   *
   * @return загруженный файл
   */
  public File uploadFile();

  /**
   * Извлекает название файла и его содержимое в память
   *
   * @param media медиа, может быть {@code null}
   * @return файд
   */
  File extractFileFromMedia(Media media);

  /**
   * Извлекает параметр из карты параметров, передаваемых через url
   *
   * @param source карта параметров
   * @param name   имя параметра
   * @param cl     класс, к которому нужно преобразовать значение
   * @return значение параметра или {@code null}, если параметр не передан
   */
  <T> T extractParameter(Map source, String name, Class<T> cl);

  public class File {
    private String name;
    private byte[] content;

    public File(String name, byte[] content) {
      this.name = name;
      this.content = content;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public byte[] getContent() {
      return content;
    }

    public void setContent(byte[] content) {
      this.content = content;
    }
  }

  /**
   * Customization of notifications
   */
  public static class Notification {
    /**
     * Type of notification
     */
    public enum Type {
      INFO(Clients.NOTIFICATION_TYPE_INFO), ERROR(Clients.NOTIFICATION_TYPE_ERROR), WARNING(Clients.NOTIFICATION_TYPE_WARNING);
      private String name;

      Type(String name) {
        this.name = name;
      }

      @Override
      public String toString() {
        return name;
      }
    }

    /**
     * Position of notification
     */
    public enum Position {
      /**
       * the message appears above the anchor, aligned to the left.
       */
      BEFORE_START,
      /**
       * the message appears above the anchor, aligned to the center
       */
      BEFORE_CENTER,
      /**
       * the message appears above the anchor, aligned to the right.
       */
      BEFORE_END,
      /**
       * the message appears below the anchor, aligned to the left.
       */
      AFTER_START,
      /**
       * the message appears below the anchor, aligned to the center.
       */
      AFTER_CENTER,
      /**
       * the message appears below the anchor, aligned to the right.
       */
      AFTER_END,
      /**
       * the message appears to the left of the anchor, aligned to the top.
       */
      START_BEFORE,
      /**
       * the message appears to the left of the anchor, aligned to the middle.
       */
      START_CENTER,
      /**
       * the message appears to the left of the anchor, aligned to the bottom.
       */
      START_AFTER,
      /**
       * the message appears to the right of the anchor, aligned to the top.
       */
      END_BEFORE,
      /**
       * the message appears to the right of the anchor, aligned to the middle.
       */
      END_CENTER,
      /**
       * the message appears to the right of the anchor, aligned to the bottom.
       */
      END_AFTER,
      /**
       * the message overlaps the anchor, with anchor and message aligned at top-left.
       */
      OVERLAP$TOP_LEFT,
      /**
       * the message overlaps the anchor, with anchor and message aligned at top-center.
       */
      TOP_CENTER,
      /**
       * the message overlaps the anchor, with anchor and message aligned at top-right.
       */
      OVERLAP_END$TOP_RIGHT,
      /**
       * the message overlaps the anchor, with anchor and message aligned at middle-left.
       */
      MIDDLE_LEFT,
      /**
       * the message overlaps the anchor, with anchor and message aligned at middle-center.
       */
      MIDDLE_CENTER,
      /**
       * the message overlaps the anchor, with anchor and message aligned at middle-right.
       */
      MIDDLE_RIGHT,
      /**
       * the message overlaps the anchor, with anchor and message aligned at bottom-left.
       */
      OVERLAP_BEFORE$BOTTOM_LEFT,
      /**
       * the message overlaps the anchor, with anchor and message aligned at bottom-center.
       */
      BOTTOM_CENTER,
      /**
       * the message overlaps the anchor, with anchor and message aligned at bottom-right.
       */
      OVERLAP_AFTER$BOTTOM_RIGHT,
      /**
       * the message appears with the upper-left aligned with the mouse cursor.
       */
      AR_POINTER,
      /**
       * the message appears with the top aligned with the bottom of the mouse cursor, with the left side of the message at the horizontal position of the mouse cursor.
       */
      AFTER_POINTER;


      @Override
      public String toString() {
        return super.toString().toLowerCase().replace("$", "/");
      }
    }

  }
}
