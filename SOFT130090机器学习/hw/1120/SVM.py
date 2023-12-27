from sklearn.svm import SVC
import joblib
from sklearn.utils import shuffle
import pandas as pd
import numpy as np
from sklearn.model_selection import KFold
import matplotlib.pyplot as plt
from sklearn.tree import DecisionTreeClassifier
import pandas as pd
from sklearn.model_selection import cross_val_predict, cross_val_score, train_test_split
from sklearn.svm import SVC
from sklearn.metrics import accuracy_score, roc_auc_score, f1_score, recall_score, precision_score
from sklearn.preprocessing import StandardScaler
import time

df = pd.read_csv("./data/select-data.csv")
df_test = pd.read_csv("./data/scalar-test.csv")
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

# 使用SVM算法
svm_model = SVC(probability=True, random_state=42)

# 10折交叉验证
start_time = time.time()
cv_accuracy = cross_val_score(svm_model, X_train, y_train, cv=10, scoring='accuracy')
end_time = time.time()
total_time = end_time - start_time

# 在测试集上进行预测
svm_model.fit(X_train_scaled, y_train)
y_pred = svm_model.predict(X_test_scaled)
y_prob = svm_model.predict_proba(X_test_scaled)[:, 1]

# 计算性能指标
accuracy = accuracy_score(y_test, y_pred)
auc = roc_auc_score(y_test, y_prob)
f1 = f1_score(y_test, y_pred)
recall = recall_score(y_test, y_pred)
precision = precision_score(y_test, y_pred)

# 打印结果
print(f'10-Fold Cross Validation Accuracy: {cv_accuracy}')
print(f'Total Time for Cross Validation: {total_time:.4f} seconds')
print(f'Test Set Accuracy: {accuracy:.4f}')
print(f'Test Set AUC: {auc:.4f}')
print(f'Test Set F1 Score: {f1:.4f}')
print(f'Test Set Recall: {recall:.4f}')
print(f'Test Set Precision: {precision:.4f}')