/* 
 *   NumberRoutingManagement - jsCommon.js
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


//$(document).ready(function () {
//    window.setTimeout(function () {
//        $(".alert-dismissible").fadeTo(2000, 500).slideUp(500, function () {
//            console.log("dismissing alert");
//            $(".alert-dismissible").hide();
//        });
//    }, 10000);
//
//});

function getDateYYYYMMDD(dateToFormat, separator) {
    return dateToFormat.getFullYear() + separator + ("00" + (dateToFormat.getMonth() + 1)).slice(-2) + separator + ("00" + dateToFormat.getDate()).slice(-2);
}


function getDateDDMMYYYY(dateToFormat, separator) {
    return ("00" + dateToFormat.getDate()).slice(-2) + separator + ("00" + (dateToFormat.getMonth() + 1)).slice(-2) + separator + dateToFormat.getFullYear();
}

function dateDiff(startDate, daysDiff) {
    let newDate = new Date(startDate);
    newDate.setDate(newDate.getDate() + daysDiff);
    return newDate;
}

function getTimeHHMMSSFFF(timeToFormat) {
    let d = timeToFormat;
    let hh = ("00" + d.getHours()).slice(-2);
    let mm = ("00" + d.getMinutes()).slice(-2);
    let ss = ("00" + d.getSeconds()).slice(-2);
    let fff = ("000" + d.getMilliseconds()).slice(-3);
    return "" + hh + ":" + mm + ":" + ss + "." + fff + "";
}

function formToJson(jQueryFormObject) {
    let formArray = jQueryFormObject.serializeArray();
    let obj = {};
    $.each(formArray, function (i, pair) {
        let cObj = obj, pObj, cpName;
        $.each(pair.name.split("."), function (i, pName) {
            pObj = cObj;
            cpName = pName;
            cObj = cObj[pName] ? cObj[pName] : (cObj[pName] = {});
        });
        pObj[cpName] = pair.value;
    });
    return obj;
}

function zeroPad(stringToPad) {
    let strTemp = "00" + stringToPad;
    return strTemp.substring(strTemp.length - 2, strTemp.length);

}

function localDateTimeArrayToYYYYMMDD_HHMMSS(data, dateSeparator, timeSeparator, dateTimeSeparator) {
    let year = (data[0] === null ? 2021 : data[0]);
    let month = zeroPad((data[1] === null ? 1 : data[1]));
    let day = zeroPad((data[2] === null ? 1 : data[2]));
    let hour = zeroPad((data[3] === null ? 0 : data[3]));
    let minute = zeroPad((data[4] === null ? 0 : data[4]));
    let second = zeroPad((data[5] === null ? 0 : data[5]));
    return year + dateSeparator + month + dateSeparator + day + dateTimeSeparator + hour + timeSeparator + minute + timeSeparator + second;
}

function localDateTimeArrayToDDMMYYYY_HHMMSS(data, dateSeparator, timeSeparator, dateTimeSeparator) {
    monthOption = {month: "short"};


    let year = (data[0] === null ? 2021 : data[0]);
    let tempMonth = zeroPad((data[1] === null ? 1 : data[1]));
    let day = zeroPad((data[2] === null ? 1 : data[2]));
    let hour = zeroPad((data[3] === null ? 0 : data[3]));
    let minute = zeroPad((data[4] === null ? 0 : data[4]));
    let second = zeroPad((data[5] === null ? 0 : data[5]));
    
    let d = new Date(year, tempMonth - 1, day, hour, minute, second, 0);
    let month = d.toLocaleDateString('en-au', monthOption);
    return day + dateSeparator + month + dateSeparator + year + dateTimeSeparator + hour + timeSeparator + minute + timeSeparator + second;
}

function toHHMMSS(value) {
    let sec_num = value; // don't forget the second param
    let hours   = Math.floor(sec_num / 3600);
    let minutes = Math.floor((sec_num - (hours * 3600)) / 60);
    let seconds = sec_num - (hours * 3600) - (minutes * 60);
 
    if (hours   < 10) {hours   = "0"+hours;}
    if (minutes < 10) {minutes = "0"+minutes;}
    if (seconds < 10) {seconds = "0"+seconds;}
    return hours+':'+minutes+':'+seconds;
}
