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

package org.mskcc.cbio.portal.scripts;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoTumorVsNormal;
import org.mskcc.cbio.portal.dao.MySQLbulkLoader;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.ProgressMonitor;

/**
 * This class imports tumor and normal tissue data
 */
/**
 * @author Karthik Kalletla
 * 
 */
public class ImportTumorVsNrmlData {

	public int importNormalData(BufferedReader br, String technology,
			ProgressMonitor pMonitor) throws DaoException {
		try {

			MySQLbulkLoader.bulkLoadOn();
			String headerLine = br.readLine();
			String parts[] = headerLine.split("\t");
			int lenParts = parts.length;
			int sampleStartIndex = 1;
			int hugoSymbolIndex = 0;
			DaoTumorVsNormal daoTumorVsNormal = DaoTumorVsNormal.getInstance();
			String sampleIds[];
			sampleIds = new String[parts.length - sampleStartIndex];
			System.arraycopy(parts, sampleStartIndex, sampleIds, 0,
					parts.length - sampleStartIndex);
			pMonitor.setCurrentMessage("Import tab delimited data for "
					+ sampleIds.length + " samples.");

			int mappingID = daoTumorVsNormal.insertNormalSampleList(technology,
					sampleIds);

			if (mappingID != -1) {
				DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();

				String line = br.readLine();
				while (line != null) {
					if (pMonitor != null) {
						pMonitor.incrementCurValue();
						ConsoleUtil.showProgress(pMonitor);
					}
					if (!line.startsWith("#") && line.trim().length() > 0) {
						parts = line.split("\t", -1);
						if (parts.length > lenParts) {
							if (line.split("\t").length > lenParts) {
								System.err
										.println("The following line has more fields ("
												+ parts.length
												+ ") than the headers("
												+ lenParts + "): \n" + parts[0]);
							}
						}
						String values[] = (String[]) ArrayUtils.subarray(parts,
								sampleStartIndex,
								parts.length > lenParts ? lenParts
										: parts.length);
						String hugo = parts[hugoSymbolIndex];
						if (hugo != null && hugo.isEmpty()) {
							hugo = null;
						}
						if (hugo != null) {
							List<CanonicalGene> genes = null;
							CanonicalGene gene = daoGene.getGene(hugo);
							if (gene != null) {
								genes = Arrays.asList(gene);
							}
							if (genes == null || genes.isEmpty()) {
								genes = Collections.emptyList();
							}
							if (genes.isEmpty()) {
								pMonitor.logWarning("Gene not found:  ["
										+ hugo
										+ "]. Ignoring it "
										+ "and all tab-delimited data associated with it!");
							} else
								for (CanonicalGene gene1 : genes) {
									daoTumorVsNormal.addNormalDatum(mappingID, gene1, values);
								}
						}
					}
					line = br.readLine();
				}
				if (MySQLbulkLoader.isBulkLoad()) {
					MySQLbulkLoader.flushAll();
				}
			}
			return mappingID;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}catch(SQLException e){
			e.printStackTrace();
		}
		return 0;
	}

	public void importNormalSampleMapping(BufferedReader br1, int mappingID,
			ProgressMonitor pMonitor) throws IOException, DaoException {
		MySQLbulkLoader.bulkLoadOn();
		pMonitor.setCurrentMessage("Imorting Normal Samples Mapping data");
		String line = br1.readLine();
		DaoTumorVsNormal daoTumorVsNormal = DaoTumorVsNormal.getInstance();
		while (line != null) {
			String parts[] = line.split(":");
			String tissue = parts[0];
			String samples[] = parts[1].trim().split("\t", -1);
			daoTumorVsNormal.addNormalSampleMapingDatum(mappingID,
					tissue, samples);
			line = br1.readLine();
		}
		if (MySQLbulkLoader.isBulkLoad()) {
			MySQLbulkLoader.flushAll();
		}
	}
}
