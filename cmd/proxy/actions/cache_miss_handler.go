package actions

import (
	"log"

	"github.com/gobuffalo/buffalo"
	"github.com/gobuffalo/buffalo/worker"
	"github.com/gomods/athens/pkg/module"
	"github.com/gomods/athens/pkg/paths"
	"github.com/gomods/athens/pkg/storage"
)

func cacheMissHandler(next buffalo.Handler, w worker.Worker, mf *module.Filter) buffalo.Handler {
	return func(c buffalo.Context) error {
		params, err := paths.GetAllParams(c)
		if err != nil {
			log.Println(err)
			return err
		}

		if !mf.ShouldProcess(params.Module) {
			return module.NewErrModuleExcluded(params.Module)
		}

		nextErr := next(c)
		if storage.IsNotFoundError(nextErr) {
			// TODO: add separate worker instead of inline reports
			if err := queueCacheMissReportJob(params.Module, params.Version, app.Worker); err != nil {
				log.Println(err)
				return nextErr
			}

		}
		return nextErr
	}
}

func queueCacheMissReportJob(module, version string, w worker.Worker) error {
	return w.Perform(worker.Job{
		Queue:   workerQueue,
		Handler: ReporterWorkerName,
		Args: worker.Args{
			workerModuleKey:  module,
			workerVersionKey: version,
		},
	})
}
