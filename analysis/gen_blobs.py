#https://scikit-learn.org/stable/modules/generated/sklearn.datasets.make_blobs.html

from sklearn.datasets.samples_generator import make_blobs
from matplotlib import pyplot as plt
import pandas as pd

# Generate features (X) and labels (y)
X, y = make_blobs(n_samples=200, centers=4, n_features=2)
print(X)
print(y)

# Group the data by labels
Xy = pd.DataFrame(dict(x1=X[:,0], x2=X[:,1], label=y))
groups = Xy.groupby('label')
 
# Plot the blobs
fig, ax = plt.subplots()
colors = ["blue", "red", "green", "purple"]
for idx, classification in groups:
    classification.plot(ax=ax, kind='scatter', x='x1', y='x2', label=idx, color=colors[idx])
plt.show()
