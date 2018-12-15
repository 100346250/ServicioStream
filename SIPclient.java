import java.io.*;
import java.util.*;
import javax.sdp.*;
import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;
import java.util.concurrent.CountDownLatch;

public class SIPclient extends SIPua {

	private CountDownLatch latch;

	/****************************************************************************/
	/* Constructor method */
	/****************************************************************************/
	public SIPclient (String localIP, int sipPort) throws Exception {
		super("client", localIP, sipPort);
		latch = new CountDownLatch(1);
	}

	public String getRtspURI() { return RTSP_URI; }
	public int getRtspPort() { return RTSP_PORT; }

	/****************************************************************************/
	/* Generates SDP offer */
	/****************************************************************************/
	private Object createSDPOffer() throws Exception {
		SessionDescription sessionDescription = sdpFactory.createSessionDescription();

		// Sets formats for "m" line
		int[] formats = new int[1];
		formats[0] = 32;

		// Creates an "m" line for RTP media
		MediaDescription mediaDescription = sdpFactory.createMediaDescription("video", 0, 1, "RTSP", formats);

		// Sets format attributes
		Vector <Attribute> attributes = new Vector <Attribute> ();
		Attribute a = sdpFactory.createAttribute("rtpmap", "32 MPV");
		attributes.add(a);

		// Sets directivity
		a = sdpFactory.createAttribute("recvonly",null);
		attributes.add(a);

		// Ends with media description
		mediaDescription.setAttributes(attributes);

		// Sets a "c" line
		Connection connection = sdpFactory.createConnection(LOCAL_IP);
		mediaDescription.setConnection(connection);

		// Sets the b parameter as 1Mbps
		BandWidth bandwidth = sdpFactory.createBandwidth(BandWidth.AS, 1000);
		Vector <BandWidth> bandwidths = new Vector <BandWidth> ();
		bandwidths.add(bandwidth);
		mediaDescription.setBandwidths(bandwidths);

		// Creates an "m" line for RTSP
		String[] formats2 = {"iptv_rtsp"};
		MediaDescription mediaDescription2 = sdpFactory.createMediaDescription("application", 9, 1, "TCP", formats2);

		// Sets TCP attributes
		Vector <Attribute> attributes2 = new Vector <Attribute> ();
		a = sdpFactory.createAttribute("setup", "active");
		attributes2.add(a);
		a = sdpFactory.createAttribute("connection", "new");
		attributes2.add(a);

		// Ends with media description
		mediaDescription2.setAttributes(attributes2);

		// Sets a "c" line
		mediaDescription2.setConnection(connection);

		Vector <MediaDescription> mediaDescriptions = new Vector <MediaDescription> ();
		mediaDescriptions.add(mediaDescription);
		mediaDescriptions.add(mediaDescription2);
		sessionDescription.setMediaDescriptions(mediaDescriptions);

		return sessionDescription;
	}

	/****************************************************************************/
	/* Utility to get an unique tag */
	/****************************************************************************/
	private String getTag() throws Exception {
		long number = generator.nextLong();
		return Long.toHexString(number);
	}

	/****************************************************************************/
	/* Processes a SIP response */
	/****************************************************************************/
	public void processResponse(ResponseEvent evt) {
		try {
			Response response = evt.getResponse();
			dialog = evt.getDialog();

			System.out.println("Received response:" + "\n" + response.toString());

			// Processes a 200 OK response
			if (response.getStatusCode() == Response.OK) {
				CSeqHeader cseqHeader = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
				// Processes an OK response to an INVITE request
				if (cseqHeader.getMethod().equals(Request.INVITE)) {
			    System.out.println(" OK response received for INVITE request. Sending ACK request");

					//TO-DO create and send an ACK, using the object "dialog"
					// ...
					long cseq = dialog.getLocalSeqNumber();
					Request ack = dialog.createAck(cseq);
					dialog.sendAck(ack);
					
					System.out.println("Session established");
					processSDP(response.getRawContent());
				}
				latch.countDown();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/****************************************************************************/
	/* Processes the SDP answer to obtain the RTSP URI of the requested video */
	/****************************************************************************/
	private void processSDP(byte[] sdpAnswer) throws Exception{
		SessionDescription sdpPayload = sdpFactory.createSessionDescription( new String(sdpAnswer) );
		Vector <MediaDescription> mediaDescriptions = sdpPayload.getMediaDescriptions(true);
		MediaDescription mediaDescription = mediaDescriptions.elementAt(1);
		String aux = mediaDescription.getAttribute("fmtp");
		RTSP_URI = aux.substring(aux.indexOf("=")+1);
		RTSP_PORT = mediaDescription.getMedia().getMediaPort();
	}

	/****************************************************************************/
	/* Creates an initial INVITE request */
	/****************************************************************************/
	private Request createINVITE(String to) throws Exception {
		// Creates the From header
		SipURI from = addressFactory.createSipURI(USER, SIP_DOMAIN);
		Address fromNameAddress = addressFactory.createAddress(from);
		fromNameAddress.setDisplayName(USER);
		FromHeader fromHeader = headerFactory.createFromHeader(fromNameAddress, getTag());

		// Creates the To header
		String username = to.substring(0, to.indexOf("@"));
		String address = to.substring(to.indexOf("@")+1);
		SipURI toAddress = addressFactory.createSipURI(username, address);
		Address toNameAddress = addressFactory.createAddress(toAddress);
		toNameAddress.setDisplayName(username);
		ToHeader toHeader = headerFactory.createToHeader(toNameAddress, null);

		// TO-DO: create the Request-URI using the object "addressFactory"
		// SipURI requestURI = ...

		SipURI requestURI = toAddress;
		// TO-DO: create the Via Header using the method "headerFactory.createViaHeader"
		//ViaHeader viaHeader = ...
		ArrayList viaHeaders = new ArrayList();
		ViaHeader viaHeader = headerFactory.createViaHeader(LOCAL_IP, SIP_PORT,"udp","z9hG4bKnashds8");
		viaHeaders.add(viaHeader);

		// TO-DO: create the Call-ID header using the object "sipProvider"
		// CallIdHeader callIdHeader = ...
		CallIdHeader callIdHeader = sipProvider.getNewCallId();

		// TO-DO: create CSeq header using the method "headerFactory.createCSeqHeader"
		// CSeqHeader cSeqHeader = ...
		CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(123456789, Request.INVITE);

		// TO-DO: create Max-Forwards header using the object "headerFactory"
		// MaxForwardsHeader maxForwards = ...
		MaxForwardsHeader maxForwards = headerFactory.createMaxForwardsHeader(70);

		// Creates the request
		Request request =  messageFactory.createRequest(requestURI, Request.INVITE, callIdHeader, cSeqHeader,	fromHeader, toHeader, viaHeaders, maxForwards);

		// TO-DO: create the Contact header, similarly to the To header, using the object "headerFactory"
		// ...
		// ContactHeader contactHeader = ...fromNameAddress
		ContactHeader contactHeader = headerFactory.createContactHeader(this.localAddress);
		request.addHeader(contactHeader);

		// Creates Content-type header
		ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("application", "sdp");
		request.addHeader(contentTypeHeader);
		request.setContent(createSDPOffer(), contentTypeHeader);

		return request;
	}

	/****************************************************************************/
	/* Initiates the session, by generating and sending an initial INVITE request/
	/****************************************************************************/
	public void initiateSession(String to) throws Exception {
		System.out.print("Creating INVITE request... ");
		Request invite = createINVITE(to);
		System.out.println("Sending the INVITE request... \n" + invite.toString());
		ClientTransaction clientTransaction = sipProvider.getNewClientTransaction(invite);
		clientTransaction.sendRequest();
		latch.await();
		latch = new CountDownLatch(1);
	}

	/****************************************************************************/
	/* Terminates the session */
	/****************************************************************************/
 	public void terminateSession() throws Exception {
		// Creates BYE request
		Request bye = dialog.createRequest(Request.BYE);
		System.out.print("Sending BYE request...");
		ClientTransaction clientTransaction = sipProvider.getNewClientTransaction(bye);
		dialog.sendRequest(clientTransaction);
		System.out.println("done");
		latch.await();
		sipStack.deleteListeningPoint(udp);
	}
}
