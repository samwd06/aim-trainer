/**
 * SUMMARY: A simple data class that remembers the coordinates and age of a missed shot.
 * This allows the game to draw a visual bullet hole on the background that slowly fades away over time.
 */
package game.bodies;

import org.jbox2d.common.Vec2;

// [JAVA TOPIC: Data Classes] This class is purely used to store data about missed shots so the UI can draw them.
public class BulletHole {

    // [OOP CONCEPT: Encapsulation] Keeping properties private.
    private Vec2 position;
    private float age;

    public BulletHole(Vec2 position) {
        this.position = position;
        this.age = 0;
    }

    public Vec2 getPosition() { return position; }
    public float getAge() { return age; }

    public void ageUp() {
        this.age += (1.0f / 60.0f);
    }
}