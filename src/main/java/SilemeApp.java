import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class SilemeApp {
    private static final String CONFIG_FILE = "sileme_config.ser";
    private Config config;
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SilemeApp().start());
    }
    
    public void start() {
        loadConfig();
        
        if (config.isFirstTimeSetup) {
            showSetupWizard();
        } else {
            showMainDialog();
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadConfig() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(CONFIG_FILE))) {
            config = (Config) ois.readObject();
        } catch (Exception e) {
            config = new Config();
        }
    }
    
    private void saveConfig() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CONFIG_FILE))) {
            oos.writeObject(config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    //配置类，实现 Serializable
    static class Config implements Serializable {
        private static final long serialVersionUID = 1L;
        public String myName;           //我的姓名（原用户）
        public String successorName;    //我嘱托的人的姓名（接管者）
        public String popupMessage;     //弹窗消息
        public List<String> folderList = new ArrayList<>(); // 要删除的文件夹列表
        public String confirmationCode; //确认码
        public boolean isFirstTimeSetup = true; //是否首次配置
        public int remainingAttempts = 20; //剩余尝试次数
        public int restartCount = 0; //重启次数
    }
    
    //首次配置向导
    private void showSetupWizard() {
        JFrame frame = new JFrame("死了么 - 首次配置");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLocationRelativeTo(null);
        
        showWelcomeScreen(frame);
    }
    
    private void showWelcomeScreen(JFrame frame) {
        JPanel panel = new JPanel(new BorderLayout());
        
        JLabel welcomeLabel = new JLabel("<html><div style='text-align: center;'><h1>欢迎使用死了么</h1><br>"
                + "本程序将在系统登录后自动运行<br>"
                + "请按照向导完成初始配置</div></html>", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        
        JButton nextButton = new JButton("开始配置");
        nextButton.addActionListener(e -> showMyNameScreen(frame));
        
        panel.add(welcomeLabel, BorderLayout.CENTER);
        panel.add(nextButton, BorderLayout.SOUTH);
        
        updateContent(frame, panel);
        frame.setVisible(true);
    }
    
    private void showMyNameScreen(JFrame frame) {
        JPanel panel = new JPanel(new BorderLayout());
        
        JLabel label = new JLabel("<html><h2>配置我的姓名</h2>请输入您的姓名（电脑原主人）：</html>");
        JTextField nameField = new JTextField(20);
        
        JButton nextButton = new JButton("下一步");
        nextButton.addActionListener(e -> {
            if (!nameField.getText().trim().isEmpty()) {
                config.myName = nameField.getText().trim();
                showSuccessorNameScreen(frame);
            } else {
                JOptionPane.showMessageDialog(frame, "请输入您的姓名");
            }
        });
        
        panel.add(label, BorderLayout.NORTH);
        panel.add(nameField, BorderLayout.CENTER);
        panel.add(nextButton, BorderLayout.SOUTH);
        
        updateContent(frame, panel);
    }
    
    private void showSuccessorNameScreen(JFrame frame) {
        JPanel panel = new JPanel(new BorderLayout());
        
        JLabel label = new JLabel("<html><h2>配置嘱托人姓名</h2>请输入您生前嘱托的人的姓名（电脑接管者）：</html>");
        JTextField nameField = new JTextField(20);
        
        JButton nextButton = new JButton("下一步");
        nextButton.addActionListener(e -> {
            if (!nameField.getText().trim().isEmpty()) {
                config.successorName = nameField.getText().trim();
                showPopupMessageScreen(frame);
            } else {
                JOptionPane.showMessageDialog(frame, "请输入嘱托人的姓名");
            }
        });
        
        panel.add(label, BorderLayout.NORTH);
        panel.add(nameField, BorderLayout.CENTER);
        panel.add(nextButton, BorderLayout.SOUTH);
        
        updateContent(frame, panel);
    }
    
    private void showPopupMessageScreen(JFrame frame) {
        JPanel panel = new JPanel(new BorderLayout());
        
        JLabel label = new JLabel("<html><h2>配置弹窗消息</h2>请输入在您死亡确认后显示给 " + config.successorName + " 的消息：</html>");
        JTextArea messageArea = new JTextArea(5, 30);
        messageArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        
        JButton nextButton = new JButton("下一步");
        nextButton.addActionListener(e -> {
            if (!messageArea.getText().trim().isEmpty()) {
                config.popupMessage = messageArea.getText().trim();
                showFolderSelectionScreen(frame);
            } else {
                JOptionPane.showMessageDialog(frame, "请输入弹窗消息");
            }
        });
        
        panel.add(label, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(nextButton, BorderLayout.SOUTH);
        
        updateContent(frame, panel);
    }
    
    private void showFolderSelectionScreen(JFrame frame) {
        JPanel panel = new JPanel(new BorderLayout());
        
        JLabel label = new JLabel("<html><h2>配置文件夹列表</h2>请选择在您死亡后需要删除的文件夹：</html>");
        
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> folderList = new JList<>(listModel);
        JScrollPane listScrollPane = new JScrollPane(folderList);
        
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("添加文件夹");
        JButton removeButton = new JButton("移除选中");
        JButton nextButton = new JButton("下一步");
        
        addButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = chooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                String path = chooser.getSelectedFile().getAbsolutePath();
                if (!listModel.contains(path)) {
                    listModel.addElement(path);
                    config.folderList.add(path);
                }
            }
        });
        
        removeButton.addActionListener(e -> {
            int selectedIndex = folderList.getSelectedIndex();
            if (selectedIndex != -1) {
                config.folderList.remove(selectedIndex);
                listModel.remove(selectedIndex);
            }
        });
        
        nextButton.addActionListener(e -> showConfirmationCodeScreen(frame));
        
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(nextButton);
        
        panel.add(label, BorderLayout.NORTH);
        panel.add(listScrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        updateContent(frame, panel);
    }
    
    private void showConfirmationCodeScreen(JFrame frame) {
        JPanel panel = new JPanel(new BorderLayout());
        
        JLabel label = new JLabel("<html><h2>配置确认码</h2>请设置确认码（用于证明您还活着）：</html>");
        JPasswordField codeField = new JPasswordField(20);
        JPasswordField confirmField = new JPasswordField(20);
        
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        inputPanel.add(new JLabel("确认码："));
        inputPanel.add(codeField);
        inputPanel.add(new JLabel("确认确认码："));
        inputPanel.add(confirmField);
        
        JButton finishButton = new JButton("完成配置");
        finishButton.addActionListener(e -> {
            String code = new String(codeField.getPassword());
            String confirm = new String(confirmField.getPassword());
            
            if (code.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "请输入确认码");
            } else if (!code.equals(confirm)) {
                JOptionPane.showMessageDialog(frame, "两次输入的确认码不一致");
            } else {
                config.confirmationCode = code;
                config.isFirstTimeSetup = false;
                saveConfig();
                JOptionPane.showMessageDialog(frame, 
                    "<html>配置完成！<br>程序将在下次登录时自动运行。<br><br>"
                    + "<b>配置摘要：</b><br>"
                    + "- 您的姓名: " + config.myName + "<br>"
                    + "- 嘱托人: " + config.successorName + "<br>"
                    + "- 确认码: " + "*".repeat(code.length()) + "<br><br>"
                    + "<b>使用说明：</b><br>"
                    + "- 程序会询问 " + config.myName + " 是否已死亡<br>"
                    + "- 如果确认死亡，将验证接管者身份并删除指定数据<br>"
                    + "- 如果未死亡，需要输入确认码证明身份</html>");
                frame.dispose();
                showMainDialog();
            }
        });
        
        panel.add(label, BorderLayout.NORTH);
        panel.add(inputPanel, BorderLayout.CENTER);
        panel.add(finishButton, BorderLayout.SOUTH);
        
        updateContent(frame, panel);
    }
    
    private void updateContent(JFrame frame, JPanel newPanel) {
        frame.getContentPane().removeAll();
        frame.getContentPane().add(newPanel);
        frame.revalidate();
        frame.repaint();
    }
    
    //主对话框
    private void showMainDialog() {
        JFrame frame = new JFrame("死了么");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(500, 220);
        frame.setLocationRelativeTo(null);
        frame.setAlwaysOnTop(true);
        
        JPanel panel = new JPanel(new BorderLayout());
        
        JLabel questionLabel = new JLabel("<html><div style='text-align: center; font-size: 16px;'>"
                + "<b>用户 " + config.myName + " 死了吗？</b><br><br>"
                + "如果你是接管者，请点击\"死了\"<br>"
                + "如果你是原用户，请点击\"没死\"</div></html>", 
                SwingConstants.CENTER);
        
        JPanel buttonPanel = new JPanel();
        JButton deadButton = new JButton("是的，他死了");
        JButton aliveButton = new JButton("不，我没死");
        
        deadButton.addActionListener(e -> handleDeathConfirmation(frame));
        aliveButton.addActionListener(e -> handleAliveConfirmation(frame));
        
        buttonPanel.add(deadButton);
        buttonPanel.add(aliveButton);
        
        panel.add(questionLabel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        frame.add(panel);
        frame.setVisible(true);
    }
    
    private void handleDeathConfirmation(JFrame frame) {
        int confirmationCount = 0;
        
        for (int i = 0; i < 3; i++) {
            int result = JOptionPane.showConfirmDialog(frame, 
                "第 " + (i + 1) + " 次确认：你真的确定 " + config.myName + " 已经死了吗？", 
                "死亡确认", JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                confirmationCount++;
            } else {
                break;
            }
        }
        
        if (confirmationCount == 3) {
            //验证接管者身份
            //GJJ I love you!!!
            String inputName = JOptionPane.showInputDialog(frame, 
                "请输入您的姓名以验证身份：");
            
            if (inputName != null && inputName.trim().equals(config.successorName)) {
                //身份验证成功
                JOptionPane.showMessageDialog(frame, 
                    "<html><b>身份验证成功！</b><br><br>"
                    + "欢迎，" + config.successorName + "<br><br>"
                    + config.popupMessage + "</html>");
                
                //执行清理操作
                performCleanup();
                
                JOptionPane.showMessageDialog(frame, 
                    "数据清理完成。");
                System.exit(0);
            } else {
                JOptionPane.showMessageDialog(frame, 
                    "身份验证失败！\n您输入的姓名与预设的嘱托人不匹配。", 
                    "验证失败", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void handleAliveConfirmation(JFrame frame) {
        int attempts = config.remainingAttempts;
        
        while (attempts > 0) {
            String inputCode = JOptionPane.showInputDialog(frame, 
                "你好 " + config.myName + "，请输入确认码证明你还活着\n剩余尝试次数: " + attempts);
            
            if (inputCode == null) {
                break;
            }
            
            if (inputCode.equals(config.confirmationCode)) {
                JOptionPane.showMessageDialog(frame, "验证成功！欢迎回来，" + config.myName + "。");
                config.remainingAttempts = 20;
                saveConfig();
                frame.dispose();
                return;
            } else {
                attempts--;
                config.remainingAttempts--;
                saveConfig();
                
                if (attempts > 0) {
                    JOptionPane.showMessageDialog(frame, 
                        "确认码错误！剩余尝试次数: " + attempts, 
                        "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        
        if (config.remainingAttempts <= 0) {
            JOptionPane.showMessageDialog(frame, 
                "尝试次数已耗尽，将删除所有数据，你本可以进入pe删除该程序的，给你机会你不中用啊", 
                "严重警告", JOptionPane.WARNING_MESSAGE);
            
            destroyAllFiles();
            System.exit(0);
        }
    }
    
    //删Edge记录和指定文件夹
    private void performCleanup() {
        clearEdgeBrowserHistory();
        
        if (config.folderList != null && !config.folderList.isEmpty()) {
            for (String folderPath : config.folderList) {
                deleteFolderRecursively(folderPath);
            }
        }
    }
    
    //删Edge历史记录
    private void clearEdgeBrowserHistory() {
        try {
            String edgeDataPath = System.getProperty("user.home") + 
                "\\AppData\\Local\\Microsoft\\Edge\\User Data\\Default";
            
            File edgeDataDir = new File(edgeDataPath);
            if (edgeDataDir.exists()) {
                deleteFilesInDirectory(edgeDataPath, "Cache");
                deleteFilesInDirectory(edgeDataPath, "Code Cache");
                deleteFilesInDirectory(edgeDataPath, "GPUCache");
                
                deleteFile(edgeDataPath, "History");
                deleteFile(edgeDataPath, "Visited Links");
                deleteFile(edgeDataPath, "Top Sites");
                deleteFile(edgeDataPath, "Shortcuts");
                deleteFile(edgeDataPath, "Cookies");
                deleteFile(edgeDataPath, "Login Data");
                deleteFile(edgeDataPath, "Web Data");
                deleteFile(edgeDataPath, "Favicons");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void deleteFilesInDirectory(String parentPath, String dirName) {
        try {
            Path dirPath = Paths.get(parentPath, dirName);
            if (Files.exists(dirPath)) {
                Files.walkFileTree(dirPath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }
                    
                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void deleteFile(String parentPath, String fileName) {
        try {
            Path filePath = Paths.get(parentPath, fileName);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    //递归删文件夹
    private void deleteFolderRecursively(String folderPath) {
        try {
            Path path = Paths.get(folderPath);
            if (Files.exists(path)) {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }
                    
                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    //确认码耗尽后删所有文件
    private void destroyAllFiles() {
        JOptionPane.showMessageDialog(null, 
            "开始执行数据销毁程序...\n这将删除用户目录下的所有文件！", 
            "数据销毁", JOptionPane.WARNING_MESSAGE);
        
        try {
            //删用户主目录下的所有文件但保留系统关键目录
            String userHome = System.getProperty("user.home");
            deleteUserFiles(userHome);
            
            //删临时文件
            cleanTempFiles();
            
            //删下载、文档、桌面...
            deleteSpecialFolders();
            
            JOptionPane.showMessageDialog(null, 
                "数据删除完成！", 
                "完成", JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                "数据删除过程中出现错误: " + e.getMessage(), 
                "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteUserFiles(String userHome) {
        File userDir = new File(userHome);
        File[] files = userDir.listFiles();
        
        if (files != null) {
            for (File file : files) {
                if (!file.getName().equals("AppData") && 
                    !file.getName().equals("Application Data") &&
                    !file.getName().equals("Local Settings")) {
                    
                    if (file.isDirectory()) {
                        deleteFolderRecursively(file.getAbsolutePath());
                    } else {
                        file.delete();
                    }
                }
            }
        }
    }
    
    private void cleanTempFiles() {
        try {
            //删临时文件
            String tempDir = System.getProperty("java.io.tmpdir");
            deleteFolderRecursively(tempDir);
            
            //删Windows临时文件
            String winTemp = "C:\\Windows\\Temp";
            File winTempDir = new File(winTemp);
            if (winTempDir.exists()) {
                File[] tempFiles = winTempDir.listFiles();
                if (tempFiles != null) {
                    for (File file : tempFiles) {
                        if (file.isFile()) {
                            file.delete();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void deleteSpecialFolders() {
        String userHome = System.getProperty("user.home");
        String[] specialFolders = {
            "Documents", "My Documents", "Downloads", "Desktop",
            "Pictures", "Music", "Videos", "Favorites"
        };
        
        for (String folder : specialFolders) {
            String folderPath = userHome + "\\" + folder;
            deleteFolderRecursively(folderPath);
        }
    }
}

// GJJ 我想和你永远做好朋友，虽然我们已经不可能在一起，但在你旁边我很开心
// I'm goona make a change
// For once in my life

// 这些注释不会存在很久，我将在稍后上传的更新中删除，所以且看且珍惜
// 如果有能力的话可不可以给我的项目点个star，这是我的第一个长期维护项目，项目地址将在更新后发送给墙墙awa
