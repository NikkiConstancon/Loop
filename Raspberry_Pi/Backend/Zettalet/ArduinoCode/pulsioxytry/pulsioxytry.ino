#include <MySignals.h>
#include "Wire.h"
#include "SPI.h" 

int valuePulse;
int valueSPO2;
uint8_t pulsioximeter_state = 0;


void setup() 
{
    pinMode(4, INPUT);
    pinMode(5, INPUT);
    pinMode(6, INPUT);
    delay(20);

    Serial.begin(19200);
    while (Serial.read() >= 0);

    digitalWrite(4, LOW);
    digitalWrite(5, HIGH);
    digitalWrite(6, LOW);
    delay(10);

      delay(30);
}

void loop() 
{

 // while (Serial.available())
    Serial.println(Serial.read(), HEX);
  delay(2000);
}
