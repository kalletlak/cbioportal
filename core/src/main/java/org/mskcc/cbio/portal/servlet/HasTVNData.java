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
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONObject;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.util.SpringUtil;
import org.mskcc.cbio.portal.web_api.ProtocolException;

/**
 * Check if the cancer study('s) has data for tumor vs normal comparison plot
 * 
 * @author Karthik Kalletla
 * 
 */
public class HasTVNData extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		JSONObject resultObject = new JSONObject();
		boolean hasTVNData = hasTVNData(request
				.getParameter("cancer_study_list"));
		resultObject.put("HAS_TVN_DATA", hasTVNData);
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(out, resultObject);
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}
	
	private Boolean hasTVNData(String cancerStudyIdListString) {
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
