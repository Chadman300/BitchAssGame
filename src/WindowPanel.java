import com.sun.security.auth.module.NTLoginModule;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.*;
import javax.swing.*;

public class WindowPanel extends JPanel implements Runnable
{
    public int windowWidth = 1040;
    public int windowHeight = 580;

    public static ArrayList<Poly> polygonList = new ArrayList<>();
    private long lastFrameTime;
    private Double deltaTime;

    //camera settings
    public double cameraZoom = 1.0;
    public double cameraZoomSpeed = 0.9;
    public double cameraSpeed = 200.0; // pixels per second
    public double cameraX = 0;
    public double cameraY = 0;

    //player
    public Player playerOne;
    public Player playerTwo;

    //physics var
    public boolean isPaused = true;
    public double gravity = 1.0 * 10000000;
    public int subSteps = 8; // Reduced from 50 to 8 for better performance
    public int gridSize = 20; // for hasing optimization
    
    // Checkbox control variables
    public boolean showGrid = false;
    public boolean showDebugInfo = false;
    
    // Mouse force application variables
    private boolean isLeftMousePressed = false;
    public  int mouseX = 0;
    public int mouseY = 0;
    
    // WASD key input variables
    private boolean wPressed = false;
    private boolean aPressed = false;
    private boolean sPressed = false;
    private boolean dPressed = false;

    // Arrow key input variables
    private boolean upPressed = false;
    private boolean leftPressed = false;
    private boolean downPressed = false;
    private boolean rightPressed = false;
    
    // FPS tracking variables
    private long lastFpsTime = System.nanoTime();
    private int frameCount = 0;
    private double currentFps = 0.0;
    private final long FPS_UPDATE_INTERVAL = 500_000_000; // Update FPS display every 0.5 seconds

    public static MouseEvent mouseEvent;
    Graphics graphicsComponent;

    public static ArrayList<Circle> circlesList = new ArrayList<>();
    public static ArrayList<Link> linksList = new ArrayList<>();
    
    // Add timer for rendering
    private javax.swing.Timer renderTimer;

    //constructer
    public WindowPanel(int _windowWidth, int _windowHeight)
    {
        lastFrameTime = System.nanoTime();

        windowWidth = _windowWidth;
        windowHeight = _windowHeight;

        StartMouseEvents();
        StartKeyEvents();

        //poly list
        polygonList.add(new Poly(null, Color.BLUE));
        
        // Initialize render timer for 60 FPS
        renderTimer = new javax.swing.Timer(16, e -> repaint()); // 16ms ≈ 60 FPS
        renderTimer.start();
        
        SpawnPlayer();
    }

    //Paint
    @Override
    protected void paintComponent(Graphics g) 
    {
        super.paintComponent(g); // Call superclass method first

        // Update FPS calculation
        updateFPS();

        graphicsComponent = g;

        Graphics2D g2d = (Graphics2D) g;

        //Enable antialiasing for smoother circles
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // DEBUG Grid - only show if checkbox is checked
        if (showGrid) {
            drawGrid(g);
        }


        for (int i = 0; i < circlesList.size(); i++) 
        { 
            Circle circle = circlesList.get(i);
            var radius = circle.radius * (cameraZoom * 1);

            // Draw circle with camera offset
            int drawX = (int)((circle.currentPos.dx + cameraX) * cameraZoom);
            int drawY = (int)((circle.currentPos.dy + cameraY) * cameraZoom);
            circle.draw(graphicsComponent, drawX, drawY, cameraZoom);

            //debug
            if (showDebugInfo) {
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
        for (Poly poly : polygonList) 
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
            if (showDebugInfo) 
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

        //DEBUG Circle bounds
        int boundsRadius = 200;
        g.setColor(Color.BLACK);
        //g.drawOval(windowWidth / 2 - boundsRadius, windowHeight/2 - boundsRadius, boundsRadius * 2, boundsRadius * 2);
        //g.drawLine(windowWidth/2 - boundsRadius, windowHeight/2 - boundsRadius/3, windowWidth/2 + boundsRadius *2, windowHeight/2 - boundsRadius/3);
        
        // Display Mouse Pos
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 16));

        if(mouseEvent != null)
        {
            g.drawString("MouseX: " + (int)(mouseEvent.getX() - cameraX), 10, 65);
            g.drawString("MouseY: " + (int)(mouseEvent.getY() - cameraY), 10, 85);
        } 

        // Display FPS
        g.drawString("FPS: " + String.format("%.1f", currentFps), 10, 25);
        g.drawString("Circles: " + circlesList.size(), 10, 45);

        //display camera pos
        g.drawString("CameraX: " + String.format("%.1f", cameraX), 10, 105);
        g.drawString("CameraY: " + String.format("%.1f", cameraY), 10, 125);
        g.drawString("Camera Zoom: " + String.format("%.01f", cameraZoom), 10, 145);
        
        // Display key states for debugging
        g.drawString("Keys 1: W:" + wPressed + " A:" + aPressed + " S:" + sPressed + " D:" + dPressed, 10, 165);
        g.drawString("Keys 2: Up:" + upPressed + " Left:" + leftPressed + " Down:" + downPressed + " Right:" + rightPressed, 10, 185);
        g.drawString("Focus: " + hasFocus(), 10, 205);
    }

    private void updateFPS() {
        frameCount++;
        long currentTime = System.nanoTime();
        long timeDiff = currentTime - lastFpsTime;
        
        // Update FPS display every 0.5 seconds
        if (timeDiff >= FPS_UPDATE_INTERVAL) {
            currentFps = frameCount / (timeDiff / 1_000_000_000.0);
            frameCount = 0;
            lastFpsTime = currentTime;
        }
    }

    public void SpawnPlayer()
    {
        if(playerOne != null)
        {
            //remove old player circles
            circlesList.remove(playerOne.groundCircle);
            circlesList.remove(playerOne.playerCircle);
        }
        if(playerTwo != null)
        {
            //remove old player circles
            circlesList.remove(playerTwo.groundCircle);
            circlesList.remove(playerTwo.playerCircle);
        }

        // Initialize player One
        var playerCircle = new Circle(10, windowHeight / 2, windowWidth/2, 1, Color.ORANGE);
        var groundCircle = new Circle(8, windowHeight / 2, windowWidth/2, 1, Color.GREEN, false, false);

        playerCircle.layer = Layer.PLAYER;
        playerCircle.restitutionCircle = 0.1; // Less bouncy for player
        playerCircle.restitutionPoly  = 0.1; // Less bouncy for player

        playerOne = new Player(playerCircle, groundCircle, PlayerType.PLAYER_ONE);
        circlesList.add(playerOne.groundCircle);
        circlesList.add(playerOne.playerCircle);

        //Init Player two
        var playerCircleTwo = new Circle(10, windowHeight / 2 + 20, windowWidth/2, 1, Color.CYAN);
        var groundCircleTwo = new Circle(8, windowHeight / 2 + 20, windowWidth/2, 1, Color.GREEN, false, false);

        playerCircleTwo.layer = Layer.PLAYER;
        playerCircleTwo.restitutionCircle = 0.1; // Less bouncy for player
        playerCircleTwo.restitutionPoly  = 0.1; // Less bouncy for player

        playerTwo = new Player(playerCircleTwo, groundCircleTwo, PlayerType.PLAYER_TWO);
        circlesList.add(playerTwo.groundCircle);
        circlesList.add(playerTwo.playerCircle);

        //Link between players
        int numLinks = 8;
        int linkLength = 15;
        int linkRadius = 5;
        float linkMass = 0.01f;
        Link curLink = null;
        for(int l = 0; l < numLinks; l++)
        {
            Circle linkCircle = 
            new Circle(
                linkRadius, 
                windowHeight / 2 + (l * linkLength), 
                windowWidth/2, 
                linkMass, 
                Color.LIGHT_GRAY);

            linkCircle.layer = Layer.LINK;
            linkCircle.isPhysical = false; //non physical link circles
            
            circlesList.add(linkCircle);

            if(l == 0)
            {
                curLink = new Link(playerOne.playerCircle, linkCircle, linkLength);
                linksList.add(curLink);
                curLink = new Link(curLink.circleB, null, linkLength);
            }  
            else
            {
                curLink.circleB = linkCircle;
                linksList.add(curLink);
                if(l == numLinks -1)
                {
                    //last link to player two
                    curLink = new Link(curLink.circleB, playerTwo.playerCircle, linkLength); 
                }
                else
                {
                    curLink = new Link(curLink.circleB, null, linkLength); 
                }
                linksList.add(curLink);
            }
        }
    }

    private void drawGrid(Graphics g) 
    {
        
        // Set grid color to a subtle gray
        g.setColor(new Color(64, 64, 64, 128)); // Semi-transparent dark gray
        
        // Draw vertical lines
        for (int x = 0; x <= windowWidth; x += gridSize) {
            g.drawLine((int)((x + cameraX) * cameraZoom), 0, (int)((x + cameraX) * cameraZoom), windowHeight);
        }
        
        // Draw horizontal lines
        for (int y = 0; y <= windowHeight; y += gridSize) {
            g.drawLine(0, (int)((y + cameraY) * cameraZoom), windowWidth, (int)((y + cameraY) * cameraZoom));
        }
        
        // Optionally, draw grid cell indices for debugging
        g.setColor(new Color(128, 128, 128, 200)); // Lighter gray for text
        g.setFont(new Font("Arial", Font.PLAIN, (int)(gridSize / 2 * cameraZoom)));
        
        for (int x = 0; x < windowWidth; x += gridSize) {
            for (int y = 0; y < windowHeight; y += gridSize) {
                int gridX = (int)((x + cameraX) / gridSize);
                int gridY = (int)((y + cameraY) / gridSize);
                g.drawString(gridX + "," + gridY, (int)((x + cameraX) * cameraZoom) + 5, (int)((y + cameraY) * cameraZoom) + 15);
            }
        }
    }

    public void Update()
    {
        // Target 60 FPS
        final long TARGET_FPS = 60;
        final long TARGET_TIME = 1_000_000_000 / TARGET_FPS; // nanoseconds per frame
        
        while (true) 
        { 
            long frameStart = System.nanoTime();
            
            //requestFocusInWindow();

            //Delta Time
            deltaTime = GetDeltaTime();
            var _subSteps = subSteps;

            // Handle WASD camera movement (works even when paused)
            //if (wPressed) cameraY += cameraSpeed * deltaTime;
            //if (sPressed) cameraY -= cameraSpeed * deltaTime;

            //dont update if paused
            if(isPaused)
            {
                // Sleep to prevent busy waiting when paused
                try {
                    Thread.sleep(16); // ~60 FPS
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                continue;
            }

            // Apply substeps to the entire physics simulation
            double subDeltaTime = deltaTime / _subSteps;
            
            for(int step = 0; step < _subSteps; step++)
            {
                playerOne.PlayerUpdate(subDeltaTime);
                playerTwo.PlayerUpdate(subDeltaTime);

                if (aPressed) playerOne.Move(subDeltaTime, -1);
                if (dPressed) playerOne.Move(subDeltaTime, 1);

                if (leftPressed) playerTwo.Move(subDeltaTime, -1);
                if (rightPressed) playerTwo.Move(subDeltaTime, 1);

                //move camera to player
                cameraX = -((playerOne.playerCircle.currentPos.dx + playerTwo.playerCircle.currentPos.dx) / 2 - (windowWidth / 2)) / cameraZoom;
                cameraY = -((playerOne.playerCircle.currentPos.dy + playerTwo.playerCircle.currentPos.dy) / 2 - (windowHeight / 2)) / cameraZoom;

                //Hash out circles into a grid for each substep
                HashMap<String, ArrayList<Circle>> grid = new HashMap<>();

                //setup grid
                for (int c = 0; c < circlesList.size(); c++) 
                {
                    Circle circle = circlesList.get(c);
                    int gridX = (int)(circle.currentPos.dx / gridSize);
                    int gridY = (int)(circle.currentPos.dy / gridSize);
                    String key = gridX + "," + gridY;

                    grid.putIfAbsent(key, new ArrayList<>());
                    grid.get(key).add(circle);
                }

                // Process collisions with neighboring cells
                for (String key : grid.keySet()) 
                {
                    ArrayList<Circle> cellCircles = grid.get(key);
                    String[] coords = key.split(",");
                    int gridX = Integer.parseInt(coords[0]);
                    int gridY = Integer.parseInt(coords[1]);
                    
                    for (int i = 0; i < cellCircles.size(); i++) 
                    {
                        Circle c1 = cellCircles.get(i);
                        c1.isCollidingCircle = false;

                        // Check collisions with circles in same cell
                        for (int j = i + 1; j < cellCircles.size(); j++) 
                        {
                            Circle c2 = cellCircles.get(j);
                            c1.CircleCollisions(c2, subDeltaTime);
                        }
                        
                        // Check collisions with neighboring cells
                        
                        for (int dx = -1; dx <= 1; dx++) 
                        {
                            for (int dy = -1; dy <= 1; dy++) 
                            {
                                if (dx == 0 && dy == 0) continue; // Skip same cell
                                
                                String neighborKey = (gridX + dx) + "," + (gridY + dy);
                                if (grid.containsKey(neighborKey)) 
                                {
                                    ArrayList<Circle> neighborCircles = grid.get(neighborKey);
                                    for (Circle c2 : neighborCircles) 
                                    {
                                        c1.CircleCollisions(c2, subDeltaTime);
                                    }
                                }
                            }
                        }
                    
                    
                        // Apply boundary collisions
                        c1.isCollidingPoly = false;
                        for (int p = 0; p < polygonList.size(); p++)
                        {
                            Poly poly = polygonList.get(p);
                            c1.PolygonCollisions(poly, subDeltaTime);
                        }
                    }
                }
            }

            // Apply physics to all circles
            for (int c = 0; c < circlesList.size(); c++) 
            {
                Circle circle = circlesList.get(c);
                
                //Apply gravity
                circle.ApplyGravity(gravity, subDeltaTime);
                
                //Apply integration
                circle.VerletIntegration(subDeltaTime);
            }

            //Apply forces to links
            for (int l = 0; l < linksList.size(); l++) 
            {
                Link link = linksList.get(l);
                link.ApplyForces(subDeltaTime);
            }
            
            // Apply mouse force if left button is held down
            if (isLeftMousePressed) {
                double radius = 100.0; // radius in pixels
                double forceMagnitude = 100000.0; // adjust as needed
                for (int i = 0; i < circlesList.size(); i++) 
                {
                    Circle circle = circlesList.get(i);
                    double dx = circle.currentPos.dx - mouseX;
                    double dy = circle.currentPos.dy - mouseY;
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    if (dist <= radius) {
                        // Direction away from mouse
                        double fx = dx / (dist + 1e-6); // avoid div by zero
                        double fy = dy / (dist + 1e-6);
                        // Apply force
                        circle.ApplyForce(forceMagnitude * subDeltaTime, new Vector2D(fx, fy));
                    }
                }
            }
            
            // Frame rate limiting for physics (not rendering)
            long frameEnd = System.nanoTime();
            long frameTime = frameEnd - frameStart;
            long sleepTime = TARGET_TIME - frameTime;
            
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime / 1_000_000, (int)(sleepTime % 1_000_000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    public void CircleCollision(Circle circle, double _deltaTime)
    {
        //Collisions circles
        //circle.KeepInBoundsCircle(windowHeight, windowWidth, deltaTime);

        for (int oc = 0; oc < circlesList.size(); oc++)
        {
            //get other circle ref
            Circle otherCircle = circlesList.get(oc);

            if(otherCircle != circle)
            {
                circle.CircleCollisions(otherCircle, _deltaTime);
            }
        }

        //collisions polys
        for (int p = 0; p < polygonList.size(); p++)
        {
            Poly poly = polygonList.get(p);
            circle.PolygonCollisions(poly, _deltaTime);
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

    public void clearAllCircles() {
        circlesList.clear();
    }

    void StartMouseEvents()
    {
        addMouseListener(new MouseAdapter() 
        { 
            @Override
            public void mousePressed(MouseEvent me) 
            { 
                // Request focus when clicked so key events work
                requestFocusInWindow();
                
                //collect mouse data
                mouseEvent = me; 
                var curButton = me.getButton();
                mouseX = (int)((me.getX() / cameraZoom) - cameraX);
                mouseY = (int)((me.getY() / cameraZoom) - cameraY);

                //left click
                if(curButton == MouseEvent.BUTTON1)
                {
                    isLeftMousePressed = true;
                }

                //middle click
                else if(curButton == MouseEvent.BUTTON2)
                {
                
                }
                //right click
                else if(curButton == MouseEvent.BUTTON3)
                {
                    
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent me) 
            {
                if(me.getButton() == MouseEvent.BUTTON1)
                {
                    isLeftMousePressed = false;
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
                //requestFocusInWindow();

                int keyCode = e.getKeyCode();
                System.out.println("Key pressed: " + KeyEvent.getKeyText(keyCode)); // Debug output
                switch (keyCode) {
                    case KeyEvent.VK_W -> {
                        wPressed = true;
                        playerOne.Jump(deltaTime);
                    }
                    case KeyEvent.VK_A -> aPressed = true;
                    case KeyEvent.VK_S -> sPressed = true;
                    case KeyEvent.VK_D -> dPressed = true;
                    case KeyEvent.VK_SHIFT -> cameraSpeed = 1000.0;
                    case KeyEvent.VK_SPACE -> {
                    }
                    case KeyEvent.VK_UP -> {
                        upPressed = true;
                        playerTwo.Jump(deltaTime);
                    }
                    case KeyEvent.VK_LEFT -> leftPressed = true;
                    case KeyEvent.VK_DOWN -> downPressed = true;
                    case KeyEvent.VK_RIGHT -> rightPressed = true;
                    default -> {
                    }
                }
                //playerOne.Jump(deltaTime);
                            }
            
            @Override
            public void keyReleased(KeyEvent e) 
            {
                //requestFocusInWindow();

                int keyCode = e.getKeyCode();
                System.out.println("Key released: " + KeyEvent.getKeyText(keyCode)); // Debug output
                switch (keyCode) {
                    case KeyEvent.VK_W -> wPressed = false;
                    case KeyEvent.VK_A -> aPressed = false;
                    case KeyEvent.VK_S -> sPressed = false;
                    case KeyEvent.VK_D -> dPressed = false;
                    case KeyEvent.VK_SHIFT -> cameraSpeed = 200.0;
                    case KeyEvent.VK_UP -> upPressed = false;
                    case KeyEvent.VK_LEFT -> leftPressed = false;
                    case KeyEvent.VK_DOWN -> downPressed = false;
                    case KeyEvent.VK_RIGHT -> rightPressed = false;
                    default -> {
                    }
                }
            }
        });
    }
}
