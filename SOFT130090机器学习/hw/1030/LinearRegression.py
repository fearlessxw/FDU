import numpy as np
import pandas as pd
import tensorflow as tf
from sklearn.linear_model import LinearRegression
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_squared_error, r2_score
from scipy.stats import ttest_ind

pd.set_option('display.max_rows', 500)
pd.set_option('display.max_columns', 100)
pd.set_option('display.width', 1000)
import warnings
warnings.filterwarnings('ignore')

#读取数据
df = pd.read_excel("身高预测参照表-1.xlsx")
print(df.head())

# 重要性检验
t_statistic, p_value = ttest_ind(df['足长'], df['身高'])
alpha = 0.05
if p_value < alpha:
    print("足长和身高存在显著差异")
else:
    print("足长和身高无显著差异")
t_statistic, p_value = ttest_ind(df['步幅'], df['身高'])
alpha = 0.05
if p_value < alpha:
    print("步幅和身高存在显著差异")
else:
    print("步幅和身高无显著差异")


X = df[['足长','步幅']]
y = df[['身高']]
# 按照8：2划分训练集和测试集
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

# 模型训练
lr = LinearRegression()
lr.fit(X_train, y_train)

# 参数
print("权重：", lr.coef_)
print("截距：", lr.intercept_)

# 测试
y_pred = lr.predict(X_test)
mse = mean_squared_error(y_test, y_pred)
r2 = r2_score(y_test, y_pred)
print("均方误差 (MSE):", mse)
print("决定系数 (R^2):", r2)

# 可视化图
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D  # 用Axes3D库画3D模型图

x1_data = X_test.drop(columns=['步幅'], axis=1)
x2_data = X_test.drop(columns=['足长'], axis=1)

# 创建一个3D图形窗口
fig = plt.figure(figsize=(8, 6))

# 创建3D坐标轴
ax3d = fig.add_subplot(111, projection='3d')

# 绘制散点图
ax3d.scatter(x1_data, x2_data, y_test, color='b', marker='*', label='actual')
ax3d.scatter(x1_data,x2_data,y_pred,color='r',label='predict')

ax3d.set_xlabel('foot length')  # 设置x轴标签
ax3d.set_ylabel('stride')  # 设置y轴标签
ax3d.set_zlabel('height')  # 设置z轴标签
plt.legend(loc='upper left')
plt.show()
