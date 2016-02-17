<script type="text/javascript" src="js/src/tvn-tab/controller/tvn-controller.js"></script>
<script type="text/javascript" src="js/src/tvn-tab/data/tvn-boxplot-data.js"></script>
<script type="text/javascript" src="js/src/tvn-tab/view/tvn-boxplot-view.js"></script>
<div id="tvn_details" class="tvn-details-content">
    <%@ include file="tumor_vs_normal/tvn_tab_content.jsp" %>
</div>
<script>

$(document).ready(function() {
	var tmrvsnrml_tab_init = false;
	$("#tabs").bind("tabsactivate", function(event, ui) {
		if (ui.newTab.text().trim().toLowerCase() === "tumor vs normals") {
				$( "#cc-tvn" ).css( "display", "block" );
				$("#tabs").find('cc-tvn').css('display', 'block');
				
				if (tmrvsnrml_tab_init === false) {
					angular.element(document).ready(function() {
						angular.bootstrap(document, ['menu']);
					});
					var scope = angular.element($("#tvn-spec")).scope();
					
					scope.$apply(function () {
						scope.getProfileData(window.PortalGlobals.getGeneList(),window.PortalGlobals.getCancerStudyId());
					});
					tmrvsnrml_tab_init = true;
				} else {
					$(window).trigger("resize");
				}
			}
		});
});
</script>