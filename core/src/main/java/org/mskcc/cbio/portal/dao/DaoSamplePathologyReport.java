package org.mskcc.cbio.portal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DaoSamplePathologyReport {

	public Map<String, String> getAllSamplesPathologyReport() {

		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Map<String, String> returnMap = new HashMap<String, String>();
		try {
			con = JdbcUtil.getDbConnection(DaoSample.class);
			pstmt = con
					.prepareStatement("SELECT * FROM sample_pathology_report");
			rs = pstmt.executeQuery();
			while (rs.next()) {
				returnMap.put(rs.getString("STABLE_ID"), rs.getString("LINK"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			JdbcUtil.closeAll(DaoSample.class, con, pstmt, rs);
		}
		return returnMap;
	}
}
