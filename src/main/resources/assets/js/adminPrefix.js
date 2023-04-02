/* 
 *   NumberRoutingManagement - adminPrefix.js
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


let hideDisabled = true;
let itemConfig;
let ajaxBase = contextPath + "admin/prefix/";
let ajaxObjectBase = ajaxBase + "item/";

let dataRecords = $('#dataTable').DataTable({
    "autoWidth": false,
    "lengthChange": false,
    "processing": true,
    "searching": false,
    "pageLength": 20,
    "order": [[0, 'asc']],
    "serverSide": true,
    'serverMethod': 'POST',
    "ajax": {
        "url": ajaxBase + "refresh",
        "contentType": "application/json",
        "type": "POST",
        "dataType": "json",
        "data": function (d) {
            d.filter = {enabled: hideDisabled};
            return JSON.stringify(d);
        }
    },
    "columns": [
        {"data": "e164Prefix"},
        {"data": "platformName"},
        {"data": "szu"},
        {"data": "allowAllocation"}
    ],
    "columnDefs": [
        {
            "orderable": false,
            "targets": 4,
            "data": "id",
            "render": function (data, type, row, meta) {
                let iconHtml = '<i class="text-info fas fa-edit px-2 fa-lg" title="Edit ' + row.name + '" onclick="editItem(' + data + ')"></i>';
                if (row.extensionCount != 100) {
                    iconHtml = iconHtml + '<i class="text-info fas fa-code-branch px-2 fa-lg" title="Create missing E164 numbers" onclick="createNumbers(' + data + ', this)"></i>';
                }
                return iconHtml;
            }
        },
    ]
});


function getConfig() {
    let editPlatformList = $("#editPlatform");
    editPlatformList.empty();
    editPlatformList.append(new Option("--Select a Platform--", "null"));
    $('#editPlatform option[value="null"]').attr("disabled", true);

    $.ajax({
        url: ajaxBase + "config",
        method: "GET",
        contentType: "application/json",
        dataType: "json",
        success: function (responseData) {
            itemConfig = responseData;
            $.each(itemConfig.platforms, function (name, id) {
                editPlatformList.append(new Option(name, id));
            });
        },
        error: function (error) {
            console.log("Error:", error);
            $('#jsErrorText').text("An error was encountered whilst retrieving the configuration parameters");
            $('#jsError').show();
        }
    });
}

function addNew() {
    getConfig();
    $('#editID').val('');
    $('#editOrigE164Prefix').val('');
    $('#editE164Prefix').val('');
    $('#editSzu').val('');
    $('#editE164Prefix').prop("disabled", false);
    $('#editPlatform option[value="null"]').attr("selected", true);
    $('#modalTitle').text("New E164 Prefix");
    $("#editE164Prefix").removeClass("is-invalid").removeClass("is-valid");
    $("#editPlatform").removeClass("is-invalid").removeClass("is-valid");
    $("#btnSave").prop("disabled", true);
    $("#saveCheckMark").show();
    $("#saveSpinner").hide();
    $("#editForm")[0].reset();
    $("#editModal").modal("show");
}

function editItem(itemId) {

    $.ajax({
        url: ajaxObjectBase + itemId,
        method: "GET",
        contentType: "application/json",
        dataType: "json",
        success: function (responseItem) {
            $('#editID').val(responseItem.id);
            $('#editOrigE164Prefix').val(responseItem.e164Prefix);
            $('#editE164Prefix').val(responseItem.e164Prefix);
            $('#editE164Prefix').prop("disabled", true);
            $('#editSzu').val(responseItem.szu);
            $('#editAllowAllocation').prop("checked", (responseItem.allowAllocation !== null && responseItem.allowAllocation));
            $('#modalTitle').text("Edit E164 Prefix (" + responseItem.e164Prefix + ")");
            if ($('#editPlatform option[value="' + responseItem.platformId + '"]').length > 0) {
                $('#editPlatform option[value="' + responseItem.platformId + '"]').attr("selected", true);
            }
            else {
                $('#editPlatform option[value="null"]').attr("selected", true);
            }

            $("#btnSave").prop("disabled", true);
            validate();
            $("#saveCheckMark").show();
            $("#saveSpinner").hide();
            $("#editModal").modal("show");
        },
        error: function (error) {
            console.log("Error looking up record:", error);
            $('#jsErrorText').text("An error was encountered whilst retrieving the record");
            $('#jsError').show();
        }
    });
}

function createNumbers(prefixId, iconObj) {
    let icon = $(iconObj);
    icon.removeClass("fa-code-branch").addClass("fa-cog fa-spin");
    
    $.ajax({
        url: ajaxObjectBase + prefixId + "/create",
        method: "POST",
        contentType: "application/json",
        dataType: "json",
        success: function (responseItem) {
            console.log("AJAX Success returned", responseItem);
            icon.addClass("fa-code-branch text-success").removeClass("fa-cog fa-spin");
            if (responseItem.success === true) {
                console.log("API success");
                $('#jsSuccessText').text("Successfully created " + responseItem.numCreated + " records");
                $('#jsSuccess').show();
            } 
            else {
                console.log("API failed");
                $('#jsErrorText').text("An error was encountered whilst adding the numbers");
                $('#jsError').show();
            }

        },
        error: function (error) {
            console.log("Error adding numbers:", error);
            icon.addClass("fa-code-branch text-danger").removeClass("fa-cog fa-spin");
            $('#jsErrorText').text("An error was encountered whilst adding the numbers");
            $('#jsError').show();
        }
    });
}


function completeSave() {
    $("#btnSave").prop("disabled", true);
    if (!validate())
        return;
    $("#editForm").submit();
}


function checkPrefixInUse(newPrefix, origPrefix) {
    if (newPrefix === origPrefix) {
        return false;
    }
    if (itemConfig.inUsePrefixes.indexOf(newPrefix) >= 0) {
        return true;
    }
    return false;
}

function validate() {

    let editE164Prefix = $('#editE164Prefix').val();
    let editOrigE164Prefix = $('#editOrigE164Prefix').val();
    let editPlatform = $('#editPlatform').val();
    
    $("#btnSave").prop("disabled", true);
    let prefixRegex = /^61[0-9]{7}\.\.$/;

    let valid = true;


    if (editE164Prefix === null || !editE164Prefix.match(prefixRegex)) {
        valid = false;
        $("#editE164Prefix").addClass("is-invalid").removeClass("is-valid");
        $("#editE164PrefixValidateError").text("Invalid AU E164 Prefix. Must be in the format of 61XXXXXXX.. where X is any number from 0-9");

    }
    else if (checkPrefixInUse(editE164Prefix, editOrigE164Prefix)) {
        $("#editE164Prefix").addClass("is-invalid").removeClass("is-valid");
        $("#editE164PrefixValidateError").text("Prefix must be unique.");
        valid = false;
    }
    else {
        $("#editE164Prefix").removeClass("is-invalid").addClass("is-valid");
    }

    if (editPlatform === null || editPlatform === "" || editPlatform === "null") {
        valid = false;
        $("#editPlatform").addClass("is-invalid").removeClass("is-valid");
    } else {
        $("#editPlatform").removeClass("is-invalid").addClass("is-valid");
    }


    $("#btnSave").prop("disabled", !valid);
    return valid;

}


$('.dataTables_length').addClass('bs-select');

$.fn.dataTable.ext.errMode = function (settings, techNote, message) {
    $('#jsErrorText').text("An error was encountered whilst retrieving the list of Prefixes. Table below may be incomplete.");
    $('#jsError').show();
    console.log('DataTables Error: ', message);
};

getConfig();

$("#editModal").on('submit', '#editForm', function (event) {
    event.preventDefault();
    $("#btnSave").prop("disabled", true);
    $("#saveCheckMark").hide();
    $("#saveSpinner").show();
    let formData = formToJson($(this));
    let objectId = $('#editID').val();
    let ajaxUrl = ajaxObjectBase;
    let ajaxAction = "POST";
    if (objectId !== null && objectId !== '' && objectId !== 0) {
        ajaxAction = "PUT";
        ajaxUrl = ajaxObjectBase + objectId;
    }
    $.ajax({
        "url": ajaxUrl,
        "method": ajaxAction,
        "contentType": "application/json",
        "dataType": "json",
        "data": JSON.stringify(formData),
        success: function (responseData) {
            $('#editForm')[0].reset();
            $('#editModal').modal('hide');
            $("#saveCheckMark").show();
            $("#saveSpinner").hide();
            dataRecords.ajax.reload();
        },
        error: function (error) {
            console.log("Error:", error);
            $('#editForm')[0].reset();
            $('#editModal').modal('hide');
            $("#saveCheckMark").show();
            $("#saveSpinner").hide();
            dataRecords.ajax.reload();
            $('#jsErrorText').text("An error was encountered whilst saving the record");
            $('#jsError').show();

        }
    });
});
