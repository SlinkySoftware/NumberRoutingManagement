/*
 *   NumberRoutingManagement - SourceController.java
 *
 *   Copyright (c) 2022-2022, Slinky Software
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

/**
 *
 * @author Michael Junek (michael@juneks.com.au)
 */

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
@RequestMapping("/source")
public class SourceController {

    @Value("${git.commit.id.abbrev}")
    private String gitCommitIdAbbrev;

    @Value("${git.branch}")
    private String gitBranch;

    @Value("${info.build.artifact}")
    private String buildArtifact;

    @Value("${info.build.version}")
    private String buildVersion;

    @GetMapping(path = "/code", produces = "application/java-archive")
    public ResponseEntity<byte[]> sourceCodeGet() throws IOException {
        final String logPrefix = "sourceCodeGet() - ";
        log.trace("{}Entering method", logPrefix);

        String gitBranchClean = gitBranch.replaceAll("^([^\\/]*)\\/([^\\/]*)$", "$1-$2");
        String fileName = buildArtifact + "-" + buildVersion + "." + gitBranchClean + "." + gitCommitIdAbbrev + "-sources.jar";
        log.debug("{}Reading source file from ClassPath: {}", logPrefix, fileName);

        byte[] sourceCode = this.getClass().getClassLoader().getResourceAsStream("/" + fileName).readAllBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("application/java-archive"));
        headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());
        headers.setContentLength(sourceCode.length);
        log.debug("{}Streaming result to browser: {}", logPrefix, headers);
        return new ResponseEntity<>(sourceCode, headers, HttpStatus.OK);
    }

}

