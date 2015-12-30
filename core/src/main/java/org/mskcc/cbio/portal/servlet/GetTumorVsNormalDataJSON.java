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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.inference.TTest;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.dao.DaoPatientList;
import org.mskcc.cbio.portal.dao.GeneticAlterationUtil;
import org.mskcc.cbio.portal.dao.TumorVsNormalUtil;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.Gene;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.PatientList;
import org.mskcc.cbio.portal.util.InternalIdUtil;
import org.mskcc.cbio.portal.util.PatientSetUtil;
import org.mskcc.cbio.portal.util.XssRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class returns object required for tumor vs normal visualization tab for
 * a single cancer dataset study.
 * 
 * Key points : 1. Separate logic for profiles with rna_seq and microarray data
 * since in the database rna_ser is raw count and microarray data is log'ed 2.
 * profiles with rna_seq data has an option to show raw values, log'ed values or
 * z-scored values 3. profiles with microarray data shows only z-scored values
 * 
 * @param cancer_study_id
 *            ,genetic_profile_id,case_set_id, case_ids_key,zscore_flag, gene
 * @return JSON objects of Tumors and Normals
 * 
 * 
 * @author Karthik Kalletla
 * 
 */
public class GetTumorVsNormalDataJSON extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory
			.getLogger(GetTumorVsNormalDataJSON.class);
	private final int ONE = 1;

	/**
	 * Handles HTTP GET Request.
	 * 
	 * @param httpServletRequest
	 *            HttpServletRequest
	 * @param httpServletResponse
	 *            HttpServletResponse
	 * @throws ServletException
	 */
	protected void doGet(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) throws ServletException,
			IOException {
		doPost(httpServletRequest, httpServletResponse);
	}

	/**
	 * Handles the HTTP POST Request.
	 * 
	 * @param httpServletRequest
	 *            HttpServletRequest
	 * @param httpServletResponse
	 *            HttpServletResponse
	 * @throws ServletException
	 */
	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) throws ServletException,
			IOException {
		String cancerStudyIdentifier = httpServletRequest
				.getParameter("cancer_study_id");
		String geneticProfileId = httpServletRequest
				.getParameter("genetic_profile_id");
		String patientSetId = httpServletRequest.getParameter("case_set_id");
		String patientIdsKey = httpServletRequest.getParameter("case_ids_key");
		boolean applyZScore = Boolean.parseBoolean(httpServletRequest
				.getParameter("zscore_flag"));
		String rawGeneIdList;
		if (httpServletRequest instanceof XssRequestWrapper) {
			rawGeneIdList = ((XssRequestWrapper) httpServletRequest)
					.getRawParameter("gene");
		} else {
			// Get the gene list
			rawGeneIdList = httpServletRequest.getParameter("gene");
		}

		String[] geneIdList = rawGeneIdList.split("\\s+");

		ObjectMapper mapper = new ObjectMapper();
		JSONObject resultJsonObject = new JSONObject();
		JSONObject finalResultObject = new JSONObject();
		JSONObject tumorJsonObject = new JSONObject();
		JSONObject normalJsonObject = new JSONObject();
		try {
			// Get the cancer study details
			CancerStudy cancerStudy = DaoCancerStudy
					.getCancerStudyByStableId(cancerStudyIdentifier);
			GeneticProfile geneticProfile = DaoGeneticProfile
					.getGeneticProfileByStableId(geneticProfileId);
			if (cancerStudy == null) {
				throw new NullPointerException("Unknown cancer study id: "
						+ cancerStudyIdentifier);
			}
			// Check whether cancer study has a normal dataset mapping and see
			// whether the genetic profile is mapped to one of normal dataset
			if ((cancerStudy.isNormalsMapping() != false)
					&& (geneticProfile.getNormalTissueMappingID() != -1)) {

				DaoPatientList daoPatientList = new DaoPatientList();
				PatientList patientList = null;
				ArrayList<String> patientIdList = new ArrayList<String>();
				if (patientSetId.equals("-1")) {
					String strPatientIds = PatientSetUtil
							.getPatientIds(patientIdsKey);
					String[] patientArray = strPatientIds.split("\\s+");
					for (String item : patientArray) {
						patientIdList.add(item);
					}
				} else {
					patientList = daoPatientList
							.getPatientListByStableId(patientSetId);
					patientIdList = patientList.getPatientList();
				}
				List<Integer> internalSampleIds = InternalIdUtil
						.getInternalNonNormalSampleIds(
								cancerStudy.getInternalId(), patientIdList);
				List<String> stableSampleIds = InternalIdUtil
						.getStableSampleIds(internalSampleIds);
				for (String geneId : geneIdList) {
					DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
					Gene gene = daoGene.getGene(geneId);
					ArrayList<Double> allSamples = new ArrayList<Double>();

					// Get tumor samplevalues
					ArrayList<String> tmpProfileDataArr = GeneticAlterationUtil
							.getGeneticAlterationDataRow(gene,
									internalSampleIds, geneticProfile);
					for (int index = 0; index < stableSampleIds.size(); index++) {
						if (!tmpProfileDataArr.get(index).equals("NaN")) {
							if (geneticProfileId.indexOf("rna_seq") != -1) {
								double tempVal = ONE
										+ Double.parseDouble(tmpProfileDataArr
												.get(index));
								allSamples.add(Math.log(tempVal) / Math.log(2));
							} else {
								allSamples.add(Double
										.parseDouble(tmpProfileDataArr
												.get(index)));
							}
						}
					}
					// Get normal sample values
					TreeMap<String, TreeMap<String, TreeMap<String, String>>> normalsMap = TumorVsNormalUtil
							.getNormals(geneId,
									geneticProfile.getNormalTissueMappingID());
					String normalDatasetName = normalsMap.firstKey();
					TreeMap<String, TreeMap<String, String>> normalsTempMap = normalsMap
							.get(normalDatasetName);
					for (String tissue : normalsTempMap.keySet()) {
						for (String sample : normalsTempMap.get(tissue)
								.keySet()) {
							double tempVal = ONE
									+ Double.parseDouble(normalsTempMap.get(
											tissue).get(sample));
							allSamples.add(Math.log(tempVal) / Math.log(2));
						}
					}
					Mean mean = new Mean();
					Double[] doubleObjects = (Double[]) (allSamples
							.toArray(new Double[allSamples.size()]));
					double[] doubleValues = ArrayUtils
							.toPrimitive(doubleObjects);
					double meanVal = mean.evaluate(doubleValues, 0,
							doubleValues.length);
					StandardDeviation deviation = new StandardDeviation();
					double deviationVal = deviation.evaluate(doubleValues,
							meanVal, 0, doubleValues.length);
					double[] normalizedTumorVal = new double[stableSampleIds
							.size()];
					JSONObject tumorTissueObject = new JSONObject();
					int tumorSizeCount = 0;
					for (int index = 0; index < stableSampleIds.size(); index++) {
						if (!tmpProfileDataArr.get(index).equals("NaN")) {
							double value;
							if (geneticProfileId.indexOf("rna_seq") != -1) {
								double tempVal = ONE
										+ Double.parseDouble(tmpProfileDataArr
												.get(index));
								value = ((Math.log(tempVal) / Math.log(2)) - meanVal)
										/ deviationVal;
							} else {
								value = (Double.parseDouble(tmpProfileDataArr
										.get(index)) - meanVal) / deviationVal;
							}

							normalizedTumorVal[tumorSizeCount] = value;
							if (!applyZScore) {
								value = Double.parseDouble(tmpProfileDataArr
										.get(index) + ONE);
							}

							tumorTissueObject.put(stableSampleIds.get(index),
									value);
							tumorSizeCount++;
						}
					}
					tumorJsonObject.put("CANCER_STUDY_ID",
							cancerStudy.getCancerStudyStableId());
					tumorJsonObject.put("CANCER_STUDY_NAME",
							cancerStudy.getName());
					tumorJsonObject.put("CANCER_TYPE",
							cancerStudy.getTypeOfCancerId());
					tumorJsonObject.put("GENETIC_PROFILE", geneticProfileId);
					tumorJsonObject.put("SAMPLES_MAP", tumorTissueObject);
					JSONArray cancerDatasetArrayObj = new JSONArray();
					cancerDatasetArrayObj.add(tumorJsonObject);
					for (String tissue : normalsMap.get(normalDatasetName)
							.keySet()) {
						int count = 0;
						JSONObject mappingJsonObject = new JSONObject();
						String pValue = "-";
						JSONObject normalTissueObject = new JSONObject();
						TreeMap<String, String> temp = normalsMap.get(
								normalDatasetName).get(tissue);
						double[] normalizedNormalVal = new double[temp.size()];
						for (String sample : temp.keySet()) {
							double tempVal = ONE
									+ Double.parseDouble(temp.get(sample));
							double value = Math.log(tempVal) / Math.log(2);
							value = (value - meanVal) / deviationVal;
							normalizedNormalVal[count] = value;
							if (!applyZScore) {
								value = Double.parseDouble(normalsMap
										.get(normalDatasetName).get(tissue)
										.get(sample)
										+ ONE);
							}
							normalTissueObject.put(sample, value);
							count++;
						}
						pValue = calculatePvalues(normalizedTumorVal,
								normalizedNormalVal);
						mappingJsonObject
								.put("SAMPLES_MAP", normalTissueObject);
						mappingJsonObject.put("PVALUE", pValue);
						normalJsonObject.put(tissue, mappingJsonObject);

					}
					resultJsonObject
							.put("TUMOR_SAMPLES", cancerDatasetArrayObj);
					resultJsonObject.put("NORMAL_SAMPLES", normalJsonObject);
					finalResultObject.put(geneId, resultJsonObject);
				}
			} else {
				throw new NullPointerException("No data for Visualization ");
			}
		} catch (DaoException | NullPointerException e) {
			e.printStackTrace();
			finalResultObject = new JSONObject();
			LOGGER.error("Caught DaoException: " + e.getMessage());
		}
		httpServletResponse.setContentType("application/json");
		PrintWriter out = httpServletResponse.getWriter();
		mapper.writeValue(out, finalResultObject);

	}

	/**
	 * This method calculates p values between the tumor and normals data set
	 * 
	 * @param normalizedNormalVal
	 * @param normalizedTumorVal
	 * @return
	 */
	private String calculatePvalues(double[] normalizedTumorVal,
			double[] normalizedNormalVal) {
		String pValues = "-";
		TTest test = new TTest();
		NumberFormat formatter = new DecimalFormat("0.##E00");
		Double output = test.tTest(normalizedTumorVal, normalizedNormalVal);
		pValues = String.valueOf(formatter.format(output));
		return pValues;
	}
}
