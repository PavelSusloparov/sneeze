package com.nytimes.automation.pocket_change_stats;



import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.contextproviders.AbstractJiraContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.util.JiraHome;
 
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.net.*;
import java.io.*;

import net.sf.json.*;


import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.CloseableHttpClient;

public class PocketChangeStats extends AbstractJiraContextProvider
{
	public class CycleReport {
		private String cycleDescr;
		private String cycleId;
		private String cycleUrl;
		private String runningCount;
		
		public CycleReport() {
			cycleDescr = "";
			cycleId = "";
			cycleUrl = "";
		}
		
		public void setCycleDescription(String cycleDescr) {
			this.cycleDescr = cycleDescr;
		}
		
		public String getCycleDescription() {
			return this.cycleDescr;
		}
		
		public void setCycleId(String cycleId) {
			this.cycleId = cycleId;
		}
		
		public String getCycleId() {
			return this.cycleId;
		}
		
		public void setCycleUrl(String cycleUrl) {
			this.cycleUrl = cycleUrl;
		}
		
		public String getCycleUrl() {
			return this.cycleUrl;
		}
		
		public void setRunningCount(String runningCount) {
			this.runningCount = runningCount;
		}
		
		public String getRunningCount() {
			return this.runningCount;
		}
	}
	
	public class CaseReport {
		private String result;
		private String caseUrl;
		private String caseDescr;
		
		public CaseReport() {
			result = "";
			caseUrl = "";
			caseDescr = "";
		}
		
		public void setResult(String result) {
			this.result = result;
		}
		
		public String getResult() {
			return this.result;
		}
		
		public void setCaseUrl(String caseUrl) {
			this.caseUrl = caseUrl;
		}
		
		public String getCaseUrl() {
			return this.caseUrl;
		}
		
		public void setCaseDescription(String caseDescr) {
			this.caseDescr = caseDescr;
		}
		
		public String getCaseDescription() {
			return this.caseDescr;
		}
	}
  

//	private static String POCKET_CHANGE_URL="http://0.0.0.0:5000";
//	private static String POCKET_CHANGE_VM_URL="http://10.0.2.2:5000";
//	private static String SEARCH_TEST_CYCLE="/rest/search/test_cycle?issue_id=";
//	private static String SEARCH_CASE_EXECUTION="/rest/search/case_execution?issue_id=";
//	private static String VIEW_TEST_CYCLE="/test_cycle_case_view/";
//	private static String VIEW_CASE_EXECUTION="/case_execution_details/";
//	private static String IMAGE_RESOURCE_URL="/download/resources/com.example.plugins.tutorial.tutorial-jira-add-content-to-view-issue-screen:tutorial-jira-add-content-to-view-issue-screen-resources/images/";
			
		
	@Override
	public Map getContextMap(User user, JiraHelper jiraHelper) {
		
		File jiraHome		= ComponentAccessor.getComponent(JiraHome.class).getHome();
		File propertyFile 	= new File(jiraHome, "pocket_change_stats.properties");
		
		FileInputStream fileInputStream = null;
		Properties p = new Properties();
		
		try {
			fileInputStream = new FileInputStream(propertyFile);
			p.load(fileInputStream);
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
			return new HashMap();
		}
		
		String pocketChangeUrl 		= p.getProperty("POCKET_CHANGE_URL");
//		String pocketChangeVMUrl 	= p.getProperty("POCKET_CHANGE_VM_URL", "");
		String searchTestCycle		= p.getProperty("SEARCH_TEST_CYCLE");
		String searchCaseExecution	= p.getProperty("SEARCH_CASE_EXECUTION");
		String viewTestCycle		= p.getProperty("VIEW_TEST_CYCLE");
		String viewCaseExecution	= p.getProperty("VIEW_CASE_EXECUTION");
		
		try {
			fileInputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new HashMap();
		}
		
		
		// TODO Auto-generated method stub
		Map contextMap 		= new HashMap();
		Issue currentIssue 	= (Issue) jiraHelper.getContextParams().get("issue");
		String issueId		= currentIssue.getId().toString();
		String issueType	= currentIssue.getIssueTypeObject().getName();
		String baseUrl		= ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_BASEURL);
		
		
		
		if (issueType.equals("Test Cycle")) {
//			String cycle_search_url	= ExternalReport.POCKET_CHANGE_URL + ExternalReport.SEARCH_TEST_CYCLE + issueId;
//			String cycle_search_url	= PocketChangeStats.POCKET_CHANGE_VM_URL + PocketChangeStats.SEARCH_TEST_CYCLE + issueId;
			String cycle_search_url = pocketChangeUrl + searchTestCycle + issueId;
			String response;
			
			try {
				response = httpGet(cycle_search_url);
				
				JSONArray jsonArray = JSONArray.fromObject(response); 
				JSONObject cycle	= jsonArray.getJSONObject(0);
				CycleReport cycleReport = new CycleReport();
				String cycleId		= cycle.getString("id");
				
//				String cycleUrl	= PocketChangeStats.POCKET_CHANGE_URL + PocketChangeStats.VIEW_TEST_CYCLE + cycleId;
				String cycleUrl = pocketChangeUrl + viewTestCycle + cycleId; 
				
				cycleReport.setCycleId(cycleId);
				cycleReport.setCycleUrl(cycleUrl);
				cycleReport.setCycleDescription(cycle.getString("description"));
				cycleReport.setRunningCount(cycle.getString("running_count"));
				
				contextMap.put("testCycle", cycleReport);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			
				return new HashMap();
 
			}

			
		} else if (issueType.equals("Test Failure")) {
//			String exec_search_url	= ExternalReport.POCKET_CHANGE_URL + ExternalReport.SEARCH_CASE_EXECUTION + issueId;
//			String exec_search_url	= PocketChangeStats.POCKET_CHANGE_VM_URL + PocketChangeStats.SEARCH_CASE_EXECUTION + issueId + "&expand=case";
			String exec_search_url = pocketChangeUrl + searchCaseExecution + issueId + "&expand=case";
			String response;
			
			
			String parentId 		= currentIssue.getParentId().toString();
			
			try {
				response = httpGet(exec_search_url);
			
				JSONObject caseMap		= JSONArray.fromObject(response).getJSONObject(0).getJSONObject("case");
				String case_id		= caseMap.getString("id");
				String case_label	= caseMap.getString("label");

				ArrayList relatedCaseExecInCycle = new ArrayList();
//				String cycleSearchUrl	= PocketChangeStats.POCKET_CHANGE_VM_URL + PocketChangeStats.SEARCH_TEST_CYCLE + parentId + "&expand=case_executions.case";
				String cycleSearchUrl	= pocketChangeUrl + searchTestCycle + parentId + "&expand=case_executions.case";
				String cycleSearchResp	= httpGet(cycleSearchUrl);
				
				contextMap.put("testUrl", cycleSearchUrl);
				JSONArray cycleCaseExecArray	= JSONArray.fromObject(cycleSearchResp).getJSONObject(0).getJSONArray("case_executions");
				
				for (int index = 0; index < cycleCaseExecArray.size(); index++) {
					JSONObject caseExecution  	= cycleCaseExecArray.getJSONObject(index);
					JSONObject caseInCaseExec 	= caseExecution.getJSONObject("case");
					CaseReport caseExecDetails	= new CaseReport();
					
					if (case_id.equals(caseInCaseExec.getString("id"))) {
						String caseExecId	 	= caseExecution.getString("id");
						String caseExecDesc		= caseExecution.getString("description");
						
//						String relatedCaseExecUrl = PocketChangeStats.POCKET_CHANGE_URL + PocketChangeStats.VIEW_CASE_EXECUTION + caseExecId;
						String relatedCaseExecUrl = pocketChangeUrl + viewCaseExecution + caseExecId;

						caseExecDetails.setResult(caseExecution.getString("result"));
						caseExecDetails.setCaseUrl(relatedCaseExecUrl);
						caseExecDetails.setCaseDescription(caseExecDesc);
						relatedCaseExecInCycle.add(caseExecDetails);
						
						
					}
				}
				
				
				
				contextMap.put("relatedCaseExec", relatedCaseExecInCycle);
			} catch (Exception e) {
				e.printStackTrace();
				return new HashMap();
			}
			
			
		} else {
		}


		contextMap.put("issueType", issueType);
		contextMap.put("issueId", issueId);
		contextMap.put("baseUrl", baseUrl);
		

		
		return contextMap;
	}
	
	
	public String httpGet(String url) throws ClientProtocolException, IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet request = new HttpGet(url);
		CloseableHttpResponse response = httpclient.execute(request);
		BufferedReader rd = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = rd.readLine()) != null) {
			sb.append(line);
		}
		
		rd.close();
		return sb.toString();

//		return "request returned";
		
		
	}
 
}