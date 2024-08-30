using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class CollisionsExample : MonoBehaviour
{
    // Start is called before the first frame update
    void Start()
    {
        
    }

    // Update is called once per frame
    void Update()
    {
        
    }

    void OnCollisionEnter(Collision info)
    {
        print("Detected collision between " + gameObject.name + " and " + info.collider.name);
        print("There are " + info.contacts.Length + " point(s) of contact");
        print("Their relative velocity is " + info.relativeVelocity);
    }

    void OnCollisionStay(Collision info)
    {
        print(gameObject.name + " and " + info.collider.name + " are still colliding");
    }

    void OnCollisionExit(Collision info)
    {
        print(gameObject.name + " and " + info.collider.name + " are no longer colliding");
    }
}
