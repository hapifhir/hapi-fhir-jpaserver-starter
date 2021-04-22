# Charts

> A collection of Helm charts

```sh
helm repo add hapifhir https://hapifhir.github.io/charts
helm repo update
```

## Development

1. Make changes to the charts

1. Mount the folder in the [kube-powertools](https://github.com/chgl/kube-powertools) container to easily run linters and checks

   ```sh
   docker run --rm -it -v $PWD:/usr/src/app ghcr.io/chgl/kube-powertools:latest
   ```

1. Run chart-testing and the `chart-powerlint.sh` script to lint the chart

   ```sh
   ct lint --config .github/ct/ct.yaml && chart-powerlint.sh
   ```

1. (Optional) View the results of the [polaris audit check](https://github.com/FairwindsOps/polaris) in your browser

   ```sh
   $ docker run --rm -it -p 9090:8080 -v $PWD:/usr/src/app ghcr.io/chgl/kube-powertools:latest
   bash-5.0: helm template charts/fhir-server/ | polaris dashboard --audit-path -
   ```

   You can now open your browser at <http://localhost:9090> and see the results and recommendations.

1. Run `generate-docs.sh` to auto-generate an updated README

   ```sh
   generate-docs.sh
   ```

1. Bump the version in the changed Chart.yaml according to SemVer (The `ct lint` step above will complain if you forget to update the version.)
