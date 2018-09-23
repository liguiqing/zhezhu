package com.zhezhu.boot.config;

import com.google.common.collect.Lists;
import com.zhezhu.access.infrastructure.shiro.DbUserRealm;
import com.zhezhu.access.infrastructure.shiro.WeChatAuthenticationFilter;
import com.zhezhu.access.infrastructure.shiro.WeChatUserRealm;
import com.zhezhu.commons.security.UserFace;
import com.zhezhu.commons.util.CollectionsUtilWrapper;
import com.zhezhu.share.infrastructure.security.MD5PasswordEncoder;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.*;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.apache.shiro.session.mgt.eis.JavaUuidSessionIdGenerator;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.session.mgt.eis.SessionIdGenerator;
import org.apache.shiro.session.mgt.quartz.QuartzSessionValidationScheduler;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.filter.authc.LogoutFilter;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.Cookie;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.*;

/**
 * Shiro安全组件配置
 *
 * @author Liguiqing
 * @since V3.0
 */

@Configuration
@Order(1)
@EnableCaching
@PropertySource("classpath:/META-INF/spring/shiroConfig.properties")
public class ShiroConfiguration {
    @Bean("shiroPlaceholder")
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
        filterRegistration.setFilter(new DelegatingFilterProxy("shiroFilter"));
        filterRegistration.setEnabled(true);
        filterRegistration.addUrlPatterns("/*");
        filterRegistration.setDispatcherTypes(DispatcherType.REQUEST);
        return filterRegistration;
    }

    @Bean
    public ShiroFilterFactoryBean shiroFilter(@Value("${shiro.filter.success.url:/home}") String successUrl,
                                              @Value("${shiro.filter.login.url:/login}") String loginUrl,
                                              @Value("${shiro.filter.unauthorized.url:/unauthorized}") String unauthorizedUrl,
                                              SecurityManager securityManager,
                                              Map<String, Filter> filters){
        ShiroFilterFactoryBean filterFactory = new ShiroFilterFactoryBean();
        filterFactory.setSecurityManager(securityManager);
        filterFactory.setSuccessUrl(successUrl);
        filterFactory.setLoginUrl(loginUrl);
        filterFactory.setUnauthorizedUrl(unauthorizedUrl);
        filterFactory.setFilters(filters);
        Map<String,String> chains  = new HashMap<>();
        //chains.put("/favicon.ico", "anon");
        chains.put("/static/**", "anon");
        chains.put("/index", "anon");
        chains.put("/", "anon");
        chains.put("/unauthorized", "anon");
        chains.put("/login", "formAuthc");
        chains.put("/logout", "logout");
        chains.put("/**", "user");
        filterFactory.setFilterChainDefinitionMap(chains);
        //filterFactory.setFilterChainDefinitions(filterChainDefinitions());
        return  filterFactory;
    }

    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    @Bean
    @DependsOn("lifecycleBeanPostProcessor")
    public DefaultAdvisorAutoProxyCreator shiroAutoProxyCreator(){
        DefaultAdvisorAutoProxyCreator autoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        autoProxyCreator.setProxyTargetClass(true);
        return autoProxyCreator;
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor advisor(SecurityManager securityManager){
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }

    @Bean
    public SessionIdGeneratorIterator sessionIdGeneratorIterator(Optional<List<SessionIdGenerator>> sessionIdGenerators){
        return new SessionIdGeneratorIterator(sessionIdGenerators);
    }

    @Bean
    public static Cookie sessionIdCookie(@Value("${shiro.session.id:SHIROSESSION}") String name,
                                         @Value("${shiro.session.id:zhezhu}") String sessionId,
                                         @Value("${shiro.cookie.maxAge:-1}") int maxAge,
                                         @Value("${shiro.cookie.domain:}") String domain,
                                         @Value("${shiro.cookie.path:/}") String path){
        SimpleCookie cookie = new SimpleCookie(sessionId);
        cookie.setHttpOnly(true);
        cookie.setDomain(domain);
        cookie.setPath(path);
        cookie.setName(name);
        cookie.setMaxAge(maxAge);
        return  cookie;
    }

    @Bean
    public static SessionDAO sessionDAO(@Value("${shiro.activeSessionCache:shiro-activeSessionCache}") String activeSessionCache,
                                        SessionIdGeneratorIterator sessionIdGenerator){
        EnterpriseCacheSessionDAO sessionDAO = new EnterpriseCacheSessionDAO();
        sessionDAO.setActiveSessionsCacheName(activeSessionCache);
        sessionDAO.setSessionIdGenerator(sessionIdGenerator);
        return sessionDAO;
    }

    @Bean
    public static SessionValidationScheduler sessionValidationScheduler(@Value("${shiro.scheduler.validationInterval:1800000}") long interval){
        QuartzSessionValidationScheduler scheduler = new QuartzSessionValidationScheduler();
        scheduler.setSessionValidationInterval(interval);
        return scheduler;
    }

    @Bean
    public SessionFactory sessionFactory(){
        return new RequestSessionFactory();
    }

    @Bean
    public static SessionManager sessionManager(@Value("${shiro.session.globalSessionTimeout:1800000}") long globalSessionTimeout,
                                                SessionValidationScheduler sessionValidationScheduler,
                                                SessionDAO sessionDAO,
                                                Cookie cookie,
                                                SessionFactory sessionFactory){
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        sessionManager.setGlobalSessionTimeout(globalSessionTimeout);
        sessionManager.setDeleteInvalidSessions(true);
        sessionManager.setSessionValidationSchedulerEnabled(true);
        sessionManager.setSessionValidationScheduler(sessionValidationScheduler);
        sessionManager.setSessionDAO(sessionDAO);
        sessionManager.setSessionIdCookieEnabled(true);
        sessionManager.setSessionIdCookie(cookie);
        sessionManager.setSessionFactory(sessionFactory);
        return sessionManager;
    }

    @Bean
    public  Realm dbUserRealm(@Value("${shiro.user.querysql:}") String sql,
                              MD5PasswordEncoder passwordEncoder,
                              JdbcTemplate jdbcTemplate){
        DbUserRealm realm =  new DbUserRealm(sql,jdbcTemplate);
        realm.setCredentialsMatcher(new PasswordCredentialsMatcher(passwordEncoder));
        return realm;
    }

    @Bean
    public Realm weChatRealm(){
        return new WeChatUserRealm();
    }

    @Bean
    public SecurityManager securityManager(SessionManager sessionManager,
                                           org.apache.shiro.cache.CacheManager cacheManager,
                                           List<Realm> realms){
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setSessionManager(sessionManager);
        securityManager.setCacheManager(cacheManager);
        securityManager.setRealms(realms);
        SecurityUtils.setSecurityManager(securityManager);

        return securityManager;
    }

    @Bean
    public  Filter formAuthc(@Value("${shiro.authc.form.password.param:password}") String passwordParam,
                             @Value("${shiro.authc.form.username.param:username}") String userNameParam){
        FormAuthenticationFilter formAuthenticationFilter = new FormAuthenticationFilter();
        formAuthenticationFilter.setPasswordParam(passwordParam);
        formAuthenticationFilter.setUsernameParam(userNameParam);
        return formAuthenticationFilter;
    }

    @Bean
    public Filter weChatAuthc(){
        return new WeChatAuthenticationFilter();
    }

//    @Bean("logout")
//    public static Filter logout(@Value("${shiro.logout.redirect.url:/index}") String logoutUrl){
//        LogoutFilter logoutFilter = new LogoutFilter();
//        logoutFilter.setRedirectUrl(logoutUrl);
//        //logoutFilter.setPostOnlyLogout(true);
//        return logoutFilter;
//    }

    @Bean
    public org.apache.shiro.cache.CacheManager shiroCacheMgr(CacheManager cacheManager){
        List<MyCache> shiroCaches = Lists.newArrayList();
        cacheManager.getCacheNames().forEach(name->
                shiroCaches.add(new MyCache(){

                    @Override
                    public Object get(Object o) throws CacheException {
                        return cacheManager.getCache(name).get(o);
                    }

                    @Override
                    public Object put(Object o, Object o2) throws CacheException {
                        cacheManager.getCache(name).put(o,o2);
                        return o2;
                    }

                    @Override
                    public Object remove(Object o) throws CacheException {
                        cacheManager.getCache(name).evict(o);
                        return o;
                    }

                    @Override
                    public void clear() throws CacheException {
                        cacheManager.getCache(name).clear();
                    }

                    @Override
                    public int size() {
                        return 0;
                    }

                    @Override
                    public Set keys() {
                        return null;
                    }

                    @Override
                    public Collection values() {
                        return null;
                    }

                    @Override
                    public String getName() {
                        return name;
                    }
                })
        );
        return new org.apache.shiro.cache.CacheManager(){

            @Override
            public MyCache getCache(String s) throws CacheException {
                for(MyCache cache:shiroCaches){
                    if(cache.getName().equalsIgnoreCase(s))
                        return cache;
                }
                return null;
            }
        };
    }

    public interface MyCache extends org.apache.shiro.cache.Cache{
        String getName();
    }

    public class SessionIdGeneratorIterator implements SessionIdGenerator{
        private List<SessionIdGenerator> sessionIdGenerators;

        private SessionIdGenerator defaultSessionIdGenerator = new JavaUuidSessionIdGenerator();

        public SessionIdGeneratorIterator(Optional<List<SessionIdGenerator>> sessionIdGenerators){
            sessionIdGenerators.ifPresent(sessionIdGenerators1 -> this.sessionIdGenerators = sessionIdGenerators1);
        }

        public Serializable generateId(Session session){
            if(CollectionsUtilWrapper.isNotNullAndNotEmpty(this.sessionIdGenerators)){
                for(SessionIdGenerator sessionIdGenerator:sessionIdGenerators){
                    Serializable sessionId = sessionIdGenerator.generateId(session);
                    if(sessionId != null){
                        return sessionId;
                    }
                }
            }
            return defaultSessionIdGenerator.generateId(session);
        }
    }

    public class RequestSimpleSession extends SimpleSession{
        private SessionContext sessionContext;

        public RequestSimpleSession(SessionContext sessionContext,String host) {
            super(host);
            this.sessionContext = sessionContext;
        }

        public HttpServletRequest getRequest(){
            return (HttpServletRequest)sessionContext.get("org.apache.shiro.web.session.mgt.DefaultWebSessionContext.SERVLET_REQUEST");
        }
    }

    public class RequestSessionFactory implements SessionFactory{
        @Override
        public Session createSession(SessionContext sessionContext) {
            if (sessionContext != null) {
                String host = sessionContext.getHost();
                if (host != null) {
                    return new RequestSimpleSession(sessionContext,host);
                }
            }
            return new SimpleSession();
        }
    }

    public class PasswordCredentialsMatcher implements CredentialsMatcher {
        private MD5PasswordEncoder passwordEncoder;

        public PasswordCredentialsMatcher(MD5PasswordEncoder passwordEncoder) {
            this.passwordEncoder = passwordEncoder;
        }

        @Override
        public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
            String tokenHashedCredentials = tokenHashedCredentials(token, info);
            byte[] tokenBytes = tokenHashedCredentials.getBytes();
            byte[] accountBytes = info.getCredentials().toString().getBytes();
            return Arrays.equals(tokenBytes, accountBytes);
        }

        private String tokenHashedCredentials(AuthenticationToken token, AuthenticationInfo info) {
            PrincipalCollection pc = info.getPrincipals();
            UserFace user = (UserFace) pc.getPrimaryPrincipal();
            UsernamePasswordToken userToken = (UsernamePasswordToken) token;
            String originalPassword = new String(userToken.getPassword());
            return this.passwordEncoder.encode(user.getUserId(), originalPassword);
        }
    }

}