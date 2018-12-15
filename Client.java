import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.SwingUtilities;

import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.mrl.RtpMrl;


public class Client{

    SIPclient sip;

    private final JFrame frame;

    private final EmbeddedMediaPlayerComponent mediaPlayerComponent;

    public static void main(final String[] args) {
        new NativeDiscovery().discover();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Client(args);
            }
        });
    }

  	final static int READY = 1;

    public Client(String[] args) {


        frame = new JFrame("Media Player");
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screenSize = tk.getScreenSize();
        int screenHeight = screenSize.height;
        int screenWidth = screenSize.width;
        int state;

        //SIP PORT: 5804
        //RTSP PORT: 5854

        if(args.length != 3) {
           System.out.println("Especify Local Ip address. ./Client [Local IP] [SIP port] [URI SIP]");
           System.exit(1);
       }
        int port_sip = Integer.parseInt(args[1]);

        int rtsp_port = 0;
        String movie = "";
        try{
          sip = new SIPclient (args[0], port_sip);
          sip.initiateSession(args[2]);
          rtsp_port = sip.getRtspPort();
          movie = sip.getRtspURI();

        }catch(Exception e){

        }
        System.out.println("PORT: " + rtsp_port);
        System.out.println("Movie: " + movie);

        RTSP rtsp = new RTSP("monito02.lab.it.uc3m.es", rtsp_port, 5804, movie);

        frame.setSize(screenWidth / 2, screenHeight / 2);
        frame.setLocation(screenWidth / 3, screenHeight / 3);

        //TO DO! choose the correct arguments for the methods below. Add more method calls as necessary
        String mrl = new RtpMrl().multicastAddress("239.0.0.189")
                         .port(5804)
                         .value();



        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mediaPlayerComponent.release();
                System.exit(0);
            }
        });

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());

        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        contentPane.add(mediaPlayerComponent, BorderLayout.CENTER);

        JPanel controlsPane = new JPanel();

        JButton setupButton = new JButton("Setup");
        controlsPane.add(setupButton);
        contentPane.add(controlsPane, BorderLayout.SOUTH);

        //Handler for SETUP button
        //-----------------------
        setupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              rtsp.send_request("SETUP");
            }
        });
        //Definition of PLAY button

        //----------------------
        JButton playButton = new JButton("Play");
        controlsPane.add(playButton);
        contentPane.add(controlsPane, BorderLayout.SOUTH);

        //Handler for PLAY button
        //-----------------------
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rtsp.send_request("PLAY");
                // if(state == READY)
                  mediaPlayerComponent.getMediaPlayer().playMedia(mrl);
            }
        });

        //TO DO! implement a PAUSE button to pause video playback.
        //...
        JButton pauseButton = new JButton("Pause");
        controlsPane.add(pauseButton);
        contentPane.add(controlsPane, BorderLayout.SOUTH);

        //Handler for PLAY button
        //-----------------------
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //TO DO!! configure the playback of the video received via RTP, or resume a paused playback.
                //...
                rtsp.send_request("PAUSE");

            }
        });


        JButton tearButton = new JButton("Stop");
        controlsPane.add(tearButton);
        contentPane.add(controlsPane, BorderLayout.SOUTH);

        //Handler for PLAY button
        //-----------------------
        tearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //TO DO!! configure the playback of the video received via RTP, or resume a paused playback.
                //...
                rtsp.send_request("TEARDOWN");
                try{
                  sip.terminateSession();
                }catch(Exception otro_e){

                }


            }
        });
        //Makes visible the window
        frame.setContentPane(contentPane);
        frame.setVisible(true);


    }

}
