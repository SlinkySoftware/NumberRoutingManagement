/*
 *   NumberRoutingManagement - adminE164Number.js
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
let ajaxBase = contextPath + "admin/e164number/";
let ajaxObjectBase = ajaxBase + "item/";

/*
 *             
    private Long id;
    private String e164;
    private NumberStatus numberStatus;
    private NumberType numberType;
    private String description;
    private String owner;
    private LocalDateTime pendingRemovalDate;
    private Long platformId;
    private String platformName;
    private Long physicalSiteId;
    private String physicalSiteName;
 */
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
            return JSON.stringify(d);
        }
    },

    "columns": [
        {"data": "e164"},
        {"data": "numberType"},
        {"data": "numberStatus"},
        {"data": "owner"},
        {"data": "platformName"},
        {"data": "physicalSiteName"},
        {"data": "description"}
    ],
    "columnDefs": [
        {
            "orderable": false,
            "targets": 8,
            "data": "id",
            "render": function (data, type, row, meta) {
                let iconHtml = '<i class="text-info fas fa-edit px-2 fa-lg" title="Edit ' + row.e164 + '" onclick="editItem(' + data + ')"></i>';
                return iconHtml;
            }
        },
        {
            "orderable": true,
            "targets": 7,
            "data": "pendingRemovalDate",
            "render": function (data, type, row, meta) {
                if (data != null) 
                    return localDateTimeArrayToDDMMYYYY_HHMMSS(data, '-', ':', ' ');
                else
                    return '';
            }

        }
    ]
});


function getConfig() {
    let editPlatformList = $("#editPlatform");
    editPlatformList.empty();
    editPlatformList.append(new Option("--Select a Platform--", "null"));
    $('#editPlatform option[value="null"]').attr("disabled", true);

    let editSiteList = $("#editSite");
    editSiteList.empty();
    editSiteList.append(new Option("--Select a Site--", "null"));
    $('#editSite option[value="null"]').attr("disabled", true);
    
    let editNumberStatus = $("#editNumberStatus");
    editNumberStatus.empty();
    
    let editNumberType = $("#editNumberType");
    editNumberType.empty();
    
    $.ajax({
        url: ajaxBase + "config",
        method: "GET",
        contentType: "application/json",
        dataType: "json",
        success: function (responseData) {
            console.log(responseData);
            itemConfig = responseData;
            $.each(itemConfig.platforms, function (name, id) {
                editPlatformList.append(new Option(name, id));
            });
            $.each(itemConfig.physicalsites, function (name, id) {
                editSiteList.append(new Option(name, id));
            });
            $.each(itemConfig.numberstatus, function (enumName, friendlyName) {
                editNumberStatus.append(new Option(friendlyName, enumName));
            });
            $.each(itemConfig.numbertype, function (enumName, friendlyName) {
                editNumberType.append(new Option(friendlyName, enumName));
            });
        },
        error: function (error) {
            console.log("Error:", error);
            $('#jsErrorText').text("An error was encountered whilst retrieving the configuration parameters");
            $('#jsError').show();
        }
    });
}


function editItem(itemId) {

    $.ajax({
        url: ajaxObjectBase + itemId,
        method: "GET",
        contentType: "application/json",
        dataType: "json",
        success: function (responseItem) {
            console.log("Response:", responseItem);
            $('#modalTitle').text("Edit E164 Number (" + responseItem.e164 + ")");
            
            $('#editID').val(responseItem.id);
            $('#editE164Number').val(responseItem.e164);
            $('#editE164Number').prop("disabled", true);

            $('#editDescription').val(responseItem.description);
            $('#editOwner').val(responseItem.owner);
            
            $('#editPlatform option').attr("selected", false);
            if ($('#editPlatform option[value="' + responseItem.platformId + '"]').length > 0) {
                $('#editPlatform option[value="' + responseItem.platformId + '"]').attr("selected", true);
            }
            else {
                $('#editPlatform option[value="null"]').attr("selected", true);
            }

            $('#editSite option').attr("selected", false);
            if ($('#editSite option[value="' + responseItem.physicalSiteId + '"]').length > 0) {
                $('#editSite option[value="' + responseItem.physicalSiteId + '"]').attr("selected", true);
            }
            else {
                $('#editSite option[value="null"]').attr("selected", true);
            }

            $('#editNumberType option').attr("selected", false);
            if ($('#editNumberType option[value="' + responseItem.numberType + '"]').length > 0) {
                $('#editNumberType option[value="' + responseItem.numberType + '"]').attr("selected", true);
            }
            else {
                $('#editNumberType option[value="EXTENSION"]').attr("selected", true);
            }

            $('#editNumberStatus option').attr("selected", false);
            if ($('#editNumberStatus option[value="' + responseItem.numberStatus + '"]').length > 0) {
                $('#editNumberStatus option[value="' + responseItem.numberStatus + '"]').attr("selected", true);
            }
            else {
                $('#editNumberStatus option[value="UNALLOCATED"]').attr("selected", true);
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


function completeSave() {
    $("#btnSave").prop("disabled", true);
    if (!validate())
        return;
    $("#editForm").submit();
}


function validate() {

    $("#btnSave").prop("disabled", true);
    let valid = true;

    let editPlatform = $('#editPlatform').val();
    let editSite = $('#editSite').val();

    if (editPlatform === null || editPlatform === "" || editPlatform === "null") {
        valid = false;
        $("#editPlatform").addClass("is-invalid").removeClass("is-valid");
    } else {
        $("#editPlatform").removeClass("is-invalid").addClass("is-valid");
    }
    
    if (editSite === null || editSite === "" || editSite === "null") {
        valid = false;
        $("#editSite").addClass("is-invalid").removeClass("is-valid");
    } else {
        $("#editSite").removeClass("is-invalid").addClass("is-valid");
    }


    $("#btnSave").prop("disabled", !valid);
    return valid;

}


$('.dataTables_length').addClass('bs-select');

$.fn.dataTable.ext.errMode = function (settings, techNote, message) {
    $('#jsErrorText').text("An error was encountered whilst retrieving the list of E164 numbers. Table below may be incomplete.");
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
    let ajaxAction = "PUT";
    let ajaxUrl = ajaxObjectBase + objectId;
    
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
