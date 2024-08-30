using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class RingTrigger : MonoBehaviour
{
    // Start is called before the first frame update
    void Start()
    {
        
    }

    // Update is called once per frame
    void Update()
    {
        
    }
     void OnTriggerEnter(Collider other){
        if (other.gameObject.CompareTag("GhostSheep") && other.gameObject.GetComponentInParent<GhostSheepBehavior>().isASheep())
        {
            other.gameObject.GetComponentInParent<GhostSheepBehavior>().givePoint();
            playSound("winPoint");
        }
    }

    public void playSound(string sound)
    {
        AudioSource audio = gameObject.AddComponent<AudioSource>();
        audio.PlayOneShot((AudioClip)Resources.Load(sound));
    }

}
