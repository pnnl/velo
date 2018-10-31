package gov.pnnl.velo.sapphire;

import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.def.ActionHandlerDef;

import gov.pnnl.velo.dataset.util.DatasetUtil;
import gov.pnnl.velo.sapphire.editor.VeloFilePathBrowseActionHandler;

public class DatasetFilePathBrowseActionHandler extends VeloFilePathBrowseActionHandler {
  public static final String PARAM_FILEDIALOGTITLE = "fileDialogTitle";
  public static final String PARAM_FILEDIALOGMESSAGE = "fileDialogMessage";
  private ActionHandlerDef def;

  @Override
  public void init(final SapphireAction action, final ActionHandlerDef def) {
    super.init(action, def);
    this.def = def;
  }

  protected String getTitle() {
    String title = def.getParam(PARAM_FILEDIALOGTITLE);
    if (title != null) {
      return title;
    } else {
      return super.getTitle();
    }
  }

  protected String getMessage() {
    String message = def.getParam(PARAM_FILEDIALOGMESSAGE);
    if (message != null) {
      return message;
    } else {
      return super.getMessage();
    }
  }

  protected Object getTreeRoot() {
    return DatasetUtil.getSelectedDatasetInActiveWindow();
  }
}
