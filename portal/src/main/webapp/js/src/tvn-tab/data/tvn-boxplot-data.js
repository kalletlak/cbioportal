var tvnBoxPlot = (function() {
	var maxY = 0;
	var minY = 0;
	var singleBoxData = {
		x : 0,
		low : 0,
		q1 : 0,
		median : 0,
		q3 : 0,
		high : 0,
		fillColor : '#FFFFFF',
		color : '#AAAAAA',
		pValue : 0
	};
	var checkBoxes = new Object();
	var linkedSignificanceXAxisName = 'Sig.(%)';
	var linkedPValuesXAxisName = 'P values';
	var boxSeriesData = {
		name : '',
		id : '',
		type : 'boxplot',
		showInLegend : true,
		data : [],
		isCancer : false,
		zIndex : 1
	};
	var scatterPointData = {
		name : '',
		x : 0,
		y : 0,
	};
	var scatterSeriesData = {
		name : '',
		type : 'scatter',
		linkedTo : '',
		data : [],
		isCancer : false,
		zIndex : 2
	};
	var singleGeneData = {
		mappingName : '',
		geneName : '',
		isCrossCancer : false,
		showPValues : false,
		isZScored : 'N',
		pValues : [],
		title : '',
		tissueNames : [],
		xAxisName : '',
		yAxisName : '',
		seriesData : [],
		maxY : 0,
		minY : 0
	};

	var isboxPlotInitalized = false;
	var mainMap = new Object();

	var searchIndexTop = function(arr, ele) {
		for (var i = arr.length - 1; i > 0; i--) {
			if (ele > arr[i]) {
				continue;
			} else if (parseFloat(ele) == parseFloat(arr[i])) {
				return i;
			} else {
				return i - 1;
			}
		}
		return arr.length - 1;
	};
	var searchIndexBottom = function(arr, ele) {
		for (var i = 0; i < arr.length; i++) {
			if (parseFloat(ele) <= parseFloat(arr[i])) {
				return i;
			} else {
				continue;
			}
		}
		return arr.length - 1;
	};
	var getSingleBoxData = function(xVal, sampleMap, color) {
		var returnData = {
			boxData : '',
			scatterData : ''
		};
		var _scatterSeriesDataArray = [];
		var _singleBoxData = jQuery.extend(true, {}, singleBoxData);
		_singleBoxData.x = xVal;
		var valArray = [];
		if (color != null)
			_singleBoxData.fillColor = color;

		$.each(sampleMap, function(sampleID, sampleVal) {
			var _x = xVal + (Math.random() * 0.2) - 0.1;
			var yVal = sampleVal;
			if (Number(sampleVal) < minY)
				minY = sampleVal;
			else if (Number(sampleVal) > maxY)
				maxY = sampleVal;
			var _scatterPointData = jQuery.extend(true, {}, scatterPointData);
			_scatterPointData.name = sampleID;
			_scatterPointData.x = _x;
			_scatterPointData.y = yVal;
			_scatterSeriesDataArray.push(_scatterPointData);
			valArray.push(yVal);
		});
		valArray.sort(function(a, b) {
			return a - b;
		});
		var quantiles = [];
		if (valArray.length > 1) {
			quantiles = jStat.quantiles(valArray, [ 0.25, 0.5, 0.75 ]);
			var IQR = quantiles[2] - quantiles[0];
			high = valArray[searchIndexTop(valArray, quantiles[2] + (1.5 * IQR))];
			low = valArray[searchIndexBottom(valArray, quantiles[0]
					- (1.5 * IQR))];
			_singleBoxData.low = low;
			_singleBoxData.q1 = quantiles[0];
			_singleBoxData.median = quantiles[1];
			_singleBoxData.q3 = quantiles[2];
			_singleBoxData.high = high;
		} else {
			_singleBoxData.low = valArray[0];
			_singleBoxData.q1 = valArray[0];
			_singleBoxData.median = valArray[0];
			_singleBoxData.q3 = valArray[0];
			_singleBoxData.high = valArray[0];
		}
		var tmparr = [];
		tmparr.push(_singleBoxData);
		returnData.scatterData = _scatterSeriesDataArray;
		returnData.boxData = tmparr;
		return returnData;
	};

	function initialize(isCrossCancer, dataSet, geneName, zScoreCheck,
			thresholdValue) {
		maxY = 0;
		minY = 0;
		$
				.each(
						dataSet,
						function(key, val) {
							if (isCrossCancer)
								checkBoxes[geneName + '-' + key] = val['SHOW_ZSCORE_CHECKBOX'];
							var _boxPlotSeriesData = [];
							var _scatterPlotSeriesData = [];
							var pValues = [];
							var tissueNames = [];
							var _singleDataSet = jQuery.extend(true, {},
									singleGeneData);
							var xCount = 0;
							if (val['TUMOR_SAMPLES'].length > 1) {
								val['TUMOR_SAMPLES']
										.sort(function(a, b) {
											return (a.CANCER_STUDY_NAME)
													.localeCompare(b.CANCER_STUDY_NAME);
										});
							}
							$
									.each(
											val['TUMOR_SAMPLES'],
											function(tumorKey, tumorValue) {
												tissueNames
														.push(tumorValue['CANCER_STUDY_NAME']
																.replace(
																		new RegExp(
																				'_',
																				'g'),
																		" "));
												var _boxColor = (window.json.cancer_colors[tumorValue['CANCER_TYPE']] != null) ? window.json.cancer_colors[tumorValue['CANCER_TYPE']]
														: '#CCCCFF';
												var returnedData = getSingleBoxData(
														xCount,
														tumorValue['SAMPLES_MAP'],
														_boxColor);
												var _boxSeriesData = jQuery
														.extend(true, {},
																boxSeriesData);
												_boxSeriesData.name = tumorValue['CANCER_STUDY_NAME']
														.replace(new RegExp(
																'_', 'g'), " ");
												_boxSeriesData.id = tumorValue['CANCER_STUDY_ID'];
												_boxSeriesData.data = returnedData.boxData;
												_boxSeriesData.showInLegend = false;
												_boxSeriesData.isCancer = true;
												var _scatterSeriesData = jQuery
														.extend(true, {},
																scatterSeriesData);
												_scatterSeriesData.name = tumorValue['CANCER_STUDY_NAME']
														.replace(new RegExp(
																'_', 'g'), " ");
												_scatterSeriesData.linkedTo = tumorValue['CANCER_STUDY_ID'];
												_scatterSeriesData.data = returnedData.scatterData;
												_scatterSeriesData.isCancer = true;
												xCount++;
												if (!isCrossCancer) {
													pValues.push("");
												}
												_boxPlotSeriesData
														.push(_boxSeriesData);
												_scatterPlotSeriesData
														.push(_scatterSeriesData);
											});

							var normalsMap = new Object();

							Object
									.keys(val['NORMAL_SAMPLES'])
									.sort()
									.forEach(
											function(key1) {
												normalsMap[key1] = val['NORMAL_SAMPLES'][key1];
											});

							$
									.each(
											normalsMap,
											function(tissueName, tissueMap) {
												tissueNames
														.push(tissueName
																.replace(
																		new RegExp(
																				'_',
																				'g'),
																		" "));
												var returnedData = getSingleBoxData(
														xCount,
														tissueMap['SAMPLES_MAP'],
														null);
												var _boxSeriesData = jQuery
														.extend(true, {},
																boxSeriesData);
												_boxSeriesData.name = tissueName
														.replace(new RegExp(
																'_', 'g'), " ");
												_boxSeriesData.id = tissueName
														.replace(new RegExp(
																'_', 'g'), " ");
												_boxSeriesData.data = returnedData.boxData;

												if (!isCrossCancer) {
													pValues
															.push(tissueMap['PVALUE']);
													_boxSeriesData.data[0].pValue = parseFloat(tissueMap['PVALUE']);

													if (parseFloat(tissueMap['PVALUE']) <= parseFloat(thresholdValue)) {
														_boxSeriesData.data[0].color = "#F80000";

													}
												}
												var _scatterSeriesData = jQuery
														.extend(true, {},
																scatterSeriesData);
												_scatterSeriesData.linkedTo = tissueName
														.replace(new RegExp(
																'_', 'g'), " ");
												_scatterSeriesData.name = tissueName
														.replace(new RegExp(
																'_', 'g'), " ");
												_scatterSeriesData.data = returnedData.scatterData;
												xCount++;
												_boxPlotSeriesData
														.push(_boxSeriesData);
												_scatterPlotSeriesData
														.push(_scatterSeriesData);
											});
							if (isCrossCancer)
								_singleDataSet.mappingName = key;
							else
								_singleDataSet.mappingName = zScoreCheck.NAME;
							_singleDataSet.tissueNames = tissueNames;
							_singleDataSet.xAxisName = 'Tissue Type';
							_singleDataSet.pValues = pValues;
							_singleDataSet.yAxisName = geneName
									+ ', Gene Expression Value';
							_singleDataSet.seriesData = $.merge(
									_boxPlotSeriesData, _scatterPlotSeriesData);
							_singleDataSet.geneName = geneName;
							_singleDataSet.title = geneName
									+ " Tumor v. Normals";
							_singleDataSet.isCrossCancer = isCrossCancer;
							_singleDataSet.maxY = maxY;
							_singleDataSet.minY = minY;
							if (isCrossCancer)
								if (zScoreCheck != null)
									_singleDataSet.isZScored = zScoreCheck ? 'Y'
											: 'N';
							mainMap[_singleDataSet.geneName + '-'
									+ _singleDataSet.mappingName + '-'
									+ _singleDataSet.isZScored] = _singleDataSet;
						});
	}

	function loadPlot(geneName, mappingName, zScoreCheck) {
		var zScoreFlag = 'N';
		if (zScoreCheck != null)
			zScoreFlag = zScoreCheck ? "Y" : "N";
		if (mainMap[geneName + '-' + mappingName + '-' + zScoreFlag] != null) {
			var map = jQuery.extend(true, {}, mainMap[geneName + '-'
					+ mappingName + '-' + zScoreFlag]);
			if (map.isCrossCancer) {
				if (!isboxPlotInitalized) {
					boxPlot.addCrossCancerEvents();
					isboxPlotInitalized = true;
				}
				boxPlot.initializePlot(map.title, map.xAxisName, map.yAxisName,
						map.tissueNames, map.isCrossCancer, map.seriesData,
						linkedSignificanceXAxisName, map.minY, map.maxY);
			} else {
				boxPlot
						.initializePlot(map.title, map.xAxisName,
								map.yAxisName, map.tissueNames,
								map.isCrossCancer, map.seriesData,
								linkedPValuesXAxisName, map.minY, map.maxY,
								map.pValues);
			}
		}

	}

	function hasData(geneName, mappingName, zScoreCheck) {
		var zScoreFlag = 'N';
		if (zScoreCheck != null)
			zScoreFlag = zScoreCheck ? "Y" : "N";
		if (mainMap[geneName + '-' + mappingName + '-' + zScoreFlag] != null) {
			return true;
		}
		return false;

	}

	function showcheckBoxes(geneName, mappingName) {
		var flag = checkBoxes[geneName + '-' + mappingName];
		if (flag != null)
			return flag;
		else
			false;
	}

	function applyLog(flag) {
		boxPlot.applyLog(flag);
	}

	function thresholdChange(thresholdValue) {
		boxPlot.thresholdChange(thresholdValue);
	}

	return {
		initialize : initialize,
		loadPlot : loadPlot,
		hasData : hasData,
		showcheckBoxes : showcheckBoxes,
		applyLog : applyLog,
		thresholdChange : thresholdChange,
	};
}());
