import json
import matplotlib.pyplot as plt
import numpy as np

class ExperimentPlotter:

    def __init__(self, file_name, processes_instances, operation_instances):
        self.file_name = file_name
        self.avg_latency_get = {}
        self.avg_latency_put = {}
        self.processes_instances = processes_instances
        self.operation_instances = operation_instances

    def setup(self):
        for n in self.processes_instances:
            for m in self.operation_instances:
                self.average_execution(n+m)

    def average_execution(self, instance):

        file_path = self.file_name + instance + ".json" 

        with open(file_path, "r") as file:
            file_result = json.load(file)

            n = file_result["numberOfProcess"]
            operations = file_result["numberOfMessages"]
            results = file_result["results"]

            f = results[0]["numberOfFaultyProcesses"]
            num_experiments = len(results)

            put_latency = sum(res["writeLatency"] for res in results) / (num_experiments * (n-f) *  operations * 1_000_000)
            get_latency = sum(res["readLatency"] for res in results) / (num_experiments * (n-f) * operations * 1_000_000)

            if n not in self.avg_latency_put:
                self.avg_latency_put[n] = []
            if n not in self.avg_latency_get:
                self.avg_latency_get[n] = []

            self.avg_latency_put[n].append({"operations": operations, "avg_latency": put_latency})
            self.avg_latency_get[n].append({"operations": operations, "avg_latency": get_latency})

    def plot_bar_graphs(self, operation):
        plt.rcParams.update({'font.size': 20})
        data = self.avg_latency_get if operation == "get" else self.avg_latency_put
        
        plt.figure(figsize=(10, 5))
        width = 0.2  

        operation_labels = self.operation_instances
        indices = np.arange(len(operation_labels))
        
        for i, (n, results) in enumerate(data.items()):
            avg_latencies = [item["avg_latency"] for item in results]
            plt.bar(indices + i * width, avg_latencies, width=width, label=f'n={n}')
        
        plt.xlabel("Number of operations (M)")
        plt.ylabel("Latency (ms)")
        plt.xticks(indices + (width * (len(data) / 2)), operation_labels)
        plt.grid(axis='y')
        plt.legend()
        plt.show()

    def plot_bar_graphs_alt(self, operation):
        plt.rcParams.update({'font.size': 20})
        data = self.avg_latency_get if operation == "get" else self.avg_latency_put
        
        plt.figure(figsize=(10, 5))
        width = 0.2  

        operation_labels = self.operation_instances
        indices = np.arange(len(operation_labels))
        
        for i, (n, results) in enumerate(data.items()):
            avg_latencies = [item["avg_latency"] for item in results]
            plt.bar(indices + i * width, avg_latencies, width=width, label=f'n={n}')
        
        plt.xlabel("Number of operations (M)")
        plt.ylabel("Latency (ms)")
        plt.xticks(indices + (width * (len(data) / 2)), operation_labels)
        plt.grid(axis='y')
        plt.legend()
        plt.show()

    def plot_line_graphs(self, operation):
        plt.rcParams.update({'font.size': 72})
        data = self.avg_latency_get if operation == "get" else self.avg_latency_put

        plt.figure(figsize=(10, 5))
       
        
        for n, result in data.items():
            operations = [item["operations"] for item in result]
            avg_latency = [item["avg_latency"] for item in result]
            plt.plot(operations, avg_latency, marker='o', linestyle='-', label=f'n={n}')
        
        plt.xlabel("Number of operations")
        plt.ylabel("Total Latency (ms)")
        plt.title(operation.capitalize() + " method performance: Average latency vs. Number of operations (M)")
        plt.grid(True)
        plt.legend()
        plt.show()


# Carregar os dados do arquivo JSON
process_instances = ["3", "10", "100"]
operation_instances = process_instances

exp_plotter = ExperimentPlotter("results", process_instances, operation_instances)
exp_plotter.setup()

exp_plotter.plot_bar_graphs("get")
exp_plotter.plot_bar_graphs("put")
