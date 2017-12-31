package server;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Server extends JFrame{
	
	/**
	 * Mohammed Faisal Qureshi
	 */
	private static final long serialVersionUID = 1L;
	private JTextField userText;
	private JTextArea chatWindow;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private ServerSocket server;
	private Socket connection;
	
	public Server() {
		super("Instant Messenger - Server");
		userText = new JTextField();
		userText.setEditable(false); //Disables typing at start.
		userText.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						sendMessage(event.getActionCommand()); //Passes string into field.
						userText.setText("");
					}
				}
			);
			add(userText, BorderLayout.NORTH);
			chatWindow = new JTextArea();
			add(new JScrollPane(chatWindow));
			setSize(400, 700); //Sets size of window 
			setVisible(true); //Makes Visable
	}
	
	//Setting Up and Running Server
	public void startProgram() {
		try {
			server = new ServerSocket(6789, 100); //Creates socket (Port and how many can connect)
			while(true) {
				try { //Attempt to connect.
					waitForConnection();
					setupStream();
					whileMessaging();
				}catch(EOFException eofException) {
					//When connection ended show error.
					showMessage("\n Server Connection Ended");
				}finally {
					cleanUp(); //Cleans up program
				}
			}
		}catch(IOException ioException) {
			ioException.printStackTrace(); //Shows exception is crashes
		}
	}
	
	//Waits for Connection then show connection information.
	private void waitForConnection() throws IOException{
		showMessage(" Waiting for connection... \n");
		connection = server.accept(); // Accepts Connection
		showMessage(" Now connection to " + connection.getInetAddress().getHostAddress()); //Shows after connection made
	}
	
	//Stream to send and receive data
	private void setupStream() throws IOException{
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		input = new ObjectInputStream(connection.getInputStream());
		showMessage("\n Streams are now setup!");
	}
	
	//When chatting to other user
	private void whileMessaging() throws IOException{
		String message =  "\n You have been connected! ";
		showMessage(message);
		ableToType(true);
		do {
			//Currently having conversation
			try {
				message = (String) input.readObject();
				showMessage("\n " + message);
			}catch(ClassNotFoundException classNotFoundExcpetion) {
				showMessage("\n Unable to recieve message!");
			}
		}while(!message.equals("Client - END")); //If user types END program stops
	}
	
	//Cleanup Method
	private void cleanUp() {
		showMessage("\n Closing Connection... \n");
		ableToType(false);
		try {
			output.close();
			input.close();
			connection.close();
		}catch(IOException ioException) {
			ioException.printStackTrace();
		}
	}
	
	//Sends message to Client
	private void sendMessage(String message) {
		try {
			output.writeObject(" Server - " + message);
			output.flush();
			showMessage("\n Server - " + message);
		}catch(IOException ioException){
			chatWindow.append("\n Error, Can't send message!");
		}
	}
	
	//Updates Chat Window
	private void showMessage(final String text) {
		SwingUtilities.invokeLater(
			new Runnable() {
				public void run() {
					chatWindow.append(text);
					}
				}
		);
	}
	
	//Enables and Disables ability to type.
	private void ableToType(final boolean canType) {
		SwingUtilities.invokeLater(
			new Runnable() {
				public void run() {
					userText.setEditable(canType); //Enables Typing
					}
				}
		);
	}
}
