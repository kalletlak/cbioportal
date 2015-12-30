/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mskcc.cbio.portal.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.GeneticAlterationUtil;
import org.mskcc.cbio.portal.dao.TumorVsNormalUtil;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.Gene;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.PatientList;
import org.mskcc.cbio.portal.util.AccessControl;
import org.mskcc.cbio.portal.util.InternalIdUtil;
import org.mskcc.cbio.portal.util.SpringUtil;
import org.mskcc.cbio.portal.util.XDebug;
import org.mskcc.cbio.portal.web_api.GetGeneticProfiles;
import org.mskcc.cbio.portal.web_api.GetPatientLists;
import org.mskcc.cbio.portal.web_api.ProtocolException;

/**
 * This class prepares corss cancer tumor vs normal comparison plot data
 * 
 * Key points : 1. Separate logic for profiles with rna_seq and microarray data
 * since in the database rna_ser is raw count and microarray data is log'ed 2.
 * profiles with rna_seq data has an option to show raw values, log'ed values or
 * z-scored values 3. profiles with microarray data shows only z-scored values
 * 
 * @param cancer_study_list
 *            , gene, zscore_flag
 * @return JSON objects of Tumors and Normals
 * 
 * 
 * @author Karthik Kalletla
 * 
 */
public class CrossCancerTumorVsNormalDataJSON extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4557344949733155305L;
	private final int ONE = 1;
	// private static Logger logger = Logger
	// .getLogger(CrossCancerTumorVsNormalDataJSON.class);

	// class which process access control to cancer studies
	private AccessControl accessControl;

	/**
	 * Initializes the servlet.
	 * 
	 * @throws ServletException
	 *             Serlvet Init Error.
	 */
	public void init() throws ServletException {
		super.init();
		accessControl = SpringUtil.getAccessControl();
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		doGet(request, response);
	}

	/**
	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
	 * methods.
	 * 
	 * @param request
	 *            servlet request
	 * @param response
	 *            servlet response
	 * @throws javax.servlet.ServletException
	 *             if a servlet-specific error occurs
	 * @throws java.io.IOException
	 *             if an I/O error occurs
	 */
	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		XDebug xdebug = new XDebug();
		xdebug.startTimer();

		response.setContentType("application/json");
		PrintWriter writer = response.getWriter();

		try {

			// Get the gene list
			String geneName = request.getParameter("gene");
			boolean applyZScore = Boolean.parseBoolean(request
					.getParameter("zscore_flag"));

			String cancerStudyIdListString = request
					.getParameter(QueryBuilder.CANCER_STUDY_LIST);
			String[] cancerStudyIdList = cancerStudyIdListString.split(",");

			// Cancer All Cancer Studies
			List<CancerStudy> cancerStudiesList = accessControl
					.getCancerStudies();
			HashMap<String, Boolean> studyMap = new HashMap<>();
			for (String studyId : cancerStudyIdList) {
				studyMap.put(studyId, Boolean.TRUE);
			}
			ArrayList<Double> allSamples = new ArrayList<Double>();
			Map<Integer, Map<String, ProfileSampleAndValues>> TumorSamplesCategory = new HashMap<Integer, Map<String, ProfileSampleAndValues>>();
			Map<Integer, TreeMap<String, TreeMap<String, String>>> normalSamplesMap = new HashMap<Integer, TreeMap<String, TreeMap<String, String>>>();
			Map<Integer, String> NormalsDatasetNamesMap = new HashMap<Integer, String>();
			DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
			Gene gene = daoGene.getGene(geneName);
			for (CancerStudy cancerStudy : cancerStudiesList) {

				String cancerStudyId = cancerStudy.getCancerStudyStableId();
				if (!studyMap.containsKey(cancerStudyId)) {
					continue;
				}
				if (cancerStudyId.equalsIgnoreCase("all"))
					continue;
				// if cancer dataset has genetic profile that is mapped to one
				// of normal data set
				if (cancerStudy.isNormalsMapping()) {
					ArrayList<GeneticProfile> geneticProfileList = GetGeneticProfiles
							.getGeneticProfiles(cancerStudyId);
					ArrayList<PatientList> patientSetList = GetPatientLists
							.getPatientLists(cancerStudyId);
					PatientList defaultPatientSet = null;
					// get patient list with all_cases_in_study or other
					for (PatientList patientList : patientSetList) {
						if (patientList.getPatientListCategory().getCategory()
								.equals("all_cases_in_study")
								|| patientList.getPatientListCategory()
										.getCategory().equals("other")) {
							defaultPatientSet = patientList;
							break;
						}
					}
					// if there is no patient set then skip the study
					if (defaultPatientSet == null)
						continue;
					List<String> sampleIds = defaultPatientSet.getPatientList();

					for (GeneticProfile geneticProfile : geneticProfileList) {
						if (geneticProfile.getNormalTissueMappingID() != -1) {

							// check for profile which have 'microarray' or 'rna
							// seq' string in their names( this is just to
							// double check if by mistake if a profile is mapped
							// to normal dataset
							if ((geneticProfile.getProfileName().toLowerCase()
									.contains("microarray"))
									|| (geneticProfile.getProfileName()
											.toLowerCase().contains("rna seq"))) {
								List<Integer> internalSampleIds = InternalIdUtil
										.getInternalSampleIds(geneticProfile
												.getCancerStudyId(), sampleIds);
								List<String> stableSampleIds = InternalIdUtil
										.getStableSampleIds(internalSampleIds);
								// get tumor samples data for the particular
								// gene
								List<String> tmpProfileDataArr = GeneticAlterationUtil
										.getGeneticAlterationDataRow(gene,
												internalSampleIds,
												geneticProfile);
								ProfileSampleAndValues profileSampleAndValues = null;
								if (geneticProfile.getProfileName()
										.toLowerCase().contains("microarray")) {
									profileSampleAndValues = new ProfileSampleAndValues(
											stableSampleIds, tmpProfileDataArr,
											false,
											geneticProfile.getStableId(),
											defaultPatientSet.getStableId(),
											cancerStudy);
								} else {
									profileSampleAndValues = new ProfileSampleAndValues(
											stableSampleIds, tmpProfileDataArr,
											true, geneticProfile.getStableId(),
											defaultPatientSet.getStableId(),
											cancerStudy);
								}
								if (TumorSamplesCategory.keySet().contains(
										geneticProfile
												.getNormalTissueMappingID())) {
									TumorSamplesCategory
											.get(geneticProfile
													.getNormalTissueMappingID())
											.put(cancerStudy
													.getCancerStudyStableId(),
													profileSampleAndValues);
								} else {
									Map<String, ProfileSampleAndValues> map = new HashMap<String, ProfileSampleAndValues>();
									map.put(cancerStudy
											.getCancerStudyStableId(),
											profileSampleAndValues);
									TumorSamplesCategory.put(geneticProfile
											.getNormalTissueMappingID(), map);
								}

								for (String sampleValue : tmpProfileDataArr) {
									if (!sampleValue.equals("NaN")) {
										if (geneticProfile.getStableId()
												.toLowerCase()
												.indexOf("rna_seq") != -1) {
											double tempVal = ONE
													+ Double.parseDouble(sampleValue);
											allSamples.add(Math.log(tempVal)
													/ Math.log(2));
										} else {
											allSamples.add(Double
													.parseDouble(sampleValue));
										}
									}
								}
							}
						}
					}
				}
			}

			// get normals dataset samples data
			for (Integer mappingID : TumorSamplesCategory.keySet()) {
				CanonicalGene canonicalGene = (CanonicalGene) gene;
				TreeMap<String, TreeMap<String, TreeMap<String, String>>> normalsMap = TumorVsNormalUtil
						.getNormals(canonicalGene.getHugoGeneSymbolAllCaps(),
								mappingID);
				String normalDatasetName = normalsMap.firstKey();
				normalSamplesMap.put(mappingID,
						normalsMap.get(normalDatasetName));
				NormalsDatasetNamesMap.put(mappingID, normalDatasetName);
				TreeMap<String, TreeMap<String, String>> normalsTempMap = normalsMap
						.get(normalDatasetName);
				for (String tissue : normalsTempMap.keySet()) {
					for (String sample : normalsTempMap.get(tissue).keySet()) {
						double tempVal = ONE
								+ Double.parseDouble(normalsTempMap.get(tissue)
										.get(sample));
						allSamples.add(Math.log(tempVal) / Math.log(2));
					}
				}
			}

			Mean mean = new Mean();
			Double[] a = (Double[]) (allSamples.toArray(new Double[allSamples
					.size()]));
			double[] d = ArrayUtils.toPrimitive(a);
			double meanVal = mean.evaluate(d, 0, d.length);
			StandardDeviation deviation = new StandardDeviation();
			double deviationVal = deviation.evaluate(d, meanVal, 0, d.length);
			JSONObject resultObject = new JSONObject();

			for (Integer mappingID : TumorSamplesCategory.keySet()) {
				JSONObject tempResultObject = new JSONObject();
				Map<String, ProfileSampleAndValues> profilesMap = TumorSamplesCategory
						.get(mappingID);
				JSONArray cancerDatasetArrayObj = new JSONArray();
				boolean canApplyZscore = false;
				boolean flag = true;
				for (String key : profilesMap.keySet()) {
					ProfileSampleAndValues profileDetails = profilesMap
							.get(key);
					boolean isAlreadyLoged = (profileDetails
							.getGeneticProfileID().toLowerCase()
							.indexOf("rna_seq") == -1) ? true : false;
					if (flag) {
						canApplyZscore = !isAlreadyLoged;
						flag = false;
					}
					if (canApplyZscore == isAlreadyLoged) {
						continue;
					}
					List<String> sampleIds = profileDetails.getSampleIds();
					List<String> values = profileDetails.getValues();

					JSONObject cancerDatasetObj = new JSONObject();
					JSONObject tumorSamplesObject = new JSONObject();
					for (int i = 0; i < sampleIds.size(); i++) {
						if (!values.get(i).equals("NaN")) {
							double value;
							if (isAlreadyLoged) {
								value = (Double.parseDouble(values.get(i)) - meanVal)
										/ deviationVal;
							} else {
								if (applyZScore) {
									double tempVal = ONE
											+ Double.parseDouble(values.get(i));
									value = ((Math.log(tempVal) / Math.log(2)) - meanVal)
											/ deviationVal;
								} else {
									value = Double.parseDouble(values.get(i)
											+ ONE);
								}
							}

							tumorSamplesObject.put(sampleIds.get(i), value);
						}
					}
					cancerDatasetObj.put("CANCER_STUDY_ID", profileDetails
							.getCancerStudy().getCancerStudyStableId());
					cancerDatasetObj.put("CANCER_STUDY_NAME", profileDetails
							.getCancerStudy().getName());
					cancerDatasetObj.put("CANCER_TYPE", profileDetails
							.getCancerStudy().getTypeOfCancerId());
					cancerDatasetObj.put("GENETIC_PROFILE",
							profileDetails.getGeneticProfileID());
					cancerDatasetObj.put("SAMPLES_MAP", tumorSamplesObject);
					cancerDatasetArrayObj.add(cancerDatasetObj);
				}
				TreeMap<String, TreeMap<String, String>> normalsMap = normalSamplesMap
						.get(mappingID);
				JSONObject normalJsonObject = new JSONObject();
				for (String tissue : normalsMap.keySet()) {
					JSONObject mappingJsonObject = new JSONObject();
					JSONObject normalTissueObject = new JSONObject();
					TreeMap<String, String> temp = normalsMap.get(tissue);
					for (String sample : temp.keySet()) {
						double tempVal = ONE
								+ Double.parseDouble(temp.get(sample));
						double value = Math.log(tempVal) / Math.log(2);
						value = (value - meanVal) / deviationVal;

						if (canApplyZscore) {
							if (!applyZScore) {
								value = Double.parseDouble(normalsMap.get(
										tissue).get(sample)
										+ ONE);
							}
						}
						normalTissueObject.put(sample, value);
					}
					mappingJsonObject.put("SAMPLES_MAP", normalTissueObject);
					normalJsonObject.put(tissue, mappingJsonObject);
				}
				tempResultObject.put("TUMOR_SAMPLES", cancerDatasetArrayObj);
				tempResultObject.put("NORMAL_SAMPLES", normalJsonObject);
				tempResultObject.put("SHOW_ZSCORE_CHECKBOX", canApplyZscore);
				resultObject.put(NormalsDatasetNamesMap.get(mappingID),
						tempResultObject);

			}

			response.setContentType("application/json");
			PrintWriter out = response.getWriter();
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(out, resultObject);

		} catch (DaoException e) {
			throw new ServletException(e);
		} catch (ProtocolException e) {
			throw new ServletException(e);
		} finally {
			writer.close();
		}
	}
}

class ProfileSampleAndValues {
	private List<String> sampleIds;
	private List<String> values;
	private String geneticProfileID;
	private String patientListStableID;
	private boolean logApplied;
	private CancerStudy cancerStudy;

	ProfileSampleAndValues(List<String> sampleIds, List<String> values,
			boolean logApplied, String geneticProfileID,
			String patientListStableID, CancerStudy cancerStudy) {
		this.sampleIds = sampleIds;
		this.values = values;
		this.logApplied = logApplied;
		this.geneticProfileID = geneticProfileID;
		this.patientListStableID = patientListStableID;
		this.cancerStudy = cancerStudy;
	}

	public List<String> getSampleIds() {
		return sampleIds;
	}

	public List<String> getValues() {
		return values;
	}

	public boolean isLogApplied() {
		return logApplied;
	}

	public String getGeneticProfileID() {
		return geneticProfileID;
	}

	public String getPatientListStableID() {
		return patientListStableID;
	}

	public CancerStudy getCancerStudy() {
		return cancerStudy;
	}

}
