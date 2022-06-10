# SkinSeer simulation analysis.

import skin_seer_parameters as parms
import numpy as np
from subprocess import call
import os
import sys

# Dataset.
dataset = []
dataset_file_name = "dataset.csv"

# Display mode?
display = False
if len(sys.argv) == 2 and sys.argv[1] == "-display":
   display = True

# Generate dataset.
cluster_frequencies_accum = []
num_cluster_frequencies = len(parms.cluster_frequencies)
for n in range(num_cluster_frequencies):
    cluster_frequencies_accum.append(parms.cluster_frequencies[n])
    if n > 0:
       cluster_frequencies_accum[n] += cluster_frequencies_accum[n - 1]
dataset_file = open(dataset_file_name, "w")
print("Dataset file:", dataset_file_name)
for scan in range(parms.dataset_size):
    print("Scan = ", scan)

    # Probabilistically choose a cluster.
    r = np.random.random(1)
    for cluster in range(num_cluster_frequencies):
        if r < cluster_frequencies_accum[cluster]:
           break

    # Write simulation parameters file.
    f = open('parameters.txt','w')
    if cluster == 0:
       print("NEVUS_VALID=false", file=f)
    else:
       v = np.random.normal(parms.feature_means[cluster - 1][0], parms.feature_std[0], 1)
       print("NEVUS_WIDTH=", v[0], file=f, sep='')
       v = np.random.normal(parms.feature_means[cluster - 1][1], parms.feature_std[1], 1)
       print("NEVUS_HEIGHT=", v[0], file=f, sep='')
       v = np.random.normal(parms.feature_means[cluster - 1][2], parms.feature_std[2], 1)
       print("NEVUS_PHOTON_ABSORPTION_PROBABILITY=", v[0], file=f, sep='')
       v = np.random.normal(parms.feature_means[cluster - 1][3], parms.feature_std[3], 1)
       print("NEVUS_PHOTON_SCATTER_PROBABILITY=", v[0], file=f, sep='')
    f.close()
    
    # Run simulation.
    if display == True:
       if os.name == 'nt':
          call(["..\\work\\run_display.bat", "-steps", str(parms.sim_steps), "-parameterFile", "parameters.txt", "-photonDetectorCountsFile", "counts.csv"])
       else:
          call(["../work/run_display.bat", "-steps", str(parms.sim_steps), "-parameterFile", "parameters.txt", "-photonDetectorCountsFile", "counts.csv"])
    else:
       if os.name == 'nt':
          call(["..\\work\\run_batch.bat", "-steps", str(parms.sim_steps), "-parameterFile", "parameters.txt", "-photonDetectorCountsFile", "counts.csv"])
       else:
          call(["../work/run_batch.bat", "-steps", str(parms.sim_steps), "-parameterFile", "parameters.txt", "-photonDetectorCountsFile", "counts.csv"])
          

    # Add scan photon counts to dataset.
    with open("counts.csv") as f:
         counts_arr = np.loadtxt(f, delimiter=",")
         counts = []
         counts_str = ""
         for n in range(len(counts_arr)):
             c = int(counts_arr[n])
             counts.append(c)
             counts_str += str(c) + ","
         dataset.append(counts)
         print(counts_str + "C" + str(cluster), file=dataset_file)

print("Dataset written")
dataset_file.close()
print("Dataset:")
print(dataset)

# Determine optimal clustering.
# Ref: http://www.scikit-yb.org/en/latest/api/cluster/elbow.html
from sklearn.cluster import KMeans
from yellowbrick.cluster import KElbowVisualizer
model = KMeans()
#visualizer = KElbowVisualizer(model, k=(1,5), timings=False)
visualizer = KElbowVisualizer(
    model, k=(2,10), metric='calinski_harabaz', timings=False
)
visualizer.fit(np.array(dataset))
visualizer.poof()





