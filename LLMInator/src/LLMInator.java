import java.awt.Color;
import java.awt.Dimension;
import java.io.File;

import javax.swing.*;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.*;
public class LLMInator {
//TODO pass in image
	
	JFrame frame;
	JPanel panel, topPanel, bottomPanel;
	JTextField field;
	JLabel imageLabel;
	JButton chooserButton, sendButton;
	JLabel output;
	File imageFile;
	AnthropicClient client;
	String apiKey; //initialize this
	public static void main(String[] args) {
		new LLMInator();
	}
	public LLMInator() {
		client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();
		frame = new JFrame("LLMInator");
		panel = new JPanel();
		frame.setSize(800,600);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		topPanel = new JPanel();
		topPanel.setBackground(new Color(220,220,220));
		
		
		JPanel bufferPanel = new JPanel();
		bufferPanel.setOpaque(false);
		bufferPanel.setPreferredSize(new Dimension(70,70));
		topPanel.add(bufferPanel);
		field = new JTextField("Input...");
		field.setColumns(50);
		topPanel.add(field);
		JPanel imgPanel = new JPanel();
		imageLabel = new JLabel();
		imgPanel.add(imageLabel);
		chooserButton = new JButton("Choose Image");
		chooserButton.addActionListener(e->{
			JFileChooser jfc = new JFileChooser();
			if(jfc.showOpenDialog(null)==JFileChooser.APPROVE_OPTION) {
				imageFile = jfc.getSelectedFile();
			} else return;
			if(imageFile.getPath().endsWith(".png")||imageFile.getPath().endsWith(".jpg")||imageFile.getPath().endsWith(".jpeg")) {	
			imageLabel.setText(imageFile.getName());
			}
		});
		imgPanel.add(chooserButton);
		topPanel.add(imgPanel);
		
		JPanel bufferPanel2 = new JPanel();
		bufferPanel2.setOpaque(false);
		bufferPanel2.setPreferredSize(new Dimension(700,1));
		topPanel.add(bufferPanel2);
		
		sendButton = new JButton("Send it!");
		topPanel.add(sendButton);
		sendButton.addActionListener(e->{
			new Thread(()->{
				output.setText("<html><body style='width: 600px;'>"+callAI()+"</html>");
			}).run();
		});
		
		
		
		bottomPanel = new JPanel();
		bottomPanel.setBackground(Color.white);
		output = new JLabel("<html><body style='width: 600px;'>Output...</html>");
		
		bottomPanel.add(output);
		
		panel.add(topPanel);
		panel.add(bottomPanel);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(panel);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	String callAI() {
		String prompt = field.getText();
		MessageCreateParams params = MessageCreateParams.builder()
				.system("Respond only in plain text.")
				.model(Model.CLAUDE_HAIKU_4_5)
				.addUserMessage(prompt)
				.maxTokens(512)
				.build();
		Message msg = client.messages().create(params);
		return msg.toString();
		
	}
}
