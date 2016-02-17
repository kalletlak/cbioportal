var transform = function(data) {
	return $.param(data);
};

app.filter('ProfileNameFilter', [ ProfileNameXFilter ]);

app.factory('cache', function($cacheFactory) {
	var cache = $cacheFactory('myCache');
	return cache;
});

function ProfileNameXFilter() {
	return function(profileList) {
		var profiles = [];
		$
				.each(
						profileList,
						function(index, value) {
							// TODO : hardcoded filter parameter, need to update
							// it
							if ((value.GENETIC_ALTERATION_TYPE == "MRNA_EXPRESSION")
									&& (value.HAS_NORMAL_MAPPING != -1)
									&& ((value.STABLE_ID).toLowerCase()
											.indexOf("zscore") == -1)) {
								value.zScoreFlag = false;

								if ((value.NAME).toLowerCase().indexOf(
										"microarray") != -1) {
									value.NAME = value.NAME + " z-Score";
									value.zScoreFlag = true;
									profiles.push(value);
								} else {
									if ((value.NAME).toLowerCase().indexOf(
											"rna seq") != -1) {
										profiles.push(value);

										var newProfile = jQuery.extend(true,
												{}, value);
										newProfile.NAME = newProfile.NAME
												+ " z-Score";
										newProfile.zScoreFlag = true;
										profiles.push(newProfile);
									}
								}
							}
						});
		return profiles;
	};
}

app
		.factory(
				"LoadDataFactory",
				function($http, $filter, cache, $q) {
					return {
						// Get genetic profile details
						getProfileData : function(cancerStudyID) {
							var defer = $q.defer();
							var paramsGetProfiles = {
								cancer_study_id : cancerStudyID
							};
							$http
									.post(
											"getGeneticProfile.json",
											paramsGetProfiles,
											{
												headers : {
													'Content-Type' : 'application/x-www-form-urlencoded; charset=UTF-8'
												},
												transformRequest : transform
											}).success(function(data) {
										defer.resolve(data);
									});
							return defer.promise;
						},
						// get single cancer study Tumor vs Normals comparison
						// data
						getSingleCancerTVNData : function(cancerStudyID,
								genetic_profile_id, gene) {
							var defer = $q.defer();
							if (cache.get(cancerStudyID + '-' + gene + '-'
									+ genetic_profile_id.STABLE_ID + '-'
									+ genetic_profile_id.zScoreFlag) != null) {
								cache.get(cancer_study_id + '-' + gene + '-'
										+ genetic_profile_id.STABLE_ID + '-'
										+ genetic_profile_id.zScoreFlag);
								defer.resolve(cache.get(cancer_study_id + '-'
										+ gene + '-'
										+ genetic_profile_id.STABLE_ID + '-'
										+ genetic_profile_id.zScoreFlag));
							} else {
								var paramsGetProfiles = {
									cancer_study_id : cancerStudyID,
									genetic_profile_id : genetic_profile_id.STABLE_ID,
									gene : gene,
									case_set_id : window.PortalGlobals
											.getCaseSetId(),
									case_ids_key : window.PortalGlobals
											.getCaseIdsKey(),
									zscore_flag : genetic_profile_id.zScoreFlag
								};
								$http
										.post(
												"getTumorVsNormalData.json",
												paramsGetProfiles,
												{
													headers : {
														'Content-Type' : 'application/x-www-form-urlencoded; charset=UTF-8'
													},
													transformRequest : transform
												})
										.success(
												function(response) {
													defer.resolve(response);
													cache
															.put(
																	cancer_study_id
																			+ '-'
																			+ gene
																			+ '-'
																			+ genetic_profile_id.STABLE_ID
																			+ '-'
																			+ genetic_profile_id.zScoreFlag,
																	response);
													defer.resolve(response);
												});
							}
							return defer.promise;
						},
						// get cross cancer study Tumor vs Normals comparison
						// data
						getCrossCancerTVNData : function(cancerStudyID,
								zscore_flag, gene) {
							var defer = $q.defer();
							var paramsGetProfiles = {
								cancer_study_list : cancerStudyID,
								gene : gene,
								zscore_flag : zscore_flag
							};
							$http
									.post(
											"crossCancerTVNData.json",
											paramsGetProfiles,
											{
												headers : {
													'Content-Type' : 'application/x-www-form-urlencoded; charset=UTF-8'
												},
												transformRequest : transform
											}).success(function(response) {
										defer.resolve(response);

									});
							return defer.promise;
						}
					};
				});

app
		.controller(
				"tvnGeneParamControl",
				function($scope, $filter, $cacheFactory, LoadDataFactory, cache) {
					$scope.cancerStudies = "";

					var isCrossCancer = false;
					var initCheckBoxes = function() {
						$scope.zScoreCheckOption = true;
						$scope.logCheckOption = true;
						$scope.zScoreCheck = false;
						$scope.logCheck = false;
					};
					var showCheckBoxes = function(geneName, mappingName) {
						if (tvnBoxPlot.showcheckBoxes(
								$scope.selectedGeneOption,
								$scope.selectedNormalDataset)) {
							$scope.zScoreCheckOption = false;
							$scope.logCheckOption = false;
						}
					};

					getTVNData = function() {
						LoadDataFactory
								.getSingleCancerTVNData($scope.cancerStudies,
										$scope.selectedProfileOption,
										$scope.selectedGeneOption)
								.then(
										function(responseData) {
											tvnBoxPlot
													.initialize(
															isCrossCancer,
															responseData,
															$scope.selectedGeneOption,
															$scope.selectedProfileOption,
															$scope.thresholdValue);
											tvnBoxPlot
													.loadPlot(
															$scope.selectedGeneOption,
															$scope.selectedProfileOption['NAME']);
										});
					};
					$scope.getProfileData = function(geneList, cancerStudy) {
						$scope.cancerStudies = cancerStudy;
						$scope.geneOptions = geneList;
						$scope.selectedGeneOption = $scope.geneOptions[0];
						LoadDataFactory
								.getProfileData(cancerStudy)
								.then(
										function(data) {
											var filteredProfileNames = $filter(
													'ProfileNameFilter')(data);
											$scope.profileOptions = filteredProfileNames;
											$scope.profileSelectionHide = false;
											$scope.thresholdValueHide = false;
											$scope.selectedProfileOption = $scope.profileOptions[0];
											getTVNData();
										});
					};
					var loadCrossCancerData = function(initFlag) {
						LoadDataFactory
								.getCrossCancerTVNData($scope.cancerStudies,
										$scope.zScoreCheck,
										$scope.selectedGeneOption)
								.then(
										function(normal_data) {

											$scope.normalDatasetOptions = _
													.keys(normal_data);
											if (_.keys(normal_data).length > 1)
												$scope.normalsSelectionHide = false;
											if (initFlag) {
												showCheckBoxes(
														$scope.selectedGeneOption,
														$scope.selectedNormalDataset);
												$scope.selectedNormalDataset = $scope.normalDatasetOptions[0];
											}
											tvnBoxPlot.initialize(
													isCrossCancer, normal_data,
													$scope.selectedGeneOption,
													$scope.zScoreCheck, null);
											tvnBoxPlot
													.loadPlot(
															$scope.selectedGeneOption,
															$scope.selectedNormalDataset,
															$scope.zScoreCheck);

										});
					};
					$scope.crossCancerData = function(_isCrossCancer, geneList,
							studiesList) {
						if (geneList != null) {
							$scope.geneOptions = geneList;
							$scope.selectedGeneOption = $scope.geneOptions[0];
							$scope.cancerStudies = studiesList;
							$scope.selectedGeneOption = $scope.geneOptions[0];
						}
						if (_isCrossCancer != null)
							isCrossCancer = _isCrossCancer;
						loadCrossCancerData(true);
					};

					$scope.geneChange = function(selectedGene) {
						$('#plots_box').html(
								"<img src='images/ajax-loader.gif'>");
						if (isCrossCancer == true) {
							initCheckBoxes();
							if (tvnBoxPlot.hasData(selectedGene,
									$scope.selectedNormalDataset,
									$scope.zScoreCheck)) {
								tvnBoxPlot.loadPlot(selectedGene,
										$scope.selectedNormalDataset,
										$scope.zScoreCheck);
								showCheckBoxes(selectedGene,
										$scope.selectedNormalDataset);
							} else {
								$scope.crossCancerData();
							}
						} else {
							$scope.logCheck = false;
							if (tvnBoxPlot.hasData(selectedGene,
									$scope.selectedProfileOption['NAME'])) {
								tvnBoxPlot.loadPlot(selectedGene,
										$scope.selectedProfileOption['NAME']);
							} else {
								getTVNData();
							}
						}
					};

					$scope
							.$watch(
									'selectedProfileOption',
									function() {
										if ($scope.selectedProfileOption != null) {
											$('#plots_box')
													.html(
															"<img src='images/ajax-loader.gif'>");
											$scope.logCheck = false;
											$scope.logCheckOption = true;
											if ((($scope.selectedProfileOption.STABLE_ID)
													.toLowerCase().indexOf(
															"rna_seq") != -1)
													&& ($scope.selectedProfileOption.zScoreFlag == false)) {
												$scope.logCheckOption = false;
											}
											if (tvnBoxPlot
													.hasData(
															$scope.selectedGeneOption,
															$scope.selectedProfileOption['NAME'])) {
												tvnBoxPlot
														.loadPlot(
																$scope.selectedGeneOption,
																$scope.selectedProfileOption['NAME']);
											} else {
												getTVNData();
											}
										}
									}, true);

					$scope.normalDatasetChange = function(selectedNormalDataset) {
						$('#plots_box').html(
								"<img src='images/ajax-loader.gif'>");
						initCheckBoxes();
						if (tvnBoxPlot.hasData($scope.selectedGeneOption,
								selectedNormalDataset, $scope.zScoreCheck)) {
							tvnBoxPlot.loadPlot($scope.selectedGeneOption,
									selectedNormalDataset, $scope.zScoreCheck);
							showCheckBoxes($scope.selectedGeneOption,
									selectedNormalDataset);
						} else {
							//TODO - if there is no normal dataset
							console.log("--- No data");
						}
					};

					$scope.zScoreCheckChange = function(zScoreCheck) {
						$('#plots_box').html(
								"<img src='images/ajax-loader.gif'>");
						if (zScoreCheck) {
							$scope.logCheckOption = true;
						} else {
							$scope.logCheckOption = false;
						}
						$scope.logCheck = false;
						if (tvnBoxPlot.hasData($scope.selectedGeneOption,
								$scope.selectedNormalDataset, zScoreCheck)) {
							tvnBoxPlot.loadPlot($scope.selectedGeneOption,
									$scope.selectedNormalDataset, zScoreCheck);
						} else {
							loadCrossCancerData(false);
						}
					};

					$scope.changeThreshold = function(thresholdValue) {
						tvnBoxPlot.thresholdChange(thresholdValue);
					};
					$scope.logChange = function(logChange) {
						tvnBoxPlot.applyLog(logChange);
					};

				});
