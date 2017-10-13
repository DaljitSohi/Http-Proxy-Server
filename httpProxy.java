/**
 * @author Daljit Sohi
 * ID: 100520358
 * HTTP Proxy Server
 * 
 */

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class httpProxy {
	public static void main(String[] args) {

		//ArrayList of blocked urls.
		//read the blocked urls from a file, and add them to a list.

		
		ArrayList<String> blocked = new ArrayList<>();
		File block_url = new File("C:/Users/Daljit Sohi/Desktop/Year 4/Distributed Systems/Assignment Wiki/HTTP_Proxy/src/filter.txt");

		try {
			BufferedReader br = new BufferedReader(new FileReader(block_url));
			String url = null;

			while((url = br.readLine()) != null) {
				blocked.add(url);
			}
			br.close();

		} catch (Exception e1) {
			e1.printStackTrace();
		}


		String host = "localhost";
		int port = 8000;

		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(port);

			while(true) {

				Socket client; //accept clients

				//Listen for clients
				try {

					System.out.println("Server is listening................");

					System.out.println("Loop started..........\n");
					//					new ProxyStart(client, blocked).start(); //start a thread for that client
					client = serverSocket.accept();
					System.out.println("Client accepted from: " + client.getInetAddress().getHostName() + " : " + client.getPort());
					new ProxyStart(client).start();

				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		} catch (IOException e1) {
			
			e1.printStackTrace();
		} //listen for clients
	}//end of httpProxy Class
}

class ProxyStart extends Thread{

	Socket client; //accept client connection
	Socket proxyClient; //Proxy as the client for the Server

	ArrayList<String> blockURLs;
	String url_toblock = "www.bbc.com";

	public ProxyStart(Socket socket, ArrayList<String> blocked) { 
		this.client = socket;
		this.blockURLs = blocked;
	}

	public ProxyStart(Socket socket) {
		this.client = socket;
		System.out.println("Client Connected..........");
	}


	//we run out proxy here
	public void run() {

		byte[] request = new byte[1024];//request from Client
		byte[] response = new byte[4096]; //response from the Server

		String input = null;
		StringBuilder sb;

		try { //try catch (Num 1)
			BufferedWriter out = new BufferedWriter(new FileWriter("C:/Users/Daljit Sohi/Desktop/Year 4/Distributed Systems/Assignment Wiki/HTTP_Proxy/src/log.txt", true));
						
						//connect to client on port 8000
						//Client -----> Server
//						in_client = new BufferedReader(new InputStreamReader(client.getInputStream())); 
			////			sb = new StringBuilder();
			//			String urlToCall = "";
			//			int count = 0;
			//
			//			while((input = in_client.readLine()) != null) {
			////				sb.append(input);				
			////				System.out.println("SB: " + sb.toString());
			//				
			////				System.out.println(input);
			//				
			//				try {
			//					StringTokenizer tok = new StringTokenizer(input);
			//					tok.nextToken();
			//				} catch(Exception e) { break;}
			//				
			//				if(count == 0) {
			//					String[] tokens = input.split(" ");
			//					urlToCall = tokens[1];
			////					System.out.println("Request for: " + urlToCall);
			//				}
			//				count++;
			//								
			//			}
			//			System.out.println("End of while loop");
			//			System.out.println("URL to Request for: " + urlToCall); //request for this in the client socket

			// if(urlToCall == null) return;
			// String web_host = urlToCall.split("://")[1].split("//?")[0];
			// System.out.println("HOST: --->>>>>>>> " + web_host);

			InputStream fromClient = this.client.getInputStream();
			OutputStream toClient = this.client.getOutputStream();

			// new thread to parse header bytes for host
//			new Thread() {
//
//
//				public void run() {
//					//Client -----> Server
//					BufferedReader in_client;
//					String input;
//
//					try {
//						in_client = new BufferedReader(new InputStreamReader(client.getInputStream()));
//						String urlToCall = "";
//						int count = 0;
//
//						while((input = in_client.readLine()) != null) {
//
//							input.startsWith("GET");
//							urlToCall = input.substring(4, 100).split("://")[1].split("//?")[0];
//						
//						}
//						
//						hostName = urlToCall;
//					} catch (IOException e1) {
//						e1.printStackTrace();
//					} 
//				}
//
//			}.start();
//
//			System.out.println("HOSTNAME: 			----> " + hostName);

			
//			System.out.println("Reaching: " + str.startsWith("GET") + " ");
//			String web_host = str.substring(4, 100).split("://")[1].split("//?")[0];
//			System.out.println("HOST: --->>>>>>>> " + web_host);
			
			
			//respond out to Server
			String host = "www.cnn.com";
			
			if(url_toblock.equals(host)) {
				System.out.println("Sorry. That website is block!");
				out.write("Access to : " + host + "Blocked!\n");
			}
			
			else {
				out.write("Access to : " + host + " Granted!\n");
				proxyClient = new Socket(host, 80);			

				InputStream fromServer = proxyClient.getInputStream();
				OutputStream toServer = proxyClient.getOutputStream();


				new Thread() {
					public void run() {
						int bytesRead;

						try {
							while((bytesRead = fromClient.read(request)) != -1) {
								String str = new String(request);
								out.write(str);
								toServer.write(request, 0, bytesRead);
								toServer.flush();
							}
						} catch(Exception ex) {
							ex.printStackTrace();
						}

						try {
							// toServer.close();
						} catch(Exception ex) {

						}
					}
				}.start();

				new Thread() {
					public void run() {
						int bytes_read;

						try {
							while((bytes_read = fromServer.read(response)) != -1) {
								toClient.write(response, 0, bytes_read);
								toClient.flush();
							}

						} catch(Exception ex) {
							ex.printStackTrace();
						}

						try {
							// toClient.close();
						} catch(Exception ex) {

						}
					}
				}.start();
			}			
			
		} catch (IOException e) {
			e.printStackTrace();
		} //end of try/catch (Num 1)

	}//end of run() method
}//end of class ProxyStart