# Data Structures Project

## Abstract

This project is an exploration of probabilistic data structures and their performance characteristics. Specifically, it implements and compares the following data structures in Java:

- **Bloom Filter**: A probabilistic data structure that uses a crc hash and less hashing technique for adding elements.
- **Cuckoo Filter**: An efficient and practical data structure that uses two buckets for filter operations.
- **Concurrent Skip List**: A thread-safe version of a Skip List, allowing concurrent operations.

The program aims to benchmark the runtime and memory usage for the core operations: insert, search, and delete.

## Configuration

The configuration for running the tests is defined in a `config.properties` file. Here is a sample configuration:

```properties
# Options include ConcurrentSkipList, BloomFilter, CuckooFilter
datastructures.type = ConcurrentSkipList
# Input Location: Excel file with strings. Example data: clickstream data
input.location = C:\Users\balaj\Downloads\dataset\eshopclothing2008.csv
# Options Include insert, delete. By default, the input file will be loaded into the data structures.
operation = delete
# Number of operations for insert, delete
querySize = 50000
```

The program outputs the runtime and memory used for the given setup.

## Usage

To trial run the current setup:

1. Navigate to the `RUN` folder.
2. Modify the `config.properties` file as needed.
3. Execute the jar file with:
   ```shell
   java -jar -Dproperties.path="config.properties" ProbalisticDataStructures-1.0-SNAPSHOT.jar
   ```
4. For the `testFramework` Python script:
   ```shell
   python -m venv env
   env\Scripts\activate
   pip install -r requirements.txt
   python testFramework.py
   ```
5. To visualize the results with `generateGraphs.ipynb`:
   ```shell
   python -m venv env
   env\Scripts\activate
   pip install -r requirements.txt
   jupyter notebook generateGraphs.ipynb
   ```

## Setup

- Modify the Java code as desired.
- Run the command to create a jar file using Maven:
  ```shell
  mvn package
  ```
- Execute the jar with the modified config file.

## Contribution

Contributions to this project are more than welcome. If you are interested in contributing, please follow these steps:

1. Fork the repository.
2. Create a new branch for your feature (`git checkout -b feature/AmazingFeature`).
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4. Push to the branch (`git push origin feature/AmazingFeature`).
5. Open a Pull Request.

Before submitting a Pull Request, please ensure your code adheres to the project's coding standards and passes all tests. If you are proposing a new feature or change to existing functionality, please discuss it in an issue before submitting a Pull Request. This helps everyone to understand the change, see the value of it, and discuss potential improvements.
