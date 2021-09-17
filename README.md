
# BigFuzz
BigFuzz: Efficient Fuzz Testing for Data Analytics using Framework Abstraction (ASE 2020)

## Summary
As big data analytics become increasingly popular, data-intensive scalable computing (DISC) systems help address the scalability issue of handling large data. However, automated testing for such data-centric applications is challenging, because data is often incomplete, continuously evolving, and hard to know a priori. Fuzz testing has been proven to be highly effective in other domains such as security; however, it is nontrivial to apply such traditional fuzzing to big data analytics directly for three reasons: (1) the long latency of DISC systems prohibits the applicability of fuzzing: naïve fuzzing would spend 98% of the time in setting up a test environment; (2) conventional branch coverage is unlikely to scale to DISC applications because most binary code comes from the framework
implementation such as Apache Spark; and (3) random bit or byte level mutations can hardly generate meaningful data, which fails to reveal real-world application bugs.

We propose a novel coverage-guided fuzz testing tool for big data analytics, called BigFuzz. The key essence of our approach is that: (a) we focus on exercising application logic as opposed to increasing framework code coverage by abstracting the DISC framework using specifications. BigFuzz performs automated source to source transformations to construct an equivalent DISC application suitable for fast test generation, and (b) we design schema-aware data mutation operators based on our in-depth study of DISC application error types. BigFuzz speeds up the fuzzing time by 78 to 1477X compared to random fuzzing, improves application code
coverage by 20% to 271%, and achieves 33% to 157% improvement in detecting application errors. When compared to the state of the art that uses symbolic execution to test big data analytics, BigFuzz is applicable to twice more programs and can find 81% more bugs.

## Team 
This project is developed by Professor [Miryung Kim](http://web.cs.ucla.edu/~miryung/)'s Software Engineering and Analysis Laboratory at UCLA. 
If you encounter any problems, please open an issue or feel free to contact us:

[Qian Zhang](http://web.cs.ucla.edu/~zhangqian/): Postdoctoral researcher, zhangqian@cs.ucla.edu;

[Jiyuan Wang](http://web.cs.ucla.edu/~wangjiyuan): PhD student, wangjiyuan@cs.ucla.edu;

## How to cite 
Please refer to our ASE'21 paper, [Efficient Fuzz Testing for Data Analytics using Framework Abstraction](http://web.cs.ucla.edu/~miryung/Publications/ase2020-bigfuzz.pdf) for more details. 
### Bibtex  
@inproceedings{10.1145/3324884.3416641,
author = {Zhang, Qian and Wang, Jiyuan and Gulzar, Muhammad Ali and Padhye, Rohan and Kim, Miryung},
title = {BigFuzz: Efficient Fuzz Testing for Data Analytics Using Framework Abstraction},
year = {2020},
isbn = {9781450367684},
publisher = {Association for Computing Machinery},
address = {New York, NY, USA},
url = {https://doi.org/10.1145/3324884.3416641},
doi = {10.1145/3324884.3416641},
booktitle = {Proceedings of the 35th IEEE/ACM International Conference on Automated Software Engineering},
pages = {722–733},
numpages = {12},
keywords = {fuzz testing, test generation, big data analytics},
location = {Virtual Event, Australia},
series = {ASE '20}
}

[DOI Link](https://dl.acm.org/doi/10.1145/3324884.3416641)


## Prerequisites

BigFuzz is built on top of [JQF](https://github.com/rohanpadhye/JQF). The source-to-source transformation is dependent on spark-2.4.3 and scala-2.11.8. The UDF extraction part is devised from [UDFExtractor](https://github.com/maligulzar/BigTest/tree/JPF-integrated/UDFExtractor/src/udfExtractor).


## How to use this tool
Aftering getting the bytecode of program under test, type the following command in terminal:

```bash
/direction/to/jqf-bigfuzz -c .:$(/direction/to/scripts/classpath.sh) testDriver testMethod num/null
```

## Video
You can watch a ASE'20 presentation video [here](https://drive.google.com/file/d/12CRdUf1NaJ7T6v4k0BZ19halQX_ducYH/view)

## FAQ 







