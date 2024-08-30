using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class GemTrigger : MonoBehaviour
{
    GameManager gameManager;
    // Start is called before the first frame update
    void Start()
    {
        Deactivate();
    }

    // Update is called once per frame
    void Update()
    {

    }
    void OnTriggerEnter(Collider other)
    {
        if (other.gameObject.CompareTag("Player"))
        {
            other.gameObject.GetComponentInParent<PointStealer>().switchCanSteal();
            other.gameObject.GetComponentInParent<PointStealer>().playSound("GemCollected");
            Deactivate();
        }
    }

    void Deactivate()
    {
        gameObject.SetActive(false);
        Invoke("Activate", Random.Range(10f, 20f));
    }

    void Activate()
    {
        gameObject.SetActive(true);
        transform.position = new Vector3(Random.Range(2, 26), 0, Random.Range(-2, -18.5f));
    }

}
