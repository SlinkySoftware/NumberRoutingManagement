/*
 *   NumberRoutingManagement - createUser.js
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
const ajaxObjectBase = ajaxBase + "aduser/";
const invalidClass = "is-invalid";
const validClass = "is-valid";
let lastLookupUser = '';

function userChange() {
    const enteredName = $("#userName").val();
    console.log("Checking last user ... curr:", enteredName, " - last:", lastLookupUser);
    if (enteredName === lastLookupUser) {
        $("#btnAllocate").prop("disabled", false);
        $("#userTable").show();
        $("#btnAllocate").show();        
    } else {
        $("#btnAllocate").prop("disabled", true);
        $("#userTable").hide();
        $("#btnAllocate").hide();        
    }
}

function refreshPage() {
    location.assign(location.href);
}

function lookupUser() {
    const userNameRegex = /^[^\/\\\[\]:;|=,+?<>@â€\s]+$/;

    let userName = $('#userName').val();
    console.log("Username:", userName);

    if (userName === null || !userName.match(userNameRegex)) {
        $("#userName").addClass(invalidClass).removeClass(validClass);
        $("#userNameValidateError").val("Invalid characters detected");
        return;
    } else {
        $("#userName").removeClass(invalidClass).addClass(validClass);
    }
    $("#resultUserName").text("");
    $("#resultFullName").text("");
    $("#resultPosition").text("");
    $("#resultDepartment").text("");
    $("#resultLocation").text("");

    $("#adSearch").prop("disabled", true);
    $("#searchIcon").hide();
    $("#searchSpinner").show();

    userName = encodeURIComponent(userName);

    $.ajax({
        url: ajaxObjectBase + userName,
        method: "GET",
        contentType: "application/json",
        dataType: "json",
        success: function (responseData) {
            console.log(responseData);
            if (responseData.adUser.samaccountName === "") {
                $("#userName").addClass(invalidClass).removeClass(validClass);
                $("#userNameValidateError").text("The specified user does not exist");
                $("#btnAllocate").prop("disabled", true);
                $("#btnAllocate").hide();
                $("#userTable").hide();
            } else if (responseData.adUser.accountDisabled) {
                $("#userName").addClass(invalidClass).removeClass(validClass);
                $("#userNameValidateError").text("The specified user is disabled");
                $("#btnAllocate").prop("disabled", true);
                $("#btnAllocate").hide();
                $("#userTable").hide();

            } else {
                let locationSelection = $("#locationSelection");
                locationSelection.empty();
                locationSelection.append(new Option("--Select a Location--", "null"));
                $('#locationSelection option[value="null"]').attr("disabled", true);
                $("#resultUserName").text(responseData.adUser.userPrincipalName);
                $("#resultFullName").text(`${responseData.adUser.firstName} ${responseData.adUser.surname}`);
                $("#resultPosition").text(responseData.adUser.position);
                $("#resultDepartment").text(responseData.adUser.department);
                lastLookupUser = responseData.adUser.samaccountName;
                console.log("Setting last user ... responseData.adUser.samaccountName:", responseData.adUser.samaccountName, " - last:", lastLookupUser, " - aduser:", responseData.adUser);
                if (responseData.promptForSite) {
                    $.each(responseData.sites, function (id, value) {
                        locationSelection.append(new Option(value.name, value.siteId));
                    });
                    $("#locationDisplayRow").hide();
                    $("#locationSelectionRow").show();
                } else {
                    $("#resultLocation").text(`${responseData.adUser.state} / ${responseData.adUser.city}`);
                    $("#locationDisplayRow").show();
                    $("#locationSelectionRow").hide();
                    $("#btnAllocate").prop("disabled", false);
                    $("#btnAllocate").show();
                }
                $("#userTable").show();
            }
            $("#adSearch").prop("disabled", false);
            $("#searchIcon").show();
            $("#searchSpinner").hide();
        },
        error: function (error) {
            console.log("Error:", error);
            if (error.status === 404) {
                $("#userName").addClass(invalidClass).removeClass(validClass);
                $("#userNameValidateError").text("The specified user does not exist");
            } else {
                $('#jsErrorText').text("An error was encountered whilst retrieving the user");
                $('#jsError').show();

            }
            $("#adSearch").prop("disabled", false);
            $("#searchIcon").show();
            $("#searchSpinner").hide();
            $("#btnAllocate").prop("disabled", true);
            $("#btnAllocate").hide();
            $("#userTable").hide();
        }
    });
}

function validate() {
    let locationSelection = $('#locationSelection').val();
    $("#btnAllocate").prop("disabled", true);
    $("#btnAllocate").hide();
    let valid = true;
    if (locationSelection === null || locationSelection === "" || locationSelection === "null") {
        valid = false;
        $("#locationSelection").addClass(invalidClass).removeClass(validClass);
    } else {
        $("#locationSelection").removeClass(invalidClass).addClass(validClass);
    }
    $("#btnAllocate").prop("disabled", !valid);
    if (valid) $("#btnAllocate").show();
    return valid;
}



$("#btnAllocate").prop("disabled", true);
$("#btnAllocate").hide();
$("#userTable").hide();
hljs.highlightAll();
new ClipboardJS('#btnCopy');
