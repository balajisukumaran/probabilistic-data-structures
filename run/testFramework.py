import subprocess
import psutil
import json
import re

# The path to your JAR file
jar_path = "ProbalisticDataStructures-1.0-SNAPSHOT.jar"
properties_filename = "config.properties"

# Specification for the nested loop
query_sizes = range(1000000, 2100000, 100000)
operations = ['insert', 'search', 'delete']
data_structures = {
    'ConcurrentSkipList': ['insert', 'search', 'delete'],
    'BloomFiler': ['insert', 'search', 'delete'],
    'CuckooFiler': ['insert', 'search', 'delete']
}

# Function to get current CPU usage
def get_cpu_usage():
    return psutil.cpu_percent(interval=1)

# List to hold the results
results = []

# Nested loop as per specification
for query_size in query_sizes:
    for ds_type, ops in data_structures.items():
        for op in ops:
            # Skip the delete operation for Bloom filters
            if ds_type == 'BloomFiler' and op == 'delete':
                continue
            # Read the current contents of the properties file
            with open(properties_filename, 'r') as file:
                lines = file.readlines()

            # Update the config file with the new settings
            with open(properties_filename, 'w') as file:
                for line in lines:
                    if line.strip().startswith('operation'):
                        file.write(f'operation = {op}\n')
                    elif line.strip().startswith('querySize'):
                        file.write(f'querySize = {query_size}\n')
                    elif line.strip().startswith('datastructures.type'):
                        file.write(f'datastructures.type = {ds_type}\n')
                    else:
                        file.write(line)

            # Start CPU usage recording
            cpu_usage_before = get_cpu_usage()

            # Run the JAR file and capture its output
            process = subprocess.Popen(['java', '-jar', '-Dproperties.path=config.properties', jar_path],
                                       stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)

            # Wait for the process to complete and capture output
            stdout, stderr = process.communicate()

            # End CPU usage recording
            cpu_usage_after = get_cpu_usage()

            # Calculate average CPU usage
            average_cpu_usage = (cpu_usage_before + cpu_usage_after) / 2

            # Extract execution time and memory used from stdout
            execution_time = None
            memory_used = None
            if stdout:
                time_match = re.search(r'Execution time: (\d+) ms', stdout)
                memory_match = re.search(r'Memory used: (\d+) MB', stdout)
                if time_match:
                    execution_time = int(time_match.group(1))
                if memory_match:
                    memory_used = int(memory_match.group(1))

            # Append the results
            results.append({
                'querySize': query_size,
                'operation': op,
                'datastructures_type': ds_type,
                'execution_time_ms': execution_time,
                'memory_used_mb': memory_used,
                'cpu_usage_percent': average_cpu_usage
            })

# Convert to JSON
results_json = json.dumps(results, indent=4)
with open('output.json', 'w') as file:
    file.write(results_json)