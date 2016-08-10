$(document).ready(function() {
    $("#sale-name").blur(function() {
        var saleName = $(this).val();
        if (saleName === "") {
            $(".alert-fake")
                    .attr("class", "alert alert-fake alert-danger")
                    .css("display", "block")
                    .text("Sale name cannot be empty");
            setTimeout(function() {
                location.reload(true);
            }, 1500);
        }
        else {
            $.ajax({
                url: "/changesalename",
                type: "POST",
                contentType: "application/json",
                dataType: "text",
                data: JSON.stringify({
                    saleName: saleName,
                    saleID: saleID,
                }),
            })
            .done(function(message) {
            $(".alert-fake")
                    .attr("class", "alert alert-fake alert-success")
                    .css("display", "block")
                    .text(message);
            });
            setTimeout(function() {
                location.reload(true);
            }, 1500);
        }
    });

    $(".update-btn").click(function() {
        var $roles = $(this).parent().parent();
        var updatedRoles = {
            rolesID: $roles.attr("id"),
            saleID: saleID,
            username: $roles.find(".username").text(),
            seller: $roles.find(".seller > input").prop("checked"),
            clerk: $roles.find(".clerk > input").prop("checked"),
            cashier: $roles.find(".cashier > input").prop("checked"),
            bookkeeper: $roles.find(".bookkeeper > input").prop("checked"),
            guest: $roles.find(".guest > input").prop("checked"),
        };

        $.ajax({
            url: "/updateroles",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify(updatedRoles),
        })
        .done(function(message) {
            $(".alert-fake")
                    .attr("class", "alert alert-fake alert-success")
                    .css("display", "block")
                    .text(message);
            setTimeout(function() {
                location.reload(true);
            }, 1500);
        });
    });

    $("#search-button").click(function() {
        var addedUser = {
            username: $("#username-input").val(),
            saleID: saleID,
        };

        $.ajax({
            url: "/adduser",
            type: "POST",
            contentType: "application/json",
            dataType: "json",
            data: JSON.stringify(addedUser),
        })
        .done(function(roles) {
            if (roles.id === -1) {
                $(".alert-fake")
                        .attr("class", "alert alert-fake alert-danger")
                        .css("display", "block")
                        .text('User "' + addedUser.username
                                + '" does not exist');
                setTimeout(function() {
                    location.reload(true);
                }, 1500);
            }
            else
                location.reload(true);
        });
    });
});
