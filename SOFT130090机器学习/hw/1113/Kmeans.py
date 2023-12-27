from sklearn.utils import shuffle
import pandas as pd
import numpy as np
from sklearn.model_selection import KFold
import matplotlib.pyplot as plt
from sklearn.tree import DecisionTreeClassifier
from sklearn.cluster import KMeans
from sklearn.preprocessing import StandardScaler
from sklearn.metrics import accuracy_score, roc_auc_score

print("data loading...")
df = pd.read_csv("./data/select-data.csv")
df_test = pd.read_csv("./data/scalar-test.csv")
print(df.columns)
features = ['Age', 'CreditScore', 'EB', 'EstimatedSalary', 'Exited',
       'Gender', 'Geography', 'HasCrCard', 'IsActiveMember', 'NumOfProducts',
       'Tenure']
df = df[features]
df_test = df_test[features]

X_train = df.drop('Exited', axis=1)
y_train = df['Exited']
X_test = df_test.drop('Exited', axis=1)
y_test = df_test['Exited']

# 数据标准化
scaler = StandardScaler()
X_train_scaled = scaler.fit_transform(X_train)
X_test_scaled = scaler.transform(X_test)

# 使用K均值进行聚类
kmeans = KMeans(n_clusters=2)
kmeans.fit(X_train_scaled)
df['Cluster'] = kmeans.fit_predict(X_train_scaled)

from sklearn.metrics import silhouette_score
silhouette_avg = silhouette_score(X_train_scaled, df['Cluster'])
print(f'Silhouette Score: {silhouette_avg:.4f}')
print(accuracy_score(y_train, df['Cluster']))

df_test['Cluster'] = kmeans.fit_predict(X_test_scaled)
silhouette_avg = silhouette_score(X_test_scaled, df_test['Cluster'])
print(f'Silhouette Score: {silhouette_avg:.4f}')
print(accuracy_score(y_test, df_test['Cluster']))