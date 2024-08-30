using System.Collections;
using System.Collections.Generic;
using UnityEngine;

//Input Keys
public enum InputKeyboard{
    arrows =0, 
    wasd = 1
}

// Colors
public enum PlayerColor {
    magenta = 0,
    blue = 1,
    yellow = 2,
    cyan = 3,
    black = 4
}

public class MoveWithKeyboardBehavior : AgentBehaviour
{

    public int points;
    private InputKeyboard inputKeyboard;
    private PlayerColor playerColor;
    public TMPro.TMP_Dropdown movementInputDropdown;
    public TMPro.TMP_Dropdown colorInputDropdown;
    private bool hasLongTouched;
    public LongTouchManager touchManager;

    Steering steering = new Steering();
    
    void Start()
    {
        inputKeyboard = (InputKeyboard)movementInputDropdown.value;
        playerColor = (PlayerColor)colorInputDropdown.value;

        // if (inputKeyboard == InputKeyboard.arrows) 
        //     (gameObject.GetComponent("CelluloAgent") as CelluloAgent).SetVisualEffect(VisualEffect.VisualEffectConstAll, Color.magenta, 255);
        // if (inputKeyboard == InputKeyboard.wasd) 
        //     (gameObject.GetComponent("CelluloAgent") as CelluloAgent).SetVisualEffect(VisualEffect.VisualEffectConstAll, Color.blue, 255);
        hasLongTouched = false;
    }

    void Update()
    {
        playerColor = (PlayerColor)colorInputDropdown.value;
        switch(playerColor) {
            case PlayerColor.magenta:
                (gameObject.GetComponent("CelluloAgent") as CelluloAgent).SetVisualEffect(VisualEffect.VisualEffectConstAll, Color.magenta, 255);
                break;
            case PlayerColor.blue:
                (gameObject.GetComponent("CelluloAgent") as CelluloAgent).SetVisualEffect(VisualEffect.VisualEffectConstAll, Color.blue, 255);
                break;
            case PlayerColor.yellow:
                (gameObject.GetComponent("CelluloAgent") as CelluloAgent).SetVisualEffect(VisualEffect.VisualEffectConstAll, Color.yellow, 255);
                break;
            case PlayerColor.cyan:
                (gameObject.GetComponent("CelluloAgent") as CelluloAgent).SetVisualEffect(VisualEffect.VisualEffectConstAll, Color.cyan, 255);
                break;
            case PlayerColor.black:
                (gameObject.GetComponent("CelluloAgent") as CelluloAgent).SetVisualEffect(VisualEffect.VisualEffectConstAll, Color.black, 255);
                break;
            default:
                break;
        }
    }

    public override Steering GetSteering()
    {
        inputKeyboard = (InputKeyboard)movementInputDropdown.value;
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

    public void LongTouch()
    {
        if (!hasLongTouched)
        {
            hasLongTouched = true;
            touchManager.addLongTouch();
        }
    }
}
