/** Copyright (c) 2014 Childrens Hospital of Philadelphia.
 * 
 */
package org.mskcc.cbio.portal.dao;

import java.util.TreeMap;

/**
 * Utility class for retrieving tumor and normals data
 *
 */

/**
 * @author kalletlak
 * 
 */
public class TumorVsNormalUtil {
	private static DaoTumorVsNormal daoTumorVsNormal = DaoTumorVsNormal.getInstance();

	/**
	 * 
	 * This method retrieves normals details from database and returns normals
	 * detail map
	 * 
	 * @param cancerStudyId
	 * @param geneid
	 * @param technology
	 * @return TreeMap
	 */
	public static TreeMap<String,TreeMap<String, TreeMap<String, String>>> getNormals(String geneid,
			int technology) {
		TreeMap<String,TreeMap<String, TreeMap<String, String>>> beans = daoTumorVsNormal
				.getNormalsByCancerStudyAndGeneId(geneid, technology);
		return beans;
	}
	
}
