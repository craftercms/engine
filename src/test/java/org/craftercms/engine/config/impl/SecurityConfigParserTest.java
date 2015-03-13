package org.craftercms.engine.config.impl;

import java.lang.reflect.Field;
import java.util.Map;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.craftercms.security.authentication.impl.AuthenticationRequiredHandlerImpl;
import org.craftercms.security.authentication.impl.LoginFailureHandlerImpl;
import org.craftercms.security.authentication.impl.LoginSuccessHandlerImpl;
import org.craftercms.security.authentication.impl.LogoutSuccessHandlerImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.craftercms.engine.config.impl.SecurityConfigParser.ALWAYS_USE_DEFAULT_TARGET_URL_PROPERTY;
import static org.craftercms.engine.config.impl.SecurityConfigParser.AUTHENTICATION_REQUIRED_HANDLER_BEAN_NAME;
import static org.craftercms.engine.config.impl.SecurityConfigParser.DEFAULT_TARGET_URL_PROPERTY;
import static org.craftercms.engine.config.impl.SecurityConfigParser.LOGIN_ALWAYS_USE_DEFAULT_SUCCESS_URL_KEY;
import static org.craftercms.engine.config.impl.SecurityConfigParser.LOGIN_DEFAULT_SUCCESS_URL_KEY;
import static org.craftercms.engine.config.impl.SecurityConfigParser.LOGIN_FAILURE_HANDLER_BEAN_NAME;
import static org.craftercms.engine.config.impl.SecurityConfigParser.LOGIN_FAILURE_URL_KEY;
import static org.craftercms.engine.config.impl.SecurityConfigParser.LOGIN_FORM_URL_KEY;
import static org.craftercms.engine.config.impl.SecurityConfigParser.LOGIN_FORM_URL_PROPERTY;
import static org.craftercms.engine.config.impl.SecurityConfigParser.LOGIN_SUCCESS_HANDLER_BEAN_NAME;
import static org.craftercms.engine.config.impl.SecurityConfigParser.LOGOUT_SUCCESS_HANDLER_BEAN_NAME;
import static org.craftercms.engine.config.impl.SecurityConfigParser.LOGOUT_SUCCESS_URL_KEY;
import static org.craftercms.engine.config.impl.SecurityConfigParser.TARGET_URL_PROPERTY;
import static org.craftercms.engine.config.impl.SecurityConfigParser.URL_RESTRICTIONS_BEAN_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for {@link org.craftercms.engine.config.impl.SecurityConfigParser}.
 *
 * @author avasquez
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:crafter/engine/services/main-services-context.xml"})
public class SecurityConfigParserTest {

    @Autowired
    private ApplicationContext parentContext;

    private SecurityConfigParser configParser;

    @Before
    public void setUp() throws Exception {
        configParser = new SecurityConfigParser();
    }

    @Test
    public void testParse() throws Exception {
        GenericApplicationContext applicationContext = new GenericApplicationContext(parentContext);
        HierarchicalConfiguration config = new XMLConfiguration("config/site.xml");

        configParser.parse(config, applicationContext);

        applicationContext.refresh();

        LoginSuccessHandlerImpl loginSuccessHandler = applicationContext.getBean(LOGIN_SUCCESS_HANDLER_BEAN_NAME,
                                                                                 LoginSuccessHandlerImpl.class);
        LoginFailureHandlerImpl loginFailureHandler = applicationContext.getBean(LOGIN_FAILURE_HANDLER_BEAN_NAME,
                                                                                 LoginFailureHandlerImpl.class);
        LogoutSuccessHandlerImpl logoutSuccessHandler = applicationContext.getBean(LOGOUT_SUCCESS_HANDLER_BEAN_NAME,
                                                                                   LogoutSuccessHandlerImpl.class);
        AuthenticationRequiredHandlerImpl authReqHandler = applicationContext
            .getBean(AUTHENTICATION_REQUIRED_HANDLER_BEAN_NAME, AuthenticationRequiredHandlerImpl.class);
        Map<String, String> urlRestrictions = applicationContext.getBean(URL_RESTRICTIONS_BEAN_NAME, Map.class);

        assertNotNull(loginSuccessHandler);
        assertEquals(config.getString(LOGIN_DEFAULT_SUCCESS_URL_KEY),
                     getFieldValue(LoginSuccessHandlerImpl.class, DEFAULT_TARGET_URL_PROPERTY, loginSuccessHandler));
        assertEquals(config.getBoolean(LOGIN_ALWAYS_USE_DEFAULT_SUCCESS_URL_KEY),
                     getFieldValue(LoginSuccessHandlerImpl.class, ALWAYS_USE_DEFAULT_TARGET_URL_PROPERTY,
                                   loginSuccessHandler));

        assertNotNull(loginFailureHandler);
        assertEquals(config.getString(LOGIN_FAILURE_URL_KEY),
                     getFieldValue(LoginFailureHandlerImpl.class, TARGET_URL_PROPERTY, loginFailureHandler));

        assertNotNull(logoutSuccessHandler);
        assertEquals(config.getString(LOGOUT_SUCCESS_URL_KEY),
                     getFieldValue(LogoutSuccessHandlerImpl.class, TARGET_URL_PROPERTY, logoutSuccessHandler));

        assertNotNull(authReqHandler);
        assertEquals(config.getString(LOGIN_FORM_URL_KEY),
                     getFieldValue(AuthenticationRequiredHandlerImpl.class, LOGIN_FORM_URL_PROPERTY, authReqHandler));

        assertNotNull(urlRestrictions);
        assertEquals(config.getString("security.urlRestrictions.restriction[0][@expression]"),
                     urlRestrictions.get(config.getString("security.urlRestrictions.restriction[0][@url]")));
        assertEquals(config.getString("security.urlRestrictions.restriction[1][@expression]"),
                     urlRestrictions.get(config.getString("security.urlRestrictions.restriction[1][@url]")));
    }

    private Object getFieldValue(Class<?> clazz, String fieldName, Object instance) throws NoSuchFieldException,
        IllegalAccessException {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);

        return field.get(instance);
    }

}
