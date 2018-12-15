import java.io.*;
import java.util.*;
import javax.sdp.*;
import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;

public class SIPserver extends SIPua {
	/****************************************************************************/
	/* Constructor method */
	/****************************************************************************/
	public SIPserver (String localIP, int sipPort, int rtspPort, String movie) throws Exception {
		super("server", localIP, sipPort);
		RTSP_PORT = rtspPort;
		RTSP_URI = movie;
	}

	/****************************************************************************/
	/* Generates SDP answer */
	/****************************************************************************/
	private Object createSDPAnswer(byte[] sdpOffer) throws Exception {
		SessionDescription sdpAnswer = sdpFactory.createSessionDescription(new String(sdpOffer));

		Vector <MediaDescription> mediaDescriptions = sdpAnswer.getMediaDescriptions(true);

		// Updates the "m" line for RTP media
		MediaDescription mediaDescription = mediaDescriptions.elementAt(0);

		// Removes attributes with non conforming values
		mediaDescription.removeAttribute("recvonly");

		// Set new attributes
		Vector <Attribute> attributes = mediaDescription.getAttributes(true);
		Attribute a = sdpFactory.createAttribute("sendonly", "");
		attributes.add(a);

		// Sets the b parameter as 1Mbps
		BandWidth bandwidth = sdpFactory.createBandwidth(BandWidth.AS, 1000);
		Vector <BandWidth> bandwidths = new Vector <BandWidth> ();
		bandwidths.add(bandwidth);
		mediaDescription.setBandwidths(bandwidths);

		// Sets a "c" line
		Connection connection = sdpFactory.createConnection(LOCAL_IP);
		mediaDescription.setConnection(connection);

		// Updates the "m" line for RTSP
		MediaDescription mediaDescription2 = mediaDescriptions.elementAt(1);
		mediaDescription2.getMedia().setMediaPort(RTSP_PORT);

		// Sets TCP attributes
		mediaDescription2.setAttribute("setup", "passive");
		attributes = mediaDescription2.getAttributes(true);
		a = sdpFactory.createAttribute("fmtp", RTSP_URI);
		attributes.add(a);

		// Sets a "c" line
		mediaDescription2.setConnection(connection);

		System.out.println("Created SDP answer: \n" + sdpAnswer.toString() );
		return sdpAnswer;
	}

	/****************************************************************************/
	/* Processes a SIP request */
	/****************************************************************************/
	public void processRequest(RequestEvent evt)  {
		try {
			Request request = evt.getRequest();
			System.out.println("Received " + request.getMethod() + " request");
			System.out.println(request.toString());
			ServerTransaction serverTransaction = evt.getServerTransaction();

			if (serverTransaction == null)
				serverTransaction = sipProvider.getNewServerTransaction(request);

			// Processes an INVITE request
			if (request.getMethod().equals(Request.INVITE) ) {
				invite = request;
				inviteServerTransaction = serverTransaction;
				dialog = serverTransaction.getDialog();

				// Configures the data plane for uplink
				SessionDescription sdpOffer = sdpFactory.createSessionDescription(new String(request.getRawContent()));

				// TO-DO: create a 200 OK response to the INVITE request, using the object "messageFactory"
				// Response response = ...
				Response response = messageFactory.createResponse(Response.OK,request);

				// TO-DO: create a Contact header using the object "headerFactory"
				// ...
				// ContactHeader contactHeader =
				ContactHeader contactHeader = headerFactory.createContactHeader(this.localAddress);
				response.addHeader(contactHeader);
				System.out.println("Un request recibido");

				//Adds an SDP answer
				ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("application", "sdp");
				response.setContent(createSDPAnswer(invite.getRawContent()), contentTypeHeader);

				// Sends the response
				inviteServerTransaction.sendResponse(response);
			}
			// Processes a BYE request
			else if (request.getMethod().equals(Request.BYE)) {
				// Creates a 200 OK response
				Response response = messageFactory.createResponse(Response.OK, request);
				serverTransaction.sendResponse(response);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
