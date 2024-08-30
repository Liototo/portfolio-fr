using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Movement : AgentBehaviour
{

    public int points;
    private InputKeyboard inputKeyboard;
    public TMPro.TMP_Dropdown dropdown;

    Steering steering = new Steering();
    
    void Start()
    {
        inputKeyboard = (InputKeyboard)dropdown.value;
        
        if (inputKeyboard == InputKeyboard.arrows) 
            (gameObject.GetComponent("CelluloAgent") as CelluloAgent).SetVisualEffect(VisualEffect.VisualEffectConstAll, Color.magenta, 255);
        if (inputKeyboard == InputKeyboard.wasd) 
            (gameObject.GetComponent("CelluloAgent") as CelluloAgent).SetVisualEffect(VisualEffect.VisualEffectConstAll, Color.blue, 255);
    }

    public override Steering GetSteering()
    {
        inputKeyboard = (InputKeyboard)dropdown.value;
        if (inputKeyboard == InputKeyboard.arrows) {

            steering.linear = new Vector3(Input.GetAxis("HorizontalArrows"), 0, Input.GetAxis("VerticalArrows")) * agent.maxAccel;
            steering.linear = this.transform.parent.TransformDirection(Vector3.ClampMagnitude(steering.linear, agent.maxAccel));

        } else if (inputKeyboard == InputKeyboard.wasd) {

            steering.linear = new Vector3(Input.GetAxis("HorizontalWASD"), 0, Input.GetAxis("VerticalWASD")) * agent.maxAccel;
            steering.linear = this.transform.parent.TransformDirection(Vector3.ClampMagnitude(steering.linear, agent.maxAccel));

        }

        return steering;
    }

    public void addPoint()
    {
        points++;
    }

    public void removePoint()
    {
        points--;
    }

}
