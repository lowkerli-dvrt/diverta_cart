$(document).ready(function() {
    $('[id^=btn_addProduct_]').click(function(evt) {
        evt.preventDefault();
        var sku = evt.currentTarget.id.substring(15);
        var button = $(this);
        var original = $("<i>").addClass("fas fa-plus");
        var spinner = $("<i>").addClass("fas fa-spinner fa-spin");
        $.ajax({
            url: "ajax/cart",
            method: "PUT",
            data: {
                'sku': sku
            },
            dataType: "JSON",
            beforeSend: function() {
                button.addClass("disabled").html(spinner);
            }
        }).done(function(res) {
            updateCartTotalsNavbar();
            toastr["success"](
                "Your product was added to the cart. You can continue shopping or move" +
                    " to the cart page to continue to the payment",
                "Product added!"
            )
        }).fail(function(err) {
            toastr["error"](
                "Seems there was an error adding your product to the cart... Maybe try again?",
                "Error on adding product"
            );
        }).always(function() {
            setTimeout(function() {
                button.removeClass("disabled").html(original);
            }, 1000);
        });
    });
});