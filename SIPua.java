import java.io.*;
import java.util.*;
import javax.sdp.*;
import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;

public abstract class SIPua implements SipListener {

	protected Address localAddress;
	protected SipFactory sipFactory;
	protected AddressFactory addressFactory;
	protected HeaderFactory headerFactory;
	protected MessageFactory messageFactory;
	protected SipStack sipStack;
	protected SipProvider sipProvider;
	protected ListeningPoint udp;
	protected SdpFactory sdpFactory;
	protected Dialog dialog;
	protected Random generator;
	Request invite;
	ServerTransaction inviteServerTransaction;
	protected String SIP_DOMAIN = "vod.net";

	protected String USER;
	protected String LOCAL_IP;
	protected int SIP_PORT;
	protected int RTSP_PORT;
	protected String RTSP_URI;

	/****************************************************************************/
	/* Constructor method */
	/****************************************************************************/
	public SIPua (String username, String localIP, int sipPort) throws Exception {
		// Initializes local attributes
		USER = username;
		LOCAL_IP = localIP;
		SIP_PORT = sipPort;

		// Initializes the SIP stack
		sdpFactory = SdpFactory.getInstance();
		sipFactory = SipFactory.getInstance();
		sipFactory.setPathName("gov.nist");
		Properties properties = new Properties();
		properties.setProperty("javax.sip.STACK_NAME", "SIPua");
		sipStack = sipFactory.createSipStack(properties);
		udp = sipStack.createListeningPoint(LOCAL_IP, SIP_PORT, "udp");
		sipProvider = sipStack.createSipProvider(udp);
		sipProvider.addSipListener(this);

		// Creates the Header, Address and Message factories
		headerFactory = sipFactory.createHeaderFactory();
		addressFactory = sipFactory.createAddressFactory();
		messageFactory = sipFactory.createMessageFactory();

		// Initializes remaining attributes
		SipURI localSipURI = addressFactory.createSipURI(USER, LOCAL_IP + ":" + SIP_PORT);
		this.localAddress = addressFactory.createAddress(localSipURI);

		// Initializes the random number generator
		generator = new Random();
		Date date = new Date();
		generator.setSeed(localSipURI.hashCode() + date.getTime());

		System.out.println("SIP UA: " + "sip: " + USER + "@"+ LOCAL_IP + ":" + SIP_PORT);
	}

	public void processRequest(RequestEvent evt) {}
	public void processResponse(ResponseEvent evt) {}
	public void processTimeout(TimeoutEvent evt) {}
	public void processIOException(IOExceptionEvent evt) {}
	public void processTransactionTerminated(TransactionTerminatedEvent evt) {}
	public void processDialogTerminated(DialogTerminatedEvent evt) {}
}
