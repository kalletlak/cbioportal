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
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mskcc.cbio.portal.model.CanonicalGene;

/**
 * This class creates DAO to tumor vs normals data
 * 
 * 
 * @author Karthik Kalletla
 * 
 */

public class DaoTumorVsNormal {
	private static DaoTumorVsNormal daoTumorVsNormal = null;
	private static final String DELIM = ",";
	private static final Log LOG = LogFactory.getLog(DaoTumorVsNormal.class);

	/**
	 * This method returns the list of objects which contains normals samples
	 * data
	 * 
	 * @param String
	 *            cancerStudyId
	 * @param String
	 *            geneid
	 * @param String
	 *            technology
	 * @return TreeMap<String, TreeMap<String, TreeMap<String, String>>>
	 */
	public TreeMap<String, TreeMap<String, TreeMap<String, String>>> getNormalsByCancerStudyAndGeneId(
			String geneid, int technology) {
		TreeMap<String, TreeMap<String, String>> normalsMap = new TreeMap<String, TreeMap<String, String>>();
		TreeMap<String, TreeMap<String, TreeMap<String, String>>> normalsFinalMap = new TreeMap<String, TreeMap<String, TreeMap<String, String>>>();

		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sampleList[] = null;

		LinkedHashMap<String, String> orderedSampleList = getNormalTissueSampleMap(technology);
		try {
			// Creating JDBC connection object
			con = JdbcUtil.getDbConnection(DaoTumorVsNormal.class);
			// Normlas prepared statement
			String normalDatasetName = "";
			pstmt = con
					.prepareStatement("SELECT * FROM normals_sample_list WHERE NORMALS_MAPPING_ID=?");
			pstmt.setInt(1, technology);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				String samples = rs.getString("ORDERED_SAMPLE_LIST");
				normalDatasetName = rs.getString("TECHNOLOGY_NAME");
				sampleList = samples.trim().split(DELIM);
			}
			pstmt.close();

			pstmt = con
					.prepareStatement("SELECT * FROM normals_sample_data WHERE HUGO_GENE_SYMBOL=? AND NORMALS_MAPPING_ID=?");
			pstmt.setString(1, geneid);
			pstmt.setInt(2, technology);
			rs = pstmt.executeQuery();

			// Extracting information form normals
			if (rs.next()) {
				String values = rs.getString("VALUES").trim();
				String valueParts[] = values.trim().split(DELIM);
				int index = 0;
				Map<String, String> sampleMap = new HashMap<String, String>();

				for (String sample : sampleList) {
					String value = valueParts[index];
					sampleMap.put(sample.trim(), value);
					index++;
				}

				for (String tissue : orderedSampleList.keySet()) {
					String samples = orderedSampleList.get(tissue);
					String sampleParts[] = samples.trim().split(DELIM);
					if (sampleParts.length > 1) {
						TreeMap<String, String> linkedHashMap = new TreeMap<String, String>();
						for (String sample : sampleParts) {
							linkedHashMap.put(sample, sampleMap.get(sample));
						}

						normalsMap.put(tissue, linkedHashMap);
					}
				}
				normalsFinalMap.put(normalDatasetName, normalsMap);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			JdbcUtil.closeAll(DaoTumorVsNormal.class, con, pstmt, rs);
		}
		return normalsFinalMap;
	}

	/**
	 * this method returns normals tissue and samples map
	 * 
	 * @param technology
	 * @return LinkedHashMap<String, String>
	 */
	private LinkedHashMap<String, String> getNormalTissueSampleMap(
			int technology) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		LinkedHashMap<String, String> samplesMap = new LinkedHashMap<String, String>();
		try {
			con = JdbcUtil.getDbConnection(DaoTumorVsNormal.class);
			pstmt = con
					.prepareStatement("SELECT * FROM normals_sample_mapping WHERE NORMALS_MAPPING_ID=? ORDER BY ID ASC");
			pstmt.setInt(1, technology);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				samplesMap.put(rs.getString("TISSUE"), rs.getString("SAMPLES")
						.trim());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			JdbcUtil.closeAll(DaoTumorVsNormal.class, con, pstmt, rs);
		}
		return samplesMap;
	}

	/**
	 * private constructor to make it singleton
	 */
	private DaoTumorVsNormal() {
	}

	/**
	 * getInstance method return instance of DaoTumorVsNormal class
	 * 
	 * @return DaoTumorVsNormal
	 * @throws DaoException
	 */
	public static DaoTumorVsNormal getInstance() {
		if (daoTumorVsNormal == null) {
			daoTumorVsNormal = new DaoTumorVsNormal();
		}
		return daoTumorVsNormal;
	}

	/**
	 * This method deletes normals data with matched technology
	 * 
	 * @param substring
	 * @return int
	 */

	public static int getNormalMappingID(String technology) {
		int mappingID = -1;
		PreparedStatement pstmt = null;
		Connection con = null;
		ResultSet rs = null;
		try {
			con = JdbcUtil.getDbConnection(DaoTumorVsNormal.class);
			pstmt = con
					.prepareStatement("SELECT NORMALS_MAPPING_ID FROM normals_sample_list WHERE TECHNOLOGY_NAME = ?");
			pstmt.setString(1, technology);
			ResultSet resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				mappingID = resultSet.getInt("NORMALS_MAPPING_ID");
			}

			pstmt.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			JdbcUtil.closeAll(DaoTumorVsNormal.class, con, pstmt, rs);
		}

		return mappingID;
	}

	public int deleteNrmlDataWithTechnology(String technology) {
		PreparedStatement pstmt = null;
		Connection con = null;
		ResultSet rs = null;
		int mappingID = -1;
		try {
			con = JdbcUtil.getDbConnection(DaoTumorVsNormal.class);
			mappingID = getNormalMappingID(technology);

			if (mappingID != -1) {
				String[] sqls = {
						"DELETE FROM normals_sample_data WHERE NORMALS_MAPPING_ID = ?",
						"DELETE FROM normals_sample_mapping WHERE NORMALS_MAPPING_ID = ?",
						"DELETE FROM normals_sample_list WHERE NORMALS_MAPPING_ID = ?" };

				for (String sql : sqls) {
					pstmt = con.prepareStatement(sql);
					if (sql.contains("?")) {
						pstmt.setInt(1, mappingID);
					}
					pstmt.executeUpdate();
				}
			}

			pstmt.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			JdbcUtil.closeAll(DaoTumorVsNormal.class, con, pstmt, rs);
		}
		return mappingID;

	}

	/**
	 * This method inserts normal data into the database
	 * 
	 * @param String
	 *            tableName
	 * @param String
	 *            geneSymbol
	 * @param String
	 *            stableID
	 * @param String
	 *            value
	 * @param String
	 *            differentiator
	 * @param String
	 *            technology
	 */
	public void addNormalDatum(String tableName, String geneSymbol,
			String stableID, String value, String differentiator,
			String technology) {
		if (MySQLbulkLoader.isBulkLoad()) {
			MySQLbulkLoader.getMySQLbulkLoader(tableName).insertRecord(
					geneSymbol, stableID, value, differentiator, technology);
		}

	}

	/**
	 * This method tells whether is there any normal dataset with that
	 * technology name
	 * 
	 * @param String
	 * @return boolean
	 * @throws SQLException
	 */
	public static boolean isTechnologyPresent(String technologyName)
			throws SQLException {
		PreparedStatement pstmt = null;
		Connection con = null;
		ResultSet rs = null;
		boolean flag = false;
		try {
			con = JdbcUtil.getDbConnection(DaoTumorVsNormal.class);
			pstmt = con
					.prepareStatement("SELECT COUNT(*) FROM normals_sample_list WHERE TECHNOLOGY_NAME = ?");
			pstmt.setString(1, technologyName);
			ResultSet resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				return resultSet.getInt(1) > 0 ? true : false;
			}

		} catch (SQLException e) {
			if (e.getErrorCode() == 1146) {
				if (LOG.isInfoEnabled()) {
					LOG.info("Normals dataset tables not found, creating it.");
				}
				try {
					createTables();
				} catch (SQLException e1) {

					if (LOG.isInfoEnabled()) {
						LOG.info("Error creating table.");
						LOG.info("isTechnologyPresent(), Error trace: "
								+ e.getMessage());
					}
					throw e;
				}
			}
			return false;
		} finally {
			JdbcUtil.closeAll(DaoTumorVsNormal.class, con, pstmt, rs);
		}
		return flag;
	}

	/**
	 * this method inserts normal tissue samples values corresponding to the
	 * gene
	 * 
	 * @param tableName
	 * @param mappingID
	 * @param gene
	 * @param values
	 */
	public void addNormalDatum(String tableName, int mappingID,
			CanonicalGene gene, String[] values) {
		StringBuffer valueBuffer = new StringBuffer();
		for (String value : values) {
			if (value.contains(DELIM)) {
				throw new IllegalArgumentException(
						"Value cannot contain delim:  " + DELIM + " --> "
								+ value);
			}
			valueBuffer.append(value).append(DELIM);
		}
		if (MySQLbulkLoader.isBulkLoad()) {
			MySQLbulkLoader.getMySQLbulkLoader(tableName).insertRecord(
					String.valueOf(mappingID), gene.getHugoGeneSymbolAllCaps(),
					valueBuffer.toString());
		}
	}

	/**
	 * this method inserts tissue samples map i.e, samples corresponding to
	 * tissue type
	 * 
	 * @param tableName
	 * @param mappingID
	 * @param tissueName
	 * @param sampleIds
	 */
	public void addNormalSampleMapingDatum(String tableName, int mappingID,
			String tissueName, String[] sampleIds) {
		StringBuffer valueBuffer = new StringBuffer();
		for (String value : sampleIds) {
			valueBuffer.append(value).append(DELIM);
		}
		if (MySQLbulkLoader.isBulkLoad()) {
			MySQLbulkLoader.getMySQLbulkLoader(tableName).insertRecord(
					"DEFAULT", String.valueOf(mappingID), tissueName,
					valueBuffer.toString());
		}
	}

	/**
	 * this method inserts new normal datased record
	 * 
	 * @param technologyName
	 * @param sampleIds
	 * @return int
	 */
	public int insertNormalSampleList(String technologyName, String[] sampleIds) {
		PreparedStatement pstmt = null;
		Connection con = null;
		ResultSet rs = null;
		StringBuffer valueBuffer = new StringBuffer();
		int mappingID = -1;
		try {

			for (String value : sampleIds) {
				if (value.contains(DELIM)) {
					throw new IllegalArgumentException(
							"Value cannot contain delim:  " + DELIM + " --> "
									+ value);
				}
				valueBuffer.append(value).append(DELIM);
			}

			con = JdbcUtil.getDbConnection(DaoTumorVsNormal.class);
			pstmt = con
					.prepareStatement(
							"INSERT INTO normals_sample_list(TECHNOLOGY_NAME,ORDERED_SAMPLE_LIST) VALUES (?,?)",
							Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, technologyName.toLowerCase());
			pstmt.setString(2, valueBuffer.toString());
			pstmt.executeUpdate();
			rs = pstmt.getGeneratedKeys();
			if (rs.next()) {
				mappingID = rs.getInt(1);
			}
			con.close();
			return mappingID;
		} catch (SQLException e) {
			e.printStackTrace();
			return mappingID;
		} finally {
			JdbcUtil.closeAll(DaoTumorVsNormal.class, con, pstmt, rs);
		}
	}

	/**
	 * this method creates new tables required to store normal tissue samples
	 * data
	 * 
	 */
	private static void createTables() throws SQLException {
		String createNormalSampleListTable = "CREATE TABLE NORMALS_SAMPLE_LIST (NORMALS_MAPPING_ID int(11) NOT NULL AUTO_INCREMENT,TECHNOLOGY_NAME varchar(50) DEFAULT NULL,ORDERED_SAMPLE_LIST longtext,PRIMARY KEY (NORMALS_MAPPING_ID))";
		String createNormalsSampleData = "CREATE TABLE normals_sample_data (NORMALS_MAPPING_ID int(11) NOT NULL,HUGO_GENE_SYMBOL varchar(255) NOT NULL,`VALUES` longtext NOT NULL,KEY QUICK_LOOK_UP (HUGO_GENE_SYMBOL,NORMALS_MAPPING_ID))";
		String createNormlasSampleMapping = "CREATE TABLE normals_sample_mapping (ID int(11) NOT NULL AUTO_INCREMENT,NORMALS_MAPPING_ID int(11) NOT NULL,TISSUE varchar(255) NOT NULL DEFAULT '',SAMPLES longtext NOT NULL,PRIMARY KEY (ID))";

		PreparedStatement pstmt = null;
		Connection con = null;
		ResultSet rs = null;
		try {
			con = JdbcUtil.getDbConnection(DaoTumorVsNormal.class);
			pstmt = con.prepareStatement(createNormalSampleListTable);
			pstmt.executeUpdate();
			pstmt = con.prepareStatement(createNormalsSampleData);
			pstmt.executeUpdate();
			pstmt = con.prepareStatement(createNormlasSampleMapping);
			pstmt.executeUpdate();
			pstmt.close();
			con.close();
		} catch (SQLException e) {
			if (LOG.isInfoEnabled()) {
				LOG.info("createTables(), Error trace: " + e.getMessage());
			}
			throw e;

		} finally {
			JdbcUtil.closeAll(DaoTumorVsNormal.class, con, pstmt, rs);
		}
	}
}
