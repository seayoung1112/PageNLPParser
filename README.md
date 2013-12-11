Workflow to parse and write books to database:

1 (optional) convert pdf to txt:

  install pdfminer (python module)
  run pdf2txt.py -o output_file original_pdf_file
  
2 run preprocess.py to get a .pre file 
  
  the format of preprocessed file will be each paragraph separated by one blank line (two \n\n) and each chapter title
  will be a single paragraph with a [--TITLE--] tag

  open this file with an text editor with grammer checking and correct possible mistakes (depending on the quality of source)
  
3 run PageParser class (Java) 
  
  the text will be divided to sentences using NLP then be paginated according to a word count setting. the final format 
  stored in mongodb will be :
  
  Page:
  {index: int, title: string(optional), book: string, sentencese: array}
  
  and blank string will appear in sentencese as a separator between paragraphs
