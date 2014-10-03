# NSI Document Distribution Service Client

The NSI Document Distribution Service (NSI DDS) is a draft standard for collection and distribution of NSI related data documents between NSA within a connected control plane.  The NSI DDS exposes a REST-based interface to clients and peer NSA supporting standard synchronous HTTP operations, as well as an asynchronous notification model.  The protocol is defined in the document https://redmine.ogf.org/dmsf_files/13243.

The NSI DDS Client is a commandline shell for interacting with an NSI DDS service instance.  It is currently a work in progress.

## Getting Started

The *nsi-dds-client* utilizes maven as a build environment.  Once you have downloaded the `nsi-dds-client` project just type `maven clean install` in the project directory to build the *nsi-dds-client* application.
```
> git clone https://github.com/BandwidthOnDemand/nsi-dds-client.git
Cloning into 'nsi-dds-client'...
remote: Counting objects: 439, done.
remote: Compressing objects: 100% (186/186), done.
remote: Total 439 (delta 224), reused 439 (delta 224)
Receiving objects: 100% (439/439), 10.30 MiB | 2.58 MiB/s, done.
Resolving deltas: 100% (224/224), done.
Checking connectivity... done.
> cd nsi-dds-client
> mvn clean install
```

## Quickstart Configuration

Open `run.sh` and edit configuration parameters as needed.  The keystore and truststore information is only required if communicating with a secured DDS server.
