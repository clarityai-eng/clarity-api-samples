# Clarity AI Async examples

Here you have ready-to-use Python code and Jupyter Notebook examples to retrieve data from Clarity AI's API async endpoints.

These endpoints are designed to retrieve big amounts of data, so normally they require to request an async job
to the API and then wait until it finishes to download the results.

All this job is done by the class `AsyncDownloader` that you can find in file `async_downloader.py`

## Available Examples

- **Python Script**: `async_examples.py` - Ready-to-run Python script
- **Jupyter Notebook**: `async_examples.ipynb` - Interactive notebook with the same examples organized in cells for step-by-step execution

Both examples demonstrate how to:
- Download ESG Risk data for equities
- Download ESG Risk data for organizations  
- Download SFDR data for organizations
- Merge multiple datasets by common identifiers

In order to request the data you need you just need to slightly modify the examples to fit your own needs.

You can check all available options in https://developer.clarity.ai/

# How to run the examples

### Install dependencies
```bash
pip install -r requirements.txt
```

For Jupyter Notebook usage, you'll also need:
```bash
pip install jupyter
```

### Set your API credentials
Set your Clarity AI public API credentials in one of these ways:

**Option 1: Environment variables (recommended)**
```bash
export CLARITY_AI_API_KEY="your_api_key_here"
export CLARITY_AI_API_SECRET="your_api_secret_here"
```

**Option 2: Direct modification**
Edit the `DEFAULT_KEY` and `DEFAULT_SECRET` constants in `async_examples.py` or the notebook cells.

### Run the examples

**Python Script:**
```bash
python async_examples.py
```

**Jupyter Notebook:**
```bash
jupyter notebook async_examples.ipynb
```

The notebook version allows you to run each step interactively and examine intermediate results, making it ideal for exploration and learning.

Enjoy!!