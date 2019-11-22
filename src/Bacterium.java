public class Bacterium {

    public int type;
    public float x;
    public float y;
    public float sx;
    public float sy;
    public float rotation;
    public boolean toBeDeleted;
    public int age;

    public float tx;
    public float ty;
    public float food;

    public float rotationSpeed = 0.2f;

    public float speed = 0.5f;
    public float slip = 0.8f;

    public Bacterium(int type, float x, float y) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.sx = 0f;
        this.sy = 0f;
        this.rotation = 0f;
        this.toBeDeleted = false;
        this.age = 0;
        this.tx = 0;
        this.ty = 0;
        this.food = 3f;
    }

}