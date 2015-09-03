package org.craftercms.engine.mobile;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

public class UserAgentTemplateDetector {
	
	private List<UserAgent> supportedAgents;
	private String agentHeaderName;
	private String agentQueryStringParamName;
	
	public List<UserAgent> getSupportedAgents() {
		return supportedAgents;
	}

	public void setSupportedAgents(List<UserAgent> supportedAgents) {
		this.supportedAgents = supportedAgents;
	}

    public String getAgentHeaderName() {
        return agentHeaderName;
    }

    public void setAgentHeaderName(String agentHeaderName) {
		this.agentHeaderName = agentHeaderName;
	}

    public String getAgentQueryStringParamName() {
        return agentQueryStringParamName;
    }

    public void setAgentQueryStringParamName(String agentQueryStringParamName) {
		this.agentQueryStringParamName = agentQueryStringParamName;
	}
	
	public String resolveAgentTemplate(HttpServletRequest request, String template) {
		String templatePath = template.substring(0, template.lastIndexOf('/')+1);
		String templateFile = template.substring(template.lastIndexOf('/')+1);
		String queryStringParam = request.getParameter(agentQueryStringParamName);

		if (!StringUtils.isEmpty(queryStringParam)) {
			Iterator<UserAgent> iter = supportedAgents.iterator();
			boolean found = false;
			while (!found && iter.hasNext()) {
				UserAgent userAgent = iter.next();
				found = StringUtils.equalsIgnoreCase(queryStringParam, userAgent.getQueryStringParamValue());
				if (found) {
					templateFile = userAgent.getTemplatePrefix() + templateFile;
				}
			}
		} else { // get agent from header
			String agent = request.getHeader(agentHeaderName);
			if (StringUtils.isEmpty(agent)) {
				agent = request.getHeader("User-Agent");
			}
			if (StringUtils.isNotEmpty(agent)) {
				Iterator<UserAgent> iter = supportedAgents.iterator();
				boolean found = false;
				while (!found && iter.hasNext()) {
					UserAgent userAgent = iter.next();
                    if (StringUtils.isNotEmpty(userAgent.getDetectionRegex())) {
                        Pattern pattern = Pattern.compile(userAgent.getDetectionRegex());
                        Matcher matcher = pattern.matcher(agent);
                        found = matcher.find();
                        if (found) {
                            templateFile = userAgent.getTemplatePrefix() + templateFile;
                        }
                    }
				}
			}

		}

		return templatePath + templateFile;
	}

}
