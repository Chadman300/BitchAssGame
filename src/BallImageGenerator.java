import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class BallImageGenerator {
    
    public static void main(String[] args) {
        createBallImages();
    }
    
    public static void createBallImages() {
        // Create images directory if it doesn't exist
        File imagesDir = new File("images");
        if (!imagesDir.exists()) {
            imagesDir.mkdirs();
        }
        
        // Create each ball image
        createBallImage("images/small_red_ball.png", 32, Color.RED);
        createBallImage("images/medium_blue_ball.png", 32, Color.BLUE);
        createBallImage("images/large_green_ball.png", 32, Color.GREEN);
        createBallImage("images/xl_yellow_ball.png", 32, Color.YELLOW);
        createBallImage("images/tiny_magenta_ball.png", 32, Color.MAGENTA);
        
        System.out.println("Ball images created successfully!");
    }
    
    private static void createBallImage(String filename, int size, Color color) {
        try {
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            
            // Enable antialiasing for smooth circles
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Clear background (transparent)
            g2d.setComposite(AlphaComposite.Clear);
            g2d.fillRect(0, 0, size, size);
            g2d.setComposite(AlphaComposite.SrcOver);
            
            // Create gradient for 3D effect
            int center = size / 2;
            int radius = size / 2 - 2;
            
            // Create radial gradient
            RadialGradientPaint gradient = new RadialGradientPaint(
                center - radius/4, center - radius/4, radius,
                new float[]{0.0f, 0.7f, 1.0f},
                new Color[]{
                    brighterColor(color, 1.5f),
                    color,
                    darkerColor(color, 0.6f)
                }
            );
            
            g2d.setPaint(gradient);
            g2d.fillOval(2, 2, size - 4, size - 4);
            
            // Add highlight
            g2d.setColor(new Color(255, 255, 255, 100));
            g2d.fillOval(center - radius/3, center - radius/2, radius/2, radius/3);
            
            // Add border
            g2d.setColor(darkerColor(color, 0.5f));
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawOval(2, 2, size - 4, size - 4);
            
            g2d.dispose();
            
            // Save the image
            ImageIO.write(image, "PNG", new File(filename));
            
        } catch (IOException e) {
            System.err.println("Error creating image " + filename + ": " + e.getMessage());
        }
    }
    
    private static Color brighterColor(Color color, float factor) {
        int r = Math.min(255, (int)(color.getRed() * factor));
        int g = Math.min(255, (int)(color.getGreen() * factor));
        int b = Math.min(255, (int)(color.getBlue() * factor));
        return new Color(r, g, b);
    }
    
    private static Color darkerColor(Color color, float factor) {
        int r = (int)(color.getRed() * factor);
        int g = (int)(color.getGreen() * factor);
        int b = (int)(color.getBlue() * factor);
        return new Color(r, g, b);
    }
}