//jQuery extension selector to detect if a text is truncated
$.expr[":"].truncated = function(obj) {
    var $this = $(obj);
    var $c = $this
        .clone()
        .css({ display: "inline", width: "auto", visibility: "hidden" })
        .appendTo("body");

    var c_width = $c.width();
    $c.remove();

    return c_width > $this.width();
};

// Global options for Toastr
toastr.options = {
    closeButton: false,
    debug: false,
    newestOnTop: true,
    progressBar: false,
    positionClass: "toast-top-right",
    preventDuplicates: false,
    onclick: null,
    showDuration: "300",
    hideDuration: "1000",
    timeOut: "5000",
    extendedTimeOut: "1000",
    showEasing: "swing",
    hideEasing: "linear",
    showMethod: "fadeIn",
    hideMethod: "fadeOut"
};

// Global options for accounting
accounting.settings = {
    currency: {
        symbol: "JPY", // default currency symbol is '$'
        format: "%s&nbsp;&nbsp;%v", // controls output: %s = symbol, %v = value/number (can be object: see below)
        decimal: ".", // decimal point separator
        thousand: "&nbsp;", // thousands separator
        precision: 0 // decimal places
    },
    number: {
        precision: 0, // default precision on numbers is 0
        thousand: "&nbsp;",
        decimal: "."
    }
};

//Global vars
var spanCartTotals = $("span#cartTotals");

function updateCartTotalsNavbar(uiDelay) {
    uiDelay = typeof uiDelay == "undefined" ? 1000 : uiDelay;
    $.ajax({
        method: "GET",
        url: "ajax/cart",
        dataType: "JSON",
        beforeSend: function() {
            spanCartTotals.html($("<i>").addClass("fas fa-spinner fa-spin"));
        },
        success: function(data) {
            setTimeout(function() {
                var totalNoShipping = data["cart"]["subTotal"] + data["cart"]["tax"];
                spanCartTotals.html(
                    data["cart"]["numElements"] + " item(s), " + accounting.formatMoney(totalNoShipping)
                );
            }, uiDelay);
        },
        failure: function(err) {
            setTimeout(function() {
                spanCartTotals
                    .html($("<i>").addClass("fas fa-exclamation-triangle"))
                    .attr("data-tooltip", "toggle")
                    .attr(
                        "title",
                        "There was a problem while getting your cart. Refreshing<br>(<span class='kbd'>F5</span>) may help."
                    )
                    .tooltip({ html: true });
            }, uiDelay);
        }
    });
}

$(document).ready(function() {
    // Add tooltips to truncated text
    $(".text-truncate:truncated").each(function(_, v) {
        $v = $(v);
        $v.attr("data-toggle", "tooltip");
        $v.attr("title", $v.text());
    });

    // Format the prices with the formatMoney function
    $("span.price").each(function(_, v) {
        var $this = $(this);
        var price = parseInt($this.text());
        $this.html(accounting.formatMoney(price));
    });

    // Format the prices inside tooltips with the formatMoney function
    $("span.tooltip-price").each(function(_, v) {
        var $this_tooltip = $(this);
        var title = $this_tooltip.attr("title");
        var parsedTitle = $.parseHTML(title);
        $(parsedTitle)
            .filter("span.price")
            .each(function(_, v) {
                var $this_title = $(this);
                var price = parseInt($this_title.text());
                $this_title.html(accounting.formatMoney(price));
            });
        var newTitle = parsedTitle
            .map(function(i) {
                return i.outerHTML !== undefined ? i.outerHTML : i.textContent;
            })
            .join("");
        $this_tooltip.attr("title", newTitle);
    });

    // Create tooltips from tag data
    $('[data-toggle="tooltip"]').tooltip();

    // Load cart data in the navbar
    updateCartTotalsNavbar();
});
