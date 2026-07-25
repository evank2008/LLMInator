import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.*;

public class LLMInator {
	
	JFrame frame;
	JPanel panel, topPanel, bottomPanel;
	JTextField field;
	JLabel imageLabel;
	JButton chooserButton, sendButton;
	JLabel output;
	File imageFile;
	AnthropicClient client;
	String apiKey; 
	public static void main(String[] args) {
		new LLMInator();
	}
	public LLMInator() {
		apiKey = System.getenv("CLAUDE_API_KEY");
		//no secrets here
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
		field = new JTextField();
		field.setColumns(50);
		topPanel.add(field);
		JPanel imgPanel = new JPanel();
		imageLabel = new JLabel();
		imgPanel.add(imageLabel);
		chooserButton = new JButton("Choose Image");
		chooserButton.addActionListener(e->{
			JFileChooser jfc = new JFileChooser();
			FileFilter ff = new FileFilter() {
				public boolean accept(File f) {
					String name = f.getName();
					
					return name.endsWith(".png")||name.endsWith(".jpg")||name.endsWith(".jpeg")||f.isDirectory();
				}

				@Override
				public String getDescription() {
					// TODO Auto-generated method stub
					return "Image Files (.png, .jpg)";
				}
			};
			jfc.setFileFilter(ff);
			if(jfc.showOpenDialog(null)==JFileChooser.APPROVE_OPTION) {
				imageFile = jfc.getSelectedFile();
			
			if(imageFile.getPath().endsWith(".png")||imageFile.getPath().endsWith(".jpg")||imageFile.getPath().endsWith(".jpeg")) {	
			imageLabel.setText(imageFile.getName());
			}} else return;
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
				try {
					output.setText("<html><body style='width: 600px;'>"+
					parseMessage(callAI(field.getText(), imageFile))+"</html>");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					JOptionPane.showMessageDialog(null, "error: "+e1.getMessage());
				}
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
	
	String callAI(String prompt, File img) throws IOException{
		if(img==null) return callAI(prompt);
		long maxTokens=512;
		boolean isJpeg = isJpeg(img.getPath());
		String b64;
			
								
				byte[] bytes = Files.readAllBytes(img.toPath());
				b64 = Base64.getEncoder().encodeToString(bytes);

			
		MessageCreateParams params = MessageCreateParams.builder()
				.system("Respond only in plain text.")
				.model(Model.CLAUDE_HAIKU_4_5)
				//.addUserMessage(prompt)
				.addMessage(MessageParam.builder()
						.role(MessageParam.Role.USER)
						.content(MessageParam.Content.ofBlockParams(List.of(
								ContentBlockParam.ofImage(ImageBlockParam.builder().source(Base64ImageSource.builder()
										.data(b64)
										.mediaType(isJpeg?Base64ImageSource.MediaType.IMAGE_JPEG:Base64ImageSource.MediaType.IMAGE_PNG)
										.build()
										).build()
						), ContentBlockParam.ofText(TextBlockParam.builder().text(prompt).build())
								))).build())
				.maxTokens(maxTokens)
				.build();
		
		Message msg = client.messages().create(params);
		return msg.toString();
			}
		
	
	String callAI(String prompt) {
		long maxTokens=512;
		MessageCreateParams params = MessageCreateParams.builder()
				.system("Respond only in plain text.")
				.model(Model.CLAUDE_HAIKU_4_5)
				.addUserMessage(prompt)
				.maxTokens(maxTokens)
				.build();
		Message msg = client.messages().create(params);
		System.out.println(msg.toString());
		return msg.toString();
	}
	boolean isJpeg(String filePath) {
		byte[] header = new byte[8];
		try {
			if(new BufferedInputStream(new FileInputStream(filePath)).read(header,0,8)<2) {
				throw new RuntimeException("GigaError - small file");
			}
			if(header[0]==(byte)0xFF&&header[1]==(byte)0xD8) {
				return true;
			}
			byte[] pngMagic = {(byte) 0x89 ,0x50 ,0x4E ,0x47 ,0x0D ,0x0A ,0x1A ,0x0A};
			if(Arrays.equals(header, pngMagic)) return false;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new RuntimeException("Super GigaError");
	}
	String parseMessage(String rawMessage) {
		return rawMessage.split("text=")[2].split(", type=text")[0];
	}
}
