import com.cordbot.core.CordCore; // IMPORTOVÁNÍ SDÍLENÉHO JÁDRA!
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Map;

public class WindowsApp {
    public static void main(String[] args) {
        JFrame frame = new JFrame("CordBot - Java & Node.js Engine");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel top = new JPanel(new GridLayout(2, 1));
        top.add(new JLabel("Discord Token:"));
        JTextField tokenField = new JTextField();
        top.add(tokenField);
        frame.add(top, BorderLayout.NORTH);

        JTextArea codeArea = new JTextArea("prefix: \"!\"\ncommand ping {\n  reply \"Pong z Node.js (Core verze)!\"\n}");
        frame.add(new JScrollPane(codeArea), BorderLayout.CENTER);

        JButton startBtn = new JButton("🚀 Spustit bota (Node.js)");
        startBtn.setBackground(new Color(67, 181, 129));
        startBtn.setForeground(Color.WHITE);
        
        startBtn.addActionListener(e -> {
            startBtn.setText("Node.js Bot běží v pozadí...");
            startBtn.setEnabled(false);
            
            new Thread(() -> {
                try {
                    String token = tokenField.getText();
                    String code = codeArea.getText();
                    
                    // VOLÁME SDÍLENÉ JÁDRO!
                    String prefix = CordCore.parsePrefix(code);
                    Map<String, String> commands = CordCore.parseCommands(code);
                    
                    // Generování Node.js kódu pomocí dat z Jádra
                    StringBuilder js = new StringBuilder();
                    js.append("const { Client, GatewayIntentBits } = require('discord.js');\n");
                    js.append("const client = new Client({ intents: [GatewayIntentBits.Guilds, GatewayIntentBits.GuildMessages, GatewayIntentBits.MessageContent] });\n");
                    js.append("client.on('messageCreate', msg => {\n");
                    js.append("  if(msg.author.bot) return;\n");
                    
                    for (Map.Entry<String, String> cmd : commands.entrySet()) {
                        js.append("  if(msg.content.startsWith('").append(prefix).append(cmd.getKey()).append("')) { msg.reply('").append(cmd.getValue()).append("'); }\n");
                    }
                    
                    js.append("});\n");
                    js.append("client.login('").append(token).append("');\n");
                    js.append("console.log('Bot bezi přes Node.js!');\n");

                    FileWriter fw = new FileWriter(new File("bot.js"));
                    fw.write(js.toString());
                    fw.close();

                    ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "npm install discord.js && node bot.js");
                    pb.redirectErrorStream(true);
                    pb.start();
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
        });
        
        frame.add(startBtn, BorderLayout.SOUTH);
        frame.setVisible(true);
    }
}
