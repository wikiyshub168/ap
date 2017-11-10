package instinct.service.client;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * This class was generated by the JAX-WS RI. JAX-WS RI 2.1.3-hudson-390-
 * Generated source version: 2.0
 * 
 */
@WebService(name = "OnlineFraudCheckSoap", targetNamespace = "http://dectechsolutions.com/Instinct")
public interface OnlineFraudCheckSoap 
{

	/**
	 * 
	 * @param inputString 
	 * @return returns java.lang.String
	 */
	@WebMethod(operationName = "InstinctFraudCheck_String", action = "http://dectechsolutions.com/Instinct/InstinctFraudCheck_String")
	@WebResult(name = "InstinctFraudCheck_StringResult", targetNamespace = "http://dectechsolutions.com/Instinct")
	@RequestWrapper(localName = "InstinctFraudCheck_String", targetNamespace = "http://dectechsolutions.com/Instinct", className = "com.dectechsolutions.client.InstinctFraudCheckString")
	@ResponseWrapper(localName = "InstinctFraudCheck_StringResponse", targetNamespace = "http://dectechsolutions.com/Instinct", className = "com.dectechsolutions.client.InstinctFraudCheckStringResponse")
	String instinctFraudCheckString(
			@WebParam(name = "inputString", targetNamespace = "http://dectechsolutions.com/Instinct") String inputString);

	/**
	 * 
	 * @param inputXMLString 
	 * @return returns java.lang.String
	 */
	@WebMethod(operationName = "InstinctFraudCheck_XMLString", action = "http://dectechsolutions.com/Instinct/InstinctFraudCheck_XMLString")
	@WebResult(name = "InstinctFraudCheck_XMLStringResult", targetNamespace = "http://dectechsolutions.com/Instinct")
	@RequestWrapper(localName = "InstinctFraudCheck_XMLString", targetNamespace = "http://dectechsolutions.com/Instinct", className = "com.dectechsolutions.client.InstinctFraudCheckXMLString")
	@ResponseWrapper(localName = "InstinctFraudCheck_XMLStringResponse", targetNamespace = "http://dectechsolutions.com/Instinct", className = "com.dectechsolutions.client.InstinctFraudCheckXMLStringResponse")
	String instinctFraudCheckXMLString(
			@WebParam(name = "inputXMLString", targetNamespace = "http://dectechsolutions.com/Instinct") String inputXMLString);

	/**
	 * 
	 * @param inputString 
	 * @return returns java.lang.String
	 */
	@WebMethod(operationName = "SubmitApplication_String", action = "http://dectechsolutions.com/Instinct/SubmitApplication_String")
	@WebResult(name = "SubmitApplication_StringResult", targetNamespace = "http://dectechsolutions.com/Instinct")
	@RequestWrapper(localName = "SubmitApplication_String", targetNamespace = "http://dectechsolutions.com/Instinct", className = "com.dectechsolutions.client.SubmitApplicationString")
	@ResponseWrapper(localName = "SubmitApplication_StringResponse", targetNamespace = "http://dectechsolutions.com/Instinct", className = "com.dectechsolutions.client.SubmitApplicationStringResponse")
	String submitApplicationString(
			@WebParam(name = "inputString", targetNamespace = "http://dectechsolutions.com/Instinct") String inputString);

}
