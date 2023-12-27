from sklearn.utils import shuffle
import pandas as pd
import numpy as np
from sklearn.model_selection import KFold
import matplotlib.pyplot as plt
from sklearn.tree import DecisionTreeClassifier
from sklearn.cluster import KMeans
from sklearn.preprocessing import StandardScaler
from sklearn.metrics import accuracy_score, roc_auc_score
from sklearn.cluster import DBSCAN
from sklearn.metrics import silhouette_score
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

# 使用DBSCAN进行聚类
dbscan = DBSCAN(eps=0.5, min_samples=5)  # 调整eps和min_samples参数
# labels = dbscan.fit_predict(X_train_scaled)
labels_test = dbscan.fit_predict(X_train_scaled)
print(set(labels_test))
# 计算轮廓系数
silhouette_avg_test = silhouette_score(X_train_scaled, labels_test)
print(f'Silhouette Score: {silhouette_avg_test:.4f}')



