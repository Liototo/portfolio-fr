using System.Linq;
using UnityEngine;

public class FollowMouseBehavior : AgentBehaviour
{
    public override Steering GetSteering()
    {
        Steering steering = new Steering();

        return steering;
    }
}
