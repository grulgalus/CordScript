import com.cordbot.core.CordCore;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Map;

public class WindowsApp {
    public static void main(String[] args) {
        JFrame frame = new JFrame("CordBot Maker - No Code Needed!");
        frame.setSize(600, 550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new FlowLayout());
        JButton helpBtn = new JButton("📖 NÁVOD / TUTORIAL");
        helpBtn.setBackground(new Color(114, 137, 218));
        helpBtn.setForeground(Color.WHITE);
        topPanel.add(helpBtn);
        frame.add(topPanel, BorderLayout.NORTH);

        String defCode = "jazyk = cz\n" +
                         "token = TVUJ_TOKEN_ZDE\n" +
                         "prefix = !\n" +
                         "slash = ano\n\n" +
                         "prikaz ping\n" +
                         "odpoved Tohle je ten nejlehci bot na svete!\n\n" +
                         "prikaz ahoj\n" +
                         "odpoved Cau, ja jsem tvuj novy bot!";
        JTextArea codeArea = new JTextArea(defCode);
        codeArea.setFont(new Font("Monospaced", Font.BOLD, 15));
        frame.add(new JScrollPane(codeArea), BorderLayout.CENTER);

        helpBtn.addActionListener(e -> {
            String tutorial = CordCore.getTutorial(codeArea.getText());
            JOptionPane.showMessageDialog(frame, tutorial, "Tutorial", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton startBtn = new JButton("🚀 SPUSTIT BOTA");
        startBtn.setBackground(new Color(67, 181, 129));
        startBtn.setForeground(Color.WHITE);
        startBtn.setFont(new Font("Arial", Font.BOLD, 16));
        
        startBtn.addActionListener(e -> {
            String code = codeArea.getText();
            String errorMsg = CordCore.validateCode(code);
            if (errorMsg != null) {
                JOptionPane.showMessageDialog(frame, errorMsg, "Chyba / Error 🕵️", JOptionPane.ERROR_MESSAGE);
                return;
            }

            startBtn.setText("Bot běží / Bot is running...");
            startBtn.setEnabled(false);
            
            new Thread(() -> {
                try {
                    String token = CordCore.getSetting(code, "token", "");
                    String prefix = CordCore.getSetting(code, "prefix", "!");
                    boolean useSlash = CordCore.getSetting(code, "slash", "ano").matches("(?i)ano|yes");
                    Map<String, String> commands = CordCore.getCommands(code);
                    
                    StringBuilder js = new StringBuilder();
                    js.append("const { Client, GatewayIntentBits, REST, Routes } = require('discord.js');\n");
                    js.append("const client = new Client({ intents: [GatewayIntentBits.Guilds, GatewayIntentBits.GuildMessages, GatewayIntentBits.MessageContent] });\n");
                    
                    if (useSlash && !commands.isEmpty()) {
                        js.append("const slashCmds = [");
                        for (String cmd : commands.keySet()) js.append("{name: '").append(cmd).append("', description: 'Command ").append(cmd).append("'},");
                        js.append("];\nconst rest = new REST({ version: '10' }).setToken('").append(token).append("');\n");
                        js.append("client.once('ready', async () => { await rest.put(Routes.applicationCommands(client.user.id), { body: slashCmds }); });\n");
                        js.append("client.on('interactionCreate', async interaction => {\n");
                        js.append("  if (!interaction.isChatInputCommand()) return;\n");
                        for (Map.Entry<String, String> cmd : commands.entrySet()) {
                            js.append("  if (interaction.commandName === '").append(cmd.getKey()).append("') { await interaction.reply('").append(cmd.getValue()).append("'); }\n");
                        }
                        js.append("});\n");
                    }
                    
                    js.append("client.on('messageCreate', msg => {\n  if(msg.author.bot) return;\n");
                    for (Map.Entry<String, String> cmd : commands.entrySet()) {
                        js.append("  if(msg.content.startsWith('").append(prefix).append(cmd.getKey()).append("')) { msg.reply('").append(cmd.getValue()).append("'); }\n");
                    }
                    js.append("});\nclient.login('").append(token).append("');\n");

                    FileWriter fw = new FileWriter(new File("bot.js")); fw.write(js.toString()); fw.close();
                    new ProcessBuilder("cmd.exe", "/c", "npm install discord.js && node bot.js").start();
                } catch (Exception ex) { ex.printStackTrace(); }
            }).start();
        });
        
        frame.add(startBtn, BorderLayout.SOUTH);
        frame.setVisible(true);
    }
}
