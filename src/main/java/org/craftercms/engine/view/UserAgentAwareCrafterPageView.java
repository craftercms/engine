package org.craftercms.engine.view;

import org.craftercms.engine.mobile.UserAgentTemplateDetector;
import org.springframework.beans.factory.annotation.Required;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class UserAgentAwareCrafterPageView extends CrafterPageView {
	
	protected UserAgentTemplateDetector userAgentTemplateDetector;
	
	@Required
	public void setUserAgentTemplateDetector(UserAgentTemplateDetector userAgentTemplateDetector) {
		this.userAgentTemplateDetector = userAgentTemplateDetector;
	}
	
	@Override
	protected void renderActualView(String pageViewName, Map<String, Object> model, HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {
    	String userAgentSpecificPageViewName = userAgentTemplateDetector.resolveAgentTemplate(request, pageViewName);

        super.renderActualView(userAgentSpecificPageViewName, model, request, response);
	}

}
