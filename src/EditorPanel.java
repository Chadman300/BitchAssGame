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
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.*;
import javax.swing.*;

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

    //camera settings
    public double cameraZoom = 1.0;
    public double cameraZoomSpeed = 0.9;
    public double cameraSpeed = 200.0; // pixels per second
    public double cameraX = 0;
    public double cameraY = 0;

    // Mouse force application variables
    private boolean isLeftMousePressed = false;
    public  int mouseX = 0;
    public int mouseY = 0;
    
    // WASD key input variables
    private boolean wPressed = false;
    private boolean aPressed = false;
    private boolean sPressed = false;
    private boolean dPressed = false;

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
        StartKeyEvents();

        
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
        for (int i = 0; i < windowPanel.circlesList.size(); i++) 
        { 
            Circle circle = windowPanel.circlesList.get(i);
            var radius = circle.radius * (cameraZoom * 1);

            // Draw circle with camera offset
            int drawX = (int)((circle.currentPos.dx + cameraX) * cameraZoom);
            int drawY = (int)((circle.currentPos.dy + cameraY) * cameraZoom);
            circle.draw(g, drawX, drawY, cameraZoom);

            //debug
            if (windowPanel.showDebugInfo) {
                g.setColor(Color.RED);
                var velNormal = Vector2D.VectorNormalize(circle.vel);
                g.drawLine(
                    drawX, 
                    drawY, 
                    (int)(drawX + (velNormal.dx * 10)), 
                    (int)(drawY + (velNormal.dy * 10)));
            }
        }

        //draw polys
        for (Poly poly : polyList) 
        {
            //color
            g.setColor(poly.color);

            boolean isEditingPoly = (poly.color == Color.BLUE);
            
            var _pointsList = poly.pointsList;
            int[] xPoints = new int[_pointsList.size()];
            int[] yPoints = new int[_pointsList.size()];

            for (int i = 0; i < _pointsList.size(); i++) 
            {
                xPoints[i] = (int)((_pointsList.get(i).dx + (int)cameraX) * cameraZoom);
                yPoints[i] = (int)((_pointsList.get(i).dy + (int)cameraY) * cameraZoom);
            }

            g.fillPolygon(xPoints, yPoints, xPoints.length);

            //Draw debug editor point when drawing
            if(isEditingPoly)
            {
                for (int i = 0; i < _pointsList.size(); i++) 
                {
                    g.setColor(Color.RED);
                    g.fillOval(
                        (int)(((_pointsList.get(i).dx + (int)cameraX) * cameraZoom) - 5),    
                        (int)(((_pointsList.get(i).dy + (int)cameraY) * cameraZoom) - 5), 
                        10, 
                        10);
                }
            }

            //debug
            if (windowPanel.showDebugInfo) 
            {
                for (int i = 0; i < poly.pointsList.size(); i++)
                {
                    //get line points
                    Vector2D lp1 = poly.pointsList.get(i);
                    Vector2D lp2 = null;

                    if(i + 1 < poly.pointsList.size())
                    {
                        lp2 = poly.pointsList.get(i + 1);      
                    }
                    else
                    {
                        lp2 = poly.pointsList.get(0);    
                    }

                    double A = lp1.dy - lp2.dy;
                    double B = lp2.dx - lp1.dx;

                    var perpLine = new Vector2D(-A, -B);
                    var normal = Vector2D.VectorNormalize(perpLine);

                    g.setColor(Color.RED);
                    g.drawLine(
                    (int)((normal.dx + (lp1.dx + lp2.dx)/2 + (int)cameraX) * cameraZoom), 
                    (int)((normal.dy + (lp1.dy + lp2.dy)/2 + (int)cameraY) * cameraZoom), 
                    (int)((normal.dx * 10 + (lp1.dx + lp2.dx)/2 + (int)cameraX) * cameraZoom), 
                    (int)((normal.dy * 10 + (lp1.dy + lp2.dy)/2 + (int)cameraY) * cameraZoom));           
                }
            }
        }

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 16));

        // Display FPS
        g.drawString("Circles: " + windowPanel.circlesList.size(), 10, 45);

        //display camera pos
        g.drawString("CameraX: " + String.format("%.1f", cameraX), 10, 105);
        g.drawString("CameraY: " + String.format("%.1f", cameraY), 10, 125);
        g.drawString("Camera Zoom: " + String.format("%.01f", cameraZoom), 10, 145);
        
        // Display key states for debugging
        g.drawString("Focus: " + hasFocus(), 10, 185);

        repaint();
    }

    public void Update()
    {
        while (true) 
        { 
            //Delta Time
            deltaTime = GetDeltaTime();

            // Handle WASD camera movement (works even when paused)
            if (wPressed) cameraY += cameraSpeed * deltaTime;
            if (sPressed) cameraY -= cameraSpeed * deltaTime;
            if (aPressed) cameraX += cameraSpeed * deltaTime;
            if (dPressed) cameraX -= cameraSpeed * deltaTime;
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
        Update();
    }

    void StartMouseEvents()
    {
        addMouseListener(new MouseAdapter() { 
            public void mousePressed(MouseEvent me) 
            { 
                //collect mouse data
                mouseEvent = me; 
                var curButton = me.getButton();

                requestFocus();

                mouseX = (int)((me.getX() / cameraZoom) - cameraX);
                mouseY = (int)((me.getY() / cameraZoom) - cameraY);

                //left click
                switch (curButton) 
                {

                    case MouseEvent.BUTTON1:

                        if(isEditing)
                        {
                            //add point to poly
                            polyList.get(curPoly).pointsList.add(new Vector2D(mouseX, mouseY));
                        }
                        else
                        {
                            //add balls using selected ball type
                            if(selectedBallType == BallType.XL_YELLOW)
                            {
                                Circle circle = new Circle(25, mouseX, mouseY, (int)(3.14*25*25), Color.YELLOW);
                                windowPanel.circlesList.add(circle);
                            }
                            else if(selectedBallType == BallType.LARGE_GREEN)
                            {
                                Circle circle = new Circle(15, mouseX, mouseY, (int)(3.14*15*15), Color.GREEN);
                                windowPanel.circlesList.add(circle);
                            }
                            else if(selectedBallType == BallType.MEDIUM_BLUE)
                            {
                                Circle circle = new Circle(10, mouseX, mouseY, (int)(3.14*10* 10), Color.BLUE);
                                windowPanel.circlesList.add(circle);
                            }
                            else if(selectedBallType == BallType.SMALL_RED)
                            {
                                Circle circle = new Circle(5, mouseX, mouseY, (int)(3.14*5*5), Color.RED);
                                windowPanel.circlesList.add(circle);
                            }
                            else if(selectedBallType == BallType.TINY_MAGENTA)
                            {
                                Circle circle = new Circle(3, mouseX, mouseY, (int)(3.14*3*3), Color.MAGENTA);
                                windowPanel.circlesList.add(circle);
                            }
                            //Links
                            else if(selectedBallType == BallType.MEDIUM_LINK)
                            {
                                Circle circle = new Circle(8, mouseX, mouseY, (int)(3.14*8*8), Color.BLUE);
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
                        break;

                    case MouseEvent.BUTTON3:

                        if(isEditing)
                        {
                            polyList.get(curPoly).color = Color.BLACK;
                            polyList.add(new Poly(null, Color.BLUE));
                            curPoly += 1;
                        }   
                        else if(selectedBallType == BallType.MEDIUM_LINK)
                        {  
                            if(curLink != null)
                            {       
                                Circle circle = new Circle(8, mouseX, mouseY, (int)(3.14*8*8), Color.BLUE);
                                windowPanel.circlesList.add(circle);

                                circle.isPinned = true;

                                curLink.circleB = circle;
                                curLink = new Link(circle, null, circle.radius *2 + 1);
                                windowPanel.linksList.add(curLink);
                                
                                //curLink = null;                       
                            }
                        }
                        break;

                    case MouseEvent.BUTTON2:
                        //transfer polys
                        if(isEditing)
                            windowPanel.polygonList = polyList;
                        else
                        {
                            //remove ball
                            Circle circleToRemove = null;
                            for (Circle circle : windowPanel.circlesList) 
                            {
                                double dist = Math.sqrt(Math.pow(circle.currentPos.dx - mouseX, 2) + Math.pow(circle.currentPos.dy - mouseY, 2));
                                if (dist <= circle.radius) 
                                {
                                    circleToRemove = circle;
                                    break;
                                }
                            }
                            if (circleToRemove != null) 
                            {
                                windowPanel.circlesList.remove(circleToRemove);
                            }
                        }
                        break;

                    default:
                        break;
                }
            } 
        });

        addMouseMotionListener(new MouseAdapter() 
        {
            @Override
            public void mouseDragged(MouseEvent me) 
            {
                mouseEvent = me;
                if(isLeftMousePressed)
                {
                    mouseX = (int)((me.getX() / cameraZoom) - cameraX);
                    mouseY = (int)((me.getY() / cameraZoom) - cameraY);
                }
            }
            
            @Override
            public void mouseMoved(MouseEvent me) 
            {
                mouseEvent = me;
            }
        });

        // Add mouse wheel listener for zooming
        addMouseWheelListener(new MouseWheelListener() 
        {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e)
            {          
                // Get the rotation amount (negative = scroll up, positive = scroll down)
                int rotation = e.getWheelRotation();
                
                // Option 1: Use for camera zoom
                double oldZoom = cameraZoom;
                if (rotation < 0) {
                    // Scroll up - zoom in
                    cameraZoom = cameraZoom / cameraZoomSpeed;
                } else {
                    // Scroll down - zoom out
                    cameraZoom = cameraZoom * cameraZoomSpeed;
                }
                
                // Adjust camera position to zoom towards mouse cursor
                 /* 
                if (cameraZoom != oldZoom) {
                    double zoomFactor = cameraZoom / oldZoom;
                    cameraX = mouseX - (e.getX() - cameraX) * zoomFactor;
                    cameraY = mouseY - (e.getY() - cameraY) * zoomFactor;
                }
                    */
                     
            }
        });
    }

    void StartKeyEvents()
    {
        // Make the panel focusable to receive key events
        setFocusable(true);
        requestFocusInWindow();
        
        addKeyListener(new KeyAdapter() 
        {
            @Override
            public void keyPressed(KeyEvent e) 
            {
                requestFocusInWindow();

                int keyCode = e.getKeyCode();
                System.out.println("Key pressed: " + KeyEvent.getKeyText(keyCode)); // Debug output
                switch (keyCode) {
                    case KeyEvent.VK_W:
                        wPressed = true;
                        break;
                    case KeyEvent.VK_A:
                        aPressed = true;
                        break;
                    case KeyEvent.VK_S:
                        sPressed = true;
                        break;
                    case KeyEvent.VK_D:
                        dPressed = true;
                        break;
                    case KeyEvent.VK_SHIFT:
                        cameraSpeed = 1000.0;
                        break;
                    case KeyEvent.VK_ESCAPE:
                        // Deselect any selected ball type
                        selectedBallType = null;
                        break;
                    case KeyEvent.VK_E:
                        isEditing = !isEditing;
                        System.out.println("Toggled editing mode: " + isEditing);
                        break;
                }
            }
            
            @Override
            public void keyReleased(KeyEvent e) 
            {
                requestFocusInWindow();

                int keyCode = e.getKeyCode();
                System.out.println("Key released: " + KeyEvent.getKeyText(keyCode)); // Debug output
                switch (keyCode) {
                    case KeyEvent.VK_W:
                        wPressed = false;
                        break;
                    case KeyEvent.VK_A:
                        aPressed = false;
                        break;
                    case KeyEvent.VK_S:
                        sPressed = false;
                        break;
                    case KeyEvent.VK_D:
                        dPressed = false;
                        break;
                    case KeyEvent.VK_SHIFT:
                        cameraSpeed = 200.0;
                        break;
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
            fileChooser.setDialogTitle("Save Polygons and Balls");
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
                    
                    // Write the number of circles (balls)
                    writer.println(WindowPanel.circlesList.size());
                    
                    for (Circle circle : WindowPanel.circlesList) 
                    {
                        if(circle.layer == Layer.PLAYER)
                            continue;

                        // Write circle properties: radius, x, y, mass, color (RGB), isPinned
                        writer.println(circle.radius + "," + 
                                     circle.currentPos.dx + "," + 
                                     circle.currentPos.dy + "," + 
                                     circle.mass + "," + 
                                     circle.color.getRed() + "," + 
                                     circle.color.getGreen() + "," + 
                                     circle.color.getBlue() + "," + 
                                     circle.isPinned);
                    }
                    
                    // Write the number of links
                    writer.println(WindowPanel.linksList.size());
                    
                    for (Link link : WindowPanel.linksList) 
                    {
                        // Write link properties: indices of circleA and circleB, restLength
                        int indexA = WindowPanel.circlesList.indexOf(link.circleA);
                        int indexB = link.circleB != null ? WindowPanel.circlesList.indexOf(link.circleB) : -1;
                        writer.println(indexA + "," + indexB + "," + link.getRestLength());
                    }
                }
                
                System.out.println("Polygons and balls saved to: " + selectedFile.getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Polygons and balls saved successfully!", "Save Complete", JOptionPane.INFORMATION_MESSAGE);
            }
        } 
        catch (Exception e) 
        {
            System.err.println("Error saving polygons and balls: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadPolys() 
    {
        try 
        {
            JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
            fileChooser.setDialogTitle("Load Polygons and Balls");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Text files", "txt"));
            
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION)
            {
                File selectedFile = fileChooser.getSelectedFile();
                loadPolysFromFile(selectedFile);
                
                System.out.println("Polygons and balls loaded from: " + selectedFile.getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Polygons and balls loaded successfully!", "Load Complete", JOptionPane.INFORMATION_MESSAGE);
            }
        } 
        catch (Exception e) 
        {
            System.err.println("Error loading polygons and balls: " + e.getMessage());
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
            // Clear current polygons and circles
            polyList.clear();
            WindowPanel.circlesList.clear();
            WindowPanel.linksList.clear();
            
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
            
            // Read circles if available
            String circlesLine = reader.readLine();
            if (circlesLine != null && !circlesLine.isEmpty()) {
                int numCircles = Integer.parseInt(circlesLine);
                
                for (int c = 0; c < numCircles; c++) {
                    String[] circleParts = reader.readLine().split(",");
                    int radius = (int)Double.parseDouble(circleParts[0]);
                    int x = (int)Double.parseDouble(circleParts[1]);
                    int y = (int)Double.parseDouble(circleParts[2]);
                    float mass = Float.parseFloat(circleParts[3]);
                    Color color = new Color(
                        Integer.parseInt(circleParts[4]),
                        Integer.parseInt(circleParts[5]),
                        Integer.parseInt(circleParts[6])
                    );
                    boolean isPinned = Boolean.parseBoolean(circleParts[7]);
                    
                    Circle circle = new Circle(radius, x, y, mass, color);
                    circle.isPinned = isPinned;
                    WindowPanel.circlesList.add(circle);
                }
                
                // Read links if available
                String linksLine = reader.readLine();
                if (linksLine != null && !linksLine.isEmpty()) {
                    int numLinks = Integer.parseInt(linksLine);
                    
                    for (int l = 0; l < numLinks; l++) {
                        String[] linkParts = reader.readLine().split(",");
                        int indexA = Integer.parseInt(linkParts[0]);
                        int indexB = Integer.parseInt(linkParts[1]);
                        double restLength = Double.parseDouble(linkParts[2]);
                        
                        Circle circleA = WindowPanel.circlesList.get(indexA);
                        Circle circleB = indexB >= 0 ? WindowPanel.circlesList.get(indexB) : null;
                        
                        Link link = new Link(circleA, circleB, restLength);
                        WindowPanel.linksList.add(link);
                    }
                }
            }
            
            // Set current polygon index
            curPoly = Math.max(0, polyList.size() - 1);
            
            // Add a new polygon for continued drawing
            if (polyList.isEmpty()) {
                polyList.add(new Poly(null, Color.BLUE));
                curPoly = 0;
            }
            
            //respawn player
            windowPanel.SpawnPlayer();

            // Refresh the display
            repaint();
        }
    }
}

