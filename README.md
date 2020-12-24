# SmartHomeAutomation

<p> The project was developed as an assignment of the IoT course of the MSc in Computer Engineering (@ Univeristy of Pisa). 
  The main purpose of the project was to develop a WSN used with a Cloud Application in order to provide SmartHome Automation.
  
  Since during the assignment period there was the SARS-CoV2 pandemic, we didn't have the possibility to use real Hardware(sensors and actuators) to deploy the network. In order to test the code of sensors/actuators and their interacton with the Cloud/Client applications, we used Cooja to simulate our WSN.</p>

## Description of the system
<p>Our proposal was to build a WSN that allowed us to remotely control the Air Conditioning system and smart light system installed in our house.
 More in detail, through the use of a Client application, we were interested in receiving instantaneous update about home information ( Temperature and room's brightness ) and in performing several operations on that systems.
 
Our project is composed by three main elements:
<ul>
  <li> WSN: Wireless sensors networks composed by some sensors and actuators useful to collect data needed by the applications.</li>
  <li> Cloud Application: Always on application which receives periodically data from sensors and performs all the important computations.</li>
  <li> Client application: Application used by the user in order to see updates and info about his home and used to request operation on AirCond/Smart ligth system.</li>
</ul>

### How the application works?

<p>Our WSN is composed by 4 types of different nodes:</p>
<ul>
  <li>Temperature sensors</li>
  <li>Brigthness sensors</li>
  <li>Temperature actuator</li>
  <li>Smart ligth actuator</li>
</ul>

<p>Exploiting these 4 types of sensors we deployed 2 smart systems inside 2 different rooms:
  - Living room: temperature sensors and Air Conditioner actuator.
  - Bed room : brightness sensors and Smart light actuator.<p>
 
 ![picture](https://github.com/rafnocerino/SmartHomeAutomation/blob/main/pic.PNG)

<p> What can be done on the Smart AirConditioning system?</p>
<ul>
  <li>ObserveTemperature: to see all the sensed temperature information of a room of the current day (from the newest to the oldest).</li>
  <li>AirCondON/OFF: to set the AirCond ON/OFF specifying also the desired temperature(if ON).</li>
  <li>AutomaticTempControlON/OFF: to enable/disable the automatic temperature control. If the temperature in the room exceeds a threshold (specifyed by the user) the Smart system will activate the AirCond until the deired temp is reached.<li>
  <li>ProgramAirCondON/OFF: to enable/disable the programmed activation of the AirConditioner on a particular hour of the day and with a specifyed temperature.</li>
</ul>

<p> What can be done on the Smart Ligth system?</p>
<ul>
  <li>ObserveBrightness: p to see all the sensed brightness information of a room of the current day (from the newest to the oldest).</li>
  <li>SetLigthON/OFF: to activate/deactivate the smartLigth.</li>
  <li>SetLightOnwithBrigthness: to activate the smart ligth with the specified brigthness percentage.</li>
  <li>progressiveLigthON/OFF: to activate/deactivate the progressiveLigth mode specifying if ON the starting hour and the maximum desired brigthness that must be reached.</li>
</ul>

#### Full documentation
A complete overview of the application can be found inside the repository in the Powerpoint presentation. (https://github.com/rafnocerino/SmartHomeAutomation/blob/main/IoT%20Project.pdf)

#### Authors
<ul>
<li> Nocerino Raffaele - rafnocerino96@gmail.com</li>
</ul>

  
  
   
  
