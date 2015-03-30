$(document).ready(function(){
    console.log("Entrei em showQuestion.scala.html");
    $("#shareButton").popover({
        html : true,
        content: function() {
            return $('#popoverContent').html();
        }
    });
});