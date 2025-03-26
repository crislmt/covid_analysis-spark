# Spark Covid Analysis

## Introduction
Spark Covid Analysis is a Java-based application that leverages Apache Spark to analyze historical COVID-19 case data. The project processes data collected by the European Centre for Disease Prevention and Control (ECDC) from March 2020 to December 14, 2020. It calculates key statistical metrics to track the spread of COVID-19 over time.

### Features
- Computes the seven-day moving average of new reported cases for each country and each day.
- Calculates the percentage increase (compared to the previous day) of the seven-day moving average.
- Identifies the top 10 countries with the highest percentage increase in the seven-day moving average for each day.

## Installation
The following steps guide you through installing and running the project on both local and distributed environments (Unix machines).

### Prerequisites
- Apache Spark (latest version)
- Tested against Java 11 (recommended version)

### Steps for Setup (Master Node)
1. Download and extract Apache Spark:
   ```sh
   tar -xvzf spark-<version>.tgz
   ```
2. Navigate to the extracted directory and start the master process:
   ```sh
   export SPARK_MASTER_HOST="<your-machine-address>"
   ./sbin/start-master.sh
   ./sbin/start-worker spark://<your-machine-address>:7077
   ```
   If running locally, replace `<your-machine-address>` with `127.0.0.1`.
3. Submit the Spark job:
   ```sh
   ./bin/spark-submit <path-to-jar> spark://<your-machine-address>:7077 <path-to-data>
   ```
   Ensure the data format matches the one provided by ECDC.

### Steps for Worker Nodes
1. Download and extract Apache Spark.
2. Navigate to the extracted directory and configure the worker process:
   ```sh
   export SPARK_MASTER_HOST="<master-node-address>"
   ./sbin/start-worker.sh spark://<master-node-address>:7077
   ```

## Approach and Assumptions
- The data from ECDC is structured, making it ideal for analysis using Spark SQL.
- Using SQL queries simplifies the analysis and allows for Spark's built-in optimizations.

## Implementation
### Data Setup
- The dataset is loaded into a Spark DataFrame (`Dataset<Row>`), with unnecessary columns dropped for performance optimization.

### Computation Steps
1. **Seven-day moving average of new reported cases:**
   - Data is partitioned by country and ordered by date using a sliding window.
2. **Percentage increase of the seven-day moving average:**
   - A new column is created to store the moving average of the previous day.
   - The percentage change is calculated based on these values.
3. **Top 10 countries with the highest increase:**
   - Data is partitioned by date and sorted by the percentage increase.
   - The top 10 countries for each day are selected using the `rank()` function.

## Testing
Performance tests were conducted using different hardware configurations:
- **Official dataset:** Performance remained stable across different core configurations.
- **Large dataset (100x original size):** Increasing the number of cores from 1 to 4 significantly improved performance.

## Contributors
- Bruno Morelli
- Cristian Lo Muto
- Vincenzo Martelli

## License
This project is open-source and distributed under the MIT License.

