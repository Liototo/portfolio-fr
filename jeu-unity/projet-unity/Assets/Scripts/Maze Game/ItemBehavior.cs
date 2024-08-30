using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public enum Item
{
    none, green, pink, gold

}

public class ItemBehavior : MonoBehaviour
{
    public Item color;

    private Vector3[] greenPos = { new Vector3(11f, -0.5f, -10.5f), 
                                   new Vector3(17f, -0.5f, -14.5f), 
                                   new Vector3(8f, -0.5f, -15f) };

    private Vector3[] pinkPos = { new Vector3(20f, -0.5f, -11.5f),
                                  new Vector3(5f, -0.5f, -17.5f),
                                  new Vector3(17f, -0.5f, -5.5f) };

    private Vector3[] goldPos = { new Vector3(2f, -0.5f, -5.5f),
                                  new Vector3(26f, -0.5f, -2f),
                                  new Vector3(8f, -0.5f, -2f), };

    // Start is called before the first frame update
    void Start()
    {
        if (color != Item.green) Deactivate();
        else transform.position = greenPos[0];
    }

    // Update is called once per frame
    void Update()
    {

    }

    void OnTriggerEnter(Collider other)
    {
        if (other.gameObject.CompareTag("Player1") || other.gameObject.CompareTag("Player2"))
        {
            if (other.gameObject.GetComponentInParent<HoldingBehavior>().held == Item.none)
            {
                other.gameObject.GetComponentInParent<HoldingBehavior>().GrabItem(color, true);               
                Deactivate();
            }
        }
    }

    void Activate()
    {
        gameObject.SetActive(true);
        switch (color)
        {
            case Item.green:
                transform.position = greenPos[Random.Range(0, greenPos.Length - 1)];
                break;
            case Item.pink:
                transform.position = pinkPos[Random.Range(0, pinkPos.Length - 1)];
                break;
            case Item.gold:
                transform.position = goldPos[Random.Range(0, pinkPos.Length - 1)];
                break;
            default: break;
        }
    }

    void Deactivate()
    {
        gameObject.SetActive(false);
        switch (color)
        {
            case Item.green:
                Invoke("Activate", Random.Range(5f, 10f));
                break;
            case Item.pink:
                Invoke("Activate", Random.Range(10f, 15f));
                break;
            case Item.gold:
                Invoke("Activate", Random.Range(15f, 20f));
                break;
            default: break;
        }
    }

    
}
