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

    public static void main(String[] args) {
        JFrame frame = new JFrame("CordBot Maker - Blbuvzdorná edice");
        frame.setSize(600, 550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new FlowLayout());
        
        JButton helpBtn = new JButton("📖 NÁVOD");
        helpBtn.setBackground(new Color(114, 137, 218));
        helpBtn.setForeground(Color.WHITE);
        
        JButton setBtn = new JButton("⚙️ NASTAVENÍ");
        setBtn.setBackground(Color.DARK_GRAY);
        setBtn.setForeground(Color.WHITE);
        
        topPanel.add(helpBtn);
        topPanel.add(setBtn);
        frame.add(topPanel, BorderLayout.NORTH);

        String defCode = "prikaz ping\nodpoved Tohle je ten nejlehci bot na svete!\n\nprikaz ahoj\nodpoved Cau, ja jsem tvuj novy bot!";
        JTextArea codeArea = new JTextArea(defCode);
        codeArea.setFont(new Font("Monospaced", Font.BOLD, 15));
        frame.add(new JScrollPane(codeArea), BorderLayout.CENTER);

        helpBtn.addActionListener(e -> JOptionPane.showMessageDialog(frame, CordCore.getTutorial(botLang), "Tutorial", JOptionPane.INFORMATION_MESSAGE));
        setBtn.addActionListener(e -> openSettings(frame));

        JButton startBtn = new JButton("🚀 SPUSTIT BOTA");
        startBtn.setBackground(new Color(67, 181, 129));
        startBtn.setForeground(Color.WHITE);
        startBtn.setFont(new Font("Arial", Font.BOLD, 16));
        
        startBtn.addActionListener(e -> {
            String code = codeArea.getText();
            String errorMsg = CordCore.validateCode(code, botLang, botToken);
            if (errorMsg != null) {
                JOptionPane.showMessageDialog(frame, errorMsg, "Chyba / Error 🕵️", JOptionPane.ERROR_MESSAGE);
                return;
            }

            startBtn.setText(botLang.equals("en") ? "Bot is running..." : "Bot běží...");
            startBtn.setEnabled(false);
            
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
                        js.append("client.once('ready', async () => { await rest.put(Routes.applicationCommands(client.user.id), { body: slashCmds }); });\n");
                        js.append("client.on('interactionCreate', async interaction => {\n");
                        js.append("  if (!interaction.isChatInputCommand()) return;\n");
                        for (Map.Entry<String, String> cmd : commands.entrySet()) {
                            js.append("  if (interaction.commandName === '").append(cmd.getKey()).append("') { await interaction.reply('").append(cmd.getValue()).append("'); }\n");
                        }
                        js.append("});\n");
                    } else {
                        js.append("client.on('messageCreate', msg => {\n  if(msg.author.bot) return;\n");
                        for (Map.Entry<String, String> cmd : commands.entrySet()) {
                            js.append("  if(msg.content.startsWith('").append(botPrefix).append(cmd.getKey()).append("')) { msg.reply('").append(cmd.getValue()).append("'); }\n");
                        }
                        js.append("});\n");
                    }
                    js.append("client.login('").append(botToken).append("');\n");

                    FileWriter fw = new FileWriter(new File("bot.js")); fw.write(js.toString()); fw.close();
                    new ProcessBuilder("cmd.exe", "/c", "npm install discord.js && node bot.js").start();
                } catch (Exception ex) { ex.printStackTrace(); }
            }).start();
        });
        
        frame.add(startBtn, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private static void openSettings(JFrame parent) {
        JDialog d = new JDialog(parent, "⚙️ Nastavení", true);
        d.setLayout(new GridLayout(5, 2, 10, 10));
        d.setSize(400, 250);

        d.add(new JLabel("Discord Token:"));
        JPasswordField tokenF = new JPasswordField(botToken); // ZATEČKOVANÝ TOKEN
        d.add(tokenF);

        d.add(new JLabel("Jazyk / Language:"));
        JComboBox<String> langC = new JComboBox<>(new String[]{"CZ", "EN"});
        langC.setSelectedItem(botLang.toUpperCase());
        d.add(langC);

        d.add(new JLabel("Slash příkazy (/):"));
        JCheckBox slashC = new JCheckBox("", botSlash);
        d.add(slashC);

        d.add(new JLabel("Prefix (pokud neni slash):"));
        JTextField prefixF = new JTextField(botPrefix);
        prefixF.setEnabled(!botSlash); // Vyšednutí podle toho, co je zrovna nastaveno
        d.add(prefixF);

        slashC.addActionListener(e -> {
            prefixF.setEnabled(!slashC.isSelected()); // Vypínání prefixu pokud je Slash
        });

        JButton saveBtn = new JButton("ULOŽIT");
        saveBtn.addActionListener(e -> {
            botToken = new String(tokenF.getPassword());
            botLang = langC.getSelectedItem().toString().toLowerCase();
            botSlash = slashC.isSelected();
            botPrefix = prefixF.getText();
            d.dispose(); // Zavře se po uložení
        });
        d.add(new JLabel("")); d.add(saveBtn);

        d.setLocationRelativeTo(parent);
        d.setVisible(true);
    }
}
