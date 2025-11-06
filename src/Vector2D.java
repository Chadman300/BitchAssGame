class Vector2D
{
    public double dx;
    public double dy;

    public Vector2D(double _dx, double _dy) 
    {
        dx = _dx;
        dy = _dy;
    }

    public Vector2D() 
    {
        dx = 0;
        dy = 0;
    }

    public static Vector2D Zero()
    {
        return new Vector2D(0,0);
    }

    public static double VectorDistance(Vector2D v1, Vector2D v2)
    {
        return Math.sqrt(((v2.dx - v1.dx) * (v2.dx - v1.dx)) + ((v2.dy - v1.dy) * (v2.dy - v1.dy)));
    }

    public static double VectorMag(Vector2D v)
    {
        return Math.sqrt((Math.pow(v.dx, 2) + Math.pow(v.dy, 2)));
    }

    public static Vector2D VectorNormalize(Vector2D v)
    {
        var mag = VectorMag(v);
        if(mag > 0)
        {
            return new Vector2D(v.dx / mag, v.dy / mag);
        }
        return Vector2D.Zero();
    }

    public static double DistanceBetweenPointAndLine(Vector2D lp1, Vector2D lp2, Vector2D p)
    {
        //d = |Ax₁ + By₁ + C| / √(A² + B²) 

        double A = lp1.dy - lp2.dy;
        double B = lp2.dx - lp1.dx;
        double C = (lp1.dx * lp2.dy) - (lp2.dx * lp1.dy);

        double distance = Math.abs(A*p.dx + B*p.dy + C) / Math.sqrt(A*A + B*B);

        return distance;
    }

    public static double RandomRange(double min, double max)
    {
        return Math.random() * (max - min) + min;
    }
}