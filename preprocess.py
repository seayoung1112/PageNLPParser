import re
import sys
import codecs

def hungerGame(inputStr):
    inputStr = re.compile(r"(\n\"[A-Z0-9\s]+\"\s+?)\n").sub(r"\1[--TITLE--]\n", inputStr)
    inputStr = re.compile(r"(^|\n)PART\s[\w\s]+\n\n").sub(r"\1", inputStr)
    return inputStr

filePath = sys.argv[1]
print "start to process %s" % filePath
with codecs.open(filePath, 'r', 'utf-8') as fp:
    fileStr = fp.read()
    fileStr = re.compile("(.)\n(.)").sub(r"\1\2", fileStr)
    fileStr = re.compile("\n\s+\n").sub("\n\n", fileStr)
    fileStr = re.compile("\n{2,}").sub("\n\n", fileStr) 
    fileStr = globals()[sys.argv[2]](fileStr)
    with codecs.open(filePath[:-3] + "pre", 'w', 'utf-8') as des:
        des.write(fileStr)


