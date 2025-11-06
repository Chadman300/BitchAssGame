import java.awt.Color;
import java.awt.Graphics;

class Circle
{
    public int radius;
    public float mass;
    public Color color;

    public Vector2D acc;
    public Vector2D vel;
    public Vector2D prevPos;
    public Vector2D currentPos;

    public boolean isPinned = false;

    public Circle(int _radius, int _x, int _y, float _mass, Color _color, boolean _isPinned)
    {
        color = _color;
        mass = _mass;
        radius = _radius;
        acc = new Vector2D(0,0);
        vel = new Vector2D(0,0);
        prevPos = new Vector2D(_x,_y);
        currentPos = new Vector2D(_x,_y);
        isPinned = _isPinned;
    }

    public Circle(int _radius, int _x, int _y, float _mass, Color _color)
    {
        color = _color;
        mass = _mass;
        radius = _radius;
        acc = new Vector2D(0,0);
        vel = new Vector2D(0,0);
        prevPos = new Vector2D(_x,_y);
        currentPos = new Vector2D(_x,_y);
        isPinned = false;
    }

    public void VerletIntegration(double deltaTime)
    {
        if(isPinned) return;

        //Calculate new position components
        double nextX = 2 * currentPos.dx - prevPos.dx + acc.dx * deltaTime * deltaTime;
        double nextY = 2 * currentPos.dy - prevPos.dy + acc.dy * deltaTime * deltaTime;

        //Set Previous position to current
        prevPos.dx = currentPos.dx;
        prevPos.dy = currentPos.dy;

        //Set new position
        currentPos.dx = nextX;
        currentPos.dy = nextY;

        acc.dx = 0;
        acc.dy = 0;
        
        //Update velocity based on position change
        updateVelocity(deltaTime);
    }
    
    public void updateVelocity(double deltaTime)
    {
        // Calculate velocity from position difference
        vel.dx = (currentPos.dx - prevPos.dx) / deltaTime;
        vel.dy = (currentPos.dy - prevPos.dy) / deltaTime;
    }
    
    public Vector2D getVelocity()
    {
        return new Vector2D(vel.dx, vel.dy);
    }

    public void KeepInBoundsCircle(int windowWidth, int windowHeight, double deltaTime)
    {
        int boundsCircleRadius = 200;

        //get distance using distance squared
        var screenMid = new Vector2D(windowHeight / 2, windowWidth / 2);
        var dx = currentPos.dx - screenMid.dx;
        var dy = currentPos.dy - screenMid.dy;
        var distanceSquared = dx * dx + dy * dy;
        var dis = Math.sqrt(distanceSquared);

        //add circle radius so it dosent use mid pt
        dis += radius;

        if(dis >= boundsCircleRadius && (dis != 0))
        {
            var posMidVector = new Vector2D(screenMid.dx - currentPos.dx, screenMid.dy - currentPos.dy);
            var normal = Vector2D.VectorNormalize(posMidVector);
            currentPos.dx = currentPos.dx + (normal.dx * (dis - boundsCircleRadius));// * deltaTime;
            currentPos.dy = currentPos.dy + (normal.dy * (dis - boundsCircleRadius));// * deltaTime;

            //acc.dx = 0;
            //acc.dy = 0;

            // Recalculate distance if needed (but try to avoid this)
            dx = currentPos.dx - screenMid.dx;
            dy = currentPos.dy - screenMid.dy;
        }
    }

    public boolean LineCollision(Vector2D lp1, Vector2D lp2, double deltaTime)
    {
        double distanceToLine = Vector2D.DistanceBetweenPointAndLine(lp1, lp2, currentPos);

        if(Math.abs(distanceToLine) <= (double)radius)
        {

            // Calculate line normal vector (perpendicular to line)
            double A = lp1.dy - lp2.dy;
            double B = lp2.dx - lp1.dx;
            var lineNormal = Vector2D.VectorNormalize(new Vector2D(A, B));

            // Determine which side of the line the circle is on
            double side = A * currentPos.dx + B * currentPos.dy + ((lp1.dx * lp2.dy) - (lp2.dx * lp1.dy));
            if (side < 0) {
                lineNormal.dx = -lineNormal.dx;
                lineNormal.dy = -lineNormal.dy;
            }
            
            //Get Point on Line
            var collisionPoint = new Vector2D(currentPos.dx + (-lineNormal.dx * distanceToLine), currentPos.dy + (-lineNormal.dy * distanceToLine));
            //System.out.println("Collision Point: " + collisionPoint.dx + ", " + collisionPoint.dy);
            
            // Check if collision point is within the line segment
            boolean withinSegment = isPointOnLineSegment(lp1, lp2, collisionPoint);
            
            if (!withinSegment) 
            {
                return false; // No collision if point is outside the line segment
            }

            // Move circle out of line collision
            double penetration = radius - Math.abs(distanceToLine);
            currentPos.dx += lineNormal.dx * penetration;
            currentPos.dy += lineNormal.dy * penetration;
            
            // Get current velocity
            Vector2D velocity = getVelocity();
            
            // Calculate velocity component along the collision normal
            double velAlongNormal = velocity.dx * lineNormal.dx + velocity.dy * lineNormal.dy;
            
            // Only resolve if moving towards the line
            if (velAlongNormal < 0) {
                // Calculate restitution (bounciness) - 0.6 for wall bounces
                double restitution = 0.6;
                
                // Reflect velocity component along normal
                velocity.dx -= (1 + restitution) * velAlongNormal * lineNormal.dx;
                velocity.dy -= (1 + restitution) * velAlongNormal * lineNormal.dy;
                
                // Add friction along the line (tangential damping)
                double friction = 0.1;
                Vector2D tangent = new Vector2D(-lineNormal.dy, lineNormal.dx);
                double velAlongTangent = velocity.dx * tangent.dx + velocity.dy * tangent.dy;
                velocity.dx -= friction * velAlongTangent * tangent.dx;
                velocity.dy -= friction * velAlongTangent * tangent.dy;
                
                // Update previous position to reflect new velocity (for Verlet integration)
                prevPos.dx = currentPos.dx - velocity.dx * deltaTime;
                prevPos.dy = currentPos.dy - velocity.dy * deltaTime;
                
                // Update velocity vector
                vel.dx = velocity.dx;
                vel.dy = velocity.dy;
            }

            //change color
            color = Color.RED;   

            return true;
        }

        if(Math.abs(distanceToLine) > (double)radius)
        {
            color = Color.GREEN;
        }

        return false;
    }

    public void PolygonCollisions(Poly poly, double deltaTime)
    {
        for (int i = 0; i < poly.pointsList.size(); i++)
        {
            //get line points
            Vector2D lp1 = poly.pointsList.get(i);
            Vector2D lp2 = new Vector2D();

            if(i + 1 < poly.pointsList.size())
            {
                lp2 = poly.pointsList.get(i + 1);      
            }
            else
            {
                lp2 = poly.pointsList.get(0);    
            }

            if(LineCollision(lp1, lp2, deltaTime))
            {

            }
        }
    }

    public void CircleCollisions(Circle otherBall, double deltaTime)
    {
        if(otherBall == null) return;

        //DEBUG
        //Color prevColor = color;

        //get distance using distance squared to avoid expensive sqrt
        var dx = otherBall.currentPos.dx - currentPos.dx;
        var dy = otherBall.currentPos.dy - currentPos.dy;
        var distanceSquared = dx * dx + dy * dy;
        var radiusDis = radius + otherBall.radius;
        var radiusDisSquared = radiusDis * radiusDis;

        if((distanceSquared < radiusDisSquared) && (distanceSquared != 0))
        {
            // Only calculate actual distance when collision is confirmed
            var dis = Math.sqrt(distanceSquared);
            
            // Calculate collision normal vector (from this ball to other ball)
            var normalX = dx / dis;
            var normalY = dy / dis;
            
            // Separate overlapping circles (both balls move)
            var overlap = radiusDis - dis;
            var separationX = normalX * overlap * 0.5;
            var separationY = normalY * overlap * 0.5;

            //dont move if pinned
            if(!isPinned)
            {
                currentPos.dx -= separationX;
                currentPos.dy -= separationY;
            }
            if(!otherBall.isPinned)
            {
                otherBall.currentPos.dx += separationX;
                otherBall.currentPos.dy += separationY;
            }
            
            // Get current velocities
            Vector2D vel1 = getVelocity();
            Vector2D vel2 = otherBall.getVelocity();
            
            // Calculate relative velocity in collision normal direction
            double relativeVelX = vel2.dx - vel1.dx;
            double relativeVelY = vel2.dy - vel1.dy;
            double velAlongNormal = relativeVelX * normalX + relativeVelY * normalY;
            
            // Don't resolve if velocities are separating
            if (velAlongNormal < 0) {
                // Calculate restitution (bounciness) - 0.8 for realistic bounce
                double restitution = 0.8;
                
                // Calculate impulse scalar using conservation of momentum
                double impulse = -(1 + restitution) * velAlongNormal;
                impulse /= (1/mass + 1/otherBall.mass);
                
                // Calculate impulse vector
                double impulseX = impulse * normalX;
                double impulseY = impulse * normalY;
                
                // Apply impulse to velocities
                vel1.dx -= impulseX / mass;
                vel1.dy -= impulseY / mass;
                vel2.dx += impulseX / otherBall.mass;
                vel2.dy += impulseY / otherBall.mass;
                
                // Update previous positions to reflect new velocities (for Verlet integration)
                if(!isPinned)
                {
                    prevPos.dx = currentPos.dx - vel1.dx * deltaTime;
                    prevPos.dy = currentPos.dy - vel1.dy * deltaTime;
                }
                if(!otherBall.isPinned)
                {
                    otherBall.prevPos.dx = otherBall.currentPos.dx - vel2.dx * deltaTime;
                    otherBall.prevPos.dy = otherBall.currentPos.dy - vel2.dy * deltaTime;
                }
                
                // Update velocity vectors in both objects
                vel.dx = vel1.dx;
                vel.dy = vel1.dy;
                otherBall.vel.dx = vel2.dx;
                otherBall.vel.dy = vel2.dy;
            }

            //change color
            //color = Color.RED;
        }

        //color = prevColor;
    }

    public void ApplyGravity(double gravity, double deltaTime)
    {
        if(deltaTime == 0) return;
        acc.dy += gravity * deltaTime;
    }

    public void NewtwonApplyGravity(double gravity, Circle otherBall, double deltaTime)
    {
        // F = G * (m1*m2) / r^2
        double G = 6.67430e-11; // Gravitational constant (in m^3 kg^-1 s^-2)

        var dx = otherBall.currentPos.dx - currentPos.dx;
        var dy = otherBall.currentPos.dy - currentPos.dy;
        var distanceSquared = dx * dx + dy * dy;
        var distance = Math.sqrt(distanceSquared);
        if(distance == 0) return; // Prevent division by zero
        double force = gravity * (mass * otherBall.mass) / distanceSquared;
        var forceVector = Vector2D.VectorNormalize(new Vector2D(dx, dy));
        ApplyForce(force, forceVector);
        otherBall.ApplyForce(force, new Vector2D(-forceVector.dx, -forceVector.dy));
    }

    public void ApplyForce(double force, Vector2D forceVector)
    {
        //f(N) = ma -> a = f/m
        acc.dx += forceVector.dx * (force / mass);
        acc.dy += forceVector.dy * (force / mass);
    }

    // Helper method to check if a point lies on a line segment
    private boolean isPointOnLineSegment(Vector2D p1, Vector2D p2, Vector2D point) {
        // Calculate the parameter t for the point on the line
        // If t is between 0 and 1, the point is on the segment
        double dx = p2.dx - p1.dx;
        double dy = p2.dy - p1.dy;
        
        // Avoid division by zero - use the component with larger absolute value
        double t;
        if (Math.abs(dx) > Math.abs(dy)) {
            t = (point.dx - p1.dx) / dx;
        } else if (Math.abs(dy) > 1e-10) { // Check for non-zero dy
            t = (point.dy - p1.dy) / dy;
        } else {
            // Line segment is a point
            double dx_point = point.dx - p1.dx;
            double dy_point = point.dy - p1.dy;
            double distSquared = dx_point * dx_point + dy_point * dy_point;
            return distSquared < 1e-12; // Very close to the point (distance squared)
        }
        
        return t >= 0.0 && t <= 1.0;
    }

    public void draw(Graphics g, int drawX, int drawY, double cameraZoom) // draw must be called by paintComponent of the panel
    {
        g.setColor(color);

        g.fillOval(drawX - (int)radius, drawY - (int)radius, (int)((radius * 2 * cameraZoom)), (int)(radius * 2 * cameraZoom));
    }
}