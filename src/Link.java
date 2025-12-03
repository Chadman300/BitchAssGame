

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
            
            // Calculate mass ratios - lighter objects move more
            double totalMass = circleA.mass + circleB.mass;
            double massRatioA = circleB.mass / totalMass; // Circle A moves proportional to B's mass
            double massRatioB = circleA.mass / totalMass; // Circle B moves proportional to A's mass
            
            // If only one circle is pinned, the other gets full correction
            if(circleA.isPinned && !circleB.isPinned)
            {
                circleB.currentPos.dx -= normalX * correctionAmount;
                circleB.currentPos.dy -= normalY * correctionAmount;
            }
            else if(!circleA.isPinned && circleB.isPinned)
            {
                circleA.currentPos.dx += normalX * correctionAmount;
                circleA.currentPos.dy += normalY * correctionAmount;
            }
            else if(!circleA.isPinned && !circleB.isPinned)
            {
                // Both circles move based on mass ratio
                circleA.currentPos.dx += normalX * correctionAmount * massRatioA;
                circleA.currentPos.dy += normalY * correctionAmount * massRatioA;
                
                circleB.currentPos.dx -= normalX * correctionAmount * massRatioB;
                circleB.currentPos.dy -= normalY * correctionAmount * massRatioB;
            }
            // If both are pinned, do nothing
        }
    }
}
