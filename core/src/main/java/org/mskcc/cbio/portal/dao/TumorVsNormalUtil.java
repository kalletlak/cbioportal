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

package org.mskcc.cbio.portal.dao;

import java.sql.SQLException;
import java.util.TreeMap;

/**
 * Utility class for retrieving tumor and normals data
 *
 */

/**
 * @author Karthik Kalletla
 * 
 */
public class TumorVsNormalUtil {
	private static DaoTumorVsNormal daoTumorVsNormal = DaoTumorVsNormal
			.getInstance();

	/**
	 * 
	 * This method retrieves normals details from database and returns normals
	 * detail map
	 * 
	 * @param cancerStudyId
	 * @param geneid
	 * @param technology
	 * @return TreeMap
	 * @throws SQLException 
	 */
	public static TreeMap<String, TreeMap<String, TreeMap<String, String>>> getNormals(
			String geneid, int technology) throws SQLException {
		TreeMap<String, TreeMap<String, TreeMap<String, String>>> normalSamplesMap = daoTumorVsNormal
				.getNormalsByCancerStudyAndGeneId(geneid, technology);
		return normalSamplesMap;
	}

}
