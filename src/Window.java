import java.awt.*;
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
        var mainEditor = new Editor(mainEditorPanel, mainWindowPanel);

        //Threads
        mainWindowPanel.StartThreads();
        mainEditorPanel.StartThreads();
    }

    public Window(WindowPanel mainWindowPanel) 
    {
        setTitle("Window :)");
        setSize(windowWidth, windowHeight);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set layout and add panels
        setLayout(new BorderLayout());
        add(mainWindowPanel, BorderLayout.CENTER);

        setVisible(true);
        setBackground(Color.black);
        setLocationRelativeTo(null); // Center the window
        setExtendedState(JFrame.MAXIMIZED_BOTH); 
    }
}
