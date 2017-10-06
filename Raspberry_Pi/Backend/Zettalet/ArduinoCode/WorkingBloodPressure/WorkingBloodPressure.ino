/*

    Copyright (C) 2016 Libelium Comunicaciones Distribuidas S.L.
   http://www.libelium.com

    By using it you accept the MySignals Terms and Conditions.
    You can find them at: http://libelium.com/legal

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Version:           0.1
    Design:            David Gascon
    Implementation:    Luis Martin / Victor Boria
*/

//#include <MySignals.h>
#include <SoftwareSerial.h>
#include "Wire.h"
#include "SPI.h"

SoftwareSerial mySerial(8, 9); // RX, TX

struct bloodPressureDataVector
{
  uint16_t systolic;
  uint16_t diastolic;
  uint16_t pulse;
};
bloodPressureDataVector bloodPressureData;

uint8_t getStatusBP()
{
  while (mySerial.available() > 0 )
  {
    mySerial.read();
  }
  uint8_t data = 0x00;
  mySerial.write("a");
  Serial.println("a");
  delay(100);
  while (mySerial.available() > 0 )
  {
    data = mySerial.read();
    Serial.println(data,HEX);
  }
  //data = 'a';
  if (data == 0x61)
  {
    return 1;
  }
  else
  {
    return 0;
  }
}

bool getBloodPressure(void)
{

  uint8_t finish_bp = 0;

  mySerial.write("e");
  Serial.println("e");
  delay(10000);

  unsigned long previous = millis();
  do
  {

    while (mySerial.available() > 0 )
    {

      uint8_t data = 0x00;

      data = mySerial.read();
      Serial.println(data);
      //delayMicroseconds(150);
      //Serial.println(data,HEX);

      if (data == 0x67)
      {
        //Serial.println("Dat1");
        delay(10);

        if (mySerial.read() == 0x2f)
        {
          //Serial.println("Dat2");
          //delay(10);
          uint8_t buffer[13];
          memset(buffer, 0x00, sizeof(buffer));

          for (uint8_t i = 0; i < 11; i++)
          {
            buffer[i] = mySerial.read();
            Serial.println(buffer[i]);
            //delay(10);
          }


          uint8_t sh = buffer[0] - 48;
          uint8_t sm = buffer[1] - 48;
          uint8_t sl = buffer[2] - 48;
          bloodPressureData.systolic = (sh * 100) + (sm * 10) + sl;

          uint8_t dh = buffer[4] - 48;
          uint8_t dm = buffer[5] - 48;
          uint8_t dl = buffer[6] - 48;
          bloodPressureData.diastolic = (dh * 100) + (dm * 10) + dl;

          uint8_t ph = buffer[8] - 48;
          uint8_t pm = buffer[9] - 48;
          uint8_t pl = buffer[10] - 48;
          bloodPressureData.pulse = (ph * 100) + (pm * 10) + pl;


          finish_bp = 1;
        }
      }

    }

  }
  while ((finish_bp == 0) && ((millis() - previous) < 60000));

  //Turn off blood pressure
  mySerial.write("i");
  Serial.println("i");

  return finish_bp;

}

void setup()
{

  Serial.begin(9600);
  mySerial.begin(19200);
}

void loop()
{
    Serial.flush();
    Serial.println("in arduino loop");
    while (mySerial.available()) 
   { 
      Serial.println(mySerial.read());
   }
  if (getStatusBP())
  {
    delay(1000);
    Serial.println("status is ok");

    if (getBloodPressure() == 1)
    {
      Serial.flush();
      Serial.println("taking measurements now");
      Serial.println();
      //delay(100);
      Serial.print("DYSPR");
      Serial.println(bloodPressureData.diastolic);
      //delay(100);
      Serial.print("SYSPR");
      Serial.println(bloodPressureData.systolic);
      //delay(100);
      Serial.print("BPPUL");
      Serial.println(bloodPressureData.pulse);

    }
  }
  delay(1000);
}
    
