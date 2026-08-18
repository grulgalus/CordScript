import com.cordbot.core.CordCore;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Map;

public class WindowsApp {
    public static void main(String[] args) {
        JFrame frame = new JFrame("CordBot Maker - Pro začátečníky");
        frame.setSize(600, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new FlowLayout());
        JButton helpBtn = new JButton("📖 NÁVOD PRO ZAČÁTEČNÍKY");
        helpBtn.setBackground(new Color(114, 137, 218));
        helpBtn.setForeground(Color.WHITE);
        helpBtn.addActionListener(e -> showTutorial(frame));
        topPanel.add(helpBtn);
        frame.add(topPanel, BorderLayout.NORTH);

        String defCode = "token = TVUJ_TOKEN_ZDE\n" +
                         "prefix = !\n" +
                         "slash = ano\n" +
                         "jazyk = cz\n\n" +
                         "prikaz(ping)\n" +
                         "odpoved(Tohle je nejlehčí bot na světě!)\n\n" +
                         "prikaz(ahoj)\n" +
                         "odpoved(Čau, já jsem tvůj nový bot!)";
        JTextArea codeArea = new JTextArea(defCode);
        codeArea.setFont(new Font("Monospaced", Font.BOLD, 14));
        frame.add(new JScrollPane(codeArea), BorderLayout.CENTER);

        JButton startBtn = new JButton("🚀 SPUSTIT BOTA");
        startBtn.setBackground(new Color(67, 181, 129));
        startBtn.setForeground(Color.WHITE);
        startBtn.setFont(new Font("Arial", Font.BOLD, 16));
        
        startBtn.addActionListener(e -> {
            startBtn.setText("Bot běží...");
            startBtn.setEnabled(false);
            
            new Thread(() -> {
                try {
                    String code = codeArea.getText();
                    String token = CordCore.getSetting(code, "token", "");
                    String prefix = CordCore.getSetting(code, "prefix", "!");
                    boolean useSlash = CordCore.getSetting(code, "slash", "ano").equalsIgnoreCase("ano");
                    Map<String, String> commands = CordCore.getCommands(code);
                    
                    StringBuilder js = new StringBuilder();
                    js.append("const { Client, GatewayIntentBits, REST, Routes } = require('discord.js');\n");
                    js.append("const client = new Client({ intents: [GatewayIntentBits.Guilds, GatewayIntentBits.GuildMessages, GatewayIntentBits.MessageContent] });\n");
                    
                    if (useSlash) {
                        js.append("const slashCmds = [");
                        for (String cmd : commands.keySet()) {
                            js.append("{name: '").append(cmd).append("', description: 'Příkaz ").append(cmd).append("'},");
                        }
                        js.append("];\n");
                        js.append("const rest = new REST({ version: '10' }).setToken('").append(token).append("');\n");
                        js.append("client.once('ready', async () => {\n");
                        js.append("  await rest.put(Routes.applicationCommands(client.user.id), { body: slashCmds });\n");
                        js.append("  console.log('Slash příkazy nahrány!');\n});\n");
                        
                        js.append("client.on('interactionCreate', async interaction => {\n");
                        js.append("  if (!interaction.isChatInputCommand()) return;\n");
                        for (Map.Entry<String, String> cmd : commands.entrySet()) {
                            js.append("  if (interaction.commandName === '").append(cmd.getKey()).append("') { await interaction.reply('").append(cmd.getValue()).append("'); }\n");
                        }
                        js.append("});\n");
                    }
                    
                    js.append("client.on('messageCreate', msg => {\n");
                    js.append("  if(msg.author.bot) return;\n");
                    for (Map.Entry<String, String> cmd : commands.entrySet()) {
                        js.append("  if(msg.content.startsWith('").append(prefix).append(cmd.getKey()).append("')) { msg.reply('").append(cmd.getValue()).append("'); }\n");
                    }
                    js.append("});\n");
                    js.append("client.login('").append(token).append("');\n");

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

    private static void showTutorial(JFrame parent) {
        String tutorial = "KROK ZA KROKEM (Jak získat Token):\n\n" +
                          "1. Otevři prohlížeč a jdi na: discord.com/developers/applications\n" +
                          "2. Přihlaš se a klikni vpravo nahoře na modré 'New Application'\n" +
                          "3. Napiš jméno bota, potvrď podmínky a dej 'Create'\n" +
                          "4. V levém menu klikni na záložku 'Bot'\n" +
                          "5. ⚠️ DŮLEŽITÉ: Sjeď dolů k sekci 'Privileged Gateway Intents'\n" +
                          "   -> Zapni (aby bylo zelené) 'Message Content Intent'!\n" +
                          "6. Vyjeď nahoru a klikni na tlačítko 'Reset Token' a pak 'Copy'\n" +
                          "7. Tento zkopírovaný kód vlož do aplikace místo TVUJ_TOKEN_ZDE\n\n" +
                          "To je vše! Nyní klikni na 'Spustit bota' a jsi hacker!";
        JOptionPane.showMessageDialog(parent, tutorial, "Návod pro začátečníky", JOptionPane.INFORMATION_MESSAGE);
    }
}
