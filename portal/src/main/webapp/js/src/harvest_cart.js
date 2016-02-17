var SampleIDQuery = (function() {
  var selectedSamples = [];

  function init(storedSamples) {
    $('#querySamples').hide();
    $('#harvestMainQuery').append('<div id="load" style="width = 100%; height=50px;" align="center">&nbsp;</div>');
    $('#load').fadeIn('fast', loadContent);
    selectedSamples = storedSamples;
  }

  function loadContent() {
    loadSelectedSamples();
    $('#querySamples').fadeIn('fast', hideLoader);
    $('#querySamples').show();
  }

  function loadSelectedSamples() {
    jQuery.each(selectedSamples, function(key, typeStr) {
      var choice, close_link = this;
      choice = $('<li />', {
        "class": "search-choice"
      }).html("<span>" + typeStr + "</span>");
      close_link = $('<a />', {
        "class": 'search-choice-close',
      });
      close_link.bind('click', function(evt) {
        deleteSelectedSample($(evt.target).parents('li').find("span")
          .text());
        $(evt.target).parents('li').first().remove();
        if ($("#groupTop li").length <= 0) {
          $("#submit").prop('disabled', true);
        }
      });
      choice.append(close_link);
      $("#inputSamples ul").append(choice);
    });
  }

  var deleteSelectedSample = function(that) {
    var data = {
      operation: "delete",
      sample: that
    };
    $.post('sessionSamples.json', data, function returnData(param) {
      if (param.returnString == "SUCCESS") {

        noty({
          text: that + " Removed",
          type: 'information',
          layout: 'topRight',
          theme: 'defaultTheme',
          timeout: 2000
        });
      }
    }, "json");
  };

  function hideLoader() {
    $('#load').fadeOut('slow', removeLoader());
  }

  function removeLoader() {
    $('#load').remove();
  }
  return {
    init: init
  };
}());
