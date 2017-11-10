package instinct.service.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;

import catfish.base.StartupConfig;

/**
 * This class was generated by the JAX-WS RI. JAX-WS RI 2.1.3-hudson-390-
 * Generated source version: 2.0
 * <p>
 * An example of how this class may be used:
 * 
 * <pre>
 * OnlineFraudCheck service = new OnlineFraudCheck();
 * OnlineFraudCheckSoap portType = service.getOnlineFraudCheckSoap();
 * portType.instinctFraudCheckString(...);
 * </pre>
 * 
 * </p>
 * 
 */
@WebServiceClient(name = "OnlineFraudCheck", targetNamespace = "http://dectechsolutions.com/Instinct")//, wsdlLocation = "file:./InstinctFraudCheck.wsdl")
public class OnlineFraudCheck extends Service 
{

	private static final URL ONLINEFRAUDCHECK_WSDL_LOCATION;
	private static final Logger logger = Logger
			.getLogger(instinct.service.client.OnlineFraudCheck.class
					.getName());

	static {
		URL url = null;
		String filepath = null;
		filepath = StartupConfig.get("instinct.onlineCheck.wsdl");
		if (filepath == null)
		{
			System.out.println("Please specify the location of InstinctFraudCheck.wsdl in startup.properties!"
					+ " Example:instinct.onlineCheck.wsdl=file:./InstinctFraudCheck.wsdl");
			throw new NullPointerException("Please specify the location of InstinctFraudCheck.wsdl in startup.properties!"
					+ " Example:instinct.onlineCheck.wsdl=file:./InstinctFraudCheck.wsdl");
		}
		try 
		{
			URL baseUrl = null;
			url = new URL(baseUrl, filepath);
		} 
		catch (MalformedURLException e) 
		{
			logger.warning("Failed to create URL for the wsdl Location:" + filepath + ", retrying as a local file");
			logger.warning(e.getMessage());
		}
		ONLINEFRAUDCHECK_WSDL_LOCATION = url;
	}

	public OnlineFraudCheck(URL wsdlLocation, QName serviceName) 
	{
		super(wsdlLocation, serviceName);
	}

	public OnlineFraudCheck() 
	{
		super(ONLINEFRAUDCHECK_WSDL_LOCATION, new QName(
				"http://dectechsolutions.com/Instinct", "OnlineFraudCheck"));
	}

	/**
	 * 
	 * @return returns OnlineFraudCheckSoap
	 */
	@WebEndpoint(name = "OnlineFraudCheckSoap")
	public OnlineFraudCheckSoap getOnlineFraudCheckSoap() 
	{
		return super.getPort(new QName("http://dectechsolutions.com/Instinct",
				"OnlineFraudCheckSoap"), OnlineFraudCheckSoap.class);
	}

	/**
	 * 
	 * @return returns OnlineFraudCheckSoap
	 */
	@WebEndpoint(name = "OnlineFraudCheckSoap12")
	public OnlineFraudCheckSoap getOnlineFraudCheckSoap12() 
	{
		return super.getPort(new QName("http://dectechsolutions.com/Instinct",
				"OnlineFraudCheckSoap12"), OnlineFraudCheckSoap.class);
	}

}
