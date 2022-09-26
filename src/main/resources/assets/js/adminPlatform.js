/* 
 *   NumberRoutingManagement - adminPlatform.js
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

let hideDisabled = true;
let itemConfig;
let ajaxBase = contextPath + "admin/platform/";
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
        {"data": "name"},
        {"data": "ipGroup"}
    ],
    "columnDefs": [
        {
            "orderable": false,
            "targets": 2,
            "data": "platformId",
            "render": function (data, type, row, meta) {
                let iconHtml = '<i class="text-info fas fa-edit px-2 fa-lg" title="Edit ' + row.name + '" onclick="editItem(' + data + ')"></i>';
                return iconHtml;
            }
        }
    ]
});


function getConfig() {
    $.ajax({
        url: ajaxBase + "config",
        method: "GET",
        contentType: "application/json",
        dataType: "json",
        success: function (responseData) {
            itemConfig = responseData;
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
    $('#editOrigName').val('');
    $('#editOrigIpGroup').val('');
    $('#modalTitle').text("New Platform");
    $("#editName").removeClass("is-invalid").removeClass("is-valid");
    $("#editIpGroup").removeClass("is-invalid").removeClass("is-valid");
    $("#btnSave").prop("disabled", true);
    $("#saveCheckMark").show();
    $("#saveSpinner").hide();
    $("#editForm")[0].reset();
    $("#editModal").modal("show");
}

function editItem(itemId) {
    getConfig();

    $.ajax({
        url: ajaxObjectBase + itemId,
        method: "GET",
        contentType: "application/json",
        dataType: "json",
        success: function (responseItem) {
            $('#editID').val(responseItem.platformId);
            $('#editOrigName').val(responseItem.name);
            $('#editName').val(responseItem.name);
            $('#editIpGroup').val(responseItem.ipGroup);
            $('#editOrigIpGroup').val(responseItem.ipGroup);
            $('#modalTitle').text("Edit Platform (" + responseItem.name + ")");
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

function completeSave() {
    $("#btnSave").prop("disabled", true);
    if (!validate())
        return;
    $("#editForm").submit();
}


function checkNameInUse(newName, origName) {
    if (newName == origName) {
        return false;
    }
    if (itemConfig.inUseNames.indexOf(newName) >= 0) {
        return true;
    }
    return false;
}

function checkIpGroupInUse(newGroup, origGroup) {
    if (newGroup == origGroup) {
        return false;
    }
    if (itemConfig.inUseIpGroups.indexOf(origGroup) >= 0) {
        return true;
    }
    return false;
}

function validate() {

    let editName = $('#editName').val();
    let editOrigName = $('#editOrigName').val();
    let editIpGroup = $('#editIpGroup').val();
    let editOrigIpGroup = $('#editOrigIpGroup').val();

    $("#btnSave").prop("disabled", true);
    let nameRegex = /^[0-9a-zA-Z]+([\-_\s\.]+[0-9a-zA-Z]+)*$/;
    
    let valid = true;


    if (editName == null || !editName.match(nameRegex)) {
        valid = false;
        $("#editName").addClass("is-invalid").removeClass("is-valid");
        $("#editNameValidateError").text("Invalid name");

    }
    else if (editName.length > 50) {
        valid = false;
        $("#editName").addClass("is-invalid").removeClass("is-valid");
        $("#editNameValidateError").text("Maximum length 50 characters");
    }
    else if (checkNameInUse(editName, editOrigName)) {
        $("#editName").addClass("is-invalid").removeClass("is-valid");
        $("#editNameValidateError").text("Name must be unique.");
        valid = false;
    }
    else {
        $("#editName").removeClass("is-invalid").addClass("is-valid");
    }

    if (editIpGroup == null || !editIpGroup.match(nameRegex)) {
        valid = false;
        $("#editIpGroup").addClass("is-invalid").removeClass("is-valid");
        $("#editIpGroupValidateError").text("Invalid IP Group Name");

    }
    else if (editIpGroup.length > 50) {
        valid = false;
        $("#editIpGroup").addClass("is-invalid").removeClass("is-valid");
        $("#editIpGroupValidateError").text("Maximum length 50 characters");
    }
    else if (checkIpGroupInUse(editIpGroup, editOrigIpGroup)) {
        $("#editIpGroup").addClass("is-invalid").removeClass("is-valid");
        $("#editIpGroupValidateError").text("IP Group Name must be unique.");
        valid = false;
    }
    else {
        $("#editIpGroup").removeClass("is-invalid").addClass("is-valid");
    }

    $("#btnSave").prop("disabled", !valid);
    return valid;

}


$('.dataTables_length').addClass('bs-select');

$.fn.dataTable.ext.errMode = function (settings, techNote, message) {
    $('#jsErrorText').text("An error was encountered whilst retrieving the list of Platforms. Table below may be incomplete.");
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
    if (objectId != null && objectId != '' && objectId != 0) {
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
