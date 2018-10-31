package gov.pnnl.velo.sapphire.tmp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })

public @interface Definition {
  String content();
}