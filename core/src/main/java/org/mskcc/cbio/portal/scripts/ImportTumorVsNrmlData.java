/** Copyright (c) 2014 Childrens Hospital of Philadelphia.
 * 
 */
package org.mskcc.cbio.portal.scripts;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
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
 * @author kalletlak
 * 
 */
public class ImportTumorVsNrmlData {

	private static DaoTumorVsNormal daoTumorVsNormal = DaoTumorVsNormal
			.getInstance();

	public int importNormalData(BufferedReader br, String technology,
			ProgressMonitor pMonitor) throws DaoException {
		try {
			return insertNormalSamplesData(br, technology, pMonitor);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private int insertNormalSamplesData(BufferedReader br, String technology,
			ProgressMonitor pMonitor) throws IOException, DaoException {
		MySQLbulkLoader.bulkLoadOn();
		String tableName = "normals_sample_data";
		String headerLine = br.readLine();
		String parts[] = headerLine.split("\t");
		int lenParts = parts.length;
		int sampleStartIndex = 1;
		int hugoSymbolIndex = 0;

		String sampleIds[];
		System.out.println("number of samples : "
				+ (parts.length - sampleStartIndex));
		sampleIds = new String[parts.length - sampleStartIndex];
		System.arraycopy(parts, sampleStartIndex, sampleIds, 0, parts.length
				- sampleStartIndex);
		pMonitor.setCurrentMessage("Import tab delimited data for "
				+ sampleIds.length + " samples.");
		
		int mappingID = daoTumorVsNormal.insertNormalSampleList(technology,sampleIds);
		
		if(mappingID!=-1){
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
										+ parts.length + ") than the headers("
										+ lenParts + "): \n" + parts[0]);
					}
				}
				String values[] = (String[]) ArrayUtils.subarray(parts,
						sampleStartIndex, parts.length > lenParts ? lenParts
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
							daoTumorVsNormal.addNormalDatum(tableName, mappingID, gene1, values);
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
		
	}

	private void addNormalSampleMapingDatum(String tableName,
			int mappingID, String tissue, String[] values) {
		daoTumorVsNormal.addNormalSampleMapingDatum(tableName, mappingID,
				tissue, values);
	}

	public void importNormalSampleMapping(BufferedReader br1,
			int mappingID, ProgressMonitor pMonitor) throws IOException, DaoException {
		MySQLbulkLoader.bulkLoadOn();
		String tableName = "normals_sample_mapping";
		pMonitor.setCurrentMessage("Imorting Normal Samples Mapping data");
		String line = br1.readLine();
		while (line != null) {
			String parts[] = line.split(":");
			String tissue = parts[0];
			String samples[]= parts[1].trim().split("\t", -1);
			addNormalSampleMapingDatum(tableName,
					mappingID,
					tissue, samples);
			line = br1.readLine();
		}
		if (MySQLbulkLoader.isBulkLoad()) {
			MySQLbulkLoader.flushAll();
		}
	}
}
