import ast

def load_document_data(file_path):
    keyword_dict = {}
    with open(file_path, 'r', encoding='unicode_escape') as file:
        content = file.read()
        contents = content.split("\n")

    for line in contents:
        if(len(line) == 0):
            break
        word, tfidf_list = line.split("\t", 1)
        my_list = ast.literal_eval(tfidf_list)
        keyword_dict[word.strip('"')] = [sublist[0]+'.txt' for sublist in my_list]

    return keyword_dict

file_path = 'result.txt'  # 替换成你的TF-IDF文档路径
keyword_dict = load_document_data(file_path)

while(1):
    word = input(">>> ")
    if(word == "quit"):
        break
    if word in keyword_dict:
        print(keyword_dict[word])
    else:
        print("No such word")
