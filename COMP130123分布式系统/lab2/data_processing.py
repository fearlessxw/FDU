import re

with open('document.dat', 'r', encoding='gb18030') as input_file:
    content = input_file.read()

doc_pattern = re.compile(r'<doc>.*?</doc>', re.DOTALL)
docs = doc_pattern.findall(content)

contents = []
for doc in docs:
    docno_match = re.search(r'<docno>(.*?)</docno>', doc)
    contenttitle_match = re.search(r'<contenttitle>(.*?)</contenttitle>', doc)
    content_match = re.search(r'<content>(.*?)</content>', doc)

    if docno_match and contenttitle_match:
        docno = docno_match.group(1)
        contenttitle = contenttitle_match.group(1)
        content = content_match.group(1) if content_match else ""
        contents.append(docno+" "+contenttitle+ " " + content)

with open(f'document.txt', 'w', encoding='utf-8') as output_file:
    for content in contents:
        output_file.write(content + '\n')
