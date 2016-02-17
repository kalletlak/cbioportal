<%--
 - This file is part of cBioPortal.
 -
 - cBioPortal is free software: you can redistribute it and/or modify
 - it under the terms of the GNU Affero General Public License as
 - published by the Free Software Foundation, either version 3 of the
 - License.
 -
 - This program is distributed in the hope that it will be useful,
 - but WITHOUT ANY WARRANTY; without even the implied warranty of
 - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 - GNU Affero General Public License for more details.
 -
 - You should have received a copy of the GNU Affero General Public License
 - along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>

<%
	String siteTitle = GlobalProperties.getTitle();
%>

<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder"%>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties"%>
<%@ page import="java.util.Set"%>


<%
	request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle	+ "::Link to Harvest");
%>
<jsp:include page="WEB-INF/jsp/global/header.jsp" flush="true" />
<script type="text/javascript" src="js/src/harvest_cart.js"></script>
<script>
$(document).ready(function() {
  var url = '<%= GlobalProperties.getHarvestUrl() %>';
  <%Set<String> samplesList =(Set<String>) session.getAttribute("selectedSamples");%>
  var i = 0
  var storedSamples = []
  <% if(samplesList!=null){
    for(String sample: samplesList){ %>
  var code = '<%= sample %>';
  storedSamples[i++] = code;
  <%}
}%>
  if (storedSamples.length > 0) {
    $("#submit").prop('disabled', false);
  }
  SampleIDQuery.init(storedSamples);


  $("#inputSamples").focusin(function() {
    $("#inputSamples").addClass("chosen-container-active");
  });
  $("#inputSamples").focusout(function() {
    $("#inputSamples").removeClass("chosen-container-active");
  });

  $("#submit").click(function() {
    var arr = [];
    var samplesList = '';
    $('#groupTop li span').each(function() {
      var sample = $(this).text();
      arr.push(sample);
      samplesList += '<input type="text" name="sample" value="' + sample + '" />'
    })

    var form = $('<form action="' + url + '" method="get" target="_blank" style="display:none;">' +
      samplesList +
      '</form>');
    $('body').append(form);
    form.submit();
  });


  $.extend({
    redirectPost: function(location, array) {

      form = '<input type="hidden" name="samples" value=' + array + '>';

      $('<form action="' + location + '" method="POST">' + form + '</form>').appendTo($(document.body)).submit();
    }
  });
});
   
</script>
<style>
#querySamples-selected {
	border: 1px solid #aaaaaa;
	border-radius: 4px;
	margin: 15px;
}

#querySamples-filter,#querySamples-selected {
	width: 850px;
	height: 220px;
}

#querySamples-filter  h4,#querySamples-selected h4 {
	margin: 15px;
	font-size: 14px;
	color: grey;
	background-color: white;
	margin-top: -6px;
	display: table;
	padding: 5px;
}

#inputSamples ul {
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

#inputSamples .chosen-container-active {
	border: 1px solid #5897fb;
	box-shadow: 0 0 5px rgba(0, 0, 0, 0.3);
}

#inputSamples .chosen-choices {
	background-color: #fff;
	background-image: linear-gradient(#eeeeee 1%, #ffffff 15%);
	border: 1px solid #aaa;
	cursor: text;
	overflow: hidden;
	padding: 0 5px;
	position: relative;
	width: 100%;
}

#inputSamples li.search-choice {
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

#inputSamples .chosen-choices li {
	float: left;
	list-style: outside none none;
}

#inputSamples li.search-choice span {
	word-wrap: break-word;
}

#inputSamples .search-choice-close {
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

#inputSamples .search-choice-close:hover {
	background-position: -42px -10px;
}

#inputSamples .search-choice a {
	cursor: pointer;
}
</style>
<link href="css/chosen.css" type="text/css" rel="stylesheet" />

<div id="harvestMainQuery">
	<div id="querySamples">
		<table>
			<tr>
				<td>
					<div id="querySamples-selected" style="height: 280px;">
						<h4>Selected Samples List</h4>
						<div class="cont" id="inputSamples" tabindex="-1">
							<ul id="groupTop" class="chosen-choices"
								style="width: 770px; height: 170px; overflow-y: auto; overflow-x: hidden;">
							</ul>
						</div>
						<div style="float: right; margin-right: 40px; margin-top: 20px;">
							<input type="button" id="submit" disabled class="btn1"
								value="Submit">
						</div>
					</div>
				</td>
			</tr>


		</table>
	</div>
</div>
</td>
<td><jsp:include page="WEB-INF/jsp/global/right_column.jsp"
		flush="true" /></td>
</tr>
<tr>
	<td colspan="3"><jsp:include page="WEB-INF/jsp/global/footer.jsp"
			flush="true" /></td>
</tr>
</table>
</div>
</center>
</div>
</form>
<jsp:include page="WEB-INF/jsp/global/xdebug.jsp" flush="true" />
</body>
</html>
