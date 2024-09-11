using System.Linq;
using UnityEngine;

public enum Mode
{
    sheep = 0,
    ghost = 1
}

// The behaviour of the "Ghost sheep" cellulos
public class GhostSheepBehavior : AgentBehaviour
{
    public Mode mode;

    GameObject[] players;

    public bool isASheep() { return mode == Mode.sheep; }

    public void Start(){
        (gameObject.GetComponent("CelluloAgent") as CelluloAgent).SetVisualEffect(VisualEffect.VisualEffectConstAll, Color.green, 255);
        (gameObject.GetComponent("CelluloAgent") as CelluloAgent).isMoved = false;
        Invoke("switchMode", Random.Range(10f, 20f));
    }

    // Give a point to the closest player
    public void givePoint()
    {
        if (mode == Mode.sheep)
        {
            findClosestPlayer().gameObject.GetComponentInParent<MoveWithKeyboardBehavior>().addPoint();
        }
    }

    // Determine movement direction (away from closest player if sheep, towards closest player if ghost)
    public override Steering GetSteering()
    {
        Vector3 closest = (findClosestPlayer().gameObject.transform.position - transform.position);

        float dist = closest.magnitude;

        Steering steering = new Steering();
        if (mode == Mode.sheep) { 
            if (dist <= 5f) steering.linear = (-closest) * agent.maxAccel; 
        }
        else steering.linear = closest * agent.maxAccel;
        return steering;
    }

    // Determine closest player
    public GameObject findClosestPlayer()
    {

        players = GameObject.FindGameObjectsWithTag("Player");
        float distance = float.MaxValue;
        Vector3 position = transform.position;
        GameObject closest = null;
        foreach(GameObject p in players)
        {
            Vector3 diff = p.transform.position - position;
            float curDistance = diff.sqrMagnitude;
            if (curDistance < distance)
            {
                closest = p;
                distance = curDistance;
            }
        }
        return closest;
    }

    // Switch between ghost (attacks player) and sheep (runs from players) modes
    public void switchMode()
    {
        players = GameObject.FindGameObjectsWithTag("Player");
        if (isASheep())
        {
            mode = Mode.ghost;
            (gameObject.GetComponent("CelluloAgent") as CelluloAgent).SetVisualEffect(VisualEffect.VisualEffectConstAll, Color.red, 255);
            playSound("wolf");
            foreach (GameObject p in players)
            {
                if (p != null)
                {
                    p.GetComponentInParent<CelluloAgent>().MoveOnStone();
                }
            }
        }
        else
        {
            mode = Mode.sheep;
            (gameObject.GetComponent("CelluloAgent") as CelluloAgent).SetVisualEffect(VisualEffect.VisualEffectConstAll, Color.green, 255);
            playSound("Sheep-Lamb-Bah");
            foreach (GameObject p in players)
            {
                if (p != null)
                {
                    p.GetComponentInParent<CelluloAgent>().ClearHapticFeedback();
                    p.GetComponentInParent<CelluloAgent>().SetCasualBackdriveAssistEnabled(true);
                }
            }
        }

        Invoke("switchMode", Random.Range(10f, 20f));
    }

    public void playSound(string sound)
    {
        AudioSource audio = gameObject.AddComponent<AudioSource>();
        audio.PlayOneShot((AudioClip)Resources.Load(sound));
    }
    
    // If ghost, make collided player lose a point
    void OnCollisionEnter(Collision collision)
    {
        if (mode == Mode.ghost && collision.collider.gameObject.CompareTag("Player"))
        {
            collision.collider.gameObject.GetComponentInParent<MoveWithKeyboardBehavior>().removePoint();
            playSound("LosePoint");
        }

    }

}
