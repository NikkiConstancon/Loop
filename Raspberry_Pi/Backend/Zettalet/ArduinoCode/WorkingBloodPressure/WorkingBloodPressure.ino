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

#include <MySignals.h>
#include "Wire.h"
#include "SPI.h"



void setup()
{

  Serial.begin(19200);
  MySignals.begin();

  MySignals.initSensorUART();
  MySignals.enableSensorUART(BLOODPRESSURE);
}

void loop()
{
      Serial.flush();
  Serial.println("in arduino loop");
  if (MySignals.getStatusBP())
  {
    delay(1000);
    Serial.println("status is ok");

    if (MySignals.getBloodPressure() == 1)
    {
      Serial.flush();
      Serial.println("taking measurements now");
      MySignals.disableMuxUART();
      Serial.println();
      //delay(100);
      Serial.print("DYSPR");
      Serial.println(MySignals.bloodPressureData.diastolic);
      //delay(100);
      Serial.print("SYSPR");
      Serial.println(MySignals.bloodPressureData.systolic);
      //delay(100);
      Serial.print("BPPUL");
      Serial.println(MySignals.bloodPressureData.pulse);
      MySignals.enableMuxUART();

    }
  }
  delay(1000);
}
    
