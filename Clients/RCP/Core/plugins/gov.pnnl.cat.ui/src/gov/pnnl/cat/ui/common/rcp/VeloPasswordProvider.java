package gov.pnnl.cat.ui.common.rcp;

import javax.crypto.spec.PBEKeySpec;

import org.eclipse.equinox.security.storage.provider.IPreferencesContainer;
import org.eclipse.equinox.security.storage.provider.PasswordProvider;

public class VeloPasswordProvider extends PasswordProvider {

  @Override
  public PBEKeySpec getPassword(IPreferencesContainer container, int passwordType) {
    String pw = "Koolcat74";
    return new PBEKeySpec(pw.toCharArray());
  }

}
