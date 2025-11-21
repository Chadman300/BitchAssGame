import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public class Editor extends JFrame
{
    public static int windowWidth = 1040;
    public static int windowHeight = 580;

    private EditorPanel mainEditorPanel;
    private WindowPanel mainWindowPanel;

    public Editor(EditorPanel mainEditorPanel, WindowPanel mainWindowPanel) 
    {
        this.mainEditorPanel = mainEditorPanel;
        this.mainWindowPanel = mainWindowPanel;

        //get num of monitors
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screenDevices = ge.getScreenDevices();

        //set window properties
        setTitle("Editor :)");
        setSize(windowWidth, windowHeight);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        if(screenDevices.length > 1)
        {
            //if more than one monitor, set window to second monitor
            GraphicsDevice secondMonitor = screenDevices[1];
            Rectangle bounds = secondMonitor.getDefaultConfiguration().getBounds();
            setLocation(bounds.x + (bounds.width - windowWidth) / 2, 
                        bounds.y + (bounds.height - windowHeight) / 2);
        }
        else
        {
            //center window on primary monitor
            setLocationRelativeTo(null);
        }

        // Create control panel with checkboxes
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        controlPanel.setBackground(Color.LIGHT_GRAY);

        // Create a panel for editor buttons
        JPanel editorPanel = new JPanel();
        editorPanel.setLayout(new FlowLayout());

        // Create sidebar panel
        JPanel sidebarPanel = createSidebarPanel();

        // Create buttons
        JCheckBox editorEditingCheckBox = new JCheckBox("Edit Simulation", mainEditorPanel.isEditing);
        JButton editorClearButton = new JButton("Clear Editor Polys");
        JButton editorSaveButton = new JButton("Save");
        JButton editorColorButton = new JButton("Change Color");
        JButton editorLoadButton = new JButton("Load");
        
        // Create checkboxes
        JCheckBox pauseCheckBox = new JCheckBox("Pause Game", mainWindowPanel.isPaused);
        JCheckBox showGridCheckBox = new JCheckBox("Show Grid", true);
        JCheckBox showDebugCheckBox = new JCheckBox("Show Debug Info", false);

        // Create gravity input field
        JLabel gravityLabel = new JLabel("Gravity:");
        JTextField gravityField = new JTextField(String.valueOf(mainWindowPanel.gravity), 5);

        // Create substeps input field
        JLabel subStepsLabel = new JLabel("Substeps:");
        JTextField subStepsField = new JTextField(String.valueOf(mainWindowPanel.subSteps), 3);

                // Create clear button
        JButton clearButton = new JButton("Clear Balls");
        JButton debugSpawnButton = new JButton("Debug Spawn");

        // Add action listeners
        editorClearButton.addActionListener(e -> mainEditorPanel.clearPolys());
        editorSaveButton.addActionListener(e -> mainEditorPanel.savePolys());
        editorColorButton.addActionListener(e -> mainEditorPanel.changeCurrentColor());
        editorLoadButton.addActionListener(e -> mainEditorPanel.loadPolys());

        // Add action listeners
        editorEditingCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainEditorPanel.isEditing = editorEditingCheckBox.isSelected();
            }
        });

        pauseCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainWindowPanel.isPaused = pauseCheckBox.isSelected();
            }
        });

        showGridCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainWindowPanel.showGrid = showGridCheckBox.isSelected();
            }
        });

        showDebugCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainWindowPanel.showDebugInfo = showDebugCheckBox.isSelected();
            }
        });

        gravityField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    double newGravity = Double.parseDouble(gravityField.getText());
                    mainWindowPanel.gravity = newGravity;
                } catch (NumberFormatException ex) {
                    // Reset to current value if invalid input
                    gravityField.setText(String.valueOf(mainWindowPanel.gravity));
                }
            }
        });

        subStepsField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int newSubSteps = Integer.parseInt(subStepsField.getText());
                    mainWindowPanel.subSteps = newSubSteps;
                } catch (NumberFormatException ex) {
                    // Reset to current value if invalid input
                    subStepsField.setText(String.valueOf(mainWindowPanel.subSteps));
                }
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainWindowPanel.clearAllCircles();
            }
        });

        debugSpawnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DebugSpawn();
            }
        });

        // Add buttons to panel
        editorPanel.add(editorEditingCheckBox);
        editorPanel.add(editorClearButton);
        editorPanel.add(editorSaveButton);
        editorPanel.add(editorColorButton);
        editorPanel.add(editorLoadButton);

        controlPanel.add(pauseCheckBox);
        controlPanel.add(showGridCheckBox);
        controlPanel.add(showDebugCheckBox);

        // Add checkboxes and button to control panel
        controlPanel.add(clearButton);
        controlPanel.add(debugSpawnButton);
        controlPanel.add(gravityLabel);
        controlPanel.add(gravityField);
        controlPanel.add(subStepsLabel);
        controlPanel.add(subStepsField);

        // Set layout and add components
        setLayout(new BorderLayout());
        add(mainEditorPanel, BorderLayout.CENTER);
        add(editorPanel, BorderLayout.SOUTH);
        add(sidebarPanel, BorderLayout.WEST);
        add(controlPanel, BorderLayout.NORTH);

        setVisible(true);
        setBackground(Color.black);
        setExtendedState(JFrame.MAXIMIZED_BOTH); 
    }

    private JPanel createSidebarPanel() 
    {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.DARK_GRAY);
        sidebar.setPreferredSize(new Dimension(150, windowHeight));
        sidebar.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY), 
            "Ball Editor", 
            0, 
            0, 
            null, 
            Color.WHITE
        ));

        // Add title label
        JLabel titleLabel = new JLabel("Select Ball Type:");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(titleLabel);
        sidebar.add(Box.createVerticalStrut(10));

        // Create different ball types
        createSelectableBall(sidebar, BallType.SMALL_RED);
        createSelectableBall(sidebar, BallType.MEDIUM_BLUE);
        createSelectableBall(sidebar, BallType.LARGE_GREEN);
        createSelectableBall(sidebar, BallType.XL_YELLOW);
        createSelectableBall(sidebar, BallType.TINY_MAGENTA);
        createSelectableBall(sidebar, BallType.MEDIUM_LINK);

        sidebar.add(Box.createVerticalGlue()); // Push everything to top

        return sidebar;
    }

    private void createSelectableBall(JPanel parent, BallType ballType) 
    {
        JPanel ballContainer = new JPanel();
        ballContainer.setLayout(new BorderLayout());
        ballContainer.setMaximumSize(new Dimension(130, 60));
        ballContainer.setBackground(Color.DARK_GRAY);
        ballContainer.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // Create ball preview panel
        JPanel ballPreview = new JPanel() {
            private BufferedImage ballImage;
            
            {
                // Try to load the image in the initializer block
                try {
                    File imageFile = new File(ballType.getImagePath());
                    if (imageFile.exists()) {
                        ballImage = ImageIO.read(imageFile);
                    }
                } catch (IOException e) {
                    System.out.println("Could not load image: " + ballType.getImagePath() + " - " + e.getMessage());
                    ballImage = null;
                }
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                
                if (ballImage != null) {
                    // Draw the image, scaled to fit the ball size
                    int drawSize = Math.min(ballType.getRadius() * 2, 30); // Limit max size for UI
                    int imageX = centerX - drawSize / 2;
                    int imageY = centerY - drawSize / 2;
                    g2d.drawImage(ballImage, imageX, imageY, drawSize, drawSize, this);
                } 
                else 
                {
                    // Fallback: draw a simple colored circle if image not found
                    g2d.setColor(Color.LIGHT_GRAY);
                    int radius = Math.min(ballType.getRadius(), 15); // Limit max size for UI
                    g2d.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
                }
                           
                // Highlight if selected
                if (mainEditorPanel.selectedBallType == ballType) 
                {
                    g2d.setColor(Color.YELLOW);
                    g2d.setStroke(new BasicStroke(3));
                    int highlightSize = Math.min(ballType.getRadius(), 15) + 4;
                    g2d.drawOval(centerX - highlightSize/2, centerY - highlightSize/2, 
                               highlightSize, highlightSize);
                }
            }
        };
        ballPreview.setPreferredSize(new Dimension(50, 40));
        ballPreview.setBackground(Color.DARK_GRAY);

        // Create label
        JLabel ballLabel = new JLabel(ballType.getDisplayName());
        ballLabel.setForeground(Color.WHITE);
        ballLabel.setFont(new Font("Arial", Font.PLAIN, 10));

        ballContainer.add(ballPreview, BorderLayout.CENTER);
        ballContainer.add(ballLabel, BorderLayout.SOUTH);

        // Add click functionality to select this ball type
        ballContainer.addMouseListener(new MouseAdapter() 
        {
            @Override
            public void mousePressed(MouseEvent e) {
                mainEditorPanel.selectedBallType = ballType;
                // Repaint the sidebar to show the selection
                parent.repaint();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ballContainer.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ballContainer.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            }
        });

        parent.add(ballContainer);
        parent.add(Box.createVerticalStrut(5));
    }

    public void DebugSpawn()
    {
        int radius = (int)(15 / 3);
        int maxBalls = 10000;
        int gridSize = 150;
        
        // Fix the circle positioning in main method
        for (int i = 0; i < maxBalls; i++) 
        {
            int x = ((windowWidth / 5)) + (radius + 1) * 2 * (i % gridSize); // Arrange in rows of 10
            int y = ((windowHeight / 7)) + (radius + 1) * 2 * (i / gridSize); // Stack vertically
            var newCircle = new Circle(radius, x, y, 1, Color.GREEN);
            //newCircle.ApplyForce(0.1, new Vector2D(0, -1));

            mainWindowPanel.circlesList.add(newCircle);
        }
    }
}

