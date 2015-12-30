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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class get the pathology report path of the samples that are mapped
 * within the institution
 * 
 * @author Karthik Kalletla
 * 
 */
public class DaoSamplePathologyReport {

	public Map<String, String> getAllSamplesPathologyReport() {

		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Map<String, String> samplesReportMap = new HashMap<String, String>();
		try {
			con = JdbcUtil.getDbConnection(DaoSample.class);
			pstmt = con
					.prepareStatement("SELECT * FROM sample_pathology_report");
			rs = pstmt.executeQuery();
			while (rs.next()) {
				samplesReportMap.put(rs.getString("STABLE_ID"),
						rs.getString("LINK"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			JdbcUtil.closeAll(DaoSample.class, con, pstmt, rs);
		}
		return samplesReportMap;
	}
}
