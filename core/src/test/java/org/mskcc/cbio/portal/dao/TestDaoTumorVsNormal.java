package org.mskcc.cbio.portal.dao;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class TestDaoTumorVsNormal {
	ArrayList<String> internalSampleIds;
	DaoTumorVsNormal daoTumorVsNormal = DaoTumorVsNormal.getInstance();
	int mappingID = -1;

	@Before
	public void setUp() throws DaoException, SQLException {
		internalSampleIds = new ArrayList<String>();
		internalSampleIds.add("SAMPLE-1");
		internalSampleIds.add("SAMPLE-2");
		internalSampleIds.add("SAMPLE-3");
		internalSampleIds.add("SAMPLE-4");
		String data = "200:400:600:800";
		String values[] = data.split(":");
		System.out.println("list : "+internalSampleIds.toArray(new String[internalSampleIds.size()])[0]);
		mappingID = daoTumorVsNormal.insertNormalSampleList(
				"test_normal_dataset", internalSampleIds.toArray(new String[internalSampleIds.size()]));
		daoTumorVsNormal.addNormalSampleMapingDatum(mappingID, "tissue",
				internalSampleIds.toArray(new String[0]));
		CanonicalGene gene = DaoGeneOptimized.getInstance().getGene("MYC");
		daoTumorVsNormal.addNormalDatum(mappingID, gene, values);
	}

	@Test
	public void testIsTechnologyPresent() {
		try {
			assertEquals(true,
					daoTumorVsNormal.isTechnologyPresent("test_normal_dataset"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetNormalMappingID() {
		try {
			assertNotNull(daoTumorVsNormal
					.getNormalMappingID("test_normal_dataset"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetNormalsByCancerStudyAndGeneId() throws SQLException {
		TreeMap<String, TreeMap<String, TreeMap<String, String>>> map = daoTumorVsNormal
				.getNormalsByCancerStudyAndGeneId("test_normal_dataset",
						daoTumorVsNormal
								.getNormalMappingID("test_normal_dataset"));
		assertEquals(1, map.keySet().size());
		assertEquals(1, map.get(map.firstKey()).keySet().size());
		TreeMap<String, TreeMap<String, String>> map2 = map.get(map.firstKey());
		assertEquals(1, map2.keySet().size());
		assertEquals("tissue", map2.firstKey().toString());
		TreeMap<String, String> map3 = map2.get(map2.firstKey());
		assertEquals("200", map3.get("SAMPLE-1"));
		assertEquals("400", map3.get("SAMPLE-2"));
		assertEquals("600", map3.get("SAMPLE-3"));
		assertEquals("800", map3.get("SAMPLE-4"));
	}
}
