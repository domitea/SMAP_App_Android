#include<Stepper.h>

#define LED_PIN 3
#define BUTTON_PIN 2
#define OPTO_PIN 7

#define STEPS_PER_REV 200

int btnPressed;
int optoLastState;
int homeSwitchEnabled;

char *message = new char[16]();
int messageIndex = 0;

float dreamAngle = 0;
float stepAngle = 360.0 / STEPS_PER_REV;
float error = 0;

float homeAngleOffset = 0.0;

unsigned long lastOptoInterrupt;
unsigned long lastButtonInterrupt;

int motor_run = HIGH;

Stepper stepper(STEPS_PER_REV, 41, 43, 45, 47);

void setup() {
  // put your setup code here, to run once:
  //Serial.begin(9600);
  //Serial.println("Running");

  pinMode(15, INPUT_PULLUP);
  Serial3.begin(9600);

  pinMode(LED_PIN, OUTPUT);
  
  pinMode(BUTTON_PIN, INPUT);
  pinMode(OPTO_PIN, INPUT);
  
  stepper.setSpeed(20);
  optoLastState = digitalRead(OPTO_PIN);
}

void loop() {
  // put your main code here, to run repeatedly:
  if (digitalRead(BUTTON_PIN) && !btnPressed) {
    Serial3.print("DISC!");
    btnPressed = 1;
  }
  
  if (digitalRead(OPTO_PIN) == HIGH)
  {
    if (optoLastState == LOW)
    {
      optoLastState = HIGH;
      Serial3.print("STEP!");
    }
  }
  else if (digitalRead(OPTO_PIN) == LOW)
  {
    if (optoLastState == HIGH)
    {
      optoLastState = LOW;
      Serial3.print("STEP!");
    }
  }
  
  while (Serial3.available()){
    char data = Serial3.read();
    
    if (data != '!') {
      message[messageIndex] = data;
      messageIndex++;
    }
    else
    {
      //Serial.println(message);
      messageIndex = 0;
      parseMessage(message);
      delete [] message;
      message = new char[16]();
    }
  }
  /*while (Serial.available()) {
    char data = Serial.read();
    
    if (data != '!') {
      message[messageIndex] = data;
      messageIndex++;
    }
    else
    {
      Serial.println(message);
      messageIndex = 0;
      parseMessage(message);
      delete [] message;
      message = new char[16]();
    }
  } */

  if (abs(dreamAngle) > stepAngle/2.0 ) {
    if (dreamAngle > 0) {
      if (error > 0 && !motor_run) {
        dreamAngle += abs(error);
        motor_run = HIGH;
      } else if (error < 0 && !motor_run) {
        dreamAngle -= abs(error);
        motor_run = HIGH;
      }
      dreamAngle -= stepAngle;
      if (homeSwitchEnabled) {
        homeAngleOffset -= stepAngle;
      }
      stepper.step(1);
      Serial3.print("GP!");
      //Serial.println("step left");
      //Serial.println(dreamAngle);
    } else if (dreamAngle < 0) {
      if (error > 0 && !motor_run) {
        dreamAngle -= abs(error);
        motor_run = HIGH;
      } else if (error < 0 && !motor_run) {
        dreamAngle += abs(error);
        motor_run = HIGH;
      }
      dreamAngle += stepAngle;
      if (homeSwitchEnabled) {
        homeAngleOffset += stepAngle;
      }
      stepper.step(-1);
      Serial3.print("GM!");
      //Serial.println("step right");
      //Serial.println(dreamAngle);
    } else {
      return;
    }
  }
}

void parseMessage(char data[]) {
    if (data[0] == 'h' && data[1] == 's') {
      //Serial.println("home position setted");
      homeSwitchEnabled = HIGH;
      homeAngleOffset = 0;
    }
    if (data[0] == 'h' && data[1] == 'r') {
      //Serial.println("home position removed");
      homeSwitchEnabled = LOW;
    }
    if (data[0] == 'h' && data[1] == 'h') {
      if (homeSwitchEnabled) {
        //Serial.println("docking to home");
        dreamAngle = homeAngleOffset;
      } else {
        //Serial.println("home not defined");
      }
    }
    if (data[0] == 'd' && data[1] == 'l') {
      //Serial.println("toggling led");
      toggleLed(LED_PIN);
    }
    if (data[0] == 'd' && data[1] == 'r') {
      //Serial.println("state reset");
      btnPressed = 0;
      error = 0;
      homeAngleOffset = 0;
    }
    if (data[0] == 'd' && data[1] == 's') {
      //Serial.println("stepper work");
      stepper.step(STEPS_PER_REV);
      delay(500);
    }
    if (data[0] == 'm' && data[1] == 'r') {
      char number[5] = { data[2], data[3], data[4], data[5], '\0' };
      int angle = atoi(number);
      //Serial.println(angle);
      moveMotor(angle);
    }
    if (data[0] == 'm' && data[1] == 'a') {
      char number[5] = { data[2], data[3], data[4], data[5], '\0' };
      int angle = atoi(number);
      if (abs(angle) > 360) {
         angle = angle % 360;
      }
      if (angle > 180) {
        angle -= 360;
      } else if (angle < -180) {
        angle += 360;
      }
      //Serial.println(angle);
      moveMotor(angle);
    }
}

void moveMotor(int angle){
  error = dreamAngle;
  dreamAngle = 0.0f;
  dreamAngle = angle;
  motor_run = LOW;
}

void toggleLed(int pin)
{
  digitalWrite(pin, !digitalRead(pin));
}
