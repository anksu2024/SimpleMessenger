/**
 * NAME       : Ankit Sarraf
 * PROJECT    : Simple Messenger
 * BACKGROUND : This project is my first project of the course CSE-586 which I took in Spring 2014
 *              under the tutelage of Prof. Steven Ko (https://nsr.cse.buffalo.edu/?page_id=272).
 *              This project is to be considered as the initial point for my other projects of the
 *              subject.
 * EMAIL      : sarrafan[at]buffalo[dot]edu
 */

package edu.buffalo.cse.cse486586.simplemessenger;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.TextView;

/**
 * SimpleMessengerActivity creates an Activity (i.e., a screen) that has an input box and a display
 * box.
 */
public class SimpleMessengerActivity extends Activity {
	static final String TAG = SimpleMessengerActivity.class.getSimpleName();
	static final String REMOTE_PORT0 = "11108";
	static final String REMOTE_PORT1 = "11112";
	static final int SERVER_PORT = 10000;

	/** Called when the Activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "New App");

		/*
		 * Allow this Activity to use a layout file that defines what UI elements to use.
		 * 
		 * R is an automatically generated class that contains pointers to statically declared
		 * "resources" such as UI elements and strings. For example, R.layout.main refers to the
		 * entire UI screen declared in res/layout/main.xml file. You can find other examples of R
		 * class variables below.
		 */
		setContentView(R.layout.main);

		/*
		 * Calculate the port number that this AVD listens on.
		 * It is just a hack that I came up with to get around the networking limitations of AVDs.
		 * The explanation is provided in the PA1 spec.
		 */
		TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
		final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));

		try {
			/*
			 * Create a server socket as well as a thread (AsyncTask) that listens on the server
			 * port.
			 * 
			 * AsyncTask is a simplified thread construct that Android provides.
			 */
			ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
			new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
		} catch (IOException e) {
			/*
			 * Log is a good way to debug your code. LogCat prints out all the messages that
			 * Log class writes.
			 */
			Log.e(TAG, "Can't create a ServerSocket");
			return;
		}

		/*
		 * Retrieve a pointer to the input box (EditText) defined in the layout
		 * XML file (res/layout/main.xml).
		 */
		final EditText editText = (EditText) findViewById(R.id.edit_text);

		/*
		 * Register an OnKeyListener for the input box. OnKeyListener is an event handler that
		 * processes each key event. The purpose of the following code is to detect an enter key
		 * press event, and create a client thread so that the client thread can send the string
		 * in the input box over the network.
		 */
		editText.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
						(keyCode == KeyEvent.KEYCODE_ENTER)) {
					/*
					 * If the key is pressed (i.e., KeyEvent.ACTION_DOWN) and it is an enter key
					 * (i.e., KeyEvent.KEYCODE_ENTER), then we display the string. Then we create
					 * an AsyncTask that sends the string to the remote AVD.
					 */
					String msg = editText.getText().toString() + "\n";
					editText.setText(""); // This is one way to reset the input box.
					TextView localTextView = (TextView) findViewById(R.id.local_text_display);
					localTextView.append("\t" + msg); // This is one way to display a string.
					TextView remoteTextView = (TextView) findViewById(R.id.remote_text_display);
					remoteTextView.append("\n");

					/*
					 * Note that the following AsyncTask uses AsyncTask.SERIAL_EXECUTOR, not
					 * AsyncTask.THREAD_POOL_EXECUTOR as the above ServerTask does.
					 */
					new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
					return true;
				}
				return false;
			}
		});
	}

	/***
	 * ServerTask is an AsyncTask that should handle incoming messages. It is created by
	 * ServerTask.executeOnExecutor() call in SimpleMessengerActivity.
	 */
	private class ServerTask extends AsyncTask<ServerSocket, String, Void> {
		@Override
		protected Void doInBackground(ServerSocket ... sockets) {
			System.out.println("In Server");
			ServerSocket serverSocket = sockets[0];

			Socket clientSocket;
			BufferedReader bufferIn;
			String inputLine;

			try {
				while(true) {
					clientSocket = serverSocket.accept();

					bufferIn = new BufferedReader(
							new InputStreamReader(clientSocket.getInputStream()));

					inputLine = bufferIn.readLine();
					if(inputLine.equals(null) || inputLine.equals("") || inputLine == null) {

						//If blank input entered, just break
						Log.i("Simple Application", "Exit client");
						break;
					}

					publishProgress(inputLine);
					Log.i("Simple Application", "Message rcvd from Client : " + inputLine);
				}

				bufferIn.close();
				Log.i("Simple Application", "bufferIn Closed");

				clientSocket.close();
				Log.i("Simple Application", "clientSocket Closed");
			}
			catch(IOException ie) {
				System.out.println(ie.getMessage());
			}

			return null;
		}

		protected void onProgressUpdate(String...strings) {
			/*
			 * The following code displays what is received in doInBackground().
			 */
			TextView remoteTextView = (TextView) findViewById(R.id.remote_text_display);
			remoteTextView.append(strings[0] + "\t\n");
			TextView localTextView = (TextView) findViewById(R.id.local_text_display);
			localTextView.append("\n");

			/*
			 * The following code creates a file in the AVD's internal storage and stores a file.
			 */

			String filename = "SimpleMessengerOutput";
			String string = strings[0] + "\n";
			FileOutputStream outputStream;

			try {
				outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
				outputStream.write(string.getBytes());
				outputStream.close();
			} catch (Exception e) {
				Log.e(TAG, "File write failed");
			}

			return;
		}
	}

	/***
	 * ClientTask is an AsyncTask that should send a string over the network.
	 * It is created by ClientTask.executeOnExecutor() call whenever OnKeyListener.onKey() detects
	 * an enter key press event.
	 */
	private class ClientTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... msgs) {
			try {
				System.out.println("In Client");
				String remotePort = REMOTE_PORT0;
				if (msgs[1].equals(REMOTE_PORT0))
					remotePort = REMOTE_PORT1;

				Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
						Integer.parseInt(remotePort));

				String msgToSend = msgs[0];

				PrintWriter printWriterOut = new PrintWriter(socket.getOutputStream(), true);
				printWriterOut.write(msgToSend);
				Log.i("Simple Application", "Message sent from Receiver : " + msgToSend);

				printWriterOut.close();
				Log.i("Simple Application", "printWriter Closed");

				socket.close();
				Log.i("Simple Application", "socket Closed");
			}
			catch (UnknownHostException e) {
				Log.e(TAG, "ClientTask UnknownHostException");
			}
			catch (IOException e) {
				Log.e(TAG, "ClientTask socket IOException");
			}
			return null;
		}
	}
}