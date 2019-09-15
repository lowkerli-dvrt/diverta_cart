//Sections
var $region = $("#region");
var $shippingWrapper = $("#shippingWrapper");
var $deliveryInPlace = $("#deliveryInPlace");
var $deliveryTokyo = $("#deliveryTokyo");
var $deliveryNational = $("#deliveryNational");

//Shipping info
var $ward_1_2 = $("#ward_1_2");
var $section_1_2 = $("#section_1_2");
var $choumeBanGo_1_2 = $("#choumeBanGo_1_2");
var $postCode_1_2 = $("#postCode_1_2");

var $dropdownPrefecture = $("#dropdownPrefecture");
var $city = $("#city");
var $ward = $("#ward");
var $section = $("#section");
var $choumeBanGo = $("#choumeBanGo");
var $postCode = $("#postCode");

//Credit card data
var $cardHolder = $("#cardHolder");
var $cardNumber = $("#cardNumber");
var $cardDueDate = $("#cardDueDate");
var $cardControlCode = $("#cardControlCode");
var $email = $("#email");
var $confirmCheck = $("#confirmCheck");

//Pre-confirm modal
var $preConfirmModal = $("#preConfirmModal");
var $modalPrePurchaseCreditCard = $("#modalPrePurchaseCreditCard");
var $modalPrePurchaseName = $("#modalPrePurchaseName");
var $btnModal_purchaseConfirm = $("#btnModal_purchaseConfirm");
var $shippingPurchaseForm = $("#shippingPurchaseForm");

//Post-confirm modal
var $postConfirmModal = $("#postConfirmModal");
var $postConfirmEmail = $("#postConfirmEmail");
var $btnModal_purchaseFinish = $("#btnModal_purchaseFinish");

var choumeBanGo_regex = /(?:[1-9]\d*-)?[1-9]\d*-[1-9]\d*/;

function all(items) {
    for (var i = 0; i < items.length; i++) {
        if (!items[i]) {
            return false;
        }
    }
    return true;
}

function updateLeftPane() {
    $shippingWrapper.waitMe({});
    var regionSelected = parseInt($region.text());
    switch (regionSelected) {
        case 1:
            $deliveryInPlace.removeClass("d-none").addClass("d-block");
            $deliveryTokyo
                .add($deliveryNational)
                .removeClass("d-block")
                .addClass("d-none");
            break;
        case 2:
            $deliveryTokyo.removeClass("d-none").addClass("d-block");
            $deliveryInPlace
                .add($deliveryNational)
                .removeClass("d-block")
                .addClass("d-none");
            break;
        default:
            $deliveryNational.removeClass("d-none").addClass("d-block");
            $deliveryInPlace
                .add($deliveryTokyo)
                .removeClass("d-block")
                .addClass("d-none");
            break;
    }
    setTimeout(function() {
        $shippingWrapper.waitMe("hide");
    }, 1000);
}

// Luhn test algorithm for JS -- See specification at CheckoutHandler.kt:22
var lunhTest = function(a, b, c, d, e) {
    for (d = +a[(b = a.length - 1)], e = 0; b--; ) (c = +a[b]), (d += ++e % 2 ? ((2 * c) % 10) + (c > 4) : c);
    return !(d % 10);
};

$(document).ready(function() {
    updateLeftPane();

    $btnModal_purchaseConfirm.click(function(evt) {
        $preConfirmModal.modal("hide");

        var formData = {};
        $shippingPurchaseForm.serializeArray().forEach(function(a) {
            formData[a["name"]] = a["value"];
        });
        var originalHTML = $confirmCheck.html();
        $.ajax({
            method: "POST",
            url: "ajax/checkout",
            data: formData,
            dataType: "JSON",
            beforeSend: function() {
                var new_html = $("<span>")
                    .text("Almost there...! ")
                    .append($("<i>").addClass(["fas", "fa-spinner", "fa-spin"]))
                    .html();
                $confirmCheck.attr("disable", true).html(new_html);
            },
            success: function(data) {
                var email = $email.val();
                $postConfirmEmail.text(email);
                $postConfirmModal.modal({ keyboard: true });
            },
            error: function(err) {
                var response =
                    "Error " + err.responseJSON.result_data.code + ": " + err.responseJSON.result_data.message;
                toastr["error"](
                    "There was an error while we processed the transaction. Credit card issuer responded:<br>" +
                        "<i>" +
                        response +
                        "</i>",
                    "Error while processing purchase"
                );
            },
            always: function() {
                setTimeout(function() {
                    $confirmCheck.attr("disable", false).html(originalHTML);
                }, 1000);
            }
        });
    });

    $confirmCheck.click(function(evt) {
        evt.preventDefault();
        var ship1OK = $deliveryInPlace.hasClass("d-block");
        var ship2OK = all([
            $ward_1_2.hasClass("is-valid"),
            $section_1_2.hasClass("is-valid"),
            $choumeBanGo_1_2.hasClass("is-valid"),
            $postCode_1_2.hasClass("is-valid")
        ]);
        var ship3OK = all([
            $dropdownPrefecture.hasClass("is-valid"),
            $city.hasClass("is-valid"),
            $section.hasClass("is-valid"),
            $choumeBanGo.hasClass("is-valid"),
            $postCode.hasClass("is-valid")
        ]);
        var allCCOK = all([
            $cardHolder.hasClass("is-valid"),
            $cardNumber.hasClass("is-valid"),
            $cardDueDate.hasClass("is-valid"),
            $cardControlCode.hasClass("is-valid"),
            $email.hasClass("is-valid")
        ]);
        var allOK = allCCOK; //&& (ship1OK || ship2OK || ship3OK);
        if (!allOK) {
            toastr["error"](
                "Seems one of the inputted values is wrong. Check it and try again.",
                "Cannot continue purchase"
            );
            return;
        }
        var cardNumber = $cardNumber.val();
        var cardHolder = $cardHolder.val();
        $modalPrePurchaseCreditCard.text(cardNumber);
        $modalPrePurchaseName.text(cardHolder);
        $preConfirmModal.modal({ keyboard: true });
    });

    $cardHolder
        .focusin(function(evt) {
            $(this).removeClass("is-valid is-invalid");
            $("#cardHolderHelper").removeClass("d-none");
        })
        .focusout(function(evt) {
            var content = $(this).val();
            if (content.length >= 3) {
                $(this).addClass("is-valid");
            } else {
                $(this).addClass("is-invalid");
                $("#cardHolderHelper").addClass("d-none");
            }
        });

    $cardNumber
        .focusin(function(evt) {
            $(this).removeClass("is-valid is-invalid");
            $("#cardNumberHelper").removeClass("d-none");
        })
        .focusout(function(evt) {
            var content = $(this).val();
            if (content.length >= 12 && content.length <= 19 && lunhTest(content)) {
                $(this).addClass("is-valid");
            } else {
                $(this).addClass("is-invalid");
                $("#cardNumberHelper").addClass("d-none");
            }
        });

    $cardDueDate
        .focusin(function(evt) {
            $(this).removeClass("is-valid is-invalid");
        })
        .focusout(function(evt) {
            var content = $(this).val();
            if (content !== "") {
                $(this).addClass("is-valid");
            }
        });

    $cardControlCode
        .focusin(function(evt) {
            $(this).removeClass("is-valid is-invalid");
        })
        .focusout(function(evt) {
            var content = $(this).val();
            if (content.length >= 3 && content.length <= 4) {
                $(this).addClass("is-valid");
            } else {
                $(this).addClass("is-invalid");
            }
        });

    $email
        .focusin(function(evt) {
            $(this).removeClass("is-valid is-invalid");
        })
        .focusout(function(evt) {
            var content = $(this).val();
            var regex = /\S+@\S+(\.\S+)*/;
            if (regex.test(content)) {
                $(this).addClass("is-valid");
            } else {
                $(this).addClass("is-invalid");
            }
        });

    $ward_1_2
        .focusin(function(evt) {
            $(this).removeClass("is-valid is-invalid");
        })
        .focusout(function(evt) {
            if ($(this).val().length > 0) {
                $(this).addClass("is-valid");
            } else {
                $(this).addClass("is-invalid");
            }
        });

    $section_1_2
        .focusin(function(evt) {
            $(this).removeClass("is-valid is-invalid");
        })
        .focusout(function(evt) {
            if ($(this).val().length > 0) {
                $(this).addClass("is-valid");
            } else {
                $(this).addClass("is-invalid");
            }
        });

    $choumeBanGo_1_2
        .focusin(function(evt) {
            $(this).removeClass("is-valid is-invalid");
        })
        .focusout(function(evt) {
            if (choumeBanGo_regex.test($(this).val())) {
                $(this).addClass("is-valid");
            } else {
                $(this).addClass("is-invalid");
            }
        });

    $postCode_1_2
        .focusin(function(evt) {
            $(this).removeClass("is-valid is-invalid");
        })
        .focusout(function(evt) {
            var code = parseInt($(this).val());
            if (code != NaN && code >= 1000000 && code <= 9999999) {
                $(this).addClass("is-valid");
            } else {
                $(this).addClass("is-invalid");
            }
        });

    $dropdownPrefecture
        .focusin(function(evt) {
            $(this).removeClass("is-valid");
        })
        .focusout(function(evt) {
            if ($(this).val() !== null) {
                $(this).addClass("is-valid");
            }
        });

    $city
        .focusin(function(evt) {
            $(this).removeClass("is-valid is-invalid");
        })
        .focusout(function(evt) {
            if ($(this).val().length > 0) {
                $(this).addClass("is-valid");
            } else {
                $(this).addClass("is-invalid");
            }
        });

    $ward
        .focusin(function(evt) {
            $(this).removeClass("is-valid");
        })
        .focusout(function(evt) {
            if ($(this).val().length > 0) {
                $(this).addClass("is-valid");
            }
        });

    $section
        .focusin(function(evt) {
            $(this).removeClass("is-valid is-invalid");
        })
        .focusout(function(evt) {
            if ($(this).val().length > 0) {
                $(this).addClass("is-valid");
            } else {
                $(this).addClass("is-invalid");
            }
        });

    $choumeBanGo
        .focusin(function(evt) {
            $(this).removeClass("is-valid is-invalid");
        })
        .focusout(function(evt) {
            if (choumeBanGo_regex.test($(this).val())) {
                $(this).addClass("is-valid");
            } else {
                $(this).addClass("is-invalid");
            }
        });

    $postCode
        .focusin(function(evt) {
            $(this).removeClass("is-valid is-invalid");
        })
        .focusout(function(evt) {
            var code = parseInt($(this).val());
            if (code != NaN && code >= 1000000 && code <= 9999999) {
                $(this).addClass("is-valid");
            } else {
                $(this).addClass("is-invalid");
            }
        });

    $btnModal_purchaseFinish.click(function(evt) {
        evt.preventDefault();
        window.location = "/";
    });
});
