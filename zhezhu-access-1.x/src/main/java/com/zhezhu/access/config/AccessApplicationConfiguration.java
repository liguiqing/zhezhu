package com.zhezhu.access.config;

import com.zhezhu.access.domain.model.wechat.WebAccessTokenFactory;
import com.zhezhu.access.domain.model.wechat.config.WeChatConfig;
import com.zhezhu.commons.config.MappingResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.persistence.PersistenceContext;

/**
 * @author Liguiqing
 * @since V3.0
 */

@Slf4j
@Configuration
@EnableCaching
@EnableJpaRepositories(value = "com.zhezhu.access.domain.model.**.*",
        includeFilters = {@ComponentScan.Filter(type=FilterType.ANNOTATION,value=Repository.class)})
@ComponentScan(value = "com.zhezhu.access.**",
        includeFilters = {
                @ComponentScan.Filter(type=FilterType.ANNOTATION,value=Service.class),
                @ComponentScan.Filter(type=FilterType.ANNOTATION,value=Component.class)},
        useDefaultFilters = false)
@PropertySource("classpath:/META-INF/spring/accessConfig.properties")
@PersistenceContext()
public class AccessApplicationConfiguration {

    @Bean
    public WeChatConfig weChatConfig(
            @Value("${wechat.appId:}") String appId,
            @Value("${wechat.appSecret:}") String appSecret,
            @Value("${wechat.aesKey:}") String aesKey,
            @Value("${wechat.mchId:}") String mchId,
            @Value("${wechat.apiKey:}") String apiKey){

        return WeChatConfig.builder()
                .appId(appId)
                .appSecret(appSecret)
                .aesKey(aesKey)
                .mchId(mchId)
                .apiKey(apiKey)
                .build();
    }

    @Bean
    public WebAccessTokenFactory webAccessTokenFactory(WeChatConfig weChatConfig){
        return new WebAccessTokenFactory(weChatConfig);
    }

    @Bean("AccessMapping")
    MappingResource mappingResource(){
        return ()->new String[]{
                "/hbm/WeChat.hbm.xml",
                "/hbm/FollowApply.hbm.xml",
                "/hbm/FollowAudit.hbm.xml",
                "/hbm/ClazzFollowApply.hbm.xml",
                "/hbm/ClazzFollowAudit.hbm.xml"};
    }
}