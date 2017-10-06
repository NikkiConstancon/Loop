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
SoftwareSerial mySerial(8, 9); // RX, TX

uint8_t getPulsioximeterMicro();
void spo2_micro_send_init_frame();

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
void setup() {
  // Open serial communications and wait for port to open:
  Serial.begin(9600);


  Serial.println("Goodnight moon!");

  // set the data rate for the SoftwareSerial port
  mySerial.begin(115200);
}

void loop() { // run over and over

  Serial.print("status is: ");
  Serial.println(getStatusPulsioximeterGeneral(), HEX  );
  getPulsioximeterMicro();
  //if (mySerial.available())
  //{
  //  Serial.println("gotsomething");
  //spo2_micro_send_init_frame();
   // while (mySerial.available()) 
   // { 
      //Serial.println(mySerial.read());
   //}

    
    //  Serial.println();
  //}

//////////////////
//mySerial.println("heyyy");
    Serial.print("PULSE");
    Serial.println(pulsioximeterData.BPM);
    Serial.print("OXYO2");
    Serial.println(pulsioximeterData.O2);
  delay(500);
}
