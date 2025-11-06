/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author vitali.stoyanov
 */

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.nio.file.*;
import java.lang.reflect.Type;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

public class EditorPanel extends JPanel implements Runnable
{
    public int windowWidth = 1040;
    public int windowHeight = 580;

    private long lastFrameTime;
    private Double deltaTime;

    public static ArrayList<Poly> polyList = new ArrayList<Poly>();
    public boolean isEditing = false;
    private int curPoly = 0;
    private Link curLink = null;

    public static MouseEvent mouseEvent;

    private WindowPanel windowPanel;

    // Ball selection variable
    public BallType selectedBallType = BallType.SMALL_RED;

    //constructer
    public EditorPanel(int _windowWidth, int _windowHeight, WindowPanel _windowPanel)
    {
        windowPanel = _windowPanel;

        lastFrameTime = System.nanoTime();

        windowWidth = _windowWidth;
        windowHeight = _windowHeight;
        polyList.add(new Poly(null, Color.BLUE));

        StartMouseEvents();
        
        // Load "Bin Open.txt" by default
        loadDefaultFile();
    }

    //Paint
    @Override
    protected void paintComponent(Graphics g) 
    {
        super.paintComponent(g); // Call superclass method first

        Graphics2D g2d = (Graphics2D) g;

        //Enable antialiasing for smoother circles
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //color
        g.setColor(Color.BLACK);

        //draw polys
        for (Poly poly : polyList) 
        {
            //color
            g.setColor(poly.color);

            var _pointsList = poly.pointsList;
            int[] xPoints = new int[_pointsList.size()];
            int[] yPoints = new int[_pointsList.size()];

            for (int i = 0; i < _pointsList.size(); i++) 
            {
                xPoints[i] = (int)_pointsList.get(i).dx;
                yPoints[i] = (int)_pointsList.get(i).dy;
            }

            g.fillPolygon(xPoints, yPoints, xPoints.length);
        }

        repaint();
    }

    public void Update()
    {
        while (true) 
        { 
            //Delta Time
            deltaTime = GetDeltaTime();

        }
    }

    public double GetDeltaTime()
    {
        long currentTime = System.nanoTime();
        double deltaTimeSeconds = (currentTime - lastFrameTime) / 1_000_000_000.0; // Convert nanoseconds to seconds

        lastFrameTime = currentTime; // Update last frame time for the next iteration
        return deltaTimeSeconds;
    }

    //Threading
    public void StartThreads()
    {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() 
    {
        //Update();
    }

    void StartMouseEvents()
    {
        addMouseListener(new MouseAdapter() { 
            public void mousePressed(MouseEvent me) 
            { 
                //collect mouse data
                mouseEvent = me; 
                var curButton = me.getButton();

                //left click
                if(curButton == MouseEvent.BUTTON1)
                {
                    //add point to poly
                    polyList.get(curPoly).pointsList.add(new Vector2D(me.getX(), me.getY()));
                }
                else if (curButton == MouseEvent.BUTTON3)
                {
                    if(isEditing)
                    {
                        polyList.get(curPoly).color = Color.BLACK;
                        polyList.add(new Poly(null, Color.BLUE));
                        curPoly += 1;
                    }
                    else
                    {
                        //add balls using selected ball type
                        if(selectedBallType == BallType.XL_YELLOW)
                        {
                            Circle circle = new Circle(25, windowPanel.mouseX, windowPanel.mouseY, (int)(3.14*25*25), Color.YELLOW);
                            windowPanel.circlesList.add(circle);
                        }
                        else if(selectedBallType == BallType.LARGE_GREEN)
                        {
                            Circle circle = new Circle(15, windowPanel.mouseX, windowPanel.mouseY, (int)(3.14*15*15), Color.GREEN);
                            windowPanel.circlesList.add(circle);
                        }
                        else if(selectedBallType == BallType.MEDIUM_BLUE)
                        {
                            Circle circle = new Circle(10, windowPanel.mouseX, windowPanel.mouseY, (int)(3.14*10* 10), Color.BLUE);
                            windowPanel.circlesList.add(circle);
                        }
                        else if(selectedBallType == BallType.SMALL_RED)
                        {
                            Circle circle = new Circle(5, windowPanel.mouseX, windowPanel.mouseY, (int)(3.14*5*5), Color.RED);
                            windowPanel.circlesList.add(circle);
                        }
                        else if(selectedBallType == BallType.TINY_MAGENTA)
                        {
                            Circle circle = new Circle(3, windowPanel.mouseX, windowPanel.mouseY, (int)(3.14*3*3), Color.MAGENTA);
                            windowPanel.circlesList.add(circle);
                        }
                        //Links
                        else if(selectedBallType == BallType.MEDIUM_LINK)
                        {
                            Circle circle = new Circle(8, windowPanel.mouseX, windowPanel.mouseY, (int)(3.14*8*8), Color.BLUE);
                            windowPanel.circlesList.add(circle);

                            if(curLink == null)
                            {
                                curLink = new Link(circle, null, circle.radius *2 + 1);
                                windowPanel.linksList.add(curLink);
                                circle.isPinned = true;
                            }
                            else
                            {
                                curLink.circleB = circle;

                                curLink = new Link(circle, null, circle.radius *2 + 1);
                                windowPanel.linksList.add(curLink);

                                //curLink = null;
                            }
                        }
                    }
                }
                else if (curButton == MouseEvent.BUTTON2)
                {
                    //transfer polys
                    windowPanel.polygonList = polyList;
                }
            } 
        });
    }

    // Add these methods to support button functionality
    public void clearPolys() {
        polyList.clear();
        polyList.add(new Poly(null, Color.BLUE));
        curPoly = 0;
        repaint();
    }
    
    public void savePolys() 
    {
        try 
        {
            JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
            fileChooser.setDialogTitle("Save Polygons");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Text files", "txt"));
            fileChooser.setSelectedFile(new File("polygons.txt"));
            
            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                
                // Ensure the file has .txt extension
                if (!selectedFile.getName().toLowerCase().endsWith(".txt")) {
                    selectedFile = new File(selectedFile.getAbsolutePath() + ".txt");
                }
                
                try (PrintWriter writer = new PrintWriter(new FileWriter(selectedFile))) 
                {
                    // Write the number of polygons
                    writer.println(polyList.size());

                    for (Poly poly : polyList) 
                    {
                        // Write color (RGB values)
                        writer.println(poly.color.getRed() + "," + poly.color.getGreen() + "," + poly.color.getBlue());

                        // Write number of points in the polygon
                        writer.println(poly.pointsList.size());

                        // Write each polygon's points
                        for (Vector2D point : poly.pointsList) 
                        {
                            writer.println(point.dx + "," + point.dy);
                        }
                    }
                }
                
                System.out.println("Polygons saved to: " + selectedFile.getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Polygons saved successfully!", "Save Complete", JOptionPane.INFORMATION_MESSAGE);
            }
        } 
        catch (Exception e) 
        {
            System.err.println("Error saving polygons: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadPolys() 
    {
        try 
        {
            JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
            fileChooser.setDialogTitle("Load Polygons");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Text files", "txt"));
            
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) //BOOBIES
            {
                File selectedFile = fileChooser.getSelectedFile();
                loadPolysFromFile(selectedFile);
                
                System.out.println("Polygons loaded from: " + selectedFile.getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Polygons loaded successfully!", "Load Complete", JOptionPane.INFORMATION_MESSAGE);
            }
        } 
        catch (Exception e) 
        {
            System.err.println("Error loading polygons: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error loading file: " + e.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void changeCurrentColor() 
    {
        Color newColor = JColorChooser.showDialog(this, "Choose Color", Color.BLUE);
        if (newColor != null && curPoly < polyList.size()) {
            polyList.get(curPoly).color = newColor;
            repaint();
        }
    }
    
    private void loadDefaultFile() 
    {
        try {
            File defaultFile = new File("Bin Open.txt");
            if (defaultFile.exists()) {
                loadPolysFromFile(defaultFile);
                System.out.println("Default file 'Bin Open.txt' loaded successfully.");
            } else {
                System.out.println("Default file 'Bin Open.txt' not found.");
            }
        } catch (Exception e) {
            System.err.println("Error loading default file: " + e.getMessage());
        }
    }
    
    private void loadPolysFromFile(File file) throws Exception 
    {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Clear current polygons
            polyList.clear();
            
            // Read number of polygons
            int numPolys = Integer.parseInt(reader.readLine());
            
            // Read each polygon
            for (int p = 0; p < numPolys; p++) {
                // Read color
                String[] colorParts = reader.readLine().split(",");
                Color color = new Color(
                    Integer.parseInt(colorParts[0]),
                    Integer.parseInt(colorParts[1]),
                    Integer.parseInt(colorParts[2])
                );
                
                // Create new polygon
                Poly newPoly = new Poly(null, color);
                
                // Read number of points
                int numPoints = Integer.parseInt(reader.readLine());
                
                // Read each point
                for (int i = 0; i < numPoints; i++) {
                    String[] pointParts = reader.readLine().split(",");
                    double x = Double.parseDouble(pointParts[0]);
                    double y = Double.parseDouble(pointParts[1]);
                    newPoly.pointsList.add(new Vector2D(x, y));
                }
                
                polyList.add(newPoly);
            }
            
            // Set current polygon index
            curPoly = Math.max(0, polyList.size() - 1);
            
            // Add a new polygon for continued drawing
            if (polyList.isEmpty()) {
                polyList.add(new Poly(null, Color.BLUE));
                curPoly = 0;
            }
            
            // Refresh the display
            repaint();
        }
    }
}

