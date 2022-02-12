#!/bin/bash

conda create -n spark-flight -c conda-forge python=3.9 pyarrow=6.0.1 pyspark=3.2.1 pandas

# conda activate spark-flight
# [optional] pip install tensorflow tensorflow-io