# Airline Unreliability Analysis with Hadoop MapReduce ✈️

This project uses Hadoop MapReduce with Java to analyze airline flight data from 2017. It calculates an "unreliability score" for each airline. This score is based on how often an airline has significant departure delays (more than 15 minutes) and flight cancellations, compared to their total number of flights.

## 🎯 Objective

The main goal is to find out which airlines in 2017 had more operational problems, like frequent long delays or cancellations, when looking at all their flights.

## 📊 Dataset Used

- **Source:** Airline Delay and Cancellation Data (2009-2018) from Kaggle.
  - Kaggle Link: [https://www.kaggle.com/datasets/yuanyuwendymu/airline-delay-and-cancellation-data-2009-2018](https://www.kaggle.com/datasets/yuanyuwendymu/airline-delay-and-cancellation-data-2009-2018)
- **Specific File:** We used the data for the year `2017.csv`.
- **Columns We Looked At:**
  - `OP_CARRIER` (the airline's code)
  - `DEP_DELAY` (departure delay in minutes)
  - `CANCELLED` (a flag: 1.0 if cancelled, 0.0 if not)

## 💻 Technologies

- **Hadoop:** Version 3.3.6
- **Java:** OpenJDK 11 (this was used for development)
- **Maven:** For building the Java project and managing libraries.
- **WSL (Ubuntu):** The Hadoop environment was set up on Windows Subsystem for Linux.

## 📁 Project Code Structure

The Java code is organized as a standard Maven project:

```
airline_job_maven_project/
├── pom.xml                     # Maven configuration file
└── src/
    └── main/
        └── java/
            └── org/myorg/airline/ # Package for our Java classes
                ├── AirlineMapper.java   # The Mapper class
                ├── AirlineReducer.java  # The Reducer class
                └── AirlineDriver.java   # The main class to run the job
```

## 🛠️ Setup and How to Run

### 1. Basic Environment (WSL, Java, Hadoop)

- **WSL & Ubuntu:** Make sure you have WSL (Windows Subsystem for Linux) installed with a Linux distribution like Ubuntu. Keep it updated (`sudo apt update && sudo apt upgrade`).
- **SSH for Hadoop:** Hadoop uses SSH to communicate, even on a single machine.
  ```bash
  sudo apt install openssh-server
  ssh-keygen -t rsa  # Press Enter for defaults (no passphrase)
  cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
  chmod 600 ~/.ssh/authorized_keys
  sudo service ssh start
  ssh localhost # Test it
  ```
- **Java (JDK):** Hadoop needs Java.
  ```bash
  sudo apt install openjdk-11-jdk
  ```
- **Hadoop Installation (3.3.6):**
  1.  Download Hadoop 3.3.6:
      ```bash
      wget https://archive.apache.org/dist/hadoop/common/hadoop-3.3.6/hadoop-3.3.6.tar.gz
      tar -xzf hadoop-3.3.6.tar.gz
      mv hadoop-3.3.6 ~/hadoop-3.3.6
      ```
  2.  **Hadoop Environment Variables:** Add these to your `~/.bashrc` file. Open it with `nano ~/.bashrc`.

      ```bash
      export HADOOP_HOME=~/hadoop-3.3.6
      export HADOOP_CONF_DIR=$HADOOP_HOME/etc/hadoop
      export HADOOP_MAPRED_HOME=$HADOOP_HOME
      export HADOOP_COMMON_HOME=$HADOOP_HOME
      export HADOOP_HDFS_HOME=$HADOOP_HOME
      export HADOOP_YARN_HOME=$HADOOP_HOME
      export PATH=$PATH:$HADOOP_HOME/bin:$HADOOP_HOME/sbin

      export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
      ```

      Then, apply the changes: `source ~/.bashrc`

  3.  **Hadoop Configuration Files:** Edit these files in `$HADOOP_HOME/etc/hadoop/`.
      - `hadoop-env.sh`: Set `JAVA_HOME`.
        ```bash
        export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
        ```
      - `core-site.xml`:
        ```xml
        <configuration>
            <property>
                <name>fs.defaultFS</name>
                <value>hdfs://localhost:9000</value>
            </property>
        </configuration>
        ```
      - `hdfs-site.xml`:
        ```xml
        <configuration>
            <property>
                <name>dfs.replication</name>
                <value>1</value> <!-- For single-node setup -->
            </property>
            <!-- Optional: Define specific paths for namenode and datanode directories -->
            <!--
            <property>
                <name>dfs.namenode.name.dir</name>
                <value>file:///path/to/your/hadoop_store/hdfs/namenode</value>
            </property>
            <property>
                <name>dfs.datanode.data.dir</name>
                <value>file:///path/to/your/hadoop_store/hdfs/datanode</value>
            </property>
            -->
        </configuration>
        ```
      - `mapred-site.xml` (copy from `mapred-site.xml.template` if it doesn't exist: `cp mapred-site.xml.template mapred-site.xml`):
        ```xml
        <configuration>
            <property>
                <name>mapreduce.framework.name</name>
                <value>yarn</value>
            </property>
            <property>
                <name>yarn.app.mapreduce.am.env</name>
                <value>HADOOP_MAPRED_HOME=${HADOOP_HOME}</value>
            </property>
            <property>
                <name>mapreduce.map.env</name>
                <value>HADOOP_MAPRED_HOME=${HADOOP_HOME}</value>
            </property>
            <property>
                <name>mapreduce.reduce.env</name>
                <value>HADOOP_MAPRED_HOME=${HADOOP_HOME}</value>
            </property>
        </configuration>
        ```
      - `yarn-site.xml`:
        ```xml
        <configuration>
            <property>
                <name>yarn.nodemanager.aux-services</name>
                <value>mapreduce_shuffle</value>
            </property>
            <property>
                <name>yarn.resourcemanager.hostname</name>
                <value>localhost</value>
            </property>
        </configuration>
        ```

### 2. Maven for Building

- **Install Maven:**
  ```bash
  sudo apt install maven
  ```

### 3. Get the Project Code and Dataset

1.  **Clone this Project:**

    ```bash
    git clone https://github.com/NuranR/airline_unreliability
    cd airline_unreliability
    ```

2.  **Download the Dataset:** Get `2017.csv` from the Kaggle link provided in the "Dataset Used" section and save it to a known location on your WSL system (e.g., `~/Downloads/2017.csv`).

### 4. Build the Java Project

```bash
mvn clean package
```

This creates a JAR file (like `airline-unreliability-job-1.0-SNAPSHOT.jar`) in the `target/` folder.

### 5. Running the Analysis

1.  **Start Hadoop Services:**
    ```bash
    start-dfs.sh
    start-yarn.sh
    ```
2.  **Prepare HDFS:**
    - Create an input directory:
      ```bash
      hdfs dfs -mkdir -p /user/$(whoami)/airline_project/input
      ```
    - Upload the `2017.csv` dataset (replace `/path/to/your/2017.csv` with the actual local path to where you saved the CSV):
      ```bash
      hdfs dfs -put /path/to/your/2017.csv /user/$(whoami)/airline_project/input/
      ```
3.  **Run the MapReduce Job:**
    - Make sure you are in the directory where you cloned the project
    - If the HDFS output directory exists from a previous run, delete it:
      ```bash
      hdfs dfs -rm -r /user/$(whoami)/airline_project/output_unreliable_airlines
      ```
    - Run the job
      ```bash
      hadoop jar airline_job_maven_project/target/airline-unreliability-job-1.0-SNAPSHOT.jar /user/$(whoami)/airline_project/input/2017.csv /user/$(whoami)/airline_project/output_unreliable_airlines
      ```

## 🛑 Stopping Hadoop

When you're finished working with Hadoop:

```bash
stop-dfs.sh
stop-yarn.sh
```

To shut down the WSL instance itself, you can run `wsl --shutdown` in a Windows PowerShell or Command Prompt.
