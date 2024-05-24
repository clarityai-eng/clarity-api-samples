# Clarity AI Async examples

Here you have ready-to-use Java code to retrieve data from Clarity AI's API async endpoints.

These endpoints are designed to retrieve big amounts of data, so normally they require to request an async job
to the API and then wait until it finishes to download the results.

All this job is done by the class `AsyncDownloader`.

Examples of how to use that class to download your jobs can be found in class `AsyncExamples`
In order to request the data you need you just need to slightly modify the examples to fit your own needs.

You can check all available options in https://developer.clarity.ai/

# How to run the examples
### Set your API credentials
Go to `AsyncExamples` class and set the DEFAULT_KEY and DEFAULT_SECRET constants to your own 
ClarityAI public API credentials.

Or, if you prefer, use the environment variables CLARITY_AI_API_KEY and CLARITY_AI_API_SECRET to provide
your Public API credentials.

### Run
`./gradlew run`

Enjoy!!
