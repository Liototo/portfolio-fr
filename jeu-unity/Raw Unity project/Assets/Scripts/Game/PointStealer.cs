using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class PointStealer : MonoBehaviour
{

    bool canSteal;

    // Start is called before the first frame update
    void Start()
    {
        canSteal = false;
    }

    // Update is called once per frame
    void Update()
    {
        
    }

    public void switchCanSteal()
    {
        canSteal = !canSteal;
    }

    void OnCollisionEnter(Collision collision)
    {
        if (canSteal && collision.collider.gameObject.CompareTag("Player"))
        {
            for (int i = 0; i < 2; ++i)
            {
                collision.collider.gameObject.GetComponentInParent<MoveWithKeyboardBehavior>().removePoint();
                gameObject.GetComponentInParent<MoveWithKeyboardBehavior>().addPoint();
            }
            playSound("PointStolen");
            switchCanSteal();
        }
    }

    public void playSound(string sound)
    {
        AudioSource audio = gameObject.AddComponent<AudioSource>();
        audio.PlayOneShot((AudioClip)Resources.Load(sound));
    }
}
