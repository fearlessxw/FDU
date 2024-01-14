from mrjob.job import MRJob
from mrjob.step import MRStep
import jieba
import os
from collections import Counter
import math
import sys

class TFIDFJob(MRJob):
    total_lines = 0

    @classmethod
    def run(cls):
        # 在 TFIDFJob.run() 之前，统计文档的总行数
        with open('document.txt', 'r', encoding='utf-8') as file:
            cls.total_lines = sum(1 for _ in file)

        # 调用父类的 run 方法，启动 MapReduce 作业
        super(TFIDFJob, cls).run()

    def mapper_init(self):
        # doc_word_count 本文档中的词频
        self.doc_word_count = {}

    def mapper(self, _, line):
        filename, content = line.split(' ', 1)

        # 使用jieba分词
        words = list(jieba.cut(line))
        word_count = len(words)

        # Emit word-document pairs with term frequency
        for word in set(words):
            yield word, (filename, words.count(word) / word_count)

    def reducer(self, word, values):

        document_list = list(values)
        document_frequency = len(document_list)

        # Calculate inverse document frequency (IDF)
        idf = math.log(200 / (1 + document_frequency))

        # Calculate TF-IDF for each document
        tfidf_values = []
        for doc, tf in document_list:
            tfidf = tf * idf
            tfidf_values.append([doc, tfidf])

        yield word, tfidf_values

if __name__ == '__main__':
    TFIDFJob.run()