/**
 * For Cashier Page's validation of ID and Username
 */
$(document).ready(function () {
    $('#item').hide();
    $('#itemDone').hide();
    $('#payment').hide();
    $('#final').hide();

    $('#usernamebtn').click(function (e) {
        e.preventDefault();
        $.ajax({
            type: "GET",
            dataType: "text",
            url: "/cashier/validateUser",
            data: {username: $('#username2').val()}
        })
            .done(function (responseText) {
                if (responseText === "true") {
                    $('#message').html("Error! Username was not found.");
                } else if (responseText == "own") {
                    $('#message').html("Error! Cannot enter a transaction for yourself.");
                } else {
                    $('#message').html(responseText + " was added! You may add items.");
                    $('#customerName').html(responseText);
                    $('#buyer').hide();
                    $('#item').show();
                }
            });
    });

    $('#itembtn').click(function (e) {
        e.preventDefault();
        $('#message').html($('#itemBox').val())
        ;
        $.ajax({
            type: "GET",
            dataType: "text",
            url: "/cashier/validateItem",
            data: {itemID: $('#itemBox').val()}
        })
            .done(function (responseText) {
                if (responseText === "true") {
                    $('#message').html("Error! Item does not exist.");
                } else if (responseText == "duplicate") {
                    $('#message').html("Error! Item already in cart.");
                } else if (responseText === "sold") {
                    $('#message').html("Error! Item has been purchased.");
                } else if (responseText === "seller") {
                    $('#message').html("Error! That item is not in cashier's sale.");
                }else {
                    $('#message').html("Success! You may add another or complete transaction.");
                    $('#subtotalCash').html(responseText);
                    $('#itemDone').show();
                }
            });
    });

    $('#itemDone').click(function (e) {
        e.preventDefault();
        $('#item').hide();
        $('#itemDone').hide();
        $('#payment').show(1000);
    });

    $('#paymentbtn').click(function (e) {
        e.preventDefault();
        $.ajax({
            type: "GET",
            dataType: "text",
            url: "/cashier/payment",
            data: {payment: $('#paymentmethod').val()}
        })
            .done(function (responseText) {
                    $('#payment').hide();
                    $('#message').html("Payment was added!");
                    $('#final').show();
                    $('#methodpayment').html(responseText);
                }
            )
    });

});