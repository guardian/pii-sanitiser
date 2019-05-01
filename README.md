# pii-sanitiser

This project uses a combination of natural language processing (NLP), specifically named entity recognition (NER), combined with regular expressions, to identify and remove personally identifiable information (PII) from a given piece of text.

## How to use

1. After cloning the repo, save your raw text file/s to be sanitised in the Files directory.
2. Start `sbt` and type `run`.
3. Enter the name of the file you want to process (e.g. 'inputfile.txt').
4. Your sanitised file will appear in the Files folder as 'sanitised.txt' and the PII that was found will be in 'pii.txt'.
5. Repeat for each file.
