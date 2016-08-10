$(document).ready(function() {
    $(".removeBtn").on("click", function() {
        var itemID = $(this).attr("id");
        $(this).closest(".dynamicRow").remove();
        $.ajax({
            url: "/updatetotal",
            type: "POST",
            contentType: "text/plain",
            dataType: "text",
            data: itemID
        })
        .done(function(total) {
            $("#dynamic-total").text("Your subtotal is: " + total);
        }); // add .fail later
    });
});
