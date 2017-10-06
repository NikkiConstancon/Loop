/*
  Software serial multple serial test

 Receives from the hardware serial, sends to software serial.
 Receives from software serial, sends to hardware serial.

 The circuit:
 * RX is digital pin 10 (connect to TX of other device)
 * TX is digital pin 11 (connect to RX of other device)

 Note:
 Not all pins on the Mega and Mega 2560 support change interrupts,
 so only the following can be used for RX:
 10, 11, 12, 13, 50, 51, 52, 53, 62, 63, 64, 65, 66, 67, 68, 69

 Not all pins on the Leonardo and Micro support change interrupts,
 so only the following can be used for RX:
 8, 9, 10, 11, 14 (MISO), 15 (SCK), 16 (MOSI).

 created back in the mists of time
 modified 25 May 2012
 by Tom Igoe
 based on Mikal Hart's example

 This example
uint8_t getPulsioximeterMicro() code is in the public domain.

 */
#include <SoftwareSerial.h>

struct pulsioximeterDataStruct
{
  uint8_t BPM;
  uint8_t O2;
};
pulsioximeterDataStruct pulsioximeterData;

uint8_t spir_measures;
struct spirometerDataStruct
{
  uint8_t spir_year;
  uint8_t spir_month;
  uint8_t spir_day;
  uint8_t spir_hour;
  uint8_t spir_minutes;
  uint16_t spir_pef;
  uint16_t spir_fev;  //Dividir entre 100 para obtener litros
};
spirometerDataStruct  spirometerData[1];

SoftwareSerial mySerial(8, 9); // RX, TX
SoftwareSerial spirometerSerial(10, 11); // RX, TX

void setup() 
{
  // Open serial communications and wait for port to open:
  Serial.begin(115200);

  // set the data rate for the SoftwareSerial port
  mySerial.begin(115200);
  spirometerSerial.begin(9600);

  
  //while (getStatusSpiro() == 0)
  //{
  //  delay(100);
  //  Serial.print("hello");
  //}
  Serial.println("Spirometer is Onnnn");
}

void loop() 
{ 
  pulsioximeterData.BPM = 0;
  pulsioximeterData.O2 = 0;
  Serial.print("status is: ");
  Serial.println(getPulsioximeterMicro(), HEX);
  Serial.print("PULSE");
  Serial.println(pulsioximeterData.BPM);
  Serial.print("OXYO2");
  Serial.println(pulsioximeterData.O2);
  delay(500);
  
  // Spirometer data
  getSpirometer();
  Serial.print(F("Number of measures:"));
  Serial.println(spir_measures);
  Serial.println();

  for (int i = 0; i < spir_measures; i++)
  {
    
    //Serial.print("SPIAF");
    //Serial.println(spirometerData[i].spir_pef);
    //delay(250);    

    Serial.print("SPIVL");
    //Serial.println(spirometerData[i].spir_fev);
    //delay(250);
  }
}


uint8_t getStatusPulsioximeterGeneral()
{
  //habilito UART y vacio contenido de la uart anterior

  //control.SerialFlush();

  //Envio trama inicial
  spo2_micro_send_init_frame();
  
  uint8_t buffer_uart[100];
  memset(buffer_uart, 0x00, sizeof(buffer_uart));
  uint8_t i = 0;
  bool exit_while = 0;
  unsigned long previous = millis();
  //capturo buffer tras haberle enviado el mensaje de inicio
  while (((millis() - previous) < 5) && exit_while == 0)
  {
    if (mySerial.available() > 0)
    {
      buffer_uart[i] = mySerial.read();
      i++;
      //No escribir mas alla de mi buffer, chafaria otras cosas indeterminadas
      if (i == 100)
      {
        exit_while = 1;
      }
    }

    //avoid millis overflow problem after approximately 50 days
    if ( millis() < previous ) previous = millis();
  }

  /*
    for (int i = 0; i < 100; i++)
    {
    Serial.print("Byte ");
    Serial.print(i);
    Serial.print(":");
    Serial.println(buffer_uart[i], HEX);

    }
  */

  //Primero compruebo si el buffer esta vacio por completo
  bool empty_buffer = 1;

  for (uint8_t j = 0; j < 100; j++)
  {
    if (buffer_uart[j] != 0)
    {
      empty_buffer = 0;
      break;
    }
  }

  if (empty_buffer == 1)
  {
    return 0;
  }


  //No buscar hasta el final del buffer, si la cadena ocupa 4 bytes, solo buscar hasta final menos 4
  for (uint8_t j = 0; j < 100 - 4; j++)
  {
    //Mensaje de inicio que contesta spo2 micro
    if (buffer_uart[j] == 0x0C)
    {
      if (buffer_uart[j + 1] == 0x80)
      {
        if (buffer_uart[j + 2] == 0x0C)
        {
          if (buffer_uart[j + 3] == 0x80)
          {
            //Serial.println("Razon 1");
            return 1;
          }
        }
      }
    }


    //Segundo Mensaje de inicio que contesta spo2 micro
    if (buffer_uart[j] == 0x02)
    {
      if (buffer_uart[j + 1] == 0x80)
      {
        if (buffer_uart[j + 2] == 0x80)
        {
          if (buffer_uart[j + 3] == 0xa0)
          {
            //Serial.println("Razon 2");
            return 1;
          }
        }
      }
    }



    //Tercer Mensaje que contesta spo2 micro si ya esta iniciado de antes
    //entender un mensaje de dato que de micro directamente
    //no solo el de entrada, porque podria pillarlo arrancado ya
    
    //Init byte in real time data package is 0x01 (bit 7 is 0)
    //The next 8 bytes always have bit 7 = 1

    //Detect init byte of a real time data package
        if (buffer_uart[j] == 0x01)
        {
          if ((buffer_uart[j + 1] & 0b10000000) == 0x80) //bit 7 must be 1
      { 
        
       if ((buffer_uart[j + 2] & 0b10000000) == 0x80) //bit 7 must be 1
       {
        if ((buffer_uart[j + 3] & 0b10000000) == 0x80) //bit 7 must be 1
          { 
          
          if ((buffer_uart[j + 4] & 0b10000000) == 0x80) //bit 7 must be 1
              { 
            
              if ((buffer_uart[j + 5] & 0b10000000) == 0x80) //bit 7 must be 1
                  { 
              
              if ((buffer_uart[j + 6] & 0b10000000) == 0x80) //bit 7 must be 1
                       { 
                 
                 if ((buffer_uart[j + 7] & 0b10000000) == 0x80) //bit 7 must be 1
                           { 
                   
                   if ((buffer_uart[j + 8] & 0b10000000) == 0x80) //bit 7 must be 1
                               { 
         
                     //Detect init byte of the next real time data package
                     if (buffer_uart[j + 9] == 0x01)
                     {
                       //Serial.println("Razon 3");
                                            return 1;
                     }
                   }
                 } 
               }       
            }        
          }          
          }
       } 
       }
          
        }
  
  }


  //Si despues del bucle anterior sigo aqui, es que hay algo no reconocido conectado
  //podria ser el sensor mini asi que voy a comprobarlo

  memset(buffer_uart, 0x00, sizeof(buffer_uart));
  i = 0;
  exit_while = 0;

  //capturo buffer
  while (((millis() - previous) < 100) && exit_while == 0)
  {
    if (mySerial.available() > 0)
    {
      buffer_uart[i] = mySerial.read();
      i++;
      //No escribir mas alla de mi buffer, chafaria otras cosas indeterminadas
      if (i == 100)
      {
        exit_while = 1;
      }
    }

    //avoid millis overflow problem after approximately 50 days
    if ( millis() < previous ) previous = millis();
  }

  /*
    for (int i = 0; i < 100; i++)
    {
    Serial.print("Byte ");
    Serial.print(i);
    Serial.print(":");
    Serial.println(buffer_uart[i], HEX);

    }
  */

  //Busco la estructura de paquete del SPO2 mini
  for (uint8_t j = 0; j < 100 - 4; j++)
  {
    //Hago mascara para solo mirar el primer bit del byte

    //si es "1", chequear el del siguiente byte
    if ((buffer_uart[j] & 0b10000000) != 0)
    {
      //si entro, es que si era 1

      //si es "0", chequear el del siguiente byte
      if ((buffer_uart[j + 1] & 0b10000000) == 0)
      {
        //si entro, es que si era 0
       
        //si es "0", chequear el del siguiente byte
        if ((buffer_uart[j + 2] & 0b10000000) == 0)
        {
          //si entro, es que si era 0
   
          //si es "0", chequear el del siguiente byte
          if ((buffer_uart[j + 3] & 0b10000000) == 0)
          {
            //si entro, es que si era 0

            //si es "0", chequear el del siguiente byte
            if ((buffer_uart[j + 4] & 0b10000000) == 0)
            {
              //si entro, es que si era 0
            
              //si es "0", chequear el del siguiente byte
              if ((buffer_uart[j + 5] & 0b10000000) != 0)
              {
                //si entro, es que si era 1, he encontrado un paquete entero de spo2 mini
                return 2;
              }
            }
          }
        }
      }
    }
  }

  return 0;
}


void spo2_micro_send_init_frame()
{
  byte m1[] = {0x7D, 0x81};
  
  ///////////////
  mySerial.write(m1, sizeof(m1));
  mySerial.write(0xA1);
  for (int i = 0; i < 6; i++)
  {
    mySerial.write(0x80);
    //Serial.println("writing 0x80");
  }
  ///////////////
}

uint8_t getPulsioximeterMicro()
{
  const uint8_t BUFFER_TOTAL_NUMBER = 200;
  uint8_t buffer_uart[BUFFER_TOTAL_NUMBER];
  memset(buffer_uart, 0x00, sizeof(buffer_uart));
  uint8_t i = 0;
  bool exit_while = 0;
  unsigned long previous = millis();
  
  
 
  //Envio trama inicial
  spo2_micro_send_init_frame();
  

  //capturo buffer
  while (((millis() - previous) < 100) && exit_while == 0)
  {
    if (mySerial.available() > 0)
    {
    //Serial.println("Leo dato");
      buffer_uart[i] = mySerial.read();
      //Juan
      Serial.println(buffer_uart[i]);
      i++;
      //No escribir mas alla de mi buffer, chafaria otras cosas indeterminadas
      if (i == BUFFER_TOTAL_NUMBER)
      {
        exit_while = 1;
        //Serial.println("salgo por tope");
      }
    }

    //avoid millis overflow problem after approximately 50 days
    if ( millis() < previous ) previous = millis();
  }

  
  /*
    for (int i = 0; i < BUFFER_TOTAL_NUMBER; i++)
    {
    Serial.print("Byte ");
    Serial.print(i);
    Serial.print(":");
    Serial.println(buffer_uart[i], HEX);

    }
  */

  
  bool data_found = 0;
  uint8_t j = 0;
  
  
  for (j = 0; j < BUFFER_TOTAL_NUMBER - 9; j++)
  {
    //Init byte in real time data package is 0x01 (bit 7 is 0)
    //The next 8 bytes always have bit 7 = 1

    //Detect init byte of a real time data package
        if (buffer_uart[j] == 0x01)
        {
      
          if ((buffer_uart[j + 1] & 0b10000000) == 0x80) //bit 7 must be 1
      { 
       if ((buffer_uart[j + 2] & 0b10000000) == 0x80) //bit 7 must be 1
       {
         
        if ((buffer_uart[j + 3] & 0b10000000) == 0x80) //bit 7 must be 1
          { 
          
          if ((buffer_uart[j + 4] & 0b10000000) == 0x80) //bit 7 must be 1
              { 
            
              if ((buffer_uart[j + 5] & 0b10000000) == 0x80) //bit 7 must be 1
                  { 
              
              if ((buffer_uart[j + 6] & 0b10000000) == 0x80) //bit 7 must be 1
                       { 
                 
                 if ((buffer_uart[j + 7] & 0b10000000) == 0x80) //bit 7 must be 1
                           { 
                   
                   if ((buffer_uart[j + 8] & 0b10000000) == 0x80) //bit 7 must be 1
                               { 
         
                     //Detect init byte of the next real time data package
                     if (buffer_uart[j + 9] == 0x01)
                     {
                      data_found = 1;
                      break;
                     }
                  }
                 } 
               }       
            }        
          }          
          }
       } 
       }
          
        }
  } 
  
  //Serial.print("j:");
  //Serial.println(j);
  
  if(data_found == 1)
  {
   uint8_t sensorValueBPM;
      uint8_t sensorValueO2;
    
    sensorValueBPM = buffer_uart[j+5] - 128;
    sensorValueO2 = buffer_uart[j+6]  - 128;

    if ((sensorValueO2 >= 50) && (sensorValueO2 <= 100) && (sensorValueBPM >= 30) && (sensorValueBPM <= 200))
    {
    pulsioximeterData.BPM = sensorValueBPM;
    pulsioximeterData.O2 = sensorValueO2;

    return 1;
    }
    else
    {
    return 2;
    }
  }
  else
  {
   return 0;
  }
}

//Spirometer functions
void getSpirometer()
{
  char data;


  spirometerSerial.println(F("MB+1200RCD?"));
  spir_measures = 0;
  delay(300);

  while (spirometerSerial.available() > 0 )
  {

    data = spirometerSerial.read();
      //Serial.println("1: " + data);

    if (data == 'O')
    {
      data = spirometerSerial.read();
     // Serial.println("2: " + data);
      if (data == 'K')
      {
        data = spirometerSerial.read();
      //Serial.println("3: " + data);
        if (data == 0xD)
        { //CR
          data = spirometerSerial.read();
      //Serial.println("4: " + data);
          if (data == 0xA)
          { //LF

            bool flag = 0;
            while (flag == 0)
            {
              data = spirometerSerial.read(); //read 2
    //  Serial.println("5: " + data);
              if (data == 50)
              {
                readSpiroMeasure(spir_measures);
                spir_measures++;


                if (spir_measures == 7)
                {
                  spirometerSerial.println(F("Limite 7 medidas!"));
                  flag = 1;
                }
              }
              else
              {
                flag = 1;
              }
            }

          }
        }
      }

    }

  }

#if MYSIGNALS_DEBUG > 0
  Serial.println();
  Serial.println(F("-----------------> SPIROMETER SENSOR"));
#endif
}

void readSpiroMeasure(uint8_t _spir_measures)
{
  spirometerSerial.read(); //read 0
  uint8_t spir_year_tens = spirometerSerial.read() - 48;
  uint8_t spir_year_units = spirometerSerial.read() - 48;
  spirometerData[_spir_measures].spir_year = (spir_year_tens * 10) + spir_year_units;

  spirometerSerial.read(); //read -

  uint8_t spir_month_tens = spirometerSerial.read() - 48;
  uint8_t spir_month_units = spirometerSerial.read() - 48;
  spirometerData[_spir_measures].spir_month = (spir_month_tens * 10) + spir_month_units;

  spirometerSerial.read(); //read -

  uint8_t spir_day_tens = spirometerSerial.read() - 48;
  uint8_t spir_day_units = spirometerSerial.read() - 48;
  spirometerData[_spir_measures].spir_day = (spir_day_tens * 10) + spir_day_units;

  spirometerSerial.read(); //read espace

  uint8_t spir_hour_tens = spirometerSerial.read() - 48;
  uint8_t spir_hour_units = spirometerSerial.read() - 48;
  spirometerData[_spir_measures].spir_hour = (spir_hour_tens * 10) + spir_hour_units;

  spirometerSerial.read(); //read :

  uint8_t spir_minutes_tens = spirometerSerial.read() - 48;
  uint8_t spir_minutes_units = spirometerSerial.read() - 48;
  spirometerData[_spir_measures].spir_minutes = (spir_minutes_tens * 10) + spir_minutes_units;

  spirometerSerial.read(); //read :
  spirometerSerial.read(); //read seconds
  spirometerSerial.read(); //read seconds
  spirometerSerial.read(); //read :
  spirometerSerial.read(); //read espace

  uint8_t spir_pef_high = spirometerSerial.read() - 48;
  uint8_t spir_pef_medium = spirometerSerial.read() - 48;
  uint8_t spir_pef_low = spirometerSerial.read() - 48;
  spirometerData[_spir_measures].spir_pef = (spir_pef_high * 100) + (spir_pef_medium * 10) + spir_pef_low;

  spirometerSerial.read(); //read L
  spirometerSerial.read(); //read /
  spirometerSerial.read(); //read m
  spirometerSerial.read(); //read i
  spirometerSerial.read(); //read n
  spirometerSerial.read(); //read -

  uint8_t spir_fev_high = spirometerSerial.read() - 48;
  uint8_t spir_fev_medium = spirometerSerial.read() - 48;
  uint8_t spir_fev_low = spirometerSerial.read() - 48;
  spirometerData[_spir_measures].spir_fev = (spir_fev_high * 100) + (spir_fev_medium * 10) + spir_fev_low;

  spirometerSerial.read(); //read L
  spirometerSerial.read(); //read CR
  spirometerSerial.read(); //read LF
}

uint8_t getStatusSpiro()
{

  while (spirometerSerial.available() > 0 )
  {
    
      Serial.println(spirometerSerial.read());
  }
  uint8_t data = 0x00;
  spirometerSerial.println(F("MB+CONNECT"));
  delay(100);

  while (spirometerSerial.available() > 0 )
  {

    data = spirometerSerial.read();
    if (data == 'O')
    {
      data = spirometerSerial.read();
      if (data == 'K')
      {
        return 1;
      }
      else
      {
        return 0;
      }
    }
    else
    {
      return 0;
    }
  }
  return 0;
}
