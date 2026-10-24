# json-editor-parameter

## Introduction

Add parameters to an input step using the [json-editor](https://github.com/json-editor/json-editor) web form.
The json editor generates an HTML form from a JSON Schema.

## Getting started

There are two principal configuration parameters: `schema`, which contains the json schema of the input form; and
`startval`, which contains the starting values of the form.  An additional parameter `options` contains the lesser used
parameters which configure the form.  See [options](https://github.com/json-editor/json-editor#options) for additional
information.

Optionally, `startvalUrl` can be set to fetch the starting values from a URL each time the form is rendered,
falling back to the static `startval` on error. Only users with Job/Configure permission can set this URL.

### Pipeline Example

```groovy
pipeline {
    agent any

    stages {
        stage('Hello') {
            steps {
                script {
                    def values= input message: 'an input test', parameters: [
                        jsonEditor(name: 'First', schema: '{"type":"object","title":"Car","properties":{"make":{"type":"string","enum":["Toyota","BMW","Honda","Ford","Chevy","VW"]},"model":{"type":"string"},"year":{"type":"integer","enum":[1995,1996,1997,1998,1999,2000,2001,2002,2003,2004,2005,2006,2007,2008,2009,2010,2011,2012,2013,2014],"default":2008},"safety":{"type":"integer","format":"rating","maximum":"5","exclusiveMaximum":false,"readonly":false}}}', startval: '{"make":"Toyota","safety":4}')
                    ]
                    println(values)
                }
            }
        }
    }
}
```

## Issues

Report issues and request enhancements at the [issues page](../../issues).

## Contributing

Pull requests are welcome!

Refer to Jenkins [contribution guidelines](https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md) for useful information.

## License

Licensed under MIT, see [LICENSE](LICENSE.md)
