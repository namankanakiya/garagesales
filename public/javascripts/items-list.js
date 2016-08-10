$(document).ready(function() {
    var $rows = $("tr");
    $("#search").keyup(function() {
        var trimmedInput = $.trim(this.value).toLowerCase();
        if (trimmedInput === "") {
            $rows.show();
        } else {
            $rows.hide();
            $("#heading").show();
            $rows.filter(function() {
                if ($(this).find($(".itemName")).text()
                        .toLowerCase().indexOf(trimmedInput) != -1) {
                    return true;
                }
                if ($(this).find($(".seller")).text()
                        .toLowerCase().indexOf(trimmedInput) != -1) {
                    return true;
                }
            }).show();
        }
    });

    $(".bid-start").click(function(e) {
        e.preventDefault();

        var $bid = $(this).parent();
        if (username === $bid.attr("class")) {
            $(".alert-fake")
                    .attr("class", "alert alert-fake alert-danger")
                    .css("display", "block")
                    .text("You cannot bid on your own item");
            return;
        }
        var $bidInfo = $bid.find(".bid-info");
        $bidInfo.css("display", "block");

        $($bid.find(".bid-btn")).click(function() {
            $.ajax({
                url: "/bid",
                type: "POST",
                contentType: "application/json",
                dataType: "json",
                data: JSON.stringify({
                    amount: $bidInfo.find(".bid-amount").val(),
                    itemID: $bid.attr("id"),
                }),
            })
            .done(function(updated) {
                if (updated.success === "no")
                    $(".alert-fake")
                            .attr("class", "alert alert-fake alert-danger")
                            .css("display", "block")
                            .text(updated.message);
                else {
                    $(".alert-fake")
                            .attr("class", "alert alert-fake alert-success")
                            .css("display", "block")
                            .text(updated.message);
                    setTimeout(function() {
                        location.reload(true);
                    }, 1500);
                }
            });
        });
    });
});
