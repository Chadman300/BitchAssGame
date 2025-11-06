
import java.awt.Color;

public enum BallType {
    SMALL_RED(10, Color.RED, "Small Red", "images/small_red_ball.png"),
    MEDIUM_BLUE(15, Color.BLUE, "Medium Blue", "images/medium_blue_ball.png"),
    LARGE_GREEN(20, Color.GREEN, "Large Green", "images/large_green_ball.png"),
    XL_YELLOW(25, Color.YELLOW, "XL Yellow", "images/xl_yellow_ball.png"),
    TINY_MAGENTA(8, Color.MAGENTA, "Tiny Magenta", "images/tiny_magenta_ball.png"),
    MEDIUM_LINK(8, Color.MAGENTA, "Medium Link", "images/medium_link_ball.png");

    private final int radius;
    private final Color color;
    private final String displayName;
    private final String imagePath;
    
    BallType(int radius, Color color, String displayName, String imagePath) {
        this.radius = radius;
        this.color = color;
        this.displayName = displayName;
        this.imagePath = imagePath;
    }
    
    public int getRadius() {
        return radius;
    }
    
    public Color getColor() {
        return color;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getImagePath() {
        return imagePath;
    }
}