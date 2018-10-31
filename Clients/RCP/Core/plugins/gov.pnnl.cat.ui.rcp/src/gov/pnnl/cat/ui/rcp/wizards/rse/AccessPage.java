package gov.pnnl.cat.ui.rcp.wizards.rse;

import gov.pnl.ui.utils.StatusUtil;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.security.PermissionsForm;
import gov.pnnl.velo.model.ACL;

import org.apache.log4j.Logger;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.statushandlers.StatusManager;

public class AccessPage extends WizardPage {

	private PermissionsForm permissionsForm;
	private final static Logger logger = CatLogger.getLogger(AccessPage.class);
	private boolean visited = false;

	protected AccessPage(String pageName) {
		super(pageName);
	}

	@Override
	public void createControl(Composite parent) {
		this.setTitle("Permissions");
		permissionsForm = new PermissionsForm(parent, SWT.NONE);
		((GridLayout)permissionsForm.getLayout()).marginWidth = 16;
		permissionsForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		permissionsForm.setFormBackgroundColor(parent.getBackground());
		setControl(permissionsForm);
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		visited = true;
		loadACL();
	}

	private void loadACL() {
		try {
			permissionsForm.loadPermissions(((ImportFilesWizard)getWizard()).getACL());
		} catch (Throwable e) {
			StatusUtil.handleStatus(
					"Error loading permissions.",
					e, StatusManager.SHOW);
		}
	}

	public ACL getPermissions() {
		ACL permission = permissionsForm.getPermissions();
		return permission;
	}

	@Override
	public boolean isPageComplete() {
		return visited;
	}
}
