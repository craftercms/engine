package org.craftercms.engine.targeting;

import java.util.List;

/**
 * Created by alfonsovasquez on 13/8/15.
 */
public interface TargetIdResolver {

    String getCurrentTargetId() throws IllegalStateException;

    String getDefaultTargetId() throws IllegalStateException;

    List<String> getAvailableTargetIds() throws IllegalStateException;

}
