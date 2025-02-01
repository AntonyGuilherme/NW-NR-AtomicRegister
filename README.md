# Robust Key-Value Store

## Overview

This project implements a fault-tolerant distributed key-value store using the ABD algorithm for multi-writer, multi-reader atomic registers. It ensures linearizability and liveness despite failures. The implementation uses the Akka framework in Java, where processes communicate asynchronously.

## How It Works

The system consists of N processes, each uniquely identified and connected through reliable asynchronous channels. Up to f < N/2 processes may fail by crashing.

## It supports two operations:

* put(k, v): Updates the value associated with key k, ensuring its propagation to a majority of processes.

* get(k): Retrieves the most recent value of key k from a majority of processes.

* Each process maintains a local copy of values with timestamps.

* Write Operation (put(k, v)): Reads current timestamps from a majority, updates the timestamp and writes the new value, then waits for acknowledgments from a majority before completing.

* Read Operation (get(k)): Reads values from a majority, selects the most recent value, writes back the value to ensure consistency before returning it.

* The system remains functional as long as a majority of processes remain active.

## Testing & Performance Analysis

The system is tested for configurations with N = {3, 10, 100} and M = {3, 10, 100}, where a subset of f processes crash randomly. Performance metrics include latency and linearizability validation.

##  Dispatcher Configuration

Akka uses a dispatcher (thread pool) to schedule actor execution.

akka.actor.default-dispatcher {
  fork-join-executor {
    parallelism-min = 2
    parallelism-max = 8
  }
}

