# RITAM CLI
A Command-line Interface for the level-based REST API of RITAM.

## Getting Started
Download the [latest release](https://github.com/Legion2/ritam/releases/latest) of RITAM CLI and extract the archive.
Optionally you can add it to your `PATH` environment variable, which allows to call it without specifying the path to the executable. 
Call the cli tool `ritam` or `ritam.bat` from a command prompt with the option `--help`.

```
Usage: ritam [OPTIONS] COMMAND [ARGS]...

Options:
  -e, --endpoint <url>  Endpoint URL of the RITAM Device and Application Manager
  --version             Show the version and exit
  -h, --help            Show this message and exit

Commands:
  get
  create
  update
  apply
  delete
```

The commands are structured as follows:
`ritam <action> <resource-type> <resource-name>`

Supported actions are:
* get
* create
* update
* apply
* delete

Supported resource types are:
* devices
* application-templates
* applications
* components

## Examples
Get all resources of a CRC Model Type:
`ritam get application-templates`
Or get a specific CRC Model by name:
`ritam get devices device1`

Delete a CRC Model:
`ritam delete application-templates temperature-app`
The deletion is asynchronous, to wait for its completion use the `--wait` or `-w` option:
`ritam delete application-templates temperature-app -w`


## Build the CLI

The CLI can be build by running `./gradlew runtimeZip`.
This will generate the `build/ritam-*.zip` files, which contain all executables to run the `ritam` CLI.
There is also a bash completion generated.
