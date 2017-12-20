var stompClient = null;
var goodCount = 0;
var badCount = 0;
var unreadableCount = 0;
var wrongCount = 0;
var duplicateCount = 0;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $(".btn-start-inspection").removeClass("disabled")
        $("#conversation").show();
        $(".fa-plug").css("color", "#00FF00");
    } else {
        $("#conversation").hide();
        $(".fa-plug").css("color", "grey");

    }
    $("#greetings").html("");
}


function connect() {
    resetCounter()
    var socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient
        .connect({},
            function(frame) {
                setConnected(true);
                console.log('Connected: ' + frame);
                stompClient.subscribe('/topic/greetings', function(
                    greeting) {
                    showGreeting(JSON.parse(greeting.body));
                });

                stompClient
                    .subscribe(
                        '/topic/input',
                        function(greeting) {

                            var body = JSON
                                .parse(greeting.body);
                            var value = body.epc;
                            var tid = body.tid;
                            if ($("#badTags input[value='" +
                                    value + "']")[0]) {
                                console.log('already exists!');
                            } else {
                                $("#badTags")
                                    .append(
                                        '<div class="row"><div class="col-md-6"><input disabled name="badTag[]" class="form-control epc-input" value="' +
                                        value +
                                        '"/></div><div class="col-md-6"><span>' +
                                        tid +
                                        '</span></div></div>');

                                var lastCount = parseInt($(
                                    "#detect-count").text());

                                $("#detect-count").text(
                                    ++lastCount);
                            }

                        });

                stompClient
                    .subscribe(
                        '/topic/detected',
                        function(resp) {
                            $("#passCount")
                                .append(
                                    "<tr><td><button></button></td></tr>");
                            $("#pass-indicator").css("color",
                                "red");
                            setTimeout(function() {
                                $("#pass-indicator").css(
                                    "color", "grey");
                            }, 1000)
                        });


                stompClient.subscribe('/topic/add', function(resp) {
                    $("#readin-indicator-add").addClass("green").removeClass("grey");

                    var tag = JSON.parse(resp.body);

                    if (status == 'GOOD') {
                        toAdd.addClass('GOODIMPT');
                    }

                    toAdd.find("td:nth-child(3)").text(tag.status);
                    toAdd.find("td:nth-child(4)").text(tag.tid);
                    toAdd.find("td:nth-child(5)").text(tag.epc);
                    
                    $('.modal-add').modal("hide");
                });

                stompClient.subscribe('/topic/reenter', function(resp) {
                    var epcs = JSON.parse(resp.body).epcs;
                    var reenterRow = $(".working");

                    if (epcs.length == 1) {
                        var epctid = epcs[0].split(",");
                        var epc = epctid[0];
                        var tid = epctid[1];
                        var reenterRow = $(".working");
                        var rowId = reenterRow.data("id");
                        $.post("/updateEmptyRead", {
                                rowId: rowId,
                                epc: epc,
                                tid: tid
                            })
                            .done(function() {

                                reenterRow.removeClass("working");
                                $("#readin-indicator").addClass("grey").removeClass("green");
                                $("#ctn-reenter input").prop("checked", "checked");

                                $('.modal-emtpy').modal("hide");
                                reenterRow.find("td:nth-child(4)").text(tid);
                                reenterRow.find("td:nth-child(5)").text(epc);
                            });

                    } else if (epcs.length > 1) {
                        var ctnEl = $("#ctn-reenter");

                        for (i in epcs) {
                            var myepc = epcs[i];
                            ctnEl.append("<div class='epc' onclick='reenter(\"" + myepc + "\")'>" + myepc + "</div>");
                        }
                    } 

                });

            },
            function(error) {
                setConnected(false);
            });
}

var firstTime = 0;

function connectWithReader2() {
    var socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {
        setConnected(true);
        console.log('Connected: ' + frame);

        stompClient.subscribe('/topic/input', function(greeting) {
            showGreeting(JSON.parse(greeting.body));
        });

    });
}

function disconnect() {
    if (stompClient != null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
    resetCounter()
}

function clearInput() {

    $("input").val("");
}

function showGreeting(resp) {
    var message = resp.tagInfo
    var order = resp.order;
    var stateClass = resp.state



    if (resp.state == 'DUPLICATE') {
        $("td").filter(function() {
            return $(this).text() === message.epc;
        }).parent().addClass("wrong");
        duplicateCount++;
        resp.state = "EPC重复(Duplicate)"
    }

    if (resp.state == "multiple-read-exception") {
        resp.state = "读到多片(" + resp.tagInfos.length + ")"
        //resp.state = "过白纸";
        stateClass = "multiple";
    }

    if (resp.isStrikeout) {
        var lastRow = $("tr").last();
        lastRow.find("td:nth-child(3)").text("黑线（Stripped out）");
        unreadableCount++;
    }


    if (resp.isLastPassWrong) {
        var lastRow = $("tr").last();
        lastRow.removeClass("GOOD").addClass("wrong");
        lastRow.find("td:nth-child(3)").text("EPC错误（wrong）");
        lastRow.find("td:nth-child(2)").text("0");
        //alert("abnormal header");
        resp.state = "GOOD";
        wrongCount++;
    }

    if (resp.wrongIndex != 0) {

        if (resp.wrongIndex == 1) {
            var lastRow = $("#greetings tr").last();
            var targetRow = lastRow.prev();
            targetRow.removeClass("GOOD").addClass("wrong");
            targetRow.find("td:nth-child(3)").text("EPC错误（wrong）");
            targetRow.find("td:nth-child(2)").text("0");

        }

        /*lastRow.find("td:nth-child(3)").text("wrong");
        lastRow.find("td:nth-child(2)").text("0");
        lastRow.addClass("wrong").removeClass("GOOD");*/
        wrongCount++;
    }




    if (resp.state == 'ABNORMAL_HEADER_LAST') {
        ++badCount
        resp.state = "ABNORMAL_HEADER";
        stateClass = "ABNORMAL_HEADER_TARGET";
    }

    if (resp.state == 'BLANK') {
        resp.state = "不可读（Can not read）";
        unreadableCount++;
    }

    var skuColor = "";

    if (resp.state == 'GOOD') {
        $("#goodCount").html(++goodCount);
        skuColor = resp.sku % 2 == 0 ? "even" : "odd"
    }

    var typeClass = "normal";
    if (resp.isLeading) {
        typeClass = "leading";
        stateClass = "LEADING";
        resp.state = "过白纸"
    }



    var tbody = $("#greetings");
    var endFlag = "";
    if (resp.isEndOfOrder && !resp.correctiveSku) {

        /*$("#greetings tr:not('.end-of-order'):not('.wrong')").attr("class",
        	"completed");
        $("#greetings tr.end-of-order").attr("class", "completed end-of-order");*/
        endFlag += " end-of-order"

        $("#greetings tr")
            .removeClass("odd")
            .removeClass("even")
            .addClass("completed");


        $("#goodCount").html("1");
        $("#badCount").html("0");
        goodCount = 1;
        badCount = 0;

        $(
            "#production-list .sbmt-order-number:contains('" +
            resp.order.orderNum + "')").parent().prev().addClass(
            "order-completed");
    }

    if (resp.correctiveSku) {
        var lastRow = $("tr").last();
        lastRow.find("td:nth-child(2)").text(0);
        lastRow.find("td:nth-child(3)").text("wrong");

        lastRow.removeClass("GOOD");
        lastRow.addClass("wrong");
        wrongCount++;

        if (!resp.lastPassEndOfOrder) {
            $("tr.end-of-order").last().removeClass('end-of-order');
            $("." + resp.order.orderNum).addClass(skuColor);
        }

    }
    var rowContent = "<td style='width:10%'>" + resp.passCount + "</td>" +
        "<td style='width:10%'>" + resp.sku + "</td>" + "<td style='width:12%'>" + resp.state + "</td>" +
        "<td style='width:29%'>" + message.tid + "</td>" + "<td style='width:29%'>" + message.epc + "</td>" +
        "<td style='width:20%'>" + resp.timeStr + "</td>";

    var data = "data-id='" + resp.rowId + "'";
    var orderClass = resp.order.orderNum;
    var classes = [];
    classes.push(stateClass);
    classes.push(endFlag);
    classes.push(typeClass);
    classes.push(skuColor);
    classes.push(orderClass);

    if (resp.isMissPass) {
        var i = parseInt(resp.diff) - 1;
        for (; i > 0; i--) {
            var myepc = message.epc;
            var len = myepc.length - 8;
            var before = myepc.substr(0, len);
            var last8 = myepc.substr(len);
            var last8last = (parseInt(last8, 16) - i).toString(16)
            var final = before + last8last;
            tbody.append("<tr data-id=" + final + " class='missing' onclick='startAdd(this)'><td style='width:10%'></td><td style='width:10%'></td><td style='width:12%'></td><td style='width:29%'></td><td style='width:29%'></td><td style='width:20%'></td></tr>");
        }
    }
    var classStr = classes.join(' ');
    var row = "<tr class='" + classStr + "' " + data + ">" + rowContent + "</tr>";

    tbody.append(row);

    if (!resp.isStrikeout && resp.needConfirm) {
        $(".BLANK").last().click(function() {
            $(this).addClass("working");
            $('.modal-emtpy').modal({
                show: true
            })


        });
    }

    if (resp.tagInfos != undefined && resp.tagInfos.length > 1) {

        var lastRow = $("tr").last();
        lastRow.find("td:nth-child(3)").text("GOOD(读到多片 " + resp.tagInfos.length + ")");
        $(".multiple").click(function() {

            var content = "<ul>";
            var tags = resp.tagInfos;
            for (i in tags) {
                content += "<li>" + tags[i].epc + "</li>"
            }
            content += "</ul>";

            $('.modal-multiple .modal-content').html(content);
            $('.modal-multiple').modal({
                show: true
            })


        });
    }

    var orderStatistic = resp.order
    unreadableCount = orderStatistic.unreadableCount;
    duplicateCount = orderStatistic.duplicateCount
    wrongCount = orderStatistic.wrongCount;
    var leadingCount = orderStatistic.leadingCount;
    var badCnt = unreadableCount + duplicateCount + wrongCount;
    $("#unreadableCount").html(unreadableCount);
    $("#duplicateCount").html(duplicateCount);
    $("#wrongCount").html(wrongCount);
    $("#leadingCount").html(leadingCount);
    $("#wrongTotal").html(badCnt);



    var pieces = orderStatistic.piecesCount
    var totalCnt = orderStatistic.goodCount + leadingCount + badCnt
    $("#totalCount").html(totalCnt);
    $("#goodCount").html(orderStatistic.goodCount);

    $("#count").html(totalCnt);
    $("#trigger-count").html(totalCnt)
    $("#read-count").html(totalCnt)


    var dataCtn = $("#greetings");
    dataCtn.scrollTop(dataCtn[0].scrollHeight);
}

/**
 * token: description: example: #YYYY# 4-digit year 1999 #YY# 2-digit year 99
 * #MMMM# full month name February #MMM# 3-letter month name Feb #MM# 2-digit
 * month number 02 #M# month number 2 #DDDD# full weekday name Wednesday #DDD#
 * 3-letter weekday name Wed #DD# 2-digit day number 09 #D# day number 9 #th#
 * day ordinal suffix nd #hhhh# 2-digit 24-based hour 17 #hhh# military/24-based
 * hour 17 #hh# 2-digit hour 05 #h# hour 5 #mm# 2-digit minute 07 #m# minute 7
 * #ss# 2-digit second 09 #s# second 9 #ampm# "am" or "pm" pm #AMPM# "AM" or
 * "PM" PM
 */
Date.prototype.customFormat = function(formatString) {
    var YYYY, YY, MMMM, MMM, MM, M, DDDD, DDD, DD, D, hhhh, hhh, hh, h, mm, m, ss, s, ampm, AMPM, dMod, th;
    YY = ((YYYY = this.getFullYear()) + "").slice(-2);
    MM = (M = this.getMonth() + 1) < 10 ? ('0' + M) : M;
    MMM = (MMMM = ["January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        ][M - 1])
        .substring(0, 3);
    DD = (D = this.getDate()) < 10 ? ('0' + D) : D;
    DDD = (DDDD = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday",
        "Friday", "Saturday"
    ][this.getDay()]).substring(0, 3);
    th = (D >= 10 && D <= 20) ? 'th' : ((dMod = D % 10) == 1) ? 'st' :
        (dMod == 2) ? 'nd' : (dMod == 3) ? 'rd' : 'th';
    formatString = formatString.replace("#YYYY#", YYYY).replace("#YY#", YY)
        .replace("#MMMM#", MMMM).replace("#MMM#", MMM).replace("#MM#", MM)
        .replace("#M#", M).replace("#DDDD#", DDDD).replace("#DDD#", DDD)
        .replace("#DD#", DD).replace("#D#", D).replace("#th#", th);
    h = (hhh = this.getHours());
    if (h == 0)
        h = 24;
    if (h > 12)
        h -= 12;
    hh = h < 10 ? ('0' + h) : h;
    hhhh = hhh < 10 ? ('0' + hhh) : hhh;
    AMPM = (ampm = hhh < 12 ? 'am' : 'pm').toUpperCase();
    mm = (m = this.getMinutes()) < 10 ? ('0' + m) : m;
    ss = (s = this.getSeconds()) < 10 ? ('0' + s) : s;
    return formatString.replace("#hhhh#", hhhh).replace("#hhh#", hhh).replace(
            "#hh#", hh).replace("#h#", h).replace("#mm#", mm).replace("#m#", m)
        .replace("#ss#", ss).replace("#s#", s).replace("#ampm#", ampm)
        .replace("#AMPM#", AMPM);
};

var d = new Date();
$("#curTime").text(d.customFormat("#DD#/#MM#/#YYYY# #hh#:#mm#:#ss#"));
$(function() {
    $("form").on('submit', function(e) {
        e.preventDefault();
    });
    $("#connect").click(function() {
        connect();
    });
    $("#disconnect").click(function() {
        disconnect();
    });
    $("#btn-submit-order").click(function() {
        appendOrder();
    });

    $(".order-info input[name='orderNumber']").on(
        'blur',
        function() {
            var orderInputEl = $(this);
            var orderNum = orderInputEl.val();
            var orderType = orderInputEl.parent().prev().find("select").val();

            if (orderNum.trim().length > 0) {

                $.get("/getSkuCountByOrderNum", {
                    orderNum: orderNum,
                    orderType: orderType
                }).done(
                    function(resp) {

                        var val = resp;
                        if (resp < 0) {
                            if (resp == -1) {
                                val = 1;
                                var parent = orderInputEl.parent().parent();
                                parent.find("div").removeClass("hidden").addClass("show");
                            } else {
                                val = "";
                                orderInputEl.parent().next().find(
                                    "input[name='skuCount']").val(val).prop("disabled", false);
                            }

                        } else {
                            orderInputEl.parent().next().find(
                                "input[name='skuCount']").val(val).prop("disabled", true);
                        }

                    })

            }
        })

    /* $(".order-info select[name='orderType']").on(
	    'change',
	    function() {
		var orderInputEl = $(this);
		var orderNum = orderInputEl.parent().next().find("input").val();
		if(orderNum.trim().length>0){
		    var orderType = orderInputEl.val();
			$.get("/getSkuCountByOrderNum",{orderNum:orderNum, orderType:orderType}).done(
				function(resp) {
				    var val = resp;
				    if(resp<0){
					if(resp == -1){
					    val = 1;
					}
					var parent = orderInputEl.parent().parent();
					 parent.find("div").removeClass("hidden").addClass("show");
				    }
				   
				    orderInputEl.parent().next().find(
				    "input[name='skuCount']").val(val).prop("disabled", true);
				})
		}
		
    })*/

    /*
     * live( ".epc-input", 'keyup', function(e) { if (e.keyCode == 13) { $(this)
     * .parent() .append( '<input name="badTag[]" class="form-control
     * epc-input" />'); $(this).next().focus(); }
     * 
     * });
     */

    $.get("/getConfigNames", function(names) {
        var configEle = $("#configName");
        names.forEach(function(e, i) {
            configEle.append("<option value='" + e.replace(".properties", "") +
                "'>" + e.replace(".properties", "") + "</option>");
        })

    });
});

// matches polyfill
this.Element &&
    function(ElementPrototype) {
        ElementPrototype.matches = ElementPrototype.matches ||
            ElementPrototype.matchesSelector ||
            ElementPrototype.webkitMatchesSelector ||
            ElementPrototype.msMatchesSelector ||
            function(selector) {
                var node = this,
                    nodes = (node.parentNode || node.document)
                    .querySelectorAll(selector),
                    i = -1;
                while (nodes[++i] && nodes[i] != node)
                ;
                return !!nodes[i];
            }
    }(Element.prototype);

// helper for enabling IE 8 event bindings
function addEvent(el, type, handler) {
    if (el.attachEvent)
        el.attachEvent('on' + type, handler);
    else
        el.addEventListener(type, handler);
}

// live binding helper using matchesSelector
function live(selector, event, callback, context) {
    addEvent(context || document, event, function(e) {
        var found, el = e.target || e.srcElement;
        while (el && el.matches && el !== context &&
            !(found = el.matches(selector)))
            el = el.parentElement;
        if (found)
            callback.call(el, e);
    });
}

function navigate() {
    $("#offline-qc").show();
    $("#main-content").hide();

}

function startAutomaticReading() {
    // stompClient.send("/app/startAutomaticReading");

    $.post("/startAutomaticReading?receiveQueueName=input").done(function(resp) {

        if (resp === "ok") {
            $("#btn-start-read").attr("disabled", "disabled");
            $("#btn-stop-read").removeAttr("disabled");
        } else {
            alert("Something wrong when starting the reader!");
        }
    }).fail(function(a, b, c) {
        alert("Something wrong when starting the reader!");
    })
}

function stopAutomaticReading() {
    // stompClient.send("/app/startAutomaticReading");

    $.post("/stopAutomaticReading").done(function(resp) {

        if (resp === "ok") {
            $("#detect-count").text("0");
            $("#btn-start-read").removeAttr("disabled");
            $("#btn-stop-read").attr("disabled", "disabled");
        } else {
            alert("Something wrong when stopping the reader!");
        }
    }).fail(function(a, b, c) {
        alert("Something wrong when stopping the reader!");
    })
}

function toMain() {
    $("#main-content").show();
    $("#offline-qc").hide();

}

function submit() {
    var orderNumber = $("#orderNumToSave").val();

    $("#filepath").html("");
    var inputs = $("#badTags input");
    var fo = [];
    inputs.each(function() {
        var thisInput = $(this);
        var val = thisInput.val();

        if (val.trim().length > 0) {
            fo.push(val)
            thisInput.val("");
        }

    });

    if (orderNumber.trim() == "") {
        alert("Order number can not be null!");
    }

    $
        .post("/save", {
            badTag: fo,
            orderNumber: orderNumber
        })
        .done(
            function(resp, b, c) {

                $("#badTags").html("");
                $("#detect-count").text("0");
                $("#filepath")
                    .html(
                        "<i class='fa fa-check' style='color:green'></i><a href='" +
                        resp.filepath +
                        "'>" +
                        resp.filepath +
                        "</a><br/><i class='fa fa-check' style='color:green'></i><a href='" +
                        resp.inspect_data_filepath +
                        "'>" +
                        resp.inspect_data_filepath +
                        "</a>");
            }).fail(function(a, b, c) {
            console.log(a);
            console.log(b);
            console.log(c);

        });

}

function resetCounter() {
    $("#trigger-count").html("0");
    $("#read-count").html("0");

    $("#count").html("0");
    $("#goodCount").html("0");
    $("#badCount").html("0");
    $("#leadingCount").html("0");
    $("#totalCount").html("0");
    goodCount = badCount = 0;
}

function startInspection() {
    resetCounter()

    $("#passCount").html("0");
    $("#greetings").html("");
    $("#production-list").html("");
    var configName = $("#configName").val();

    var data = {
        configName: configName,
        orders: getOrdersToInspect()
    };
    console.log(data);
    $.ajax({
        url: "/startInspection",
        type: "POST",
        contentType: "application/json",
        dataType: "json",
        data: JSON.stringify(data)
    }).done(function(resp, statusCode, xhrObj) {
        $("select[name='orderType']").prop("selectedIndex", 0);
        $(".order-info").find("input:not('.btn')").val("");
        $("#working-indicator").css("color", "#00FF00");
        $(".btn-start-inspection").addClass("disabled")
        $(".btn-end-inspection").removeClass("disabled")
    }).fail(function(errObj, statusCode, message) {
        alert("Something wrong had happened when connecting to the reader!");
    });
}

function appendOrder() {
	var me = $(".row.order-info").first(); me.before(me.clone());
    /*var data = {
        configName: "",
        orders: getOrdersToInspect()
    };
    $.ajax({
        url: "/appendOrder",
        type: "POST",
        contentType: "application/json",
        dataType: "json",
        data: JSON.stringify(data)
    }).done(function(resp, statusCode, xhrObj) {
        alert("append order ok");
    }).fail(function(errObj, statusCode, message) {
        alert("sth wrong appending order!");
        console.log(errObj);
        console.log(statusCode);
        console.log(message);
    });*/
}

function stopInspection() {
    resetCounter();
    unreadableCount = 0;
    wrongCount = 0;
    duplicateCount = 0;
    badCount = 0;

    $("#unreadableCount").html(unreadableCount);
    $("#duplicateCount").html(duplicateCount);
    $("#wrongCount").html(wrongCount);
    $("#wrongTotal").html(unreadableCount + duplicateCount + wrongCount);


    $("#production-list").html("");
    $("#greetings").html("");
    $.post("/stopInspection").done(function(a, b, c) {
        $(".fa-circle").css("color", "grey");
        $(".btn-end-inspection").addClass("disabled")
        $(".btn-start-inspection").removeClass("disabled")
    }).fail(function(a, b, c) {
        console.log(a);
        console.log(b);
        console.log(c);

    });
}

function getOrdersToInspect() {
    var resl = [];
    $(".order-info")
        .each(
            function(i, e) {
                var orderNumber = $(e).find("input")[0].value;
                var skuCount = $(e).find("input")[1].value;
                var codeType = $(e).find("select")[0].value;

                if (orderNumber.trim() && skuCount.trim()) {
                    resl.push({
                        orderNum: orderNumber,
                        skuCount: skuCount,
                        codeType: codeType
                    });
                    $("#production-list")
                        .append(
                            "<div class='col-sm-6 sbmt-order'><span class='sbmt-order-number'>" +
                            orderNumber +
                            "</span><span class='col-sm-6 sbmt-count fa fa-tags'>SKU COUNT(<span>" +
                            skuCount +
                            "</span>)</span></div>");
                }

            })

    return resl;
}

function onCheckOne(me) {
    var parent = $(me).parent();
    if (me.checked) {
        parent.removeClass("show").addClass("hidden");
    } else {
        parent.prev().removeAttr("disabled");
        parent.remove();
    }
}

function readInEPC(me) {
    if (me.checked) {

    } else {
        $.post("/startAutomaticReading?receiveQueueName=reenter").done(function() {
            $("#readin-indicator").addClass("green").removeClass("grey");
        });
    }
}

function reenter(epcTidStr) {


    var reenterRow = $(".working");
    var rowId = reenterRow.data("id");
    var epctid = epcTidStr.split(",");
    var epc = epctid[0];
    var tid = epctid[1];
    reenterRow.find("td:nth-child(3)").text("GOOD");
    reenterRow.find("td:nth-child(4)").text(tid);
    reenterRow.find("td:nth-child(5)").text(epc);


    $("#ctn-reenter")
    $.post("/updateEmptyRead", {
            rowId: rowId,
            epc: epc,
            tid: tid
        })
        .done(function() {
            reenterRow.removeClass("working");
            $("#readin-indicator").addClass("grey").removeClass("green");
            $("#ctn-reenter input").prop("checked", "checked");

            $('.modal-emtpy').modal("hide");
        });

}

var toAdd;

function startAdd(me) {
    toAdd = $(me);

    $('.modal-add').modal("show");
    $.post("/startAutomaticReading?receiveQueueName=add").done(function(resp) {
        $("#readin-indicator-add").addClass("green").removeClass("grey");
    });
}