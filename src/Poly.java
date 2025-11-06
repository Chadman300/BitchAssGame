import java.awt.Color;
import java.util.ArrayList;

class Poly
{
    public ArrayList<Vector2D> pointsList = new ArrayList<Vector2D>(); 
    public Color color;

    public Poly(ArrayList<Vector2D> _pointsList, Color _color)
    {
        if(_color == null)
            _color = Color.BLACK; 

        if(_pointsList == null)
            _pointsList = new ArrayList<Vector2D>(); 
            
        color = _color;
        pointsList = _pointsList;
    }
}
