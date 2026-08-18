import com.cordbot.core.CordCore;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Map;

public class WindowsApp {
    static String botToken = "";
    static String botLang = "cz";
    static boolean botSlash = true;
    static String botPrefix = "!";
    static Process runningProcess = null; // Přidáno pro správné vypnutí bota

    public static void main(String[] args) {
        JFrame frame = new JFrame("CordBot Maker - Python Style Edition");
        frame.setSize(700, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new FlowLayout());
        JButton helpBtn = new JButton("📖 NÁVOD"); helpBtn.setBackground(new Color(114, 137, 218)); helpBtn.setForeground(Color.WHITE);
        JButton setBtn = new JButton("⚙️ NASTAVENÍ"); setBtn.setBackground(Color.DARK_GRAY); setBtn.setForeground(Color.WHITE);
        topPanel.add(helpBtn); topPanel.add(setBtn);
        frame.add(topPanel, BorderLayout.NORTH);

        // Kód s Python syntaxí (Dvojtečka a 4 mezery)
        String defCode = "prikaz ping:\n    odpoved Tohle je ten nejlehci bot na svete!\n\nprikaz ahoj:\n    odpoved Cau, ja jsem tvuj novy bot!";
        JTextArea codeArea = new JTextArea(defCode);
        codeArea.setFont(new Font("Monospaced", Font.BOLD, 15));
        
        // KONZOLE
        JTextArea consoleArea = new JTextArea("Zde uvidíš výstup konzole...\n");
        consoleArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        consoleArea.setBackground(Color.BLACK);
        consoleArea.setForeground(new Color(0, 255, 0)); // Hackerská zelená
        consoleArea.setEditable(false);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(codeArea), new JScrollPane(consoleArea));
        splitPane.setDividerLocation(300);
        frame.add(splitPane, BorderLayout.CENTER);

        helpBtn.addActionListener(e -> JOptionPane.showMessageDialog(frame, CordCore.getTutorial(botLang), "Tutorial", JOptionPane.INFORMATION_MESSAGE));
        setBtn.addActionListener(e -> openSettings(frame));

        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        JButton startBtn = new JButton("🚀 SPUSTIT BOTA"); startBtn.setBackground(new Color(67, 181, 129)); startBtn.setForeground(Color.WHITE);
        JButton stopBtn = new JButton("🛑 ZASTAVIT"); stopBtn.setBackground(new Color(240, 71, 71)); stopBtn.setForeground(Color.WHITE); stopBtn.setEnabled(false);
        bottomPanel.add(startBtn); bottomPanel.add(stopBtn);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        stopBtn.addActionListener(e -> {
            if(runningProcess != null) runningProcess.destroy();
            consoleArea.append("\n[INFO] Bot byl manuálně vypnut.\n");
            startBtn.setEnabled(true); stopBtn.setEnabled(false);
        });
        
        startBtn.addActionListener(e -> {
            String code = codeArea.getText();
            String errorMsg = CordCore.validateCode(code, botLang, botToken);
            if (errorMsg != null) { JOptionPane.showMessageDialog(frame, errorMsg, "Chyba 🕵️", JOptionPane.ERROR_MESSAGE); return; }

            consoleArea.setText("Generuji kód bota...\n");
            startBtn.setEnabled(false); stopBtn.setEnabled(true);
            
            new Thread(() -> {
                try {
                    Map<String, String> commands = CordCore.getCommands(code, botLang);
                    StringBuilder js = new StringBuilder();
                    js.append("const { Client, GatewayIntentBits, REST, Routes } = require('discord.js');\n");
                    js.append("const client = new Client({ intents: [GatewayIntentBits.Guilds, GatewayIntentBits.GuildMessages, GatewayIntentBits.MessageContent] });\n");
                    
                    if (botSlash && !commands.isEmpty()) {
                        js.append("const slashCmds = [");
                        for (String cmd : commands.keySet()) js.append("{name: '").append(cmd).append("', description: 'Command ").append(cmd).append("'},");
                        js.append("];\nconst rest = new REST({ version: '10' }).setToken('").append(botToken).append("');\n");
                        js.append("client.once('ready', async () => {\n  console.log('✅ Bot je online a připojen!');\n  await rest.put(Routes.applicationCommands(client.user.id), { body: slashCmds });\n  console.log('✅ Slash příkazy byly úspěšně nahrány!');\n});\n");
                        js.append("client.on('interactionCreate', async interaction => {\n  if (!interaction.isChatInputCommand()) return;\n");
                        for (Map.Entry<String, String> cmd : commands.entrySet()) {
                            js.append("  if (interaction.commandName === '").append(cmd.getKey()).append("') { console.log(`Uživatel použil /${interaction.commandName}`); await interaction.reply('").append(cmd.getValue()).append("'); }\n");
                        }
                        js.append("});\n");
                    } else {
                        js.append("client.once('ready', () => console.log('✅ Bot je online!'));\n");
                        js.append("client.on('messageCreate', msg => {\n  if(msg.author.bot) return;\n");
                        for (Map.Entry<String, String> cmd : commands.entrySet()) {
                            js.append("  if(msg.content.startsWith('").append(botPrefix).append(cmd.getKey()).append("')) { console.log(`Přijat příkaz: ${msg.content}`); msg.reply('").append(cmd.getValue()).append("'); }\n");
                        }
                        js.append("});\n");
                    }
                    js.append("client.login('").append(botToken).append("').catch(err => console.error('❌ CHYBA TOKENU: Zkontroluj token a oprávnění na Discordu!'));\n");

                    FileWriter fw = new FileWriter(new File("bot.js")); fw.write(js.toString()); fw.close();
                    
                    ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "npm install discord.js && node bot.js");
                    runningProcess = pb.start();
                    
                    // ČTENÍ LOGŮ DO KONZOLE
                    BufferedReader reader = new BufferedReader(new InputStreamReader(runningProcess.getInputStream()));
                    String line; while ((line = reader.readLine()) != null) { 
                        String l = line; SwingUtilities.invokeLater(() -> consoleArea.append(l + "\n")); 
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }).start();
        });
        
        frame.setVisible(true);
    }

    private static void openSettings(JFrame parent) {
        JDialog d = new JDialog(parent, "⚙️ Nastavení", true); d.setLayout(new GridLayout(5, 2, 10, 10)); d.setSize(400, 250);
        d.add(new JLabel("Discord Token:")); JPasswordField tokenF = new JPasswordField(botToken); d.add(tokenF);
        d.add(new JLabel("Jazyk / Language:")); JComboBox<String> langC = new JComboBox<>(new String[]{"CZ", "EN"}); langC.setSelectedItem(botLang.toUpperCase()); d.add(langC);
        d.add(new JLabel("Slash příkazy (/):")); JCheckBox slashC = new JCheckBox("", botSlash); d.add(slashC);
        d.add(new JLabel("Prefix (pokud neni slash):")); JTextField prefixF = new JTextField(botPrefix); prefixF.setEnabled(!botSlash); d.add(prefixF);
        slashC.addActionListener(e -> prefixF.setEnabled(!slashC.isSelected()));
        JButton saveBtn = new JButton("ULOŽIT");
        saveBtn.addActionListener(e -> { botToken = new String(tokenF.getPassword()); botLang = langC.getSelectedItem().toString().toLowerCase(); botSlash = slashC.isSelected(); botPrefix = prefixF.getText(); d.dispose(); });
        d.add(new JLabel("")); d.add(saveBtn); d.setLocationRelativeTo(parent); d.setVisible(true);
    }
}
