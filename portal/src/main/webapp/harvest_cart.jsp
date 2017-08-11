<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder"%>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties"%>


<!-- js files: -->
<script type="text/javascript" src="js/lib/jquery.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/underscore-min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/backbone-min.js?<%=GlobalProperties.getAppVersion()%>"></script>

<script type="text/javascript" src="js/lib/showdown.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/showdown-github.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/url_based_content.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/cbio-util.js?<%=GlobalProperties.getAppVersion()%>"></script>

<style>
#cases-container ul {
	color: #333333;
	font-family: verdana, arial, sans-serif;
	font-size: 12px;
	line-height: 18px;
	list-style-type: disc;
	margin-bottom: 5px;
	margin-top: 5px;
	text-align: justify;
	margin-left: 38px;
}

#cases-container .chosen-choices {
	background-color: #fff;
	background-image: linear-gradient(#eeeeee 1%, #ffffff 15%);
	border: 1px solid #aaa;
	cursor: text;
	overflow: hidden;
	padding: 0 5px;
	position: relative;
	width: 100%;
}

#cases-container li.search-choice {
	background-clip: padding-box;
	background-color: #eeeeee;
	background-image: linear-gradient(#f4f4f4 20%, #f0f0f0 50%, #e8e8e8 52%, #eeeeee 100%);
	background-repeat: repeat-x;
	background-size: 100% 19px;
	border: 1px solid #aaa;
	border-radius: 3px;
	box-shadow: 0 0 2px white inset, 0 1px 0 rgba(0, 0, 0, 0.05);
	color: #333;
	cursor: default;
	line-height: 13px;
	margin: 3px 5px 3px 0;
	max-width: 100%;
	padding: 3px 20px 3px 5px;
	position: relative;
}

#cases-container .chosen-choices li {
	float: left;
	list-style: outside none none;
}

#cases-container li.search-choice span {
	word-wrap: break-word;
}

#cases-container .search-choice-close {
	background: rgba(0, 0, 0, 0) url("images/chosen-sprite.png") no-repeat
		scroll -42px 1px;
	display: block;
	font-size: 1px;
	height: 12px;
	position: absolute;
	right: 3px;
	top: 4px;
	width: 12px;
}

#cases-container .search-choice-close:hover {
	background-position: -42px -10px;
}

#cases-container .search-choice a {
	cursor: pointer;
}
</style>

    
<%
    String siteTitle = GlobalProperties.getTitle() + "::Link to Harvest";
%>

<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:template title="<%= siteTitle %>" defaultRightColumn="false" fixedWidth="true">

    <jsp:attribute name="head_area">
        <script>
        window.loadReactApp({ defaultRoute: 'blank' });
        </script>
    </jsp:attribute>

    <jsp:attribute name="body_area">
        <h1>Selected Samples</h1>
        <div id="harvestPage">
			<div id="cases-container">
				<ul id="groupTop" class="chosen-choices"
					style="width: 770px; height: 170px; overflow-y: auto; overflow-x: hidden;">
				</ul>
			</div>
			<div style="margin-right: 40px; margin-top: 20px;">
				<input type="button" id="clear" class="btn btn-default btn-lg"
					disabled value="Clear"> 
				<input type="button" id="submit" disabled
					class="btn btn-default btn-lg" value="Submit">
			</div>
        </div>
        <div id="reactRoot" class="hidden"></div>
    </jsp:attribute>


</t:template>



<script>
$(document).ready(function() {
	function init(storedSamples) {
	    jQuery.each(storedSamples, function(key, typeStr) {
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
	          $("#clear").prop('disabled', true);
	        }
	      });
	      choice.append(close_link);
	      $("#cases-container ul").append(choice);
	    });
	  }

	  function deleteSelectedSample(sample) {
	    if(cbio.util.localStorageUtil('harvest_samples', 'delete', sample)) {

	    }
	  }


  var url = '<%= GlobalProperties.getHarvestUrl() %>';
  var samples = cbio.util.localStorageUtil('harvest_samples', 'get')
  if (url !== undefined && url !== null && url.length > 0 &&samples.length > 0) {
    $("#submit").prop('disabled', false);
    $("#clear").prop('disabled', false);
  }
  init(samples);

$("#clear").click(function() {
	cbio.util.localStorageUtil('harvest_samples', 'clear')
	$('#cases-container ul').empty()
})
  $("#submit").click(function() {
  	var formOpts = {
      action: url,
      method: 'get',
      // Could not figure out why Sarafi won't allow to open a new tab for query page
      target: !cbio.util.browser.safari ? '_blank' : ''
    };
    var $form = $('<form>', formOpts);

    $('#groupTop li span').each(function() {
      var sample = $(this).text();
      $('<input>').attr({
        type: 'text',
        name: "sample",
        value: sample
      }).appendTo($form);
    });

    // Firefox requires form to be attached to document body.
    $form.appendTo(document.body);
    
    $form.submit();
  });
});

</script>

