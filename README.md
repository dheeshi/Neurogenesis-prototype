## **Neurogenesis Prototype: Curiosity-Driven Neural Network Simulation**

**💡 Project Overview**
This project is a research-oriented simulation that explores alternative models for Artificial General Intelligence (AGI) inspired by biological processes like neurogenesis and curiosity-driven learning.

Instead of relying on large-scale data and pre-built machine learning libraries, this simulation is built from the ground up to demonstrate how an artificial system can autonomously develop complex behaviors and adaptation strategies based purely on intrinsic motivation and real-time feedback.

The goal is to showcase the principles of algorithmic thinking, mathematical modeling, and core software engineering skills applied to complex AI problem-solving.

**🧠 Core Concepts & Simulation Logic**
The neural network and learning model are entirely custom-built in Java, focusing on the following principles:

1. Hebbian Learning (Synaptic Plasticity)
The system implements a form of Hebbian learning where synaptic connections (weights) are adjusted dynamically. Connections between neurons that frequently fire together are strengthened, mimicking how biological brains form and reinforce associative memories.

2. Curiosity-Driven Exploration
The simulation incorporates an intrinsic reward mechanism. When the network encounters a novel or unpredictable state—where its internal predictive model fails—it generates an internal "curiosity reward." This intrinsic motivation drives the agent to explore and learn in environments with sparse external feedback.

3. Real-Time Adaptation and Feedback
Neurons adjust their parameters (weights, thresholds) in real-time based on both extrinsic (external success/failure) and intrinsic (curiosity) feedback, effectively simulating continuous learning and decision-making within a controlled environment.

**🛠️ Technology Stack**
| **Category**              | **Technology**             | **Details**                                                                                                                                      |
|----------------------------|----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------|
| **Language & Platform**    | Java 17                    | Core implementation language, ensuring modern Java best practices.                                                                               |
| **User Interface (GUI)**   | JavaFX 22.0.2              | Used to build the graphical front-end, allowing for real-time visualization of network states, neuron firing, and learning progress.             |
| **Build Tool**             | Apache Maven               | Used for project management, compilation, and dependency handling (including the necessary JavaFX modules).                                      |
| **Core Software Design**   | Object-Oriented Programming (OOP) | Structured application around core domain objects: `Neuron`, `Synapse`, `Network`, and `Environment`.                                           |
| **Data Management**        | Data Structures (`List`, `Map`) | Extensive use of core Java data structures for managing network topology, input/output mappings, and weight arrays.                              |


**🚀 Getting Started**
This project requires Java 17 (or newer) and Apache Maven to run due to its JavaFX dependency configuration.

***Prerequisites***
Java Development Kit (JDK) 17+

Apache Maven

Running the Application
Clone the Repository:

git clone [(https://github.com/dheeshi/Neurogenesis-prototype.git)]
cd neurogenesis-prototype

Compile and Run with Maven:

The project is configured to run using the javafx:run goal provided by the JavaFX Maven plugin. This handles the module path setup required for JavaFX applications.

mvn clean javafx:run

The application window should launch, displaying the interactive simulation environment.
