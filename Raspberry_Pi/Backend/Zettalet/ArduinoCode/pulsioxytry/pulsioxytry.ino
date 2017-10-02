#include <MySignals.h>
#include "Wire.h"
#include "SPI.h" 

int valuePulse;
int valueSPO2;
uint8_t pulsioximeter_state = 0;

void setup() 
{

  Serial.begin(19200);
  MySignals.begin();
  
  MySignals.initSensorUART();
  MySignals.enableSensorUART(PULSIOXIMETER);
}

void loop() 
{

  // First way of getting sensor data
  MySignals.enableSensorUART(PULSIOXIMETER);
  
    Serial.print(F("Try this anyway:"));
    Serial.print(MySignals.pulsioximeterData.BPM);
  Serial.println();
  pulsioximeter_state = MySignals.getPulsioximeterMini();
  if (pulsioximeter_state == 1)
  {
    Serial.print(F("Pulse:"));
    Serial.print(MySignals.pulsioximeterData.BPM);
    Serial.print(F("bpm / SPO2:"));
    Serial.print(MySignals.pulsioximeterData.O2);
    Serial.println(F("%"));
  }
  else if (pulsioximeter_state == 2)
  {
    Serial.println(F("Not valid data"));
  }
  else
  {
    Serial.println(F("No available data"));
  }
  MySignals.disableSensorUART();
  delay(2000);
}
