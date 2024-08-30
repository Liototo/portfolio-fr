using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class ClearZone : MonoBehaviour
{
    // Start is called before the first frame update
    void Start()
    {
        
    }

    // Update is called once per frame
    void Update()
    {
        
    }

    void OnTriggerEnter(Collider other)
    {
        if ((other.gameObject.CompareTag("Player1") || other.gameObject.CompareTag("Player2")) && other.gameObject.GetComponentInParent<HoldingBehavior>().held != Item.none)
            other.gameObject.GetComponentInParent<HoldingBehavior>().setClear(true);
    }
}
