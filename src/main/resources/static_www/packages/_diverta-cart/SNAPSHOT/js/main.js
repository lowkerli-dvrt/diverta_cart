//jQuery extension selector to detect if a text is truncated
$.expr[':'].truncated = function(obj) {
    var $this = $(obj);
    var $c = $this
        .clone()
        .css({ display: 'inline', width: 'auto', visibility: 'hidden' })
        .appendTo('body');

    var c_width = $c.width();
    $c.remove();

    return c_width > $this.width();
};

toastr.options = {
    "closeButton": false,
    "debug": false,
    "newestOnTop": true,
    "progressBar": false,
    "positionClass": "toast-top-right",
    "preventDuplicates": false,
    "onclick": null,
    "showDuration": "300",
    "hideDuration": "1000",
    "timeOut": "5000",
    "extendedTimeOut": "1000",
    "showEasing": "swing",
    "hideEasing": "linear",
    "showMethod": "fadeIn",
    "hideMethod": "fadeOut"
}

$(document).ready(function() {
    // Add tooltips to truncated text
    $('.text-truncate:truncated').each(function(_, v) {
        $v = $(v);
        $v.attr('data-toggle', 'tooltip');
        $v.attr('title', $v.text());
    });
    // Create tooltips from tag data
    $('[data-toggle="tooltip"]').tooltip();
});