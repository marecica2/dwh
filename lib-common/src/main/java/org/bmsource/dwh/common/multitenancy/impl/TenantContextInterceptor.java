package org.bmsource.dwh.common.multitenancy.impl;

import org.bmsource.dwh.common.multitenancy.Constants;
import org.bmsource.dwh.common.multitenancy.TenantContext;
import org.bmsource.dwh.common.multitenancy.TenantNotFoundException;
import org.bmsource.dwh.common.portal.TenantDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class TenantContextInterceptor extends HandlerInterceptorAdapter {

    Logger logger = LoggerFactory.getLogger(getClass());

    {
        logger.debug("Creating TenantInterceptor interceptor");
    }

    @Autowired
    @Lazy
    TenantDao tenantDao;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenantUuid = request.getHeader(Constants.TENANT_HEADER);
        String tenantSchema = tenantUuid != null ? tenantDao.findById(tenantUuid)
            .orElseThrow(() -> new TenantNotFoundException("Tenant not found"))
            .getSchemaName() : null;
        logger.debug("Set TenantContext: {}", tenantSchema);
        TenantContext.setTenantSchema(tenantSchema);
        return true;
    }

    @Override
    public void postHandle(
        HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        logger.debug("Clear TenantContext: {}", TenantContext.getTenantSchema());
        TenantContext.setTenantSchema(null);
    }

}
