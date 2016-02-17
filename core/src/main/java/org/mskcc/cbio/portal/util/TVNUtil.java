/** Copyright (c) 2015 Childrens Hospital of Philadelphia.
 * 
 */
package org.mskcc.cbio.portal.util;

import java.util.ArrayList;
import java.util.List;

import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.web_api.ProtocolException;

/**
 * Utility class for retrieving tumor and normals data
 *
 */

/**
 * @author kalletlak
 * 
 */
public class TVNUtil {

	/**
	 * This method finds whether the cancer study identifiers has tumor vs normals visualization data
	 * 
	 * @param cancerStudyIdListString
	 * @param geneId
	 * @param technology
	 * @return Boolean
	 */
	public static Boolean hasTVNData(String cancerStudyIdListString) {
		String[] cancerStudyIdList = cancerStudyIdListString.split(",");
		List<CancerStudy> cancerStudiesList = null;
		try {
			cancerStudiesList = SpringUtil.getAccessControl()
					.getCancerStudies();
		} catch (DaoException | ProtocolException e) {
			e.printStackTrace();
		}
		List<String> studyMap = new ArrayList<String>();
		for (String studyId : cancerStudyIdList) {
			studyMap.add(studyId);
		}
		for (CancerStudy cancerStudy : cancerStudiesList) {
			String cancerStudyId = cancerStudy.getCancerStudyStableId();
			if ((studyMap.contains(cancerStudyId))
					&& (cancerStudy.isNormalsMapping() == true)) {
				return true;
			}
		}
		return false;
	}

}
