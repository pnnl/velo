package gov.pnnl.velo.sapphire.tmp;

import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.localization.LocalizationService;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * Creates fact statements about property by using static content specified in @Fact annotations.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class DefenitionService extends Service {
  protected String content() {
    final PropertyDef property = context(PropertyDef.class);

    final Definition defAnnotation = property.getAnnotation(Definition.class);

    if (defAnnotation != null) {
      final LocalizationService localization = context(PropertyDef.class).getLocalizationService();
      return localization.text(defAnnotation.content(), CapitalizationType.FIRST_WORD_ONLY, true);
    }
    return null;
  }

  public static final class Condition extends ServiceCondition {
    @Override
    public boolean applicable(final ServiceContext context) {
      final PropertyDef property = context.find(PropertyDef.class);
      return (property != null && (property.hasAnnotation(Definition.class)));
    }
  }

}
