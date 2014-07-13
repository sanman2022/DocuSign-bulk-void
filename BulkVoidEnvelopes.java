// DocuSign API Walkthrough 05 in Java - Get Set of Envelopes based on filter
import java.io.*;
import java.net.URL;
import java.net.HttpURLConnection;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.xpath.*;
import org.xml.sax.InputSource;

public class BulkVoidEnvelopes
{	
	// Enter your info:
	static String email = "YOUR EMAIL ADDRESS";			// account email
	static String password = "YOUR PASSSWORD";			// account password
	static String integratorKey = "YOUR IK";		// integrator key (found on Preferences -> API page)
	
	// construct the DocuSign authentication header
	static String authenticationHeader = 
				"<DocuSignCredentials>" + 
					"<Username>" + email + "</Username>" +
					"<Password>" + password + "</Password>" + 
					"<IntegratorKey>" + integratorKey + "</IntegratorKey>" + 
				"</DocuSignCredentials>";
	
	static String baseURL = "https://demo.docusign.net/restapi/v2/accounts/";		// we will retrieve this
	static String accountId = "YOUR ACCOUNT ID";		// will retrieve
	
	//***********************************************************************************************
	// main()
	//***********************************************************************************************
	public static void main(String[] args) throws Exception
	{	
		
		ReadAndVoidEnvelopes();
		
	} //end main()
	
	//***********************************************************************************************
	//***********************************************************************************************
	// --- HELPER FUNCTIONS ---
	//***********************************************************************************************
	//***********************************************************************************************
	
	public static void ReadAndVoidEnvelopes()
	{
		try {
	 	File fXmlFile = new File("EnvelopeList.xml");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
	 	doc.getDocumentElement().normalize();
		NodeList nList = doc.getElementsByTagName("envelopeId");
	 
		for (int temp = 0; temp < nList.getLength(); temp++) {
	 
			Node nNode = nList.item(temp);
	 		System.out.println("\nEnvelope #" + (temp+1));
	 		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				String envelopeID=nNode.getFirstChild().getNodeValue();
				System.out.println("Checking status of Envelope id : " + envelopeID );	 
				String sStatus= checkEnvelopeStatus(envelopeID);
				if (sStatus!=null && !sStatus.equals("completed") && !sStatus.equals("voided") && !sStatus.equals("declined")) {
					Boolean bStatus= voidEnvelope(envelopeID);
					if (bStatus==true){
						System.out.println("Envelope Succcessfully voided: ");
						}
					}
				else {
					System.out.println("Envelope cannot be voided: Status is " + sStatus);	
					}
				}
			}
		} catch (Exception e) {
		e.printStackTrace();
		}
	}
		///////////////////////////////////////////////////////////////////////////////////////////////
	public static String getResponseBody(HttpURLConnection conn) {
		BufferedReader br = null;
		StringBuilder body = null;
		String line = "";
		try {
	        // we use xPath to get the baseUrl and accountId from the XML response body
 			br = new BufferedReader(new InputStreamReader( conn.getInputStream()));
 			body = new StringBuilder();
 			while ( (line = br.readLine()) != null)
 				body.append(line);
 			return body.toString();
		} catch (Exception e) {
	        	throw new RuntimeException(e); // simple exception handling, please review it
	    }
	}
	
	public static String checkEnvelopeStatus(String envelopeID) {
		
		try {
			HttpURLConnection conn = null;		// connection object used for each request
			String url = baseURL+"/"+accountId+"/envelopes/"+envelopeID;			// end-point for each api call
			String body = "";			// request body
			String response = "";			// response body
			int status;				// response status		
		
			// create connection object, set request method, add request headers
			conn = InitializeRequest(url, "GET", body);
			status=conn.getResponseCode();
			if (status!=200)
			{
				errorParse(conn, status);
				return null;
			}
			response = getResponseBody(conn);
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(response));
			Document doc = db.parse(is);
			NodeList nodes = doc.getElementsByTagName("status");
			String envelopeStatus= nodes.item(0).getFirstChild().getNodeValue();
			System.out.println ("Envelope Status is...."+ envelopeStatus+"\n");
			return envelopeStatus;
			
		} catch (Exception e) {
	        	throw new RuntimeException(e); // simple exception handling, please review it
	    }
	}
	
	public static Boolean voidEnvelope(String envelopeID) {
	try {
		HttpURLConnection conn = null;		// connection object used for each request
		String url = baseURL+"/"+accountId+"/envelopes/"+envelopeID;			// end-point for each api call
		String body = "<envelope  xmlns=\"http://www.docusign.com/restapi\">"+
						"<status>voided</status>"+
						"<voidedReason>**BULK VOID***</voidedReason>"+
						"</envelope>";
		String response = "";			// response body
		int status;				// response status		
	
		// create connection object, set request method, add request headers
		conn = InitializeRequest(url, "PUT", body);
		status=conn.getResponseCode();
		if (status!=200)
		{
			errorParse(conn, status);
			return false;
		}
		return true;
		
	} catch (Exception e) {
		throw new RuntimeException(e); // simple exception handling, please review it
		}
	}
	
	public static HttpURLConnection InitializeRequest(String url, String method, String body) {
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection)new URL(url).openConnection();
			
			conn.setRequestMethod(method);
			conn.setRequestProperty("X-DocuSign-Authentication", authenticationHeader);
			conn.setRequestProperty("Content-Type", "application/xml");
			conn.setRequestProperty("Accept", "application/xml");
			if (method.equalsIgnoreCase("POST") || method.equalsIgnoreCase("PUT"))
			{
				conn.setRequestProperty("Content-Length", Integer.toString(body.length()));
				conn.setDoOutput(true);
				// write body of the POST request 
				DataOutputStream dos = new DataOutputStream( conn.getOutputStream() );
				dos.writeBytes(body); dos.flush(); dos.close();
			}
			return conn;
			
		} catch (Exception e) {
	        	throw new RuntimeException(e); // simple exception handling, please review it
	    }
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	public static String parseXMLBody(String body, String searchToken) {
		String xPathExpression;
		try {
	        	// we use xPath to parse the XML formatted response body
			xPathExpression = String.format("//*[1]/*[local-name()='%s']", searchToken);
 			XPath xPath = XPathFactory.newInstance().newXPath();
 			return (xPath.evaluate(xPathExpression, new InputSource(new StringReader(body))));
		} catch (Exception e) {
	        	throw new RuntimeException(e); // simple exception handling, please review it
	    }
	}	
	

	///////////////////////////////////////////////////////////////////////////////////////////////
	public static void errorParse(HttpURLConnection conn, int status) { 
		BufferedReader br;
		String line;
		StringBuilder responseError;
		try {
			System.out.print("API call failed, status returned was: " + status);
			InputStreamReader isr = new InputStreamReader( conn.getErrorStream() );
			br = new BufferedReader(isr);
			responseError = new StringBuilder();
			line = null;
			while ( (line = br.readLine()) != null)
				responseError.append(line);
			System.out.println("\nError description:  " + responseError);
			return;
		}
		catch (Exception e) {
			throw new RuntimeException(e); // simple exception handling, please review it
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	public static String prettyFormat(String input, int indent) { 
		try {
	    		Source xmlInput = new StreamSource(new StringReader(input));
	        	StringWriter stringWriter = new StringWriter();
	        	StreamResult xmlOutput = new StreamResult(stringWriter);
	        	TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        	transformerFactory.setAttribute("indent-number", indent);
	        	Transformer transformer = transformerFactory.newTransformer(); 
	        	transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        	transformer.transform(xmlInput, xmlOutput);
	        	return xmlOutput.getWriter().toString();
	    	} catch (Exception e) {
	        	throw new RuntimeException(e); // simple exception handling, please review it
	    	}
	}
} // end class