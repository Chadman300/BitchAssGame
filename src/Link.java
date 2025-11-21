

public class Link 
{
    public Circle circleA;
    public Circle circleB;
    private double restLength;

    public Link(Circle a, Circle b, double restLength) 
    {
        this.circleA = a;
        this.circleB = b;
        this.restLength = restLength;
    }

    public double getRestLength() 
    {
        return restLength;
    }

    public void ApplyForces(double deltaTime)
    {
        if(circleA == null || circleB == null) return;

        // Calculate current distance between centers
        var dx = circleB.currentPos.dx - circleA.currentPos.dx;
        var dy = circleB.currentPos.dy - circleA.currentPos.dy;
        double currentDistance = Math.sqrt(dx * dx + dy * dy);
        
        // Target distance is just the rest length (center to center)
        double targetDistance = restLength;
        
        // Avoid division by zero
        if(currentDistance < 0.001) return;
        
        // Calculate how much we need to correct
        double difference = currentDistance - targetDistance;
        
        // Only apply correction if there's a significant difference
        if(Math.abs(difference) > 0.001)
        {
            // Normalize the direction vector using the ACTUAL distance
            double normalX = dx / currentDistance;
            double normalY = dy / currentDistance;
            
            // Apply only half the correction per frame for stability
            double correctionAmount = difference * 0.5;
            double correctionX = normalX * correctionAmount;
            double correctionY = normalY * correctionAmount;
            
            // Move circles toward correct distance
            if(!circleA.isPinned)
            {
                circleA.currentPos.dx += correctionX * 0.5; // Each circle gets half the correction
                circleA.currentPos.dy += correctionY * 0.5;
            }
            
            if(!circleB.isPinned)
            {
                circleB.currentPos.dx -= correctionX * 0.5;
                circleB.currentPos.dy -= correctionY * 0.5;
            }
            
            // If only one circle is pinned, the other gets full correction
            if(circleA.isPinned && !circleB.isPinned)
            {
                circleB.currentPos.dx -= correctionX;
                circleB.currentPos.dy -= correctionY;
            }
            else if(!circleA.isPinned && circleB.isPinned)
            {
                circleA.currentPos.dx += correctionX;
                circleA.currentPos.dy += correctionY;
            }
        }
    }
}
