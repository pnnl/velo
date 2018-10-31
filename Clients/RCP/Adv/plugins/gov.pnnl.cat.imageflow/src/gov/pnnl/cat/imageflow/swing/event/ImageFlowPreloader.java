package gov.pnnl.cat.imageflow.swing.event;

import java.util.concurrent.Executors;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.codebeach.ui.ImageFlow;
import com.codebeach.ui.ImageFlowItem;

/**
 * Preload images for the {@link ImageFlow} in a separate thread, triggered by selection events.
 */
public class ImageFlowPreloader implements ListSelectionListener {
  private ImageFlow imageFlow;

  private Thread preloader;

  /**
   * Set the {@link ImageFlow} to be updated by the {@link Preloader} thread.
   * <p>
   * Start an instance of the thread to preload the initial set of images.
   * </p>
   * 
   * @param imageFlow
   *          {@link ImageFlow}
   */
  public ImageFlowPreloader(ImageFlow imageFlow) {
    this.imageFlow = imageFlow;
    startPreloader();
  }

  /**
   * Tell the thread to update the image cache.
   * 
   * @param event
   *          {@link ListSelectionEvent} from {@link ImageFlow}
   * 
   * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
   */
  @Override
  public void valueChanged(ListSelectionEvent event) {
    startPreloader();
  }

  private void startPreloader() {
    Thread currentPreloader = preloader;
    this.preloader = Executors.defaultThreadFactory().newThread(new Preloader());

    if (currentPreloader != null) {
      currentPreloader.interrupt();
    }

    preloader.start();
  }

  /**
   * Preload the images in {@link ImageFlow} in a separate thread.
   */
  private class Preloader implements Runnable {

    @Override
    public void run() {
      Thread thisThread = Thread.currentThread();

      int selectedIndex = imageFlow.getSelectedIndex();
      int start = Math.max(0, selectedIndex - 5);
      int end = Math.min(imageFlow.getAvatars().size() - 1, start + 10);

      for (int i = 0; i < imageFlow.getAvatars().size() && preloader == thisThread; i++) {
        ImageFlowItem avatar = imageFlow.getAvatars().get(i);

        if (i >= start && i <= end) {
          avatar.loadImage();
        } else {
          avatar.unloadImage();
        }
      }
    }
  }
}
