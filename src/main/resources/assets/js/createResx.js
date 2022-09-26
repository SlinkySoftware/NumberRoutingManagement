/*
 *   NumberRoutingManagement - createResx.js
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


const ajaxBase = contextPath + "frontend/";
const invalidClass = "is-invalid";
const validClass = "is-valid";

function refreshPage() {
    location.assign(location.href);
}

function refreshDialog() {

    $.ajax({
        url: ajaxBase + "resources",
        method: "GET",
        contentType: "application/json",
        dataType: "json",
        success: function (responseData) {
            $("#btnAllocate").prop("disabled", true);
            console.log(responseData);
            let locationSelection = $("#locationSelection");
            locationSelection.empty();
            locationSelection.append(new Option("--Select a Phone Number Location--", "null"));
            $('#locationSelection option[value="null"]').attr("disabled", true);
            $.each(responseData.sites, function (id, value) {
                locationSelection.append(new Option(value.name, value.siteId));
            });


            let platformSelection = $("#platformSelection");
            platformSelection.empty();
            platformSelection.append(new Option("--Select a Platform--", "null"));
            $('#platformSelection option[value="null"]').attr("disabled", true);
            $.each(responseData.platforms, function (id, value) {
                platformSelection.append(new Option(value.name, value.platformId));
            });
            
            let typeSelection = $("#typeSelection");
            typeSelection.empty();
            typeSelection.append(new Option("--Select a Resource Type--", "null"));
            $('#typeSelection option[value="null"]').attr("disabled", true);
            $.each(responseData.numbertypes, function (id, value) {
                typeSelection.append(new Option(value, id));
            });
        },
        error: function (error) {
            console.log("Error:", error);
            $('#jsErrorText').text("An error was encountered whilst retrieving the configuration");
            $('#jsError').show();

            $("#btnAllocate").prop("disabled", true);
        }
    });
}

function validate() {
    let locationSelection = $('#locationSelection').val();
    let platformSelection = $('#platformSelection').val();
    let typeSelection = $('#typeSelection').val();
    let deviceDescription = $('#deviceDescription').val();
    let deviceName = $('#deviceName').val();

    let deviceNameRegex = /^(?:[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*)|(?:SEP(?:[0-9a-f]){12})|(?:[a-zA-Z0-9](?:[a-zA-Z0-9\s\+\.\-_\(\)]+[a-zA-Z0-9])*)$/gmi;
    let deviceDescriptionRegex = /^[a-zA-Z0-9](?:[a-zA-Z0-9\s\+\.\-_\(\)]+[a-zA-Z0-9])*$/;

    $("#btnAllocate").prop("disabled", true);
    let valid = true;
    if (locationSelection === null || locationSelection === "" || locationSelection === "null") {
        valid = false;
        $("#locationSelection").addClass(invalidClass).removeClass(validClass);
    }
    else {
        $("#locationSelection").removeClass(invalidClass).addClass(validClass);
    }
    
    if (platformSelection === null || platformSelection === "" || platformSelection === "null") {
        valid = false;
        $("#platformSelection").addClass(invalidClass).removeClass(validClass);
    }
    else {
        $("#platformSelection").removeClass(invalidClass).addClass(validClass);
    }
    
    if (typeSelection === null || typeSelection === "" || typeSelection === "null") {
        valid = false;
        $("#typeSelection").addClass(invalidClass).removeClass(validClass);
    }
    else {
        $("#typeSelection").removeClass(invalidClass).addClass(validClass);
    }
    
    if (deviceDescription === null || !deviceDescription.match(deviceDescriptionRegex)) {
        valid = false;
        $("#deviceDescription").addClass(invalidClass).removeClass(validClass);
    }
    else {
        $("#deviceDescription").removeClass(invalidClass).addClass(validClass);
    }
    
    if (deviceName === null || !deviceName.match(deviceNameRegex)) {
        valid = false;
        $("#deviceName").addClass(invalidClass).removeClass(validClass);
    }
    else {
        $("#deviceName").removeClass(invalidClass).addClass(validClass);
    }

    $("#btnAllocate").prop("disabled", !valid);
    return valid;
}



$("#btnAllocate").prop("disabled", true);
hljs.highlightAll();
refreshDialog();
new ClipboardJS('#btnCopy');

