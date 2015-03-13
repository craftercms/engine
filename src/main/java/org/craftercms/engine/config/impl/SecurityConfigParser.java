package org.craftercms.engine.config.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.engine.config.ConfigParser;
import org.craftercms.engine.exception.ConfigurationException;
import org.craftercms.security.authentication.impl.AuthenticationRequiredHandlerImpl;
import org.craftercms.security.authentication.impl.LoginFailureHandlerImpl;
import org.craftercms.security.authentication.impl.LoginSuccessHandlerImpl;
import org.craftercms.security.authentication.impl.LogoutSuccessHandlerImpl;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.MapFactoryBean;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import static org.craftercms.engine.util.spring.BeanDefinitionUtils.addPropertyIfNotNull;
import static org.craftercms.engine.util.spring.BeanDefinitionUtils.createBeanDefinitionFromOriginal;

/**
 * {@link org.craftercms.engine.config.ConfigParser} that looks for security configuration, creating any pertinent
 * bean definitions based in the config.
 *
 * @author avasquez
 */
public class SecurityConfigParser implements ConfigParser {

    public static final String LOGIN_FORM_URL_KEY = "security.login[@formUrl]";
    public static final String LOGIN_DEFAULT_SUCCESS_URL_KEY = "security.login[@defaultSuccessUrl]";
    public static final String LOGIN_ALWAYS_USE_DEFAULT_SUCCESS_URL_KEY = "security.login[@alwaysUseDefaultSuccessUrl]";
    public static final String LOGIN_FAILURE_URL_KEY = "security.login[@failureUrl]";
    public static final String LOGOUT_SUCCESS_URL_KEY = "security.logout[@successUrl]";
    public static final String URL_RESTRICTION_KEY = "security.urlRestrictions.restriction";
    public static final String URL_RESTRICTION_URL_KEY = "[@url]";
    public static final String URL_RESTRICTION_EXPRESSION_KEY = "[@expression]";

    public static final String LOGIN_SUCCESS_HANDLER_BEAN_NAME = "crafter.loginSuccessHandler";
    public static final String LOGIN_FAILURE_HANDLER_BEAN_NAME = "crafter.loginFailureHandler";
    public static final String LOGOUT_SUCCESS_HANDLER_BEAN_NAME = "crafter.logoutSuccessHandler";
    public static final String AUTHENTICATION_REQUIRED_HANDLER_BEAN_NAME = "crafter.authenticationRequiredHandler";
    public static final String URL_RESTRICTIONS_BEAN_NAME = "crafter.urlRestrictions";

    public static final String DEFAULT_TARGET_URL_PROPERTY = "defaultTargetUrl";
    public static final String ALWAYS_USE_DEFAULT_TARGET_URL_PROPERTY = "alwaysUseDefaultTargetUrl";
    public static final String TARGET_URL_PROPERTY = "targetUrl";
    public static final String LOGIN_FORM_URL_PROPERTY = "loginFormUrl";
    public static final String SOURCE_MAP_PROPERTY = "sourceMap";

    @Override
    public void parse(HierarchicalConfiguration config,
                      GenericApplicationContext applicationContext) throws ConfigurationException {
        BeanDefinition loginSuccessHandlerDef = createLoginSuccessHandlerDefinition(config, applicationContext);
        BeanDefinition loginFailureHandlerDef = createLoginFailureHandlerDefinition(config, applicationContext);
        BeanDefinition logoutSuccessHandlerDef = createLogoutSuccessHandlerDefinition(config, applicationContext);
        BeanDefinition authenticationRequiredHandlerDef = createAuthenticationRequiredHandlerDefinition(
            config, applicationContext);
        BeanDefinition urlRestrictionsDef = createUrlRestrictionsDefinition(config);

        if (loginSuccessHandlerDef != null) {
            applicationContext.registerBeanDefinition(LOGIN_SUCCESS_HANDLER_BEAN_NAME, loginSuccessHandlerDef);
        }
        if (loginFailureHandlerDef != null) {
            applicationContext.registerBeanDefinition(LOGIN_FAILURE_HANDLER_BEAN_NAME, loginFailureHandlerDef);
        }
        if (loginSuccessHandlerDef != null) {
            applicationContext.registerBeanDefinition(LOGOUT_SUCCESS_HANDLER_BEAN_NAME, logoutSuccessHandlerDef);
        }
        if (authenticationRequiredHandlerDef != null) {
            applicationContext.registerBeanDefinition(AUTHENTICATION_REQUIRED_HANDLER_BEAN_NAME,
                                                      authenticationRequiredHandlerDef);
        }
        if (urlRestrictionsDef != null) {
            applicationContext.registerBeanDefinition(URL_RESTRICTIONS_BEAN_NAME, urlRestrictionsDef);
        }
    }

    protected BeanDefinition createLoginSuccessHandlerDefinition(HierarchicalConfiguration config,
                                                                 ApplicationContext applicationContext) {
        String defaultTargetUrl = config.getString(LOGIN_DEFAULT_SUCCESS_URL_KEY);
        String alwaysUseDefaultTargetUrl = config.getString(LOGIN_ALWAYS_USE_DEFAULT_SUCCESS_URL_KEY);
        BeanDefinition beanDefinition = null;

        if (StringUtils.isNotEmpty(defaultTargetUrl) || StringUtils.isNotEmpty(alwaysUseDefaultTargetUrl)) {
            beanDefinition = createBeanDefinitionFromOriginal(applicationContext, LOGIN_SUCCESS_HANDLER_BEAN_NAME);
            beanDefinition.setBeanClassName(LoginSuccessHandlerImpl.class.getName());

            addPropertyIfNotNull(beanDefinition, DEFAULT_TARGET_URL_PROPERTY, defaultTargetUrl);
            addPropertyIfNotNull(beanDefinition, ALWAYS_USE_DEFAULT_TARGET_URL_PROPERTY, alwaysUseDefaultTargetUrl);
        }

        return beanDefinition;
    }

    protected BeanDefinition createLoginFailureHandlerDefinition(HierarchicalConfiguration config,
                                                                 ApplicationContext applicationContext) {
        String targetUrl = config.getString(LOGIN_FAILURE_URL_KEY);
        BeanDefinition beanDefinition = null;

        if (StringUtils.isNotEmpty(targetUrl)) {
            beanDefinition = createBeanDefinitionFromOriginal(applicationContext, LOGIN_FAILURE_HANDLER_BEAN_NAME);
            beanDefinition.setBeanClassName(LoginFailureHandlerImpl.class.getName());

            addPropertyIfNotNull(beanDefinition, TARGET_URL_PROPERTY, targetUrl);
        }

        return beanDefinition;
    }

    protected BeanDefinition createLogoutSuccessHandlerDefinition(HierarchicalConfiguration config,
                                                                  ApplicationContext applicationContext) {
        String targetUrl = config.getString(LOGOUT_SUCCESS_URL_KEY);
        BeanDefinition beanDefinition = null;

        if (StringUtils.isNotEmpty(targetUrl)) {
            beanDefinition = createBeanDefinitionFromOriginal(applicationContext, LOGOUT_SUCCESS_HANDLER_BEAN_NAME);
            beanDefinition.setBeanClassName(LogoutSuccessHandlerImpl.class.getName());

            addPropertyIfNotNull(beanDefinition, TARGET_URL_PROPERTY, targetUrl);
        }

        return beanDefinition;
    }

    protected BeanDefinition createAuthenticationRequiredHandlerDefinition(HierarchicalConfiguration config,
                                                                           ApplicationContext applicationContext) {
        String loginFormUrl = config.getString(LOGIN_FORM_URL_KEY);
        BeanDefinition beanDefinition = null;

        if (StringUtils.isNotEmpty(loginFormUrl)) {
            beanDefinition = createBeanDefinitionFromOriginal(applicationContext,
                                                              AUTHENTICATION_REQUIRED_HANDLER_BEAN_NAME);
            beanDefinition.setBeanClassName(AuthenticationRequiredHandlerImpl.class.getName());

            addPropertyIfNotNull(beanDefinition, LOGIN_FORM_URL_PROPERTY, loginFormUrl);
        }

        return beanDefinition;
    }

    protected BeanDefinition createUrlRestrictionsDefinition(HierarchicalConfiguration config)
        throws ConfigurationException {
        List<HierarchicalConfiguration> restrictionsConfig = config.configurationsAt(URL_RESTRICTION_KEY);
        BeanDefinition beanDefinition = null;

        if (CollectionUtils.isNotEmpty(restrictionsConfig)) {
            beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClassName(MapFactoryBean.class.getName());

            Map<String, String> restrictionsMap = new LinkedHashMap<>(restrictionsConfig.size());
            for (HierarchicalConfiguration restrictionConfig : restrictionsConfig) {
                String url = restrictionConfig.getString(URL_RESTRICTION_URL_KEY);
                String expression = restrictionConfig.getString(URL_RESTRICTION_EXPRESSION_KEY);

                if (StringUtils.isEmpty(url)) {
                    throw new ConfigurationException("URL restriction element missing 'url' attribute");
                }
                if (StringUtils.isEmpty(expression)) {
                    throw new ConfigurationException("URL restriction element missing 'expression' attribute");
                }

                restrictionsMap.put(url, expression);
            }

            addPropertyIfNotNull(beanDefinition, SOURCE_MAP_PROPERTY, restrictionsMap);
        }

        return beanDefinition;
    }

}
