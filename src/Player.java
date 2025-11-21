

public class Player 
{
    //public
    public Circle playerCircle;
    public Circle groundCircle;

    //private
    private int score;

    public float moveForce;
    public float airMoveForceReduction;

    public float jumpForce;

    private float health;
    private float maxHealth;

    public Player(Circle playerCircle, Circle groundCircle) 
    {
        this.playerCircle = playerCircle;
        this.groundCircle = groundCircle;
        this.score = 0;

        this.health = 100.0f; // Default health
        this.maxHealth = 100.0f; // Default max health

        this.jumpForce = 50000000.0f; // Default jump force
        this.moveForce = 5000000.0f; // Default move force
        this.airMoveForceReduction = 0.15f; // 15% movement control in air
    }

    public void Jump(double deltaTime) 
    {
        // Apply an upward force to the player's circle
        //only jump if on ground
        if(groundCircle.isCollidingPoly || groundCircle.isCollidingCircle)
        {
            playerCircle.ApplyForce(jumpForce * deltaTime, new Vector2D(0, -1));
            groundCircle.isCollidingPoly = false;
            groundCircle.isCollidingCircle = false;
        }   
    }

    public void Move(double deltaTime, float direction) 
    {
        // Apply a horizontal force to the player's circle
        var curMoveForce = moveForce;
        if(!groundCircle.isCollidingPoly)

        {
            direction *= airMoveForceReduction; // Reduced control in air
        }

        playerCircle.ApplyForce(curMoveForce * deltaTime, new Vector2D(direction, 0));
    }

    public void PlayerUpdate(double deltaTime) 
    {
        // Update player logic here (e.g., health regeneration, status effects)
        if (health > maxHealth) 
        {
            health = maxHealth;
        }

        groundCircle.currentPos.dx = playerCircle.currentPos.dx;
        groundCircle.currentPos.dy = playerCircle.currentPos.dy + (playerCircle.radius);

        if(groundCircle.isCollidingPoly)
        {
            // Friction effect when on the ground
            System.out.println("Applying ground friction");
        }
    }

    public int getScore() 
    {
        return score;
    }

    public float getHealth() 
    {
        return health;
    }

    public void addScore(int points) 
    {
        score += points;
    }
}
