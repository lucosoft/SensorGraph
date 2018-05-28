import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.fazecast.jSerialComm.SerialPort;

public class SensorGraph {
	
	static SerialPort chosenPort;
	static int x = 0;

	static byte[] buffer = {'c','o','n','e','c','t','e','d'};
	
	public static void main(String[] args) {
		
		// create and configure the window
		JFrame window = new JFrame();
		window.setSize(400, 100);
		window.setLayout(new BorderLayout());
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// create a drop-down box and connect button, then place them at the top of the window
		JComboBox<String> portList = new JComboBox<String>();
		JButton connectButton = new JButton("Conectar");
		JButton enviarButton = new JButton("Enviar");
		JPanel topPanel = new JPanel();
		JTextField textField = new JTextField(5);
		topPanel.add(portList);
		topPanel.add(connectButton);
		topPanel.add(textField);
		topPanel.add(enviarButton);
		window.add(topPanel, BorderLayout.NORTH);
		
		// populate the drop-down box
		SerialPort[] portNames = SerialPort.getCommPorts();
		for(int i = 0; i < portNames.length; i++)
			portList.addItem(portNames[i].getSystemPortName());
		
		// configure the connect button and use another thread to listen for data
		enviarButton.addActionListener(new ActionListener(){
			@Override public void actionPerformed(ActionEvent arg0) {
				
				String textFieldAux = textField.getText()+"\n\r";
				chosenPort.writeBytes(textFieldAux.getBytes(), textFieldAux.length());
			}
		});

		// configure the connect button and use another thread to listen for data
		connectButton.addActionListener(new ActionListener(){
			@Override public void actionPerformed(ActionEvent arg0) {
				if(connectButton.getText().equals("Conectar")) {
					// attempt to connect to the serial port
					chosenPort = SerialPort.getCommPort(portList.getSelectedItem().toString());
					chosenPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
					boolean isOpenPort = chosenPort.openPort();
					if(chosenPort.openPort()) {
						connectButton.setText("Desconectar");
						portList.setEnabled(false);
						chosenPort.writeBytes(buffer, buffer.length);
						System.out.println(new String(buffer));
						
					}
					
					// create a new thread that listens for incoming text and populates the graph
					Thread thread = new Thread(){
						@Override public void run() {
							Scanner scanner = new Scanner(chosenPort.getInputStream());
							while(scanner.hasNextLine()) {
								try {
									String line = scanner.nextLine();									
									int number = Integer.parseInt(line);
									System.out.println(number);
								} catch(Exception e) {}
							}
							scanner.close();
						}
					};
					thread.start();
				} else {
					// disconnect from the serial port
					chosenPort.closePort();
					portList.setEnabled(true);
					connectButton.setText("Conectar");
				}
			}
		});
		
		// show the window
		window.setVisible(true);
	}

}