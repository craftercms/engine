package org.craftercms.engine.util.logging;

import org.apache.log4j.MDC;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Log4j filter that decides to log an entry based on whether an MDC key exists or not. If the key specified by
 * {@code key} exists, and {@code acceptIfKeyExists} is true, then {@link #ACCEPT} is returned. If
 * {@code acceptIfKeyExists} is false, {@link #DENY} is returned. If the key doesn't exist, and
 * {@code denyIfKeyDoesNotExist} is true, then {@link #DENY} is returned, otherwise {@link #NEUTRAL} is returned.
 *
 * @author avasquez
 */
public class MDCKeyExistsFilter extends Filter {

    private String key;
    private boolean acceptIfKeyExists;
    private boolean denyIfKeyDoesNotExist;

    public void setKey(String key) {
        this.key = key;
    }

    public void setAcceptIfKeyExists(boolean acceptIfKeyExists) {
        this.acceptIfKeyExists = acceptIfKeyExists;
    }

    public void setDenyIfKeyDoesNotExist(boolean denyIfKeyDoesNotExist) {
        this.denyIfKeyDoesNotExist = denyIfKeyDoesNotExist;
    }

    @Override
    public int decide(LoggingEvent event) {
        if (key == null) {
            return NEUTRAL;
        }

        if (MDC.get(key) != null) {
            if (acceptIfKeyExists) {
                return ACCEPT;
            } else {
                return DENY;
            }
        } else {
            if (denyIfKeyDoesNotExist) {
                return DENY;
            } else {
                return NEUTRAL;
            }
        }
    }


}
