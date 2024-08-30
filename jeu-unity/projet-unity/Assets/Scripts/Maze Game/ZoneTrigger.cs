using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public enum player { p1, p2 }

public class ZoneTrigger : MonoBehaviour
{
    

    public player associatedPlayer;

    // Start is called before the first frame update
    void Start()
    {
        
    }

    // Update is called once per frame
    void Update()
    {
        
    }

    void OnTriggerEnter (Collider other)
    {
        if ((associatedPlayer == player.p1 && other.gameObject.CompareTag("Player1")) || (associatedPlayer == player.p2 && other.gameObject.CompareTag("Player2")))
            other.gameObject.GetComponentInParent<HoldingBehavior>().DropItem();
    }
}
