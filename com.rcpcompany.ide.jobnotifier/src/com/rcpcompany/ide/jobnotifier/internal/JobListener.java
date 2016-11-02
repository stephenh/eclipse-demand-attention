package com.rcpcompany.ide.jobnotifier.internal;

import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.swt.internal.gtk.OS;
import org.eclipse.swt.widgets.Shell;

import com.sun.jna.NativeLong;
import com.sun.jna.platform.unix.X11;
import com.sun.jna.platform.unix.X11.Display;
import com.sun.jna.platform.unix.X11.Window;
import com.sun.jna.platform.unix.X11.XClientMessageEvent;
import com.sun.jna.platform.unix.X11.XEvent;

@SuppressWarnings("restriction")
public class JobListener implements IJobChangeListener {

  public static void main(String[] args) throws Exception {
    // SWT example
    //    org.eclipse.swt.widgets.Display display = new org.eclipse.swt.widgets.Display();
    //    Shell shell = new Shell(display);
    //    Text helloWorldTest = new Text(shell, SWT.NONE);
    //    helloWorldTest.setText("Hello World SWT");
    //    helloWorldTest.pack();
    //    shell.pack();
    //    shell.open();
    //    Window window = new Window(getWindowIdFromShell(shell));
    //    Thread.sleep(2000);
    //    sendMessage(x.XOpenDisplay(null), window, NET_WM_STATE_ADD, NET_WM_STATE_DEMANDS_ATTENTION);
    //    while (!shell.isDisposed()) {
    //      if (!display.readAndDispatch())
    //        display.sleep();
    //    }
    //    display.dispose();

    // AWT example
    // JFrame frame = new JFrame("HelloWorldSwing");
    // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    // JLabel label = new JLabel("Hello World");
    // frame.getContentPane().add(label);
    // frame.pack();
    // frame.setVisible(true);
    // long windowId = Native.getWindowID(frame);
  }

  private static long getWindowIdFromShell(Shell shell) {
    long handle = shell.handle;
    long topWidget = OS._gtk_widget_get_toplevel(handle);
    long topWindow = OS._gtk_widget_get_window(topWidget);
    long topXid = OS._gdk_x11_window_get_xid(topWindow);
    return topXid;
  }

  private static final X11 x = X11.INSTANCE;
  private static final long NET_WM_STATE_ADD = 1L;
  private static final String NET_WM_STATE = "_NET_WM_STATE";
  private static final String NET_WM_STATE_DEMANDS_ATTENTION = "_NET_WM_STATE_DEMANDS_ATTENTION";

  @Override
  public void aboutToRun(IJobChangeEvent event) {
  }

  @Override
  public void awake(IJobChangeEvent event) {
  }

  @Override
  public void done(IJobChangeEvent event) {
    Activator.getDefault().getLog().log(new Status(Status.INFO, "Done " + event.getJob().getName(), "asdf"));
    sendMessage(NET_WM_STATE_ADD, NET_WM_STATE_DEMANDS_ATTENTION);
  }

  @Override
  public void running(IJobChangeEvent event) {
  }

  @Override
  public void scheduled(IJobChangeEvent event) {
  }

  @Override
  public void sleeping(IJobChangeEvent event) {
  }

  private static void sendMessage(final long mode, final String msg) {
    final org.eclipse.swt.widgets.Display swtDisplay = org.eclipse.swt.widgets.Display.getDefault();
    if (swtDisplay == null) {
      return;
    }
    swtDisplay.asyncExec(new Runnable() {
      public void run() {
        Shell shell = swtDisplay.getActiveShell();
        if (shell == null && swtDisplay.getShells().length > 0) {
          shell = swtDisplay.getShells()[0];
        }
        if (shell == null) {
          return;
        }
        Display display = null;
        try {
          display = x.XOpenDisplay(null);
          Window window = new Window(getWindowIdFromShell(shell));
          sendMessage(display, window, mode, msg);
        } finally {
          if (display != null) {
            x.XCloseDisplay(display);
          }
        }
      }
    });
  }

  private static void sendMessage(Display display, Window window, long mode, String message) {
    XEvent event = new XEvent();
    event.type = X11.ClientMessage;
    event.setType(XClientMessageEvent.class);
    event.xclient.type = X11.ClientMessage;
    event.xclient.display = display;
    event.xclient.window = window;
    event.xclient.message_type = x.XInternAtom(display, NET_WM_STATE, false);
    event.xclient.format = 32;
    event.xclient.serial = new NativeLong(0);
    event.xclient.send_event = 0;

    event.xclient.data.setType(NativeLong[].class);
    event.xclient.data.l[0] = new NativeLong(mode); // ADD or REMOVE
    event.xclient.data.l[1] = x.XInternAtom(display, message, false);
    event.xclient.data.l[2] = new NativeLong(0L);
    event.xclient.data.l[3] = new NativeLong(0L);
    event.xclient.data.l[4] = new NativeLong(0L);

    NativeLong mask = new NativeLong(X11.SubstructureNotifyMask);
    x.XSendEvent(display, window, 1, mask, event);
    x.XFlush(display);
  }
}
