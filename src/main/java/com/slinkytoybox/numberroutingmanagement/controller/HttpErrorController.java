/*
 *   NumberRoutingManagement - HttpErrorController.java
 *
 *   Copyright (c) 2022-2023, Slinky Software
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as
 *   published by the Free Software Foundation, either version 3 of the
 *   License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   A copy of the GNU Affero General Public License is located in the 
 *   AGPL-3.0.md supplied with the source code.
 *
 */
package com.slinkytoybox.numberroutingmanagement.controller;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author Michael Junek (michael@juneks.com.au)
 */
@Controller
@Lazy
@Slf4j
public class HttpErrorController implements ErrorController {

    @Value("${company.name}")
    String companyName;

    @Value("${app.name}")
    String appName;

    @Value("${app.copyright}")
    String appCopyright;

    @Value("${info.build.name}")
    String buildName;

    @Value("${info.build.version}")
    String buildVersion;

    @Value("${company.logo}")
    String companyLogo;

    @Autowired
    private ErrorAttributes errorAttributes;

    @RequestMapping(value = "/error", produces = "text/html")
    public ModelAndView errorHtml(WebRequest request) {
        final String logPrefix = "errorHtml() - ";
        log.trace("{}Entering method", logPrefix);

        final String pageTitle = "Error Encountered";
        final String appVer = buildName + " v" + buildVersion;

        Map<String, Object> body = getErrorAttributes(request);

        body.put("pagetitle", appName + " / " + pageTitle + " (" + companyName + ")");
        body.put("companylogo", companyLogo);
        body.put("appname", appName);
        body.put("copyright", appCopyright);
        body.put("pageheader", pageTitle);
        body.put("appver", appVer);
        body.put("moduleName", "Home");
        body.put("navBarFragment", "none");

        String errorPage = "errors/error";
        if (body.get("status") != null) {
            Integer statusCode = (Integer) body.get("status");
            switch (statusCode) {
                case 404:
                case 403:
                    errorPage = "errors/error-" + statusCode.toString();
                    break;
                default:
                    errorPage = "errors/error";
            }
        }
        log.info("{}Rendering error page", logPrefix);
        return new ModelAndView(errorPage, body);
    }

    @RequestMapping(value = "/error")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> error(WebRequest request, HttpServletRequest httpReq) {
        final String logPrefix = "error() - ";
        log.trace("{}Entering method", logPrefix);
        Map<String, Object> body = getErrorAttributes(request);
        HttpStatus status = getStatus(httpReq);
        log.info("{}Rendering error page", logPrefix);
        return new ResponseEntity<>(body, status);
    }

    private Map<String, Object> getErrorAttributes(WebRequest request) {
        ErrorAttributeOptions eao = ErrorAttributeOptions.defaults().including(
                ErrorAttributeOptions.Include.MESSAGE,
                ErrorAttributeOptions.Include.STACK_TRACE,
                ErrorAttributeOptions.Include.EXCEPTION,
                ErrorAttributeOptions.Include.BINDING_ERRORS
        );
        return this.errorAttributes.getErrorAttributes(request, eao);
    }

    private HttpStatus getStatus(HttpServletRequest request) {
        final String logPrefix = "getStatus() - ";
        log.trace("{}Entering method", logPrefix);
        Integer statusCode = (Integer) request
                .getAttribute("javax.servlet.error.status_code");
        if (statusCode != null) {
            log.trace("{}Status Code: {}", logPrefix, statusCode);
            return HttpStatus.valueOf(statusCode);
        }
        log.trace("{}Status Code defaulted to 500 Internal Server Error", logPrefix);
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

}
