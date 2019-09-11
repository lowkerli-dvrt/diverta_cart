var shipping_data = {};
var cart_data = {};

// Navbar link resume
var $spanCartTotals = $("span#cartTotals");

// Cart div layers
var $cart_wrapper = $("#cart_wrapper");
var $cart_empty = $("#cart_empty");
var $cart_not_empty = $("#cart_not_empty");
var $cart_contents_table = $("#cart_contents_table");

// Total table
var $subtotal = $("#subtotal");
var $tax = $("#tax");
var $shipping = $("#shipping");
var $total = $("#total");

// Modal "product remove"
var $modalProductName = $("#modalProductName");
var $modalProductSKU = $("#modalProductSKU");
var $removeProductModal = $("#removeProductModal");
var $btnModal_removeConfirm = $("#btnModal_removeConfirm");

// Modal "empty cart"
var $btn_emptyCart = $("#btn_emptyCart");
var $emptyCartModal = $("#emptyCartModal");
var $btnModal_emptyConfirm = $("#btnModal_emptyConfirm");

// Checkout form
var $checkoutForm = $("#checkoutForm");
var $checkout_tt = $("#checkout_tt");
var $checkout_region = $("#checkout_region");

function refreshSubtotals(_st, _tx, _sh) {
    var _tt = _st + _tx + _sh;
    $subtotal.html(accounting.formatMoney(_st));
    $tax.html(accounting.formatMoney(_tx));
    $shipping.html(accounting.formatMoney(_sh));
    $total.html(accounting.formatMoney(_tt));
    $checkout_tt.val(_tt);
}

function reloadView() {
    $.ajax({
        method: "GET",
        url: "ajax/cart",
        dataType: "JSON",
        beforeSend: function() {
            $cart_wrapper.waitMe({});
        },
        success: function(data) {
            cart_data = data["cart"];
            console.log(cart_data);
            updateView(cart_data);
        },
        always: function() {
            setTimeout(function() {
                $cart_wrapper.waitMe('hide');
            }, 1000);
        }
    });
}

function updateView(cart_data) {
    //Clear table contents
    $cart_contents_table.empty();

    //Set the view to whether we have items or not
    if (cart_data["numElements"] > 0) {
        // If we do have elements

        // Process each product, generating the corresponding table row and adding
        // it to the table
        $(cart_data["contents"]).each(function(_, pr) {
            var tr = $("<tr>");
            var small = $("<small>")
                .addClass(["d-block", "text-muted"])
                .text("SKU " + pr["sku"])
                .prop("outerHTML");
            var input = $("<input>")
                .attr({
                    id: "qtySpinner_" + pr["sku"],
                    type: "number",
                    value: pr["quantity"],
                    min: 1,
                    max: 100
                })
                .addClass(["form-control", "form-control-sm", "text-right"])
                .focusout(function(evt) {
                    //Listener when spinner loses focus (updates cart)
                    evt.preventDefault();
                    var sku = evt.currentTarget.id.substring(11);
                    var quant = $(evt.currentTarget).prop("value");
                    $.ajax({
                        url: "ajax/cart",
                        method: "PATCH",
                        data: {
                            "sku": sku,
                            "qty": quant
                        },
                        dataType: "JSON",
                        beforeSend: function() {
                            $cart_wrapper.waitMe({});
                        },
                        success: function(res) {
                            reloadView();
                            toastr["success"](
                                "The product quantity was updated successfully",
                                "Product quantity modified"
                            );
                        },
                        error: function(err) {
                            console.log(err);
                            toastr["error"](
                                "The product quantity could not be modified (maybe a connection error?)",
                                "Error in modifying quantity"
                            );
                        },
                        always: function() {
                            setTimeout(function() {
                                $cart_wrapper.waitMe('hide');
                            }, 1000);
                        }
                    });
                });

            var td_array = [
                $("<td>")
                    .addClass(["align-middle"])
                    .attr("width", "50%")
                    .html(pr["name"] + small),
                $("<td>")
                    .addClass(["align-middle", "text-right"])
                    .attr("width", "20%")
                    .html(accounting.formatMoney(pr["unitPrice"])),
                $("<td>")
                    .addClass(["align-middle", "text-right"])
                    .attr("width", "5%")
                    .append(input),
                $("<td>")
                    .addClass(["align-middle", "text-right"])
                    .attr("width", "20%")
                    .html(accounting.formatMoney(pr["price"])),
                $("<td>")
                    .addClass(["align-middle", "text-right"])
                    .attr("width", "5%")
                    .append(
                        $("<a>")
                            .attr({
                                id: "btn_removeProduct_" + pr["sku"]
                            })
                            .append($("<i>").addClass(["fas", "fa-times", "text-danger"]))
                            .click(function(evt) {
                                // Event fired when pressing the "X" at the end of each element
                                evt.preventDefault();
                                $modalProductName.text(pr["name"]);
                                $modalProductSKU.text(pr["sku"])
                                //Show modal
                                $removeProductModal.modal({ keyboard: true });
                            })
                    )
            ];

            $(td_array).each(function(__, td_pr) {
                tr.append(td_pr);
            });
            $cart_contents_table.append(tr);
        });

        //Refresh totals
        refreshSubtotals(cart_data.subTotal, cart_data.tax, 0);

        //Whether we have items or not, we show up everything after a second
        setTimeout(function() {
            $cart_not_empty.removeClass("d-none").addClass("d-responsive-table");
            $cart_empty.removeClass("d-responsive-table").addClass("d-none");
            $cart_wrapper.waitMe("hide");
        }, 1000);
    } else {
        setTimeout(function() {
            $cart_empty.removeClass("d-none").addClass("d-responsive-table");
            $cart_not_empty.removeClass("d-responsive-table").addClass("d-none");
            $cart_wrapper.waitMe("hide");
        }, 1000);
    }
}

$(document).ready(function() {
    //Enable the shipping region dropdown design and the corresponding event lister
    $(".select-picker")
        .change(function(evt) {
            evt.preventDefault();
            var $this = $(this);
            var region = $this.val();
            var selectedRegion = shipping_data.prices.filter(function(i) {
                return i.id == region;
            })[0];
            refreshSubtotals(cart_data.subTotal, cart_data.tax, selectedRegion.price);
            $checkout_region.val(selectedRegion.id);
            $("#delay").text(
                selectedRegion.delay == 0 ?
                "on the day" :
                "within " + selectedRegion.delay + " business days"
            );
        })
        .selectpicker();

    // Correct some z-depths for properly see the dropdown
    $("button.dropdown-toggle").addClass("z-1000");
    $("div.dropdown-menu").addClass("z-1050")

    // Initialize listeners for the modals
    $btnModal_removeConfirm.click(function(evt) {
        evt.preventDefault();
        $removeProductModal.modal('hide');
        $.ajax({
            method: "DELETE",
            url: "ajax/cart",
            data: {
                "sku": $modalProductSKU.text()
            },
            dataType: "JSON",
            beforeSend: function() {
                $cart_wrapper.waitMe({});
            },
            success: function(data) {
                cart_data = data["cart"];
                console.log(cart_data);
                toastr["success"](
                    "The product was removed from your cart",
                    "Product deleted successfully"
                );
                updateView(cart_data);
            },
            error: function(err) {
                toastr["error"](
                    "The product could not be removed (maybe a connection error?)",
                    "Error while removing product"
                );
            },
            always: function() {
                setTimeout(function() {
                    $cart_wrapper.waitMe('hide');
                }, 1000);
            }
        });
    });

    $btn_emptyCart.click(function(evt) {
        evt.preventDefault();
        $emptyCartModal.modal({keyboard: true});
    });

    $btnModal_emptyConfirm.click(function(evt) {
        evt.preventDefault();
        $emptyCartModal.modal('hide');
        $.ajax({
            method: "DELETE",
            url: "ajax/cart",
            dataType: "JSON",
            beforeSend: function() {
                $cart_wrapper.waitMe({});
            },
            success: function(data) {
                cart_data = data["cart"];
                console.log(cart_data);
                toastr["success"](
                    "Time to fill it again!",
                    "Cart emptied successfully"
                );
                updateView(cart_data);
            },
            error: function(err) {
                toastr["error"](
                    "The cart could not be emptied (maybe a connection error?)",
                    "Error while emptying"
                );
            },
            always: function() {
                setTimeout(function() {
                    $cart_wrapper.waitMe('hide');
                }, 1000);
            }
        });
    });

    // Get the shipping prices and regions
    $.ajax({
        method: "GET",
        url: "ajax/shipping",
        dataType: "JSON",
        success: function(data) {
            shipping_data = data["data"];
        }
    });

    //Setup the checkout form before sending it
    $checkoutForm.submit(function(evt) {
        $cart_wrapper.waitMe({});
        return true; //Do send it
    });

    //Initialize the view
    reloadView();
});
