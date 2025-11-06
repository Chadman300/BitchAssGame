import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class Window extends JFrame
{
    public static int windowWidth = 1920;
    public static int windowHeight = 1080;

    public static WindowPanel mainWindowPanel;
    public static Window mainWindow;
    
    public static void main(String[] args) throws Exception 
    {
        //main window
        mainWindowPanel = new WindowPanel(windowWidth, windowHeight);
        mainWindow = new Window(mainWindowPanel);
        
        //editor window
        var mainEditorPanel = new EditorPanel(Editor.windowWidth, Editor.windowHeight, mainWindowPanel);
        var mainEditor = new Editor(mainEditorPanel);

        //Threads
        //mainWindowPanel.StartThreads();
        mainWindowPanel.Update();
    }

    public Window(WindowPanel mainWindowPanel) 
    {
        setTitle("Window :)");
        setSize(windowWidth, windowHeight);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create control panel with checkboxes
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        controlPanel.setBackground(Color.LIGHT_GRAY);

        // Create checkboxes
        JCheckBox pauseCheckBox = new JCheckBox("Pause Game", mainWindowPanel.isPaused);
        JCheckBox showGridCheckBox = new JCheckBox("Show Grid", true);
        JCheckBox showDebugCheckBox = new JCheckBox("Show Debug Info", false);

        // Create clear button
        JButton clearButton = new JButton("Clear Balls");
        JButton debugSpawnButton = new JButton("Debug Spawn");

        // Create gravity input field
        JLabel gravityLabel = new JLabel("Gravity:");
        JTextField gravityField = new JTextField(String.valueOf(mainWindowPanel.gravity), 5);

        // Create substeps input field
        JLabel subStepsLabel = new JLabel("Substeps:");
        JTextField subStepsField = new JTextField(String.valueOf(mainWindowPanel.subSteps), 3);
        // Add action listeners
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


        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainWindowPanel.clearAllCircles();
                mainWindowPanel.cameraX = 0;
                mainWindowPanel.cameraY = 0;
                mainWindowPanel.cameraZoom = 1;
            }
        });

        debugSpawnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DebugSpawn();
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

        // Add checkboxes and button to control panel
        controlPanel.add(pauseCheckBox);
        controlPanel.add(showGridCheckBox);
        controlPanel.add(showDebugCheckBox);
        controlPanel.add(clearButton);
        controlPanel.add(debugSpawnButton);
        controlPanel.add(gravityLabel);
        controlPanel.add(gravityField);
        controlPanel.add(subStepsLabel);
        controlPanel.add(subStepsField);

        // Set layout and add panels
        setLayout(new BorderLayout());
        add(controlPanel, BorderLayout.NORTH);
        add(mainWindowPanel, BorderLayout.CENTER);

        setVisible(true);
        setBackground(Color.black);
        setLocationRelativeTo(null); // Center the window
        setExtendedState(JFrame.MAXIMIZED_BOTH); 
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
