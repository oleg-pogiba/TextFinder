# Text Finder

##Technologies
- Kotlin
- RxJava2
- Retrofit2
- Coin

##Pre-conditions
- only portrait orientation
- search is related to activity life cycle (only foreground)
- multithreading processing has been implemented by the mean of rxjava with flatMap
- "no internet connection" state is not processed

####Input data
- url should has http or https protocol 
- amount of threads should be in range [1..20]
- amount of url to processing should be in range [1..1000]
- search query length should be in range [1..100]

##Testing
- unit test: junit + mockito
- ui tests: espresso + mock server (okhttp)

[link to video](video.webm)