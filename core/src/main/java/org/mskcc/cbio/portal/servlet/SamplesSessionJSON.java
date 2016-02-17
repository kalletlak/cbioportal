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

public class SamplesSessionJSON extends HttpServlet {

	protected void doGet(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) throws ServletException,
			IOException {
		doPost(httpServletRequest, httpServletResponse);
	}

	protected void doPost(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) throws ServletException,
			IOException {
		HttpSession s=httpServletRequest.getSession(true);
		String sample = httpServletRequest.getParameter("sample");
		Set<String> temp = new HashSet<String>();
		String operation = httpServletRequest.getParameter("operation");
		Set<String> selectedSmaples = (Set<String>) s.getAttribute("selectedSamples");
		if(operation.equals("add")){
		if(selectedSmaples==null){
			temp.add(sample);
			s.setAttribute("selectedSamples", temp);
		}else{
			temp.addAll(selectedSmaples);
			temp.add(sample);
			s.removeAttribute("selectedSamples");
			s.setAttribute("selectedSamples", temp);
		}
		}else if (operation.equals("delete")){
			selectedSmaples.remove(sample);
			s.removeAttribute("selectedSamples");
			s.setAttribute("selectedSamples", selectedSmaples);
		}
	
		httpServletResponse.setContentType("application/json");
		PrintWriter out = httpServletResponse.getWriter();
		ObjectMapper mapper = new ObjectMapper();
		JsonNode result = mapper.createObjectNode();
		((ObjectNode) result).put("returnString", "SUCCESS");
		mapper.writeValue(out, result);
	}
}
