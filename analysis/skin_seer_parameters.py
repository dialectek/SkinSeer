# Nevi generation parameters.
# Types: none, benign and melanoma.
# Statistical properties (see SkinSeerSim.java).
# NEVUS_WIDTH
# NEVUS_HEIGHT
# NEVUS_PHOTON_ABSORPTION_PROBABILITY
# NEVUS_PHOTON_SCATTER_PROBABILITY

# Refs:
# https://scikit-learn.org/stable/modules/generated/sklearn.datasets.make_blobs.html
# https://scikit-learn.org/stable/auto_examples/plot_anomaly_comparison.html#sphx-glr-auto-examples-plot-anomaly-comparison-py

# Cluster frequencies.
# [ <no nevi> <benign nevi> <melanoma nevi> ]
cluster_frequencies = [ 0.75, 0.2, 0.05 ]

# Feature means for the benign and melanoma cases.
feature_means = [ [ 40.0, 80.0, 0.1, 0.1 ], [ 60.0, 120.0, 0.25, 0.25 ] ]

# Feature standard deviations.
feature_std = [ 5.0, 10.0, 0.01, 0.01 ]

# Simulation steps.
sim_steps = 500

# Generated dataset size.
dataset_size = 50
