#include "BluetoothSerial.h"

#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif

BluetoothSerial SerialBT;

const int trigPin = 5;
const int echoPin = 18;

//define sound speed in cm/uS
#define SOUND_SPEED 0.034
#define CM_TO_INCH 0.393701
const float alpha = 0.9;
long duration;
float distanceCm;
float distanceInch;

void setup() {
  Serial.begin(9600); // Starts the serial communication
  SerialBT.begin("DolevThePrince"); //Bluetooth device name
  pinMode(trigPin, OUTPUT); // Sets the trigPin as an Output
  pinMode(echoPin, INPUT); // Sets the echoPin as an Input
}

void loop() {
  // Clears the trigPin
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  // Sets the trigPin on HIGH state for 10 micro seconds
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);
  
  // Reads the echoPin, returns the sound wave travel time in microseconds
  duration = pulseIn(echoPin, HIGH);
  
  // Calculate the distance
  distanceCm = alpha * distanceCm + (1-alpha) * duration * SOUND_SPEED/2;
  
  // Prints the distance in the Serial Monitor
  //Serial.print("Distance (cm): ");
  Serial.println((String)+"Distance:"+distanceCm);
  SerialBT.println((String)+"Distance:"+distanceCm); 

  
  delay(50);
}
