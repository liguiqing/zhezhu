package com.zhezhu.commons.port.adaptor.http.controller;

import com.zhezhu.commons.lang.Throwables;
import com.zhezhu.commons.security.UserFace;
import com.zhezhu.commons.security.UserFaceService;
import com.zhezhu.commons.util.ClientType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

/**
 * @author Liguiqing
 * @since V3.0
 */


public abstract class AbstractHttpController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final static ServletWrapper defaultServletWrapper = new ServletWrapper() {};

    private final static MessageSourceFactory defaultMessageSourceFactory = new MessageSourceFactory() {};

    @Autowired(required=false)
    private ServletWrapper servletWrapper;

    @Autowired(required=false)
    private MessageSourceFactory messageSourceFactory;

    @Autowired(required=false)
    private UserFaceService userFaceService;

    protected void output(String content,HttpServletResponse response){
        if(response == null)
            response = getResponse();
        PrintWriter out = null;
        try {
            out = response.getWriter();
            out.print(content);
        } catch (IOException e) {
            logger.error(Throwables.toString(e));
        }
    }

    protected ServletWrapper getServletWrapper(){
        if(this.servletWrapper == null){
            this.servletWrapper = defaultServletWrapper;
        }
        return this.servletWrapper;
    }

    protected MessageSourceFactory getMessageSourceFactory(){
        if(this.messageSourceFactory == null){
            this.messageSourceFactory = defaultMessageSourceFactory;
        }
        return this.messageSourceFactory;
    }

    protected HttpServletRequest getRequest(){
        return this.getServletWrapper().getRequest();
    }

    protected HttpServletResponse getResponse(){
        return this.getServletWrapper().getResponse();
    }

    protected ModelAndViewBuilder newModelAndViewBuilder(String view){
        return this.newModelAndViewBuilder(view,getRequest());
    }

    protected ModelAndViewBuilder newModelAndViewBuilder(String view,HttpServletRequest request){
        MessageSource messageSource = getMessageSourceFactory().lookup(request.getParameter("local"));
        return new ModelAndViewBuilder(view,messageSource);
    }

    protected UserFace getUser(){
        return userFaceService.getUser();
    }

    protected int pageStart(int page,int size){
        if(page <=0)
            page = 1;
        if(size<0)
            size = 100;
        return page * size - size;
    }

    protected ClientType getClientType(){
        HttpServletRequest request = getRequest();
        //TODO
        return ClientType.WeChatApp;
    }

    protected <T extends DataFilter> void filte(Collection<T> ts){
        ts.forEach(t -> t.filter(getClientType()));
    }

}