<style>
#tumor_vs_normals .tvn{
	height: 670px;
	border: 1px solid #aaaaaa;
	border-radius: 4px;
}

#tvn-sidebar-div{
	width: 280px;
}

#tvn-sidebar-div h4 {
        margin: 15px;
        font-size: 12px;
        color: grey;
        background-color: white;
        margin-top: -6px;
        display: table;
        padding: 5px;
    }

#tvn-sidebar-div h5 {
        margin-left: 20px;
        padding-left: 5px;
        padding-right: 5px;
        display: inline-block;
        margin-bottom: 10px;
    }
#tvn-sidebar-div select {
        max-width: 180px;
    }
#tvn-plots-box {
        width: 940px;
        float: right;
    }

input[type="number"]:valid {
    color: green;
}

input[type="number"]:invalid {
    color: red;
}

</style>



<div id="tumor_vs_normals">
	<table>
        <tr>
            <td>
			  <div id="tvn-sidebar-div" class="tvn">
			<h4>Parameters Filter</h4>
			<div id="tvn-spec" ng-controller="tvnGeneParamControl">
			<br>
				<div>
			<h5>Gene</h5>
			
					<select   ng-change="geneChange(selectedGeneOption)" ng-model="selectedGeneOption" ng-options="gene as gene for gene in geneOptions track by gene"></select>
					
					
					<div ng-model="normalsSelectionHide" ng-hide="normalsSelectionHide" ng-init="normalsSelectionHide=true">
						<br>
						<h5>Normal Dataset</h5>
						<select   ng-change="normalDatasetChange(selectedNormalDataset)" ng-model="selectedNormalDataset" ng-options="normalData as normalData for normalData in normalDatasetOptions track by normalData"></select>
					</div>
					
					<div ng-model="profileSelectionHide" ng-hide="profileSelectionHide" ng-init="profileSelectionHide=true">
						<br>
						<h5>Profile</h5>
						<select  ng-model="selectedProfileOption" ng-options="profile.NAME for profile in profileOptions"></select>
					</div>
					
					<div ng-model="zScoreCheckOption" ng-hide="zScoreCheckOption" ng-init="zScoreCheckOption=true">
						<br>
						<h5>Apply Z-Score</h5>
						<input ng-model="zScoreCheck" ng-change="zScoreCheckChange(zScoreCheck)" ng-init="zScoreCheck=false" type="checkbox" checked="">
					</div>
					
					<div ng-model="logCheckOption" ng-hide="logCheckOption" ng-init="logCheckOption=true">
						<br>
						<h5>Apply Log Scale</h5>
						<input ng-model="logCheck" ng-change="logChange(logCheck)" ng-init="logCheck=false" type="checkbox" checked="">
					</div>
					
					<div ng-model="thresholdValueHide" ng-hide="thresholdValueHide" ng-init="thresholdValueHide=true">
						<br>
						<h5>Threshold Value</h5>
						<input type=number step=0.000001 pattern="^[+-]?\d*(?:\.\d+)?$" ng-model="thresholdValue" ng-change="changeThreshold(thresholdValue)"  ng-model-options="{ debounce: 1000 }"  ng-init="thresholdValue=0.01" placeholder="value" style='width: 100px;' />
					</div>
					
					</div>
				 </div>
				 </div>
            </td>
			 <td>
                <div id="tvn-plots-box" class="tvn">
					<div id="plots_box" style='width: 940px; height: 650px;'> <img src="images/ajax-loader.gif"/></div>
                </div>
            </td>
        </tr>
    </table>
</div>