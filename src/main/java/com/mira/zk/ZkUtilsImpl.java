package com.mira.zk;

import com.mira.utils.ClassUtils;
import com.mira.utils.StreamUtils;
import com.mira.utils.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.*;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Fileupload;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.impl.InputElement;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.zkoss.zul.Messagebox.Button.*;


public class ZkUtilsImpl implements ZkUtils {
  protected static Logger logger = Logger.getLogger(ZkUtilsImpl.class);
  protected String defaultCaption;
  private int defaultNotificationTimeout = 5000;
  private Notification.Position defaultNotificationPosition = Notification.Position.MIDDLE_CENTER;

  /**
   * Устанавливает заголовок для модальных окон по умолчанию
   *
   * @param defaultCaption заголовок по умолчанию
   */
  public void setDefaultCaption(String defaultCaption) {
    this.defaultCaption = defaultCaption;
  }

  /**
   * Устанавливает таймаут по умолчанию для нотификаций
   *
   * @param defaultNotificationTimeout таймаут
   */
  public void setDefaultNotificationTimeout(int defaultNotificationTimeout) {
    this.defaultNotificationTimeout = defaultNotificationTimeout;
  }

  /**
   * Устанавливает позицию нотификации по умолчанию
   *
   * @param defaultNotificationPosition позиция нотификации
   */
  public void setDefaultNotificationPosition(Notification.Position defaultNotificationPosition) {
    this.defaultNotificationPosition = defaultNotificationPosition;
  }


  @Override
  public void threadShowBusy(Desktop desktop, final String message) {
    threadDoInActivatedDesktop(desktop, new Runnable() {
      @Override
      public void run() {
        Clients.clearBusy();
        Clients.showBusy(message);
      }
    });
  }

  @Override
  public void threadClearBusy(Desktop desktop) {
    threadDoInActivatedDesktop(desktop, new Runnable() {
      @Override
      public void run() {
        Clients.clearBusy();
      }
    });
  }

  @Override
  public void threadDoInActivatedDesktop(Desktop desktop, Runnable action) {
    try {
      Executions.activate(desktop);
      action.run();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      Executions.deactivate(desktop);
    }
  }

  @Override
  public InputStream getResourceAsStream(String path) {
    Execution exec = Executions.getCurrent();
    return exec.getDesktop().getWebApp().getResourceAsStream(exec.toAbsoluteURI(path, false));
  }

  @Override
  public Cookie getCookie(String name) {
    Execution execution = Executions.getCurrent();
    if (execution == null
        || execution.getNativeRequest() == null
        || !(execution.getNativeRequest() instanceof HttpServletRequest)) {
      return null;
    }
    Cookie[] cookies = ((HttpServletRequest) execution.getNativeRequest()).getCookies();
    if (cookies == null) {
      return null;
    }

    for (Cookie cookie : cookies) {
      if (name.equals(cookie.getName())) {
        return cookie;
      }
    }
    return null;
  }

  @Override
  public void setCookie(Cookie cookie) {
    ((HttpServletResponse) Executions.getCurrent().getNativeResponse()).addCookie(cookie);
  }

  @Override
  public void clearAllErrorMessages(org.zkoss.zk.ui.Component parent, org.zkoss.zk.ui.Component... excludes) {
    for (Object child : parent.getChildren()) {
      clearAllErrorMessages((org.zkoss.zk.ui.Component) child, excludes);
    }
    if (parent instanceof InputElement
        && !ArrayUtils.contains(excludes, parent)) {
      ((InputElement) parent).clearErrorMessage();
    }
  }

  @Override
  public byte[] extractBytes(Media media) {
    if (media == null) {
      return null;
    } else if (media.inMemory()) {
      return media.getByteData();
    } else {
      try {
        return StreamUtils.readAll(media.getStreamData(), true);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public File extractFileFromMedia(Media media) {
    if (media == null) {
      return null;
    } else {
      byte[] content;
      if (media.inMemory()) {
        content = media.getByteData();
      } else {
        try {
          content = StreamUtils.readAll(media.getStreamData(), true);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
      return new File(media.getName(), content);
    }
  }

  @Override
  public Collection<WrongValueException> getValidationException
      (org.zkoss.zk.ui.Component
           parent, org.zkoss.zk.ui.Component... excludes) {
    Collection<WrongValueException> result = new LinkedList<WrongValueException>();
    for (Object child : parent.getChildren()) {
      result.addAll(getValidationException((org.zkoss.zk.ui.Component) child, excludes));
    }
    if (parent instanceof InputElement
        && !ArrayUtils.contains(excludes, parent)
        && !((InputElement) parent).isValid()) {
      result.add(new WrongValueException(parent, ((InputElement) parent).getErrorMessage()));
    }

    return result;
  }

  public void validate(org.zkoss.zk.ui.Component parent, org.zkoss.zk.ui.Component... excludes) {
    List<WrongValueException> exceptions = new ArrayList<WrongValueException>(getValidationException(parent, excludes));
    if (!exceptions.isEmpty()) {
      throw new WrongValuesException(exceptions.toArray(new WrongValueException[exceptions.size()]));
    }
  }

  @Override
  public void showInformation(String message) {
    showInformation(null, message);
  }


  @Override
  public void showInformation(String caption, String message) {
    if (StringUtils.isEmpty(caption)) {
      caption = defaultCaption;
    }
    show(message, caption, new Messagebox.Button[]{OK}, Messagebox.INFORMATION);
  }

  @Override
  public void showNotification(String message) {
    showNotification(message, Notification.Type.INFO);
  }

  @Override
  public void showNotification(String message, Notification.Type type) {
    showNotification(message, type, defaultNotificationTimeout);
  }

  @Override
  public void showNotification(String message, Notification.Type type, int timeout) {
    showNotification(message, type, timeout, defaultNotificationPosition);
  }

  @Override
  public void showNotification(String message, Notification.Type type, int timeout, Notification.Position position) {
    Clients.showNotification(message, type.toString(), null, position.toString(), timeout, true);
  }

  @Override
  public void showWarning(String message) {
    showWarning(defaultCaption, message);
  }

  @Override
  public void showWarning(String title, String message) {
    show(message, title, new Messagebox.Button[]{OK}, Messagebox.EXCLAMATION);
  }

  @Override
  public void showQuestion(String title, String message, Runnable onYes) {
    Messagebox.show(message, title, new Messagebox.Button[]{YES, NO}, Messagebox.QUESTION, event -> {
      if (Messagebox.Button.YES.equals(event.getButton())) {
        onYes.run();
      }
    });
  }

  private static Messagebox.Button show(String message, String title, Messagebox.Button[] buttons, String icon) {
    return ClassUtils.coalesce(Messagebox.show(message, title, buttons, null, icon, null, null), CANCEL);
  }

  @Override
  public <T> T extractParameter(Map source, String name, Class<T> cl) {
    Object value = source.get(name);
    if (value == null) {
      return null;
    } else if (value instanceof String[]) {
      String[] values = ((String[]) value);
      return values.length > 0 ? ClassUtils.convert(cl, values[0]) : null;
    } else if (value instanceof Collection) {
      Collection values = (Collection) value;
      return values.isEmpty() ? null : ClassUtils.convert(cl, values.iterator().next());
    } else {
      return ClassUtils.convert(cl, value);
    }
  }
}
