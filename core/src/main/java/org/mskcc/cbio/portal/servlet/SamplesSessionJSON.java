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
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

/**
 * Add/ Delete sample from session
 * 
 * @author Karthik Kalletla
 * 
 */
public class SamplesSessionJSON extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) throws ServletException,
			IOException {
		doPost(httpServletRequest, httpServletResponse);
	}

	protected void doPost(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) throws ServletException,
			IOException {
		HttpSession session = httpServletRequest.getSession(true);
		String sample = httpServletRequest.getParameter("sample");
		Set<String> samplesSet = new HashSet<String>();
		String operation = httpServletRequest.getParameter("operation");
		@SuppressWarnings("unchecked")
		Set<String> selectedSmaples = (Set<String>) session
				.getAttribute("selectedSamples");
		if (operation.equals("add")) {
			if (selectedSmaples == null) {
				samplesSet.add(sample);
				session.setAttribute("selectedSamples", samplesSet);
			} else {
				samplesSet.addAll(selectedSmaples);
				samplesSet.add(sample);
				session.removeAttribute("selectedSamples");
				session.setAttribute("selectedSamples", samplesSet);
			}
		} else if (operation.equals("delete")) {
			selectedSmaples.remove(sample);
			session.removeAttribute("selectedSamples");
			session.setAttribute("selectedSamples", selectedSmaples);
		}

		httpServletResponse.setContentType("application/json");
		PrintWriter out = httpServletResponse.getWriter();
		ObjectMapper mapper = new ObjectMapper();
		JsonNode result = mapper.createObjectNode();
		((ObjectNode) result).put("returnString", "SUCCESS");
		mapper.writeValue(out, result);
	}
}
